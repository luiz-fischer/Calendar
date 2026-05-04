package com.example.calendar.ui

import androidx.lifecycle.ViewModel
import com.example.calendar.data.HolidayProvider
import com.example.calendar.model.Holiday
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.YearMonth

data class CalendarUiState(
    val selectedMonth: YearMonth = YearMonth.now(),
    val selectedState: String? = null,
    val selectedCity: String? = null,
    val holidays: List<Holiday> = emptyList(),
    val currentDay: LocalDate = LocalDate.now(),
    val isLocationLoaded: Boolean = false
)

class CalendarViewModel : ViewModel() {
    private val holidayProvider = HolidayProvider()
    
    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        // Automatically synced with LocalDate.now() in data class default
        updateHolidays()
    }

    fun onMonthChange(newMonth: YearMonth) {
        if (newMonth.year >= 1987) {
            _uiState.value = _uiState.value.copy(selectedMonth = newMonth)
            updateHolidays()
        }
    }

    fun onLocationDetected(state: String, city: String) {
        _uiState.value = _uiState.value.copy(
            selectedState = state,
            selectedCity = city,
            isLocationLoaded = true
        )
        updateHolidays()
    }

    fun onManualLocationSet(state: String, city: String) {
        _uiState.value = _uiState.value.copy(
            selectedState = state,
            selectedCity = city,
            isLocationLoaded = true
        )
        updateHolidays()
    }

    private fun updateHolidays() {
        val year = _uiState.value.selectedMonth.year
        val state = _uiState.value.selectedState
        val city = _uiState.value.selectedCity
        _uiState.value = _uiState.value.copy(
            holidays = holidayProvider.getHolidays(year, state, city)
        )
    }
}
