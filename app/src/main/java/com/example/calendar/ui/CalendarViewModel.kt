package com.example.calendar.ui

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.example.calendar.data.HolidayProvider
import com.example.calendar.data.local.AppDatabase
import com.example.calendar.data.local.EventEntity
import com.example.calendar.data.local.RecurrenceType
import com.example.calendar.data.local.SyncStatus
import com.example.calendar.model.Holiday
import com.example.calendar.ui.theme.AppTheme
import com.example.calendar.ui.theme.ThemeMode
import com.example.calendar.worker.NotificationWorker
import com.example.calendar.worker.SyncWorker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.TimeZone
import java.util.concurrent.TimeUnit

enum class NotificationFrequency {
    ONCE_A_DAY, EVERY_1_HOUR, EVERY_4_HOURS, EVERY_6_HOURS
}

data class NotificationSettings(
    val advanceDays: Int = 1,
    val frequency: NotificationFrequency = NotificationFrequency.ONCE_A_DAY,
    val preferredHour: Int = 8,
    val preferredMinute: Int = 0,
    val enabled: Boolean = false
)

enum class Screen {
    CALENDAR, SETTINGS
}

data class CalendarUiState(
    val selectedMonth: YearMonth = YearMonth.now(),
    val holidays: List<Holiday> = emptyList(),
    val events: List<EventEntity> = emptyList(),
    val currentDay: LocalDate = LocalDate.now(),
    val notificationSettings: NotificationSettings = NotificationSettings(),
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val appTheme: AppTheme = AppTheme.DEFAULT,
    val isGoogleLoggedIn: Boolean = false,
    val conflictingEvents: List<EventEntity> = emptyList(),
    val selectedTimezone: String = ZoneId.systemDefault().id,
    val currentScreen: Screen = Screen.CALENDAR,
    val selectedDateForNote: LocalDate? = null,
    val showNoteDialog: Boolean = false
)

class CalendarViewModel(application: Application) : AndroidViewModel(application) {
    private val holidayProvider = HolidayProvider()
    private val database = AppDatabase.getDatabase(application)
    private val eventDao = database.eventDao()
    
