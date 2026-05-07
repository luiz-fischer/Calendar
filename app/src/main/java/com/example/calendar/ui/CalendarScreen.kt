package com.example.calendar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.calendar.data.local.EventEntity
import com.example.calendar.data.local.RecurrenceType
import com.example.calendar.model.Holiday
import com.example.calendar.model.HolidayType
import com.example.calendar.ui.theme.HolidayNational
import com.example.calendar.ui.theme.HolidayState
import com.example.calendar.ui.theme.HolidayMunicipal
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    currentMonth: YearMonth,
    selectedDay: LocalDate,
    holidays: List<Holiday>,
    events: List<EventEntity>,
    filteredEvents: List<EventEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onDayClick: (LocalDate) -> Unit,
    onEventClick: (EventEntity) -> Unit,
    onTodayClick: () -> Unit,
    onMonthChange: (YearMonth) -> Unit,
    showEventDialog: Boolean,
    selectedDateForEvent: LocalDate?,
    selectedEventForEdit: EventEntity?,
    onSaveEvent: (LocalDate, String, String, Color, String?, String?, Int, Boolean, RecurrenceType) -> Unit,
    onDeleteEvent: (EventEntity) -> Unit,
    onDismissDialog: () -> Unit
) {
    var isListView by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedHolidayForDetail by remember { mutableStateOf<Holiday?>(null) }

    selectedHolidayForDetail?.let { holiday ->
        HolidayDetailDialog(holiday = holiday, onDismiss = { selectedHolidayForDetail = null })
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDay.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val date = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                        onMonthChange(YearMonth.from(date))
                        onDayClick(date)
                    }
                    showDatePicker = false
                }) { Text("Ir") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showEventDialog && selectedDateForEvent != null) {
        EventEditDialog(
            selectedDate = selectedDateForEvent,
            event = selectedEventForEdit,
            onSave = onSaveEvent,
            onDelete = onDeleteEvent,
            onDismiss = onDismissDialog
        )
    }

    Scaffold(
        floatingActionButton = {
            if (!isListView && !isSearchActive) {
                FloatingActionButton(
                    onClick = { onDayClick(selectedDay) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, "Novo Evento")
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Surface(tonalElevation = 2.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSearchActive) {
                        SearchBar(
                            query = searchQuery,
                            onQueryChange = onSearchQueryChange,
                            onClose = { isSearchActive = false; onSearchQueryChange("") }
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clip(MaterialTheme.shapes.small).clickable { showDatePicker = true }.padding(4.dp)
                        ) {
                            Text(
                                text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale("pt", "BR")).replaceFirstChar { it.uppercase() }} ${currentMonth.year}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(Icons.Default.ArrowDropDown, null)
                        }
                        Row {
                            IconButton(onClick = onTodayClick) { Icon(Icons.Default.Today, "Hoje") }
                            IconButton(onClick = { isSearchActive = true }) { Icon(Icons.Default.Search, "Busca") }
                            IconButton(onClick = { isListView = !isListView }) {
                                Icon(if (isListView) Icons.Default.CalendarViewMonth else Icons.Default.ViewAgenda, "Visão")
                            }
                        }
                    }
                }
            }

            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                if (isSearchActive || isListView) {
                    val holidaysToShow = if (isSearchActive && searchQuery.isNotBlank()) {
                        holidays.filter { it.name.contains(searchQuery, ignoreCase = true) }
                    } else if (isListView) {
                        holidays.filter { it.date.month == currentMonth.month && it.date.year == currentMonth.year }
                    } else {
                        emptyList()
                    }

                    val eventsToShow = if (isSearchActive) filteredEvents else events.filter {
                        try {
                            val date = LocalDate.parse(it.date.trim())
                            date.month == currentMonth.month && date.year == currentMonth.year
                        } catch (e: Exception) { false }
                    }

                    EventsListView(eventsToShow, holidaysToShow, onEventClick, { selectedHolidayForDetail = it })
                } else {
                    CombinedCalendarView(currentMonth, selectedDay, holidays, events, onDayClick, onEventClick, { selectedHolidayForDetail = it })
                }
            }
        }
    }
}

@Composable
fun EventEditDialog(
    selectedDate: LocalDate,
    event: EventEntity?,
    onSave: (LocalDate, String, String, Color, String?, String?, Int, Boolean, RecurrenceType) -> Unit,
    onDelete: (EventEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var titleText by remember { mutableStateOf(event?.title ?: "") }
    var descriptionText by remember { mutableStateOf(event?.description ?: "") }
    var startTime by remember { mutableStateOf(event?.startTime ?: "") }
    var endTime by remember { mutableStateOf(event?.endTime ?: "") }
    var selectedColor by remember { mutableStateOf(event?.color?.let { Color(it) } ?: Color(0xFF0061A4)) }
    var recurrenceType by remember { mutableStateOf(event?.recurrenceType ?: RecurrenceType.NONE) }
    var showColorDialog by remember { mutableStateOf(false) }

    if (showColorDialog) {
        ColorSelectionDialog(
            initialColor = selectedColor,
            onColorSelected = { selectedColor = it; showColorDialog = false },
            onDismiss = { showColorDialog = false }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface, tonalElevation = 6.dp) {
            Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = if (event == null) "Novo evento" else "Editar evento", style = MaterialTheme.typography.titleLarge)
                    if (event != null) {
                        IconButton(onClick = { onDelete(event) }) { Icon(Icons.Default.Delete, "Excluir", tint = MaterialTheme.colorScheme.error) }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = titleText, onValueChange = { titleText = it },
                    placeholder = { Text("Título", style = MaterialTheme.typography.headlineSmall) },
                    modifier = Modifier.fillMaxWidth(), textStyle = MaterialTheme.typography.headlineSmall,
                    colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                )

                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, null)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(selectedDate.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("pt", "BR"))))
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth().clickable { showColorDialog = true }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(selectedColor))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Cor do evento", modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, null)
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (titleText.isNotBlank()) {
                                onSave(selectedDate, titleText, descriptionText, selectedColor, startTime.ifBlank { null }, endTime.ifBlank { null }, 0, recurrenceType != RecurrenceType.NONE, recurrenceType)
                                // O fechamento (onDismiss) agora é controlado pelo ViewModel no onSaveEvent
                            }
                        },
                        enabled = titleText.isNotBlank()
                    ) { Text("Salvar") }
                }
            }
        }
    }
}

