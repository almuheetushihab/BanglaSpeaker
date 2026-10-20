package com.banglaenglish.speaker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF4F46E5),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = Color(0xFF1E1B4B),
    secondary = Color(0xFF0D9488),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = Color(0xFF042F2E),
    background = Color(0xFFF7F7FB),
    onBackground = Color(0xFF14142B),
    surface = Color.White,
    onSurface = Color(0xFF14142B),
    surfaceVariant = Color(0xFFEDEDF7),
    onSurfaceVariant = Color(0xFF55557A),
    error = Color(0xFFE11D48),
    onError = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF818CF8),
    onPrimary = Color(0xFF1E1B4B),
    primaryContainer = Color(0xFF312E81),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = Color(0xFF2DD4BF),
    onSecondary = Color(0xFF042F2E),
    secondaryContainer = Color(0xFF134E4A),
    onSecondaryContainer = Color(0xFFCCFBF1),
    background = Color(0xFF0F0F1A),
    onBackground = Color(0xFFEDEDF7),
    surface = Color(0xFF181826),
    onSurface = Color(0xFFEDEDF7),
    surfaceVariant = Color(0xFF26263A),
    onSurfaceVariant = Color(0xFFB4B4D0),
    error = Color(0xFFFB7185),
    onError = Color(0xFF4C0519),
)

@Composable
fun BanglaSpeakerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
