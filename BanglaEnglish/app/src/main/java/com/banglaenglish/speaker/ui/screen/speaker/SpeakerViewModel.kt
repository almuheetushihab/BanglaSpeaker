package com.banglaenglish.speaker.ui.screen.speaker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.banglaenglish.speaker.R
import com.banglaenglish.speaker.data.ConversationItem
import com.banglaenglish.speaker.data.SpeechEvent
import com.banglaenglish.speaker.data.SpeechRecognizerManager
import com.banglaenglish.speaker.data.TranslationEngineType
import com.banglaenglish.speaker.data.TranslationRepository
import com.banglaenglish.speaker.data.TranslationRequest
import com.banglaenglish.speaker.data.TranslationStyle
import com.banglaenglish.speaker.data.TtsManager
import com.banglaenglish.speaker.data.UserPreferencesRepository
import com.banglaenglish.speaker.data.local.AppDatabase
import com.banglaenglish.speaker.data.local.ConversationEntity
import com.banglaenglish.speaker.ui.UiText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SpeakerViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(SpeakerUiState())
    val state: StateFlow<SpeakerUiState> = _state.asStateFlow()

    private val db = AppDatabase.getInstance(application)
    private val conversationDao = db.conversationDao()
    private val speech = SpeechRecognizerManager(application)
    private val translation = TranslationRepository()
    private val userPrefsRepo = UserPreferencesRepository(application)
    private val tts = TtsManager(application) { msg -> showMessage(UiText.DynamicString(msg)) }

    private var dictationJob: Job? = null
    private var timerJob: Job? = null
    private var isDictatingSessionActive = false

    init {
        downloadModel()

        // Load conversation history from Room DB
        viewModelScope.launch {
            conversationDao.getAllFlow().collect { entities ->
                val domainItems = entities.map { it.toDomainModel() }
                _state.update { it.copy(conversation = domainItems) }
            }
        }

        // Observe TTS speaking state
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
            runCatching { translation.prepareOfflineModel() }
                .onSuccess { _state.update { it.copy(modelState = ModelState.Ready) } }
                .onFailure { _state.update { it.copy(modelState = ModelState.Failed) } }
        }
    }

    fun onMicTapped() {
        when (_state.value.phase) {
            Phase.Listening -> finishDictation()
            Phase.Translating -> Unit
            else -> startDictation()
        }
    }

    private fun startDictation() {
        if (!speech.isAvailable()) {
            showMessage(UiText.StringResource(R.string.error_speech_not_available))
            return
        }

        tts.stop()
        dictationJob?.cancel()
        timerJob?.cancel()

        isDictatingSessionActive = true
        _state.update {
            it.copy(
                phase = Phase.Listening,
                accumulatedText = "",
                livePartialText = "",
                micLevel = 0f,
                recordingSeconds = 0L
            )
        }

        // Timer job
        timerJob = viewModelScope.launch {
            while (isDictatingSessionActive) {
                delay(1000)
                _state.update { it.copy(recordingSeconds = it.recordingSeconds + 1) }
            }
        }

        // Dictation loop
        dictationJob = viewModelScope.launch {
            runDictationLoop()
        }
    }

    private suspend fun runDictationLoop() {
        while (isDictatingSessionActive) {
            speech.listen().collect { event ->
                when (event) {
                    SpeechEvent.Ready -> Unit
                    is SpeechEvent.Level -> _state.update { it.copy(micLevel = event.value) }
                    is SpeechEvent.Partial -> _state.update { it.copy(livePartialText = event.text) }
                    is SpeechEvent.FinalChunk -> {
                        val currentAcc = _state.value.accumulatedText
                        val updatedAcc = if (currentAcc.isBlank()) event.text else "$currentAcc ${event.text}"
                        _state.update {
                            it.copy(
                                accumulatedText = updatedAcc,
                                livePartialText = "",
                                micLevel = 0f
                            )
                        }
                    }
                    is SpeechEvent.Failure -> {
                        _state.update { it.copy(livePartialText = "", micLevel = 0f) }
                        if (!event.isTransientSilence) {
                            isDictatingSessionActive = false
                            _state.update {
                                it.copy(
                                    phase = Phase.Idle,
                                    message = UiText.DynamicString(event.message)
                                )
                            }
                        }
                    }
                }
            }

            if (isDictatingSessionActive) {
                delay(150) // Small delay before silently restarting recognizer loop
            }
        }
    }

    fun finishDictation() {
        isDictatingSessionActive = false
        dictationJob?.cancel()
        timerJob?.cancel()

        val currentState = _state.value
        val capturedText = currentState.getDisplayTranscript().trim()

        _state.update {
            it.copy(
                phase = Phase.Idle,
                accumulatedText = "",
                livePartialText = "",
                micLevel = 0f,
                recordingSeconds = 0L
            )
        }

        if (capturedText.isBlank()) {
            showMessage(UiText.StringResource(R.string.error_speech_no_match))
            return
        }

        val autoTranslate = userPrefsRepo.userPreferences.value.autoTranslate
        if (autoTranslate) {
            translateAndSpeak(capturedText)
        } else {
            _state.update {
                it.copy(
                    phase = Phase.EditingTranscript,
                    editableTranscript = capturedText
                )
            }
        }
    }

    fun updateEditableTranscript(text: String) {
        _state.update { it.copy(editableTranscript = text) }
    }

    fun confirmEditAndTranslate() {
        val text = _state.value.editableTranscript.trim()
        if (text.isBlank()) return
        _state.update { it.copy(phase = Phase.Idle) }
        translateAndSpeak(text)
    }

    fun cancelEditAndReRecord() {
        _state.update { it.copy(phase = Phase.Idle, editableTranscript = "") }
        startDictation()
    }

    fun translateTyped(text: String) {
        val clean = text.trim()
        if (clean.isEmpty()) return
        val phase = _state.value.phase
        if (phase == Phase.Listening || phase == Phase.Translating) return

        tts.stop()
        dictationJob?.cancel()
        timerJob?.cancel()
        isDictatingSessionActive = false

        translateAndSpeak(clean)
    }

    private fun translateAndSpeak(bangla: String) {
        _state.update { it.copy(phase = Phase.Translating) }

        viewModelScope.launch {
            val scenario = userPrefsRepo.userPreferences.value.scenario
            val request = TranslationRequest(
                banglaText = bangla,
                history = _state.value.conversation,
                scenario = scenario
            )

            runCatching { translation.translate(request) }
                .onSuccess { result ->
                    val item = ConversationItem(
                        bangla = bangla,
                        correctedBangla = result.correctedBangla,
                        intent = result.intent,
                        english = result.english,
                        englishFormal = result.englishFormal,
                        englishCasual = result.englishCasual,
                        tipBn = result.tipBn,
                        keyPhrases = result.keyPhrases,
                        engineUsed = result.engineUsed
                    )

                    // Insert into Room DB
                    val id = withContext(Dispatchers.IO) {
                        conversationDao.insert(ConversationEntity.fromDomainModel(item))
                    }
                    val savedItem = item.copy(id = id)

                    _state.update {
                        it.copy(
                            phase = Phase.Idle,
                            editableTranscript = ""
                        )
                    }

                    // Inform engine used if ML Kit fallback was used
                    if (result.engineUsed == TranslationEngineType.ML_KIT) {
                        showMessage(UiText.StringResource(R.string.engine_mlkit))
                    }

                    // Speak natural English translation
                    speakItem(savedItem)
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
    }

    fun selectStyle(item: ConversationItem, style: TranslationStyle) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = item.copy(selectedStyle = style)
            conversationDao.update(ConversationEntity.fromDomainModel(updated))
            withContext(Dispatchers.Main) {
                speakItem(updated)
            }
        }
    }

    fun toggleBookmark(item: ConversationItem) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = item.copy(isBookmarked = !item.isBookmarked)
            conversationDao.update(ConversationEntity.fromDomainModel(updated))
        }
    }

    fun speakItem(item: ConversationItem) {
        val textToSpeak = item.getActiveEnglishText()
        val prefs = userPrefsRepo.userPreferences.value
        tts.speak(textToSpeak, prefs.speechSpeed, prefs.accent)
    }

    fun toggleSlowSpeech() {
        _state.update { it.copy(slowSpeech = !it.slowSpeech) }
    }

    fun clearConversation() {
        tts.stop()
        viewModelScope.launch(Dispatchers.IO) {
            conversationDao.deleteAll()
        }
    }

    fun showMessage(message: UiText) {
        _state.update { it.copy(message = message) }
    }

    fun consumeMessage() {
        _state.update { it.copy(message = null) }
    }

    override fun onCleared() {
        isDictatingSessionActive = false
        dictationJob?.cancel()
        timerJob?.cancel()
        tts.shutdown()
        translation.close()
        super.onCleared()
    }
}