@Composable
fun CombinedCalendarView(month: YearMonth, selectedDay: LocalDate, holidays: List<Holiday>, events: List<EventEntity>, onDaySelect: (LocalDate) -> Unit, onEventClick: (EventEntity) -> Unit, onHolidayClick: (Holiday) -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
            listOf("D", "S", "T", "Q", "Q", "S", "S").forEach {
                Text(it, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium)
            }
        }
        CalendarStaticGrid(month, selectedDay, holidays, events, onDaySelect)
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        
        LazyColumn(modifier = Modifier.weight(1f)) {
            val selectedDayStr = selectedDay.toString()
            val dayEvents = events.filter { it.date.trim() == selectedDayStr }
            val dayHolidays = holidays.filter { it.date == selectedDay }
            
            if (dayEvents.isEmpty() && dayHolidays.isEmpty()) {
                item { Text("Sem eventos", modifier = Modifier.fillMaxWidth().padding(32.dp), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                items(dayHolidays) { HolidayItem(it, onClick = { onHolidayClick(it) }) }
                items(dayEvents) { EventItem(it, onClick = { onEventClick(it) }) }
            }
        }
    }
}

@Composable
fun CalendarStaticGrid(month: YearMonth, selectedDay: LocalDate, holidays: List<Holiday>, events: List<EventEntity>, onDayClick: (LocalDate) -> Unit) {
    val daysInMonth = month.lengthOfMonth()
    val firstDayOfMonth = month.atDay(1).dayOfWeek.value
    val emptyDaysBefore = if (firstDayOfMonth == 7) 0 else firstDayOfMonth
    val allCells = List(emptyDaysBefore) { null } + (1..daysInMonth).toList()
    
    Column {
        allCells.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    Box(modifier = Modifier.weight(1f).aspectRatio(1f)) {
                        if (day != null) {
                            val date = month.atDay(day)
                            val isSelected = date == selectedDay
                            val isToday = date == LocalDate.now()
                            
                            Column(
                                modifier = Modifier.fillMaxSize().clip(MaterialTheme.shapes.extraSmall)
                                    .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent)
                                    .clickable { onDayClick(date) },
                                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
                            ) {
                                Surface(modifier = Modifier.size(32.dp), shape = CircleShape, color = if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = day.toString(), color = if (isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.height(4.dp)) {
                                    if (holidays.any { it.date == date }) Box(modifier = Modifier.size(4.dp).background(HolidayNational, CircleShape))
                                    events.filter { it.date.trim() == date.toString() }.take(3).forEach { ev ->
                                        Box(modifier = Modifier.size(4.dp).background(Color(ev.color ?: Color.Gray.toArgb()), CircleShape))
                                    }
                                }
                            }
                        }
                    }
                }
                if (week.size < 7) repeat(7 - week.size) { Box(modifier = Modifier.weight(1f).aspectRatio(1f)) }
            }
        }
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit, onClose: () -> Unit) {
    TextField(
        value = query, onValueChange = onQueryChange, modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Pesquisar eventos...") },
        leadingIcon = { Icon(Icons.Default.Search, null) },
        trailingIcon = { IconButton(onClick = onClose) { Icon(Icons.Default.Close, "Fechar") } },
        singleLine = true,
        colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
    )
}

@Composable
fun HolidayItem(holiday: Holiday, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(when(holiday.type) { HolidayType.NATIONAL -> HolidayNational; HolidayType.STATE -> HolidayState; else -> HolidayMunicipal }))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = holiday.name, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun EventItem(event: EventEntity, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color(event.color ?: Color.Gray.toArgb())))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = event.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            if (!event.startTime.isNullOrBlank()) Text(text = "${event.startTime}${event.endTime?.let { " - $it" } ?: ""}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun EventsListView(events: List<EventEntity>, holidays: List<Holiday>, onEventClick: (EventEntity) -> Unit, onHolidayClick: (Holiday) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(holidays) { HolidayItem(it, onClick = { onHolidayClick(it) }) }
        items(events) { EventItem(it, onClick = { onEventClick(it) }) }
    }
}

@Composable
fun ColorSelectionDialog(initialColor: Color, onColorSelected: (Color) -> Unit, onDismiss: () -> Unit) {
    val colors = listOf(Color(0xFFD50000), Color(0xFFF4511E), Color(0xFFF6BF26), Color(0xFF33B864), Color(0xFF039BE5), Color(0xFF3F51B5), Color(0xFF8E24AA), Color(0xFF616161))
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Escolher cor") }, text = {
        LazyVerticalGrid(columns = GridCells.Adaptive(48.dp), modifier = Modifier.height(150.dp)) {
            items(colors) { color ->
                Box(modifier = Modifier.size(48.dp).padding(4.dp).clip(CircleShape).background(color).clickable { onColorSelected(color) }) {
                    if (color == initialColor) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }, confirmButton = {})
}

@Composable
fun HolidayDetailDialog(holiday: Holiday, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(holiday.name) }, text = {
        Text(holiday.date.format(DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM", Locale("pt", "BR"))))
    }, confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } })
}
