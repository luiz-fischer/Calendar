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

        // State Holidays
        when (state?.uppercase()) {
            "AC" -> holidays.add(Holiday(LocalDate.of(year, Month.JUNE, 15), "Aniversário do Acre", HolidayType.STATE, "AC"))
            "AL" -> holidays.add(Holiday(LocalDate.of(year, Month.SEPTEMBER, 16), "Emancipação de Alagoas", HolidayType.STATE, "AL"))
            "AP" -> holidays.add(Holiday(LocalDate.of(year, Month.SEPTEMBER, 13), "Criação do Território", HolidayType.STATE, "AP"))
            "AM" -> holidays.add(Holiday(LocalDate.of(year, Month.SEPTEMBER, 5), "Elevação do Amazonas", HolidayType.STATE, "AM"))
            "BA" -> holidays.add(Holiday(LocalDate.of(year, Month.JULY, 2), "Independência da Bahia", HolidayType.STATE, "BA"))
            "CE" -> holidays.add(Holiday(LocalDate.of(year, Month.MARCH, 25), "Data Magna do Ceará", HolidayType.STATE, "CE"))
            "DF" -> holidays.add(Holiday(LocalDate.of(year, Month.APRIL, 21), "Fundação de Brasília", HolidayType.STATE, "DF"))
            "ES" -> {} // Vários feriados municipais, mas poucos estaduais fixos
            "GO" -> {} 
            "MA" -> holidays.add(Holiday(LocalDate.of(year, Month.JULY, 28), "Adesão do Maranhão", HolidayType.STATE, "MA"))
            "MT" -> holidays.add(Holiday(LocalDate.of(year, Month.NOVEMBER, 20), "Consciência Negra", HolidayType.STATE, "MT"))
            "MS" -> holidays.add(Holiday(LocalDate.of(year, Month.OCTOBER, 11), "Criação do Estado", HolidayType.STATE, "MS"))
            "MG" -> holidays.add(Holiday(LocalDate.of(year, Month.APRIL, 21), "Data Magna", HolidayType.STATE, "MG"))
            "PA" -> holidays.add(Holiday(LocalDate.of(year, Month.AUGUST, 15), "Adesão do Pará", HolidayType.STATE, "PA"))
            "PB" -> holidays.add(Holiday(LocalDate.of(year, Month.AUGUST, 5), "Fundação da Paraíba", HolidayType.STATE, "PB"))
            "PR" -> holidays.add(Holiday(LocalDate.of(year, Month.DECEMBER, 19), "Emancipação do Paraná", HolidayType.STATE, "PR"))
            "PE" -> holidays.add(Holiday(LocalDate.of(year, Month.MARCH, 6), "Data Magna", HolidayType.STATE, "PE"))
            "PI" -> holidays.add(Holiday(LocalDate.of(year, Month.OCTOBER, 19), "Dia do Piauí", HolidayType.STATE, "PI"))
            "RJ" -> holidays.add(Holiday(LocalDate.of(year, Month.APRIL, 23), "Dia de São Jorge", HolidayType.STATE, "RJ"))
            "RN" -> holidays.add(Holiday(LocalDate.of(year, Month.OCTOBER, 3), "Mártires de Cunhaú e Uruaçu", HolidayType.STATE, "RN"))
            "RS" -> holidays.add(Holiday(LocalDate.of(year, Month.SEPTEMBER, 20), "Revolução Farroupilha", HolidayType.STATE, "RS"))
            "RO" -> holidays.add(Holiday(LocalDate.of(year, Month.JANUARY, 4), "Criação de Rondônia", HolidayType.STATE, "RO"))
            "RR" -> holidays.add(Holiday(LocalDate.of(year, Month.OCTOBER, 5), "Criação de Roraima", HolidayType.STATE, "RR"))
            "SC" -> holidays.add(Holiday(LocalDate.of(year, Month.AUGUST, 11), "Criação da Capitania", HolidayType.STATE, "SC"))
            "SP" -> holidays.add(Holiday(LocalDate.of(year, Month.JULY, 9), "Rev. Constitucionalista", HolidayType.STATE, "SP"))
            "SE" -> holidays.add(Holiday(LocalDate.of(year, Month.JULY, 8), "Autonomia de Sergipe", HolidayType.STATE, "SE"))
            "TO" -> holidays.add(Holiday(LocalDate.of(year, Month.OCTOBER, 5), "Criação de Tocantins", HolidayType.STATE, "TO"))
        }

        // Municipal Holidays
        val cityClean = city?.lowercase()?.trim() ?: ""
        
        when {
            cityClean.contains("são paulo") && state == "SP" -> {
                holidays.add(Holiday(LocalDate.of(year, Month.JANUARY, 25), "Aniv. São Paulo", HolidayType.MUNICIPAL, "SP", "São Paulo"))
            }
            cityClean.contains("rio de janeiro") && state == "RJ" -> {
                holidays.add(Holiday(LocalDate.of(year, Month.JANUARY, 20), "Dia de São Sebastião", HolidayType.MUNICIPAL, "RJ", "Rio de Janeiro"))
            }
            cityClean.contains("belo horizonte") && state == "MG" -> {
                holidays.add(Holiday(LocalDate.of(year, Month.DECEMBER, 8), "Imaculada Conceição", HolidayType.MUNICIPAL, "MG", "Belo Horizonte"))
            }
            cityClean.contains("curitiba") && state == "PR" -> {
                holidays.add(Holiday(LocalDate.of(year, Month.SEPTEMBER, 8), "N. Sra. da Luz", HolidayType.MUNICIPAL, "PR", "Curitiba"))
            }
            cityClean.contains("fortaleza") && state == "CE" -> {
                holidays.add(Holiday(LocalDate.of(year, Month.AUGUST, 15), "N. Sra. da Assunção", HolidayType.MUNICIPAL, "CE", "Fortaleza"))
            }
            cityClean.contains("manaus") && state == "AM" -> {
                holidays.add(Holiday(LocalDate.of(year, Month.OCTOBER, 24), "Aniv. Manaus", HolidayType.MUNICIPAL, "AM", "Manaus"))
            }
            cityClean.contains("porto alegre") && state == "RS" -> {
                holidays.add(Holiday(LocalDate.of(year, Month.FEBRUARY, 2), "N. Sra. dos Navegantes", HolidayType.MUNICIPAL, "RS", "Porto Alegre"))
            }
            cityClean.contains("salvador") && state == "BA" -> {
                holidays.add(Holiday(LocalDate.of(year, Month.DECEMBER, 8), "N. Sra. da Conceição", HolidayType.MUNICIPAL, "BA", "Salvador"))
            }
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
