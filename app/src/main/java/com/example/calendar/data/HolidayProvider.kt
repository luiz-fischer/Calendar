package com.example.calendar.data

import com.example.calendar.model.Holiday
import com.example.calendar.model.HolidayType
import java.time.LocalDate
import java.time.Month

class HolidayProvider {
    /**
     * Returns a list of holidays for the given year, state, and city.
     */
    fun getHolidays(year: Int, state: String?, city: String?): List<Holiday> {
        val holidays = mutableListOf<Holiday>()

        // Fixed National Holidays
        holidays.add(Holiday(LocalDate.of(year, Month.JANUARY, 1), "Confraternização Universal", HolidayType.NATIONAL))
        holidays.add(Holiday(LocalDate.of(year, Month.APRIL, 21), "Tiradentes", HolidayType.NATIONAL))
        holidays.add(Holiday(LocalDate.of(year, Month.MAY, 1), "Dia do Trabalhador", HolidayType.NATIONAL))
        holidays.add(Holiday(LocalDate.of(year, Month.SEPTEMBER, 7), "Independência do Brasil", HolidayType.NATIONAL))
        holidays.add(Holiday(LocalDate.of(year, Month.OCTOBER, 12), "Nossa Senhora Aparecida", HolidayType.NATIONAL))
        holidays.add(Holiday(LocalDate.of(year, Month.NOVEMBER, 2), "Finados", HolidayType.NATIONAL))
        holidays.add(Holiday(LocalDate.of(year, Month.NOVEMBER, 15), "Proclamação da República", HolidayType.NATIONAL))
        holidays.add(Holiday(LocalDate.of(year, Month.NOVEMBER, 20), "Dia de Zumbi e da Consciência Negra", HolidayType.NATIONAL))
        holidays.add(Holiday(LocalDate.of(year, Month.DECEMBER, 25), "Natal", HolidayType.NATIONAL))

        // Mobile National Holidays (Easter based)
        val easter = getEaster(year)
        holidays.add(Holiday(easter.minusDays(47), "Carnaval", HolidayType.NATIONAL))
        holidays.add(Holiday(easter.minusDays(2), "Sexta-feira Santa", HolidayType.NATIONAL))
        holidays.add(Holiday(easter, "Páscoa", HolidayType.NATIONAL))
        holidays.add(Holiday(easter.plusDays(60), "Corpus Christi", HolidayType.NATIONAL))

        // State Holidays (Simplified examples)
        when (state?.uppercase()) {
            "SP" -> holidays.add(Holiday(LocalDate.of(year, Month.JULY, 9), "Revolução Constitucionalista", HolidayType.STATE, "SP"))
            "RS" -> holidays.add(Holiday(LocalDate.of(year, Month.SEPTEMBER, 20), "Revolução Farroupilha", HolidayType.STATE, "RS"))
            "BA" -> holidays.add(Holiday(LocalDate.of(year, Month.JULY, 2), "Independência da Bahia", HolidayType.STATE, "BA"))
            "RJ" -> holidays.add(Holiday(LocalDate.of(year, Month.APRIL, 23), "Dia de São Jorge", HolidayType.STATE, "RJ"))
            "MG" -> holidays.add(Holiday(LocalDate.of(year, Month.APRIL, 21), "Data Magna de Minas Gerais", HolidayType.STATE, "MG"))
            "PE" -> holidays.add(Holiday(LocalDate.of(year, Month.MARCH, 6), "Data Magna de Pernambuco", HolidayType.STATE, "PE"))
        }

        // Municipal Holidays (Simplified examples)
        if (city?.lowercase()?.contains("são paulo") == true && state?.uppercase() == "SP") {
            holidays.add(Holiday(LocalDate.of(year, Month.JANUARY, 25), "Aniversário de São Paulo", HolidayType.MUNICIPAL, "SP", "São Paulo"))
        } else if (city?.lowercase()?.contains("rio de janeiro") == true && state?.uppercase() == "RJ") {
            holidays.add(Holiday(LocalDate.of(year, Month.JANUARY, 20), "Dia de São Sebastião", HolidayType.MUNICIPAL, "RJ", "Rio de Janeiro"))
        }

        return holidays.sortedBy { it.date }
    }

    private fun getEaster(year: Int): LocalDate {
        val a = year % 19
        val b = year / 100
        val c = year % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val month = (h + l - 7 * m + 114) / 31
        val day = ((h + l - 7 * m + 114) % 31) + 1
        return LocalDate.of(year, month, day)
    }
}
