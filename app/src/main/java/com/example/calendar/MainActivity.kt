package com.example.calendar

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.calendar.ui.CalendarScreen
import com.example.calendar.ui.CalendarViewModel
import com.example.calendar.ui.Screen
import com.example.calendar.ui.SettingsScreen
import com.example.calendar.ui.theme.CalendarTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

class MainActivity : ComponentActivity() {
    private val viewModel: CalendarViewModel by viewModels()

    @OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val uiState by viewModel.uiState.collectAsState()
            
            CalendarTheme(
                themeMode = uiState.themeMode,
                appTheme = uiState.appTheme
            ) {
                // Pedir apenas permissão de Notificações (Android 13+)
                val permissionsToRequest = mutableListOf<String>()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                }

                val permissionsState = rememberMultiplePermissionsState(permissionsToRequest)

                // Solicitar permissão de notificação se necessário ao iniciar
                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionsState.launchMultiplePermissionRequest()
                    }
                }

                if (uiState.currentScreen == Screen.SETTINGS) {
                    BackHandler {
                        viewModel.navigateTo(Screen.CALENDAR)
                    }
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { 
                                Text(
                                    if (uiState.currentScreen == Screen.CALENDAR) 
                                        stringResource(id = R.string.app_name) 
                                    else "Configurações"
                                ) 
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                titleContentColor = Color.White
                            ),
                            navigationIcon = {
                                if (uiState.currentScreen == Screen.SETTINGS) {
                                    IconButton(onClick = { viewModel.navigateTo(Screen.CALENDAR) }) {
                                        Icon(
                                            imageVector = Icons.Filled.ArrowBack,
                                            contentDescription = "Voltar",
                                            tint = Color.White
                                        )
                                    }
                                }
                            },
                            actions = {
                                if (uiState.currentScreen == Screen.CALENDAR) {
                                    IconButton(onClick = { viewModel.navigateTo(Screen.SETTINGS) }) {
                                        Icon(
                                            imageVector = Icons.Filled.Settings,
                                            contentDescription = "Configurações",
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                        )
                    }
                ) { paddingValues ->
                    Surface(
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        when (uiState.currentScreen) {
                            Screen.CALENDAR -> {
                                CalendarScreen(
                                    holidays = uiState.holidays,
                                    events = uiState.events,
                                    onDayClick = { date -> viewModel.onDayClick(date) },
                                    onMonthChange = { newMonth -> viewModel.onMonthChange(newMonth) },
                                    showNoteDialog = uiState.showNoteDialog,
                                    selectedDateForNote = uiState.selectedDateForNote,
                                    onSaveNote = { date, note -> viewModel.onSaveNote(date, note) },
                                    onDismissNoteDialog = { viewModel.onDismissNoteDialog() }
                                )
                            }
                            Screen.SETTINGS -> {
                                SettingsScreen(
                                    themeMode = uiState.themeMode,
                                    appTheme = uiState.appTheme,
                                    onThemeChange = { mode, theme -> viewModel.updateThemeSettings(mode, theme) },
                                    notificationSettings = uiState.notificationSettings,
                                    onNotificationSettingsChange = { settings -> viewModel.updateNotificationSettings(settings) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
