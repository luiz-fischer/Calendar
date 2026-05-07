package com.example.calendar.ui

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.example.calendar.data.local.AppDatabase
import com.example.calendar.data.local.EventEntity
import com.example.calendar.data.local.RecurrenceType
import com.example.calendar.data.local.SyncStatus
import com.example.calendar.model.Holiday
import com.example.calendar.data.HolidayProvider
import com.example.calendar.ui.theme.AppTheme
import com.example.calendar.ui.theme.ThemeMode
import com.example.calendar.worker.NotificationWorker
import com.example.calendar.worker.SyncWorker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.concurrent.TimeUnit

enum class NotificationFrequency(val hours: Long) {
    ONCE_A_DAY(24), EVERY_1_HOUR(1), EVERY_4_HOURS(4), EVERY_6_HOURS(6)
}

data class NotificationSettings(
    val advanceDays: Int = 1,
    val frequency: NotificationFrequency = NotificationFrequency.ONCE_A_DAY,
    val preferredHour: Int = 8,
    val preferredMinute: Int = 0,
    val enabled: Boolean = false
)

enum class Screen { CALENDAR, SETTINGS }

data class CalendarUiState(
    val selectedMonth: YearMonth = YearMonth.now(),
    val selectedDay: LocalDate = LocalDate.now(),
    val holidays: List<Holiday> = emptyList(),
    val events: List<EventEntity> = emptyList(),
    val filteredEvents: List<EventEntity> = emptyList(),
    val notificationSettings: NotificationSettings = NotificationSettings(),
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val appTheme: AppTheme = AppTheme.DEFAULT,
    val isGoogleLoggedIn: Boolean = false,
    val currentScreen: Screen = Screen.CALENDAR,
    val selectedDateForNote: LocalDate? = null,
    val selectedEventForEdit: EventEntity? = null,
    val showNoteDialog: Boolean = false,
    val searchQuery: String = ""
)

class CalendarViewModel(application: Application) : AndroidViewModel(application) {
    private val holidayProvider = HolidayProvider()
    private val database = AppDatabase.getDatabase(application)
    private val eventDao = database.eventDao()
    
    private val sharedPreferences = application.getSharedPreferences("calendar_prefs", Context.MODE_PRIVATE)
    private val settingsPrefs = application.getSharedPreferences("notification_settings", Context.MODE_PRIVATE)
    private val themePrefs = application.getSharedPreferences("theme_settings", Context.MODE_PRIVATE)
    
    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
        observeEvents()
        updateHolidays()
    }

    private fun loadSettings() {
        val isLoggedIn = sharedPreferences.getBoolean("google_logged_in", false)
        val mode = ThemeMode.valueOf(themePrefs.getString("theme_mode", ThemeMode.SYSTEM.name)!!)
        val theme = AppTheme.valueOf(themePrefs.getString("app_theme", AppTheme.DEFAULT.name)!!)
        
        val notifEnabled = settingsPrefs.getBoolean("enabled", false)
        val notifFreq = NotificationFrequency.valueOf(settingsPrefs.getString("frequency", NotificationFrequency.ONCE_A_DAY.name)!!)
        
        _uiState.update { it.copy(
            isGoogleLoggedIn = isLoggedIn,
            themeMode = mode,
            appTheme = theme,
            notificationSettings = NotificationSettings(enabled = notifEnabled, frequency = notifFreq)
        )}
    }

    private fun observeEvents() {
        viewModelScope.launch {
            eventDao.getAllEvents().collect { eventList ->
                Log.d("CalendarViewModel", "Recebidos ${eventList.size} eventos do DB")
                _uiState.update { it.copy(
                    events = eventList,
                    filteredEvents = filterEvents(eventList, it.searchQuery)
                )}
            }
        }
    }

    private fun filterEvents(events: List<EventEntity>, query: String): List<EventEntity> {
        if (query.isBlank()) return events
        return events.filter { it.title.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true) }
    }

    fun onDayClick(date: LocalDate) {
        _uiState.update { it.copy(selectedDay = date, selectedDateForNote = date, selectedEventForEdit = null, showNoteDialog = true) }
    }
    
    fun onSelectDay(date: LocalDate) {
        _uiState.update { it.copy(selectedDay = date) }
    }

    fun onEventClick(event: EventEntity) {
        val date = try { LocalDate.parse(event.date) } catch(e: Exception) { LocalDate.now() }
        _uiState.update { it.copy(selectedDateForNote = date, selectedEventForEdit = event, showNoteDialog = true) }
    }

    fun onSaveEvent(
        date: LocalDate, title: String, description: String, color: Color,
        startTime: String?, endTime: String?, reminder: Int, isRec: Boolean, recType: RecurrenceType
    ) {
        // Capturamos o evento em edição ANTES de disparar a coroutine para evitar race conditions
        val eventBeingEdited = _uiState.value.selectedEventForEdit
        
        viewModelScope.launch {
            try {
                val dateString = date.toString()
                val eventToSave = eventBeingEdited?.copy(
                    title = title, description = description, date = dateString,
                    color = color.toArgb(), startTime = startTime, endTime = endTime,
                    reminderMinutes = reminder, isRecurring = isRec, recurrenceType = recType,
                    lastUpdated = System.currentTimeMillis()
                ) ?: EventEntity(
                    title = title, description = description, date = dateString,
                    color = color.toArgb(), startTime = startTime, endTime = endTime,
                    reminderMinutes = reminder, isRecurring = isRec, recurrenceType = recType
                )

                if (eventBeingEdited != null) {
                    eventDao.updateEvent(eventToSave)
                } else {
                    eventDao.insertEvent(eventToSave)
                }
                
                Log.d("CalendarViewModel", "Evento salvo com sucesso para $dateString")
                onDismissNoteDialog() // Fecha apenas após o sucesso
            } catch (e: Exception) {
                Log.e("CalendarViewModel", "Erro ao salvar", e)
            }
        }
    }

    fun onDeleteEvent(event: EventEntity) {
        viewModelScope.launch {
            eventDao.deleteEvent(event)
            onDismissNoteDialog()
        }
    }

    fun onDismissNoteDialog() {
        _uiState.update { it.copy(showNoteDialog = false, selectedDateForNote = null, selectedEventForEdit = null) }
    }

    fun goToToday() {
        val today = LocalDate.now()
        _uiState.update { it.copy(selectedMonth = YearMonth.from(today), selectedDay = today) }
        updateHolidays()
    }

    fun onMonthChange(newMonth: YearMonth) {
        _uiState.update { it.copy(selectedMonth = newMonth) }
        updateHolidays()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query, filteredEvents = filterEvents(it.events, query)) }
    }

    fun navigateTo(screen: Screen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    private fun updateHolidays() {
        val year = _uiState.value.selectedMonth.year
        _uiState.update { it.copy(holidays = holidayProvider.getAllHolidays(year)) }
    }

    fun updateThemeSettings(mode: ThemeMode, theme: AppTheme) {
        themePrefs.edit().putString("theme_mode", mode.name).putString("app_theme", theme.name).apply()
        _uiState.update { it.copy(themeMode = mode, appTheme = theme) }
    }

    fun updateNotificationSettings(settings: NotificationSettings) {
        settingsPrefs.edit().putBoolean("enabled", settings.enabled).putString("frequency", settings.frequency.name).apply()
        _uiState.update { it.copy(notificationSettings = settings) }
    }
}
