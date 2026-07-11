package com.eliasgreen18.vocabularytracker.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color
import com.eliasgreen18.vocabularytracker.domain.model.AppTheme

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

private val SepiaColorScheme = lightColorScheme(
    primary = Color(0xFF704214),
    secondary = Color(0xFF966919),
    surface = Color(0xFFF4ECD8),
    background = Color(0xFFF4ECD8),
    onSurface = Color(0xFF5B4636),
    onBackground = Color(0xFF5B4636)
)

private val OledColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    surface = Color.Black,
    background = Color.Black,
    onSurface = Color.White,
    onBackground = Color.White
)

@Composable
fun VocabularyTrackerTheme(
    appTheme: AppTheme = AppTheme.SYSTEM,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when (appTheme) {
        AppTheme.SYSTEM -> {
            if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else if (darkTheme) DarkColorScheme else LightColorScheme
        }
        AppTheme.LIGHT -> LightColorScheme
        AppTheme.DARK -> DarkColorScheme
        AppTheme.SEPIA -> SepiaColorScheme
        AppTheme.OLED -> OledColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}