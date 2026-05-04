package com.example.calendar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calendar.R
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = viewModel(),
    onRequestLocation: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val states = listOf("AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO")
    var showStateDialog by remember { mutableStateOf(false) }
    var showYearDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = onRequestLocation) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Detectar Localização"
                        )
                    }
                    TextButton(onClick = { showStateDialog = true }) {
                        Text(uiState.selectedState, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            CalendarHeader(
                selectedMonth = uiState.selectedMonth,
                onPreviousMonth = { viewModel.onMonthChange(uiState.selectedMonth.minusMonths(1)) },
                onNextMonth = { viewModel.onMonthChange(uiState.selectedMonth.plusMonths(1)) },
                onYearClick = { showYearDialog = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            DaysOfWeekHeader()

            CalendarGrid(
                selectedMonth = uiState.selectedMonth,
                holidays = uiState.holidays,
                currentDay = uiState.currentDay
            )
            
            if (showStateDialog) {
                StateSelectionDialog(
                    states = states,
                    onStateSelected = {
                        viewModel.onStateChange(it)
                        showStateDialog = false
                    },
                    onDismiss = { showStateDialog = false }
                )
            }

            if (showYearDialog) {
                YearSelectionDialog(
                    selectedYear = uiState.selectedMonth.year,
                    onYearSelected = { year ->
                        viewModel.onMonthChange(uiState.selectedMonth.withYear(year))
                        showYearDialog = false
                    },
                    onDismiss = { showYearDialog = false }
                )
            }
        }
    }
}

@Composable
fun CalendarHeader(
    selectedMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onYearClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.previous))
        }
        Text(
            text = "${selectedMonth.month.getDisplayName(TextStyle.FULL, Locale("pt", "BR")).replaceFirstChar { it.uppercase() }} ${selectedMonth.year}",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clickable { onYearClick() }
                .padding(8.dp)
        )
        IconButton(onClick = onNextMonth) {
            Icon(Icons.Default.ArrowForward, contentDescription = stringResource(R.string.next))
        }
    }
}

@Composable
fun DaysOfWeekHeader() {
    Row(modifier = Modifier.fillMaxWidth()) {
        val daysOfWeek = listOf(
            stringResource(R.string.day_sun),
            stringResource(R.string.day_mon),
            stringResource(R.string.day_tue),
            stringResource(R.string.day_wed),
            stringResource(R.string.day_thu),
            stringResource(R.string.day_fri),
            stringResource(R.string.day_sat)
        )
        daysOfWeek.forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun CalendarGrid(
    selectedMonth: YearMonth,
    holidays: List<com.example.calendar.model.Holiday>,
    currentDay: LocalDate
) {
    val firstDayOfMonth = selectedMonth.atDay(1)
    val daysInMonth = selectedMonth.lengthOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // 0 = Sunday

    val days = mutableListOf<LocalDate?>()
    for (i in 0 until firstDayOfWeek) {
        days.add(null)
    }
    for (i in 1..daysInMonth) {
        days.add(selectedMonth.atDay(i))
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(days) { date ->
            if (date != null) {
                val dayHolidays = holidays.filter { it.date == date }
                val isToday = date == currentDay
                
                CalendarDay(
                    date = date,
                    holidayName = dayHolidays.firstOrNull()?.name,
                    isToday = isToday
                )
            } else {
                Box(modifier = Modifier.aspectRatio(1f))
            }
        }
    }
}

@Composable
fun CalendarDay(
    date: LocalDate,
    holidayName: String?,
    isToday: Boolean
) {
    Column(
        modifier = Modifier
            .aspectRatio(0.8f)
            .padding(2.dp)
            .background(
                if (isToday) MaterialTheme.colorScheme.primaryContainer 
                else if (holidayName != null) MaterialTheme.colorScheme.errorContainer 
                else Color.Transparent,
                shape = MaterialTheme.shapes.small
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyLarge,
            color = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer 
                    else if (holidayName != null) MaterialTheme.colorScheme.onErrorContainer 
                    else MaterialTheme.colorScheme.onSurface
        )
        if (holidayName != null) {
            Text(
                text = holidayName,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 8.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = 9.sp,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun StateSelectionDialog(
    states: List<String>,
    onStateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_state)) },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.height(300.dp)
            ) {
                items(states) { state ->
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clickable { onStateSelected(state) }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(state)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

@Composable
fun YearSelectionDialog(
    selectedYear: Int,
    onYearSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val years = (2000..2050).toList()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Selecionar Ano") },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.height(300.dp)
            ) {
                items(years) { year ->
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .background(
                                if (year == selectedYear) MaterialTheme.colorScheme.primaryContainer
                                else Color.Transparent,
                                shape = MaterialTheme.shapes.small
                            )
                            .clickable { onYearSelected(year) }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = year.toString(),
                            color = if (year == selectedYear) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}
