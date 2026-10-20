package com.banglaenglish.speaker.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Extended color palette and tuned gradients for BanglaSpeaker.
 */
@Immutable
data class ExtendedColors(
    val primaryGradient: Brush,
    val coolGradient: Brush,
    val warmGradient: Brush,
    val successGradient: Brush,
    val screenBackgroundGradient: Brush,
    val cardBorderGradient: Brush,
    // Intent Colors
    val intentQuestion: Color,
    val intentAnswer: Color,
    val intentStatement: Color,
    val intentRequest: Color,
    val intentCommand: Color,
    val intentGreeting: Color,
    val intentOther: Color
)

val LightExtendedColors = ExtendedColors(
    primaryGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6), Color(0xFFEC4899))
    ),
    coolGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF14B8A6), Color(0xFF06B6D4), Color(0xFF6366F1))
    ),
    warmGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFF59E0B), Color(0xFFF43F5E), Color(0xFFEC4899))
    ),
    successGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF10B981), Color(0xFF14B8A6))
    ),
    screenBackgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFF5F3FF), Color(0xFFFDF2F8), Color(0xFFECFEFF))
    ),
    cardBorderGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1))
    ),
    intentQuestion = Color(0xFFD97706),
    intentAnswer = Color(0xFF059669),
    intentStatement = Color(0xFF2563EB),
    intentRequest = Color(0xFF7C3AED),
    intentCommand = Color(0xFFE11D48),
    intentGreeting = Color(0xFFDB2777),
    intentOther = Color(0xFF64748B)
)

val DarkExtendedColors = ExtendedColors(
    primaryGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF818CF8), Color(0xFFA78BFA), Color(0xFFF472B6))
    ),
    coolGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF2DD4BF), Color(0xFF22D3EE), Color(0xFF818CF8))
    ),
    warmGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFBBF24), Color(0xFFFB7185), Color(0xFFF472B6))
    ),
    successGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF34D399), Color(0xFF2DD4BF))
    ),
    screenBackgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0B0B1A), Color(0xFF14112E), Color(0xFF0F172A))
    ),
    cardBorderGradient = Brush.linearGradient(
        colors = listOf(Color(0x33818CF8), Color(0x33A78BFA))
    ),
    intentQuestion = Color(0xFFFBBF24),
    intentAnswer = Color(0xFF34D399),
    intentStatement = Color(0xFF60A5FA),
    intentRequest = Color(0xFFA78BFA),
    intentCommand = Color(0xFFFB7185),
    intentGreeting = Color(0xFFF472B6),
    intentOther = Color(0xFF94A3B8)
)

val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

object AppTheme {
    val colors: ExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalExtendedColors.current
}
