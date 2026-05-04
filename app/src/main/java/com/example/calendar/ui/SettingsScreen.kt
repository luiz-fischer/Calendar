package com.example.calendar.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.calendar.ui.theme.AppTheme
import com.example.calendar.ui.theme.ThemeMode

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

        SettingsSection(title = "Tema") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Modo Escuro")
                Switch(
                    checked = themeMode == ThemeMode.DARK,
                    onCheckedChange = { isDark ->
                        onThemeChange(if (isDark) ThemeMode.DARK else ThemeMode.LIGHT, appTheme)
                    }
                )
            }
        }

        SettingsSection(title = "Notificações") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Habilitar Notificações")
                Switch(
                    checked = notificationSettings.enabled,
                    onCheckedChange = { enabled ->
                        onNotificationSettingsChange(notificationSettings.copy(enabled = enabled))
                    }
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
