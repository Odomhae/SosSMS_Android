package com.odom.sosSms.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = CareGreen,
    onPrimary = OnCareGreen,
    primaryContainer = CareGreenContainer,
    onPrimaryContainer = OnCareGreenContainer,
    secondary = SageSecondary,
    onSecondary = OnSageSecondary,
    background = WarmBackground,
    onBackground = WarmOnSurface,
    surface = WarmSurface,
    onSurface = WarmOnSurface,
)

@Composable
fun BrainTheme(
    // Dynamic color is intentionally off by default: this app has a deliberate
    // warm-green brand identity that shouldn't be replaced by the system's
    // Material You wallpaper colors.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Always light: this app never switches to a dark scheme, regardless of
    // the system dark-mode setting.
    val colorScheme = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        dynamicLightColorScheme(context)
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
