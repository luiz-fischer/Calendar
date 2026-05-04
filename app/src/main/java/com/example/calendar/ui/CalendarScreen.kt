package com.example.calendar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calendar.data.local.EventEntity
import com.example.calendar.model.Holiday
import com.example.calendar.model.HolidayType
import com.example.calendar.ui.theme.HolidayMunicipal
import com.example.calendar.ui.theme.HolidayNational
import com.example.calendar.ui.theme.HolidayState
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(
    holidays: List<Holiday>,
    events: List<EventEntity>,
    onDayClick: (LocalDate) -> Unit,
    onMonthChange: (YearMonth) -> Unit,
    showNoteDialog: Boolean,
    selectedDateForNote: LocalDate?,
    onSaveNote: (LocalDate, String) -> Unit,
    onDismissNoteDialog: () -> Unit
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    if (showNoteDialog && selectedDateForNote != null) {
        val existingEvent = events.find { it.date == selectedDateForNote.toString() }
        var noteText by remember { mutableStateOf(existingEvent?.title ?: "") }

        AlertDialog(
            onDismissRequest = onDismissNoteDialog,
            title = { Text("Nota para ${selectedDateForNote}") },
            text = {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Escreva sua nota aqui...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 150.dp, max = 300.dp),
                    maxLines = 10
                )
            },
            confirmButton = {
                Button(onClick = { onSaveNote(selectedDateForNote, noteText) }) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissNoteDialog) {
                    Text("Cancelar")
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Navegação de Meses
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = {
                currentMonth = currentMonth.minusMonths(1)
                onMonthChange(currentMonth)
            }) {
                Text("<")
            }

            Text(
                text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale("pt", "BR")).uppercase()} ${currentMonth.year}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Button(onClick = {
                currentMonth = currentMonth.plusMonths(1)
                onMonthChange(currentMonth)
            }) {
                Text(">")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dias da Semana
        val daysOfWeek = listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb")
        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Grade do Calendário
        Box(modifier = Modifier.weight(1f)) {
            CalendarGrid(
                month = currentMonth,
                holidays = holidays,
                events = events,
                onDayClick = onDayClick
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Legenda
        HolidayLegend()
    }
}

@Composable
fun HolidayLegend() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Text(
            text = "Legenda",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        @OptIn(ExperimentalLayoutApi::class)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LegendItem(color = HolidayNational, label = "Nacional")
            LegendItem(color = HolidayState, label = "Estadual")
            LegendItem(color = HolidayMunicipal, label = "Municipal")
            LegendItem(color = MaterialTheme.colorScheme.tertiaryContainer, label = "Nota/Evento")
            LegendItem(color = MaterialTheme.colorScheme.primaryContainer, label = "Hoje")
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarGrid(
    month: YearMonth,
    holidays: List<Holiday>,
    events: List<EventEntity>,
    onDayClick: (LocalDate) -> Unit
) {
    val daysInMonth = month.lengthOfMonth()
    val firstOfMonth = month.atDay(1)

    // Calcula espaços vazios (0 = Domingo, 1 = Segunda...)
    val dayOfWeekValue = firstOfMonth.dayOfWeek.value
    val emptyDaysBefore = if (dayOfWeekValue == 7) 0 else dayOfWeekValue

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Espaços em branco
        items(emptyDaysBefore) {
            Box(modifier = Modifier.aspectRatio(1f))
        }

        // Dias do mês
        items(daysInMonth) { index ->
            val day = index + 1
            val date = month.atDay(day)
            val isToday = date == LocalDate.now()
            
            // Busca feriados para este dia
            val holiday = holidays.find { it.date == date }
            val hasEvent = events.any { it.date == date.toString() }

            val holidayColor = when (holiday?.type) {
                HolidayType.NATIONAL -> HolidayNational
                HolidayType.STATE -> HolidayState
                HolidayType.MUNICIPAL -> HolidayMunicipal
                null -> Color.Transparent
            }

            Card(
                onClick = { onDayClick(date) },
                modifier = Modifier
                    .padding(2.dp)
                    .aspectRatio(1f),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        isToday -> MaterialTheme.colorScheme.primaryContainer
                        holiday != null -> holidayColor
                        hasEvent -> MaterialTheme.colorScheme.tertiaryContainer
                        else -> Color.Transparent
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = day.toString(),
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp,
                            color = when {
                                holiday != null || hasEvent || isToday -> Color.White
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                        
                        if (holiday != null) {
                            val label = when (holiday.type) {
                                HolidayType.NATIONAL -> "NAC"
                                HolidayType.STATE -> holiday.state ?: "EST"
                                HolidayType.MUNICIPAL -> "MUN"
                            }
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        if (hasEvent) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .padding(top = 1.dp)
                            ) {
                                // Pequeno indicador para eventos
                                Surface(shape = androidx.compose.foundation.shape.CircleShape, color = Color.White) {
                                    Box(modifier = Modifier.fillMaxSize())
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
