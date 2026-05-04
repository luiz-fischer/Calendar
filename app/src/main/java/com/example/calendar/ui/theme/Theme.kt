package com.example.calendar.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

enum class AppTheme(val color: Color) {
    DEFAULT(Purple40),
    OCEAN(OceanPrimary),
    FOREST(ForestPrimary),
    SUNSET(SunsetPrimary),
    LAVENDER(LavenderPrimary),
    ROSE(RosePrimary),
    TEAL(TealPrimary)
}

private val LightDefault = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

private val DarkDefault = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightOcean = lightColorScheme(
    primary = OceanPrimary,
    secondary = OceanSecondary,
    tertiary = OceanTertiary
)

private val DarkOcean = darkColorScheme(
    primary = OceanPrimaryDark,
    secondary = OceanSecondary,
    tertiary = OceanTertiary
)

private val LightForest = lightColorScheme(
    primary = ForestPrimary,
    secondary = ForestSecondary,
    tertiary = ForestTertiary
)

private val DarkForest = darkColorScheme(
    primary = ForestPrimaryDark,
    secondary = ForestSecondary,
    tertiary = ForestTertiary
)

private val LightSunset = lightColorScheme(
    primary = SunsetPrimary,
    secondary = SunsetSecondary,
    tertiary = SunsetTertiary
)

private val DarkSunset = darkColorScheme(
    primary = SunsetPrimaryDark,
    secondary = SunsetSecondary,
    tertiary = SunsetTertiary
)

private val LightLavender = lightColorScheme(
    primary = LavenderPrimary,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

private val DarkLavender = darkColorScheme(
    primary = LavenderPrimaryDark,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightRose = lightColorScheme(
    primary = RosePrimary,
    secondary = SunsetSecondary,
    tertiary = Pink40
)

private val DarkRose = darkColorScheme(
    primary = RosePrimaryDark,
    secondary = SunsetSecondary,
    tertiary = Pink80
)

private val LightTeal = lightColorScheme(
    primary = TealPrimary,
    secondary = ForestSecondary,
    tertiary = OceanTertiary
)

private val DarkTeal = darkColorScheme(
    primary = TealPrimaryDark,
    secondary = ForestSecondary,
    tertiary = OceanTertiary
)

@Composable
fun CalendarTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    appTheme: AppTheme = AppTheme.DEFAULT,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> when (appTheme) {
            AppTheme.DEFAULT -> if (darkTheme) DarkDefault else LightDefault
            AppTheme.OCEAN -> if (darkTheme) DarkOcean else LightOcean
            AppTheme.FOREST -> if (darkTheme) DarkForest else LightForest
            AppTheme.SUNSET -> if (darkTheme) DarkSunset else LightSunset
            AppTheme.LAVENDER -> if (darkTheme) DarkLavender else LightLavender
            AppTheme.ROSE -> if (darkTheme) DarkRose else LightRose
            AppTheme.TEAL -> if (darkTheme) DarkTeal else LightTeal
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
