package com.timenw.cattracker.ui.theme

import android.app.Activity
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 暖橙色猫主题
val CatOrange = Color(0xFFFF8A65)
val CatOrangeDark = Color(0xFFE64A19)
val CatOrangeLight = Color(0xFFFFAB91)
val CatCream = Color(0xFFFFF3E0)
val CatGold = Color(0xFFFFB74D)
val CatBrown = Color(0xFF8D6E63)
val CatPink = Color(0xFFF8BBD0)
val CatSafe = Color(0xFF66BB6A)
val CatWarning = Color(0xFFFFA726)
val CatDanger = Color(0xFFEF5350)
val CatPurple = Color(0xFFAB47BC)
val CatTeal = Color(0xFF26A69A)

private val DarkColorScheme = darkColorScheme(
    primary = CatOrange,
    onPrimary = Color.White,
    primaryContainer = CatOrangeDark,
    onPrimaryContainer = CatCream,
    secondary = CatGold,
    onSecondary = CatBrown,
    secondaryContainer = CatBrown,
    onSecondaryContainer = CatCream,
    tertiary = CatPink,
    onTertiary = CatBrown,
    background = Color(0xFF1A0F0A),
    onBackground = CatCream,
    surface = Color(0xFF2C1810),
    onSurface = CatCream,
    surfaceVariant = Color(0xFF3E2723),
    onSurfaceVariant = CatOrangeLight,
    error = CatDanger,
    outline = CatBrown
)

@Composable
fun CatTrackerTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DarkColorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography(),
        content = content
    )
}
