package com.banglaenglish.speaker.ui.theme

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

/**
 * Custom MultiPreview annotation rendering 4 key variants:
 * Light-EN, Dark-EN, Light-BN, Dark-BN.
 */
@Preview(name = "Light - EN", group = "Themes", showBackground = true, locale = "en")
@Preview(name = "Dark - EN", group = "Themes", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, locale = "en")
@Preview(name = "Light - BN", group = "Themes", showBackground = true, locale = "bn")
@Preview(name = "Dark - BN", group = "Themes", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, locale = "bn")
annotation class ThemePreviews

/**
 * Preview annotation testing 200% large accessibility font scaling.
 */
@Preview(name = "Font Scale 200%", group = "Accessibility", fontScale = 2.0f, showBackground = true)
annotation class FontScalePreview
