package com.adamtri.mynoteapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
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

private val DarkColorScheme = darkColorScheme(
    primary = TertiaryForest,
    secondary = LightSlate,
    tertiary = NoteGreen,
    background = DarkSlate,
    surface = SlateGrey
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryForest,
    secondary = SecondaryForest,
    tertiary = TertiaryForest,
    background = Color(0xFFF8F9F9),
    surface = Color.White
)

@Composable
fun MyNoteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    primaryColor: Color? = null,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> {
            if (primaryColor != null) {
                darkColorScheme(
                    primary = primaryColor,
                    onPrimary = Color.White,
                    secondary = LightSlate,
                    tertiary = NoteGreen
                )
            } else {
                DarkColorScheme
            }
        }
        else -> {
            if (primaryColor != null) {
                lightColorScheme(
                    primary = primaryColor,
                    onPrimary = Color.White,
                    secondary = SecondaryForest,
                    tertiary = TertiaryForest
                )
            } else {
                LightColorScheme
            }
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
