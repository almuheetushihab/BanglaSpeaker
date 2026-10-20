package com.banglaenglish.speaker.ui.screen.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.banglaenglish.speaker.BuildConfig
import com.banglaenglish.speaker.data.AppLanguage
import com.banglaenglish.speaker.data.AppTheme
import com.banglaenglish.speaker.data.ConversationScenario
import com.banglaenglish.speaker.data.UserPreferences
import com.banglaenglish.speaker.data.UserPreferencesRepository
import com.banglaenglish.speaker.data.VoiceAccent
import com.banglaenglish.speaker.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val userPreferences: UserPreferences = UserPreferences(),
    val isGeminiConfigured: Boolean = BuildConfig.GEMINI_API_KEY.isNotBlank(),
    val showClearConfirmation: Boolean = false
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val userPrefsRepo = UserPreferencesRepository(application)
    private val db = AppDatabase.getInstance(application)

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            userPrefsRepo.userPreferences.collect { prefs ->
                _state.update { it.copy(userPreferences = prefs) }
            }
        }
    }

    fun setScenario(scenario: ConversationScenario) = userPrefsRepo.setScenario(scenario)
    fun setAutoTranslate(enabled: Boolean) = userPrefsRepo.setAutoTranslate(enabled)
    fun setAccent(accent: VoiceAccent) = userPrefsRepo.setAccent(accent)
    fun setSpeechSpeed(speed: Float) = userPrefsRepo.setSpeechSpeed(speed)
    fun setTheme(theme: AppTheme) = userPrefsRepo.setTheme(theme)
    fun setLanguage(language: AppLanguage) = userPrefsRepo.setLanguage(language)

    fun showClearConfirmation(show: Boolean) {
        _state.update { it.copy(showClearConfirmation = show) }
    }

    fun clearAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            db.conversationDao().deleteAll()
            _state.update { it.copy(showClearConfirmation = false) }
        }
    }
}
