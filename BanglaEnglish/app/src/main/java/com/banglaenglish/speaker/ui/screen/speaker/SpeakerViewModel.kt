package com.banglaenglish.speaker.ui.screen.speaker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.banglaenglish.speaker.R
import com.banglaenglish.speaker.data.ConversationItem
import com.banglaenglish.speaker.data.SpeechEvent
import com.banglaenglish.speaker.data.SpeechRecognizerManager
import com.banglaenglish.speaker.data.TranslationRepository
import com.banglaenglish.speaker.data.TtsManager
import com.banglaenglish.speaker.ui.UiText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SpeakerViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(SpeakerUiState())
    val state: StateFlow<SpeakerUiState> = _state.asStateFlow()

    private val speech = SpeechRecognizerManager(application)
    private val translation = TranslationRepository()
    private val tts = TtsManager(application) { msg -> showMessage(UiText.DynamicString(msg)) }

    private var listenJob: Job? = null

    init {
        downloadModel()

        viewModelScope.launch {
            tts.isSpeaking.collect { speaking ->
                _state.update {
                    when {
                        speaking -> it.copy(phase = Phase.Speaking)
                        it.phase == Phase.Speaking -> it.copy(phase = Phase.Idle)
                        else -> it
                    }
                }
            }
        }
    }

    fun downloadModel() {
        _state.update { it.copy(modelState = ModelState.Downloading) }
        viewModelScope.launch {
            runCatching { translation.prepareModel() }
                .onSuccess { _state.update { it.copy(modelState = ModelState.Ready) } }
                .onFailure { _state.update { it.copy(modelState = ModelState.Failed) } }
        }
    }

    fun onMicTapped() {
        when (_state.value.phase) {
            Phase.Listening -> cancelListening()
            Phase.Translating -> Unit
            else -> startListening()
        }
    }

    fun translateTyped(text: String) {
        val clean = text.trim()
        if (clean.isEmpty()) return
        val phase = _state.value.phase
        if (phase == Phase.Listening || phase == Phase.Translating) return

        tts.stop()
        listenJob?.cancel()
        listenJob = viewModelScope.launch { translateAndSpeak(clean) }
    }

    fun replay(item: ConversationItem) {
        tts.speak(item.english, _state.value.slowSpeech)
    }

    fun toggleSlowSpeech() {
        _state.update { it.copy(slowSpeech = !it.slowSpeech) }
    }

    fun clearConversation() {
        tts.stop()
        _state.update { it.copy(conversation = emptyList()) }
    }

    fun showMessage(message: UiText) {
        _state.update { it.copy(message = message) }
    }

    fun consumeMessage() {
        _state.update { it.copy(message = null) }
    }

    private fun startListening() {
        if (!speech.isAvailable()) {
            showMessage(UiText.StringResource(R.string.error_speech_not_available))
            return
        }
        tts.stop()
        listenJob?.cancel()
        _state.update { it.copy(phase = Phase.Listening, partialText = "", micLevel = 0f) }

        listenJob = viewModelScope.launch {
            speech.listen().collect { event ->
                when (event) {
                    SpeechEvent.Ready -> Unit
                    is SpeechEvent.Level -> _state.update { it.copy(micLevel = event.value) }
                    is SpeechEvent.Partial -> _state.update { it.copy(partialText = event.text) }
                    is SpeechEvent.Final -> {
                        _state.update { it.copy(partialText = "", micLevel = 0f) }
                        translateAndSpeak(event.text)
                    }
                    is SpeechEvent.Failure -> _state.update {
                        it.copy(
                            phase = Phase.Idle,
                            partialText = "",
                            micLevel = 0f,
                            message = UiText.DynamicString(event.message)
                        )
                    }
                }
            }
        }
    }

    private fun cancelListening() {
        listenJob?.cancel()
        _state.update { it.copy(phase = Phase.Idle, partialText = "", micLevel = 0f) }
    }

    private suspend fun translateAndSpeak(bangla: String) {
        _state.update { it.copy(phase = Phase.Translating) }
        runCatching { translation.translate(bangla) }
            .onSuccess { english ->
                val item = ConversationItem(bangla = bangla, english = english)
                _state.update { it.copy(phase = Phase.Idle, conversation = it.conversation + item) }
                tts.speak(english, _state.value.slowSpeech)
            }
            .onFailure { e ->
                if (e is CancellationException) throw e
                _state.update {
                    it.copy(
                        phase = Phase.Idle,
                        message = UiText.StringResource(R.string.error_translation_failed)
                    )
                }
            }
    }

    override fun onCleared() {
        listenJob?.cancel()
        tts.shutdown()
        translation.close()
        super.onCleared()
    }
}
