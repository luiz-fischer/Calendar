package com.example.calendar.model

import java.time.LocalDate

enum class HolidayType {
    NATIONAL, STATE, MUNICIPAL
}

data class Holiday(
    val date: LocalDate,
    val name: String,
    val type: HolidayType,
    val state: String? = null,
    val city: String? = null
)
