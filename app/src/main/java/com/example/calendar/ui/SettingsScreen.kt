package com.example.calendar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.calendar.ui.theme.AppTheme
import com.example.calendar.ui.theme.ThemeMode

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    appTheme: AppTheme,
    onThemeChange: (ThemeMode, AppTheme) -> Unit,
    notificationSettings: NotificationSettings,
    onNotificationSettingsChange: (NotificationSettings) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Configurações",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        SettingsSection(title = "Modo do Tema") {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                ThemeMode.values().forEach { mode ->
                    Row(
                        modifier = Modifier
                            .clickable { onThemeChange(mode, appTheme) }
                            .padding(end = 16.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = themeMode == mode,
                            onClick = { onThemeChange(mode, appTheme) }
                        )
                        Text(
                            text = when (mode) {
                                ThemeMode.SYSTEM -> "Sistema"
                                ThemeMode.LIGHT -> "Claro"
                                ThemeMode.DARK -> "Escuro"
                            },
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        }

        SettingsSection(title = "Cor do App (Paleta)") {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AppTheme.values().forEach { theme ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(theme.color)
                            .border(
                                width = if (appTheme == theme) 3.dp else 1.dp,
                                color = if (appTheme == theme) MaterialTheme.colorScheme.onSurface else Color.Gray.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                            .clickable { onThemeChange(themeMode, theme) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (appTheme == theme) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selecionado",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }

        SettingsSection(title = "Notificações") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Habilitar Notificações")
                Switch(
                    checked = notificationSettings.enabled,
                    onCheckedChange = { enabled ->
                        onNotificationSettingsChange(notificationSettings.copy(enabled = enabled))
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Sincronização",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Text(
                    text = "O calendário utiliza SQLite (Room) e segue o padrão ISO-8601 (YYYY-MM-DD), compatível com o Google Calendar API.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
        Divider(modifier = Modifier.padding(top = 8.dp))
    }
}
