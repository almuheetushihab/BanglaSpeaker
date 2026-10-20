package com.banglaenglish.speaker.data

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserPreferences(
    val scenario: ConversationScenario = ConversationScenario.CASUAL_CHAT,
    val autoTranslate: Boolean = true,
    val accent: VoiceAccent = VoiceAccent.US,
    val speechSpeed: Float = 1.0f,
    val theme: AppTheme = AppTheme.SYSTEM,
    val language: AppLanguage = AppLanguage.SYSTEM
)

class UserPreferencesRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "bangla_speaker_prefs",
        Context.MODE_PRIVATE
    )

    private val _userPreferences = MutableStateFlow(loadPreferences())
    val userPreferences: StateFlow<UserPreferences> = _userPreferences.asStateFlow()

    private fun loadPreferences(): UserPreferences {
        val rawScenario = prefs.getString(KEY_SCENARIO, ConversationScenario.CASUAL_CHAT.rawValue) ?: ConversationScenario.CASUAL_CHAT.rawValue
        val rawAccent = prefs.getString(KEY_ACCENT, VoiceAccent.US.rawValue) ?: VoiceAccent.US.rawValue
        val rawTheme = prefs.getString(KEY_THEME, AppTheme.SYSTEM.rawValue) ?: AppTheme.SYSTEM.rawValue
        val rawLang = prefs.getString(KEY_LANGUAGE, AppLanguage.SYSTEM.rawValue) ?: AppLanguage.SYSTEM.rawValue

        return UserPreferences(
            scenario = ConversationScenario.fromRawValue(rawScenario),
            autoTranslate = prefs.getBoolean(KEY_AUTO_TRANSLATE, true),
            accent = VoiceAccent.fromRawValue(rawAccent),
            speechSpeed = prefs.getFloat(KEY_SPEECH_SPEED, 1.0f),
            theme = AppTheme.fromRawValue(rawTheme),
            language = AppLanguage.fromRawValue(rawLang)
        )
    }

    fun setScenario(scenario: ConversationScenario) {
        prefs.edit().putString(KEY_SCENARIO, scenario.rawValue).apply()
        _userPreferences.value = _userPreferences.value.copy(scenario = scenario)
    }

    fun setAutoTranslate(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_TRANSLATE, enabled).apply()
        _userPreferences.value = _userPreferences.value.copy(autoTranslate = enabled)
    }

    fun setAccent(accent: VoiceAccent) {
        prefs.edit().putString(KEY_ACCENT, accent.rawValue).apply()
        _userPreferences.value = _userPreferences.value.copy(accent = accent)
    }

    fun setSpeechSpeed(speed: Float) {
        prefs.edit().putFloat(KEY_SPEECH_SPEED, speed).apply()
        _userPreferences.value = _userPreferences.value.copy(speechSpeed = speed)
    }

    fun setTheme(theme: AppTheme) {
        prefs.edit().putString(KEY_THEME, theme.rawValue).apply()
        _userPreferences.value = _userPreferences.value.copy(theme = theme)
    }

    fun setLanguage(language: AppLanguage) {
        prefs.edit().putString(KEY_LANGUAGE, language.rawValue).apply()
        _userPreferences.value = _userPreferences.value.copy(language = language)

        val localeList = if (language.localeCode.isEmpty()) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(language.localeCode)
        }
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    companion object {
        private const val KEY_SCENARIO = "scenario"
        private const val KEY_AUTO_TRANSLATE = "auto_translate"
        private const val KEY_ACCENT = "accent"
        private const val KEY_SPEECH_SPEED = "speech_speed"
        private const val KEY_THEME = "theme"
        private const val KEY_LANGUAGE = "language"
    }
}