    private val sharedPreferences = application.getSharedPreferences("calendar_prefs", Context.MODE_PRIVATE)
    private val settingsPrefs = application.getSharedPreferences("notification_settings", Context.MODE_PRIVATE)
    private val themePrefs = application.getSharedPreferences("theme_settings", Context.MODE_PRIVATE)
    private val timezonePrefs = application.getSharedPreferences("timezone_settings", Context.MODE_PRIVATE)
    
    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadNotificationSettings()
        loadThemeSettings()
        loadTimeZoneSettings()
        loadGoogleLoginStatus()
        updateHolidays()
        observeEvents()
    }

    private fun loadGoogleLoginStatus() {
        val isLoggedIn = sharedPreferences.getBoolean("google_logged_in", false)
        _uiState.update { it.copy(isGoogleLoggedIn = isLoggedIn) }
    }

    private fun observeEvents() {
        viewModelScope.launch {
            eventDao.getAllEvents().collect { eventList ->
                val conflicts = eventList.filter { it.syncStatus == SyncStatus.CONFLICT }
                _uiState.update { it.copy(events = eventList, conflictingEvents = conflicts) }
            }
        }
    }

    fun onSaveNote(
        date: LocalDate, 
        text: String, 
        isRecurring: Boolean = false, 
        recurrenceType: RecurrenceType = RecurrenceType.NONE
    ) {
        viewModelScope.launch {
            val dateStr = date.toString()
            val existingEvents = eventDao.getEventsByDate(dateStr)
            
            if (text.isBlank()) {
                existingEvents.forEach { eventDao.deleteEvent(it) }
            } else {
                if (existingEvents.isNotEmpty()) {
                    val event = existingEvents[0].copy(
                        title = text,
                        lastUpdated = System.currentTimeMillis(),
                        syncStatus = if (existingEvents[0].googleId != null) SyncStatus.PENDING_UPDATE else SyncStatus.LOCAL_ONLY,
                        isRecurring = isRecurring,
                        recurrenceType = recurrenceType
                    )
                    eventDao.updateEvent(event)
                } else {
                    val newEvent = EventEntity(
                        title = text,
                        description = "",
                        date = dateStr,
                        lastUpdated = System.currentTimeMillis(),
                        syncStatus = SyncStatus.LOCAL_ONLY,
                        isRecurring = isRecurring,
                        recurrenceType = recurrenceType
                    )
                    eventDao.insertEvent(newEvent)
                }
            }
            if (_uiState.value.isGoogleLoggedIn) {
                syncWithGoogle()
            }
            onDismissNoteDialog()
        }
    }

    fun syncWithGoogle() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(getApplication()).enqueueUniqueWork(
            "google_calendar_sync",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }

    fun resolveConflict(event: EventEntity, useLocal: Boolean) {
        viewModelScope.launch {
            if (useLocal) {
                eventDao.updateEvent(event.copy(syncStatus = SyncStatus.PENDING_UPDATE))
                syncWithGoogle()
            } else {
                syncWithGoogle()
            }
        }
    }

    private fun loadTimeZoneSettings() {
        val savedTimezone = timezonePrefs.getString("selected_timezone", ZoneId.systemDefault().id)
        _uiState.update { it.copy(selectedTimezone = savedTimezone!!) }
    }

    fun updateTimeZoneSettings(timezoneId: String) {
        timezonePrefs.edit().putString("selected_timezone", timezoneId).apply()
        _uiState.update { it.copy(selectedTimezone = timezoneId) }
    }

    fun getAllAvailableTimezones(): List<String> {
        return TimeZone.getAvailableIDs().toList().sorted()
    }

    private fun loadNotificationSettings() {
        val enabled = settingsPrefs.getBoolean("enabled", false)
        val advanceDays = settingsPrefs.getInt("advanceDays", 1)
        val frequencyName = settingsPrefs.getString("frequency", NotificationFrequency.ONCE_A_DAY.name)
        val settings = NotificationSettings(
            enabled = enabled,
            advanceDays = advanceDays,
            frequency = NotificationFrequency.valueOf(frequencyName!!),
        )
        _uiState.update { it.copy(notificationSettings = settings) }
    }

    private fun loadThemeSettings() {
        val modeName = themePrefs.getString("theme_mode", ThemeMode.SYSTEM.name)
        val themeName = themePrefs.getString("app_theme", AppTheme.DEFAULT.name)
        _uiState.update { it.copy(themeMode = ThemeMode.valueOf(modeName!!), appTheme = AppTheme.valueOf(themeName!!)) }
    }

    fun updateThemeSettings(mode: ThemeMode, theme: AppTheme) {
        themePrefs.edit().putString("theme_mode", mode.name).putString("app_theme", theme.name).apply()
        _uiState.update { it.copy(themeMode = mode, appTheme = theme) }
    }

    fun updateNotificationSettings(settings: NotificationSettings) {
        settingsPrefs.edit().putBoolean("enabled", settings.enabled).putInt("advanceDays", settings.advanceDays).putString("frequency", settings.frequency.name).apply()
        _uiState.update { it.copy(notificationSettings = settings) }
        if (settings.enabled) scheduleNotifications(settings)
        else WorkManager.getInstance(getApplication()).cancelUniqueWork("calendar_notifications")
    }

    private fun scheduleNotifications(settings: NotificationSettings) {
        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
            if (settings.frequency == NotificationFrequency.ONCE_A_DAY) 24 else 4, TimeUnit.HOURS
        ).build()
        WorkManager.getInstance(getApplication()).enqueueUniquePeriodicWork("calendar_notifications", ExistingPeriodicWorkPolicy.UPDATE, workRequest)
    }

    fun onMonthChange(newMonth: YearMonth) {
        if (newMonth.year >= 1987) {
            _uiState.update { it.copy(selectedMonth = newMonth) }
            updateHolidays()
        }
    }

    fun onDayClick(date: LocalDate) {
        _uiState.update { it.copy(selectedDateForNote = date, showNoteDialog = true) }
    }

    fun onDismissNoteDialog() {
        _uiState.update { it.copy(showNoteDialog = false, selectedDateForNote = null) }
    }

    fun navigateTo(screen: Screen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    private fun updateHolidays() {
        val year = _uiState.value.selectedMonth.year
        _uiState.update { it.copy(holidays = holidayProvider.getAllHolidays(year)) }
    }

    fun requestLocationPermission() {
        Log.d("CalendarViewModel", "Location permission requested.")
    }
}
