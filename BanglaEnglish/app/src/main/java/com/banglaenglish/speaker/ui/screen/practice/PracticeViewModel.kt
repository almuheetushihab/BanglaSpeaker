package com.banglaenglish.speaker.ui.screen.practice

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.banglaenglish.speaker.data.ConversationScenario
import com.banglaenglish.speaker.data.SpeechEvent
import com.banglaenglish.speaker.data.SpeechRecognizerManager
import com.banglaenglish.speaker.data.TranslationRepository
import com.banglaenglish.speaker.data.TranslationRequest
import com.banglaenglish.speaker.data.TtsManager
import com.banglaenglish.speaker.data.UserPreferencesRepository
import com.banglaenglish.speaker.data.local.AppDatabase
import com.banglaenglish.speaker.data.local.PracticeSessionEntity
import com.banglaenglish.speaker.data.local.VocabularyEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

data class WordResult(
    val word: String,
    val isMatched: Boolean
)

data class RolePlayMessage(
    val sender: String, // "AI" or "User"
    val englishText: String,
    val banglaTranslation: String = ""
)

data class PracticeUiState(
    val selectedTab: Int = 0, // 0 = Repeat After Me, 1 = AI Roleplay, 2 = Vocab Notebook
    val streakDays: Int = 1,
    val targetSentence: String = "I would like to order a cup of coffee, please.",
    val recognizedSpeech: String = "",
    val wordComparison: List<WordResult> = emptyList(),
    val score: Int? = null,
    val isRecording: Boolean = false,

    // Roleplay state
    val rolePlayScenario: String = ConversationScenario.JOB_INTERVIEW.rawValue,
    val rolePlayMessages: List<RolePlayMessage> = emptyList(),
    val isRoleplayLoading: Boolean = false,

    // Vocab Notebook
    val dueVocabList: List<VocabularyEntity> = emptyList(),
    val currentVocabIndex: Int = 0,
    val isCardFlipped: Boolean = false
)

class PracticeViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(PracticeUiState())
    val state: StateFlow<PracticeUiState> = _state.asStateFlow()

    private val db = AppDatabase.getInstance(application)
    private val practiceDao = db.practiceDao()
    private val vocabDao = db.vocabularyDao()

    private val speech = SpeechRecognizerManager(application)
    private val translation = TranslationRepository()
    private val userPrefsRepo = UserPreferencesRepository(application)
    private val tts = TtsManager(application) { }

    private var recordJob: Job? = null

    init {
        calculateStreak()
        loadVocabNotebook()
        initDefaultRoleplay()
    }

    fun setSelectedTab(index: Int) {
        _state.update { it.copy(selectedTab = index) }
    }

    fun setTargetSentence(sentence: String) {
        _state.update {
            it.copy(
                targetSentence = sentence,
                recognizedSpeech = "",
                wordComparison = emptyList(),
                score = null
            )
        }
    }

    fun playTargetSentence() {
        val prefs = userPrefsRepo.userPreferences.value
        tts.speak(_state.value.targetSentence, prefs.speechSpeed, prefs.accent)
    }

    fun startListeningUserAttempt() {
        if (!speech.isAvailable()) return

        tts.stop()
        recordJob?.cancel()

        _state.update {
            it.copy(
                isRecording = true,
                recognizedSpeech = "",
                wordComparison = emptyList(),
                score = null
            )
        }

        recordJob = viewModelScope.launch {
            // Listen in English for practice
            speech.listen(languageTag = "en-US").collect { event ->
                when (event) {
                    is SpeechEvent.Partial -> _state.update { it.copy(recognizedSpeech = event.text) }
                    is SpeechEvent.FinalChunk -> {
                        val fullSpeech = event.text
                        _state.update { it.copy(recognizedSpeech = fullSpeech, isRecording = false) }
                        evaluateSpeechAttempt(fullSpeech)
                    }
                    is SpeechEvent.Failure -> {
                        _state.update { it.copy(isRecording = false) }
                    }
                    else -> Unit
                }
            }
        }
    }

    fun stopListeningUserAttempt() {
        recordJob?.cancel()
        _state.update { it.copy(isRecording = false) }
        val currentRecognized = _state.value.recognizedSpeech
        if (currentRecognized.isNotBlank()) {
            evaluateSpeechAttempt(currentRecognized)
        }
    }

    private fun evaluateSpeechAttempt(userSpoken: String) {
        val targetWords = _state.value.targetSentence
            .lowercase()
            .replace(Regex("[^a-z0-9\\s]"), "")
            .split("\\s+".toRegex())

        val userWords = userSpoken
            .lowercase()
            .replace(Regex("[^a-z0-9\\s]"), "")
            .split("\\s+".toRegex())

        var matchedCount = 0
        val comparison = targetWords.map { word ->
            val match = userWords.contains(word)
            if (match) matchedCount++
            WordResult(word = word, isMatched = match)
        }

        val accuracyScore = if (targetWords.isNotEmpty()) {
            ((matchedCount.toFloat() / targetWords.size.toFloat()) * 100).toInt().coerceIn(0, 100)
        } else 0

        _state.update {
            it.copy(
                wordComparison = comparison,
                score = accuracyScore
            )
        }

        // Save session
        viewModelScope.launch(Dispatchers.IO) {
            practiceDao.insert(
                PracticeSessionEntity(
                    targetSentence = _state.value.targetSentence,
                    spokenText = userSpoken,
                    score = accuracyScore
                )
            )
            calculateStreak()
        }
    }

    private fun calculateStreak() {
        viewModelScope.launch(Dispatchers.IO) {
            val timestamps = practiceDao.getAllSessionTimestamps()
            if (timestamps.isEmpty()) {
                _state.update { it.copy(streakDays = 1) }
                return@launch
            }

            var streak = 1
            val calendar = Calendar.getInstance()

            var lastDayOfYear = -1
            for (ts in timestamps) {
                calendar.timeInMillis = ts
                val day = calendar.get(Calendar.DAY_OF_YEAR)
                if (lastDayOfYear != -1 && lastDayOfYear - day == 1) {
                    streak++
                }
                lastDayOfYear = day
            }

            _state.update { it.copy(streakDays = streak.coerceAtLeast(1)) }
        }
    }

    // AI Roleplay logic
    private fun initDefaultRoleplay() {
        _state.update {
            it.copy(
                rolePlayMessages = listOf(
                    RolePlayMessage(
                        sender = "AI",
                        englishText = "Good morning! Welcome to the interview. Could you tell me a little bit about yourself?",
                        banglaTranslation = "শুভ সকাল! ইন্টারভিউতে আপনাকে স্বাগতম। আপনার নিজের সম্পর্কে কিছু বলবেন?"
                    )
                )
            )
        }
    }

    fun setRoleplayScenario(scenario: ConversationScenario) {
        val initialMsg = when (scenario) {
            ConversationScenario.SHOPPING -> RolePlayMessage(
                sender = "AI",
                englishText = "Hi there! What can I get started for you today?",
                banglaTranslation = "হাই! আজ আপনার জন্য কী অর্ডার নেওয়া যাক?"
            )
            ConversationScenario.TRAVEL -> RolePlayMessage(
                sender = "AI",
                englishText = "Hello! Welcome to Grand Palace Hotel. How may I assist you today?",
                banglaTranslation = "হ্যালো! গ্র্যান্ড প্যালেস হোটেলে স্বাগতম। আপনাকে কীভাবে সাহায্য করতে পারি?"
            )
            else -> RolePlayMessage(
                sender = "AI",
                englishText = "Good morning! Welcome to the interview. Could you tell me a little bit about yourself?",
                banglaTranslation = "শুভ সকাল! ইন্টারভিউতে আপনাকে স্বাগতম। আপনার নিজের সম্পর্কে কিছু বলবেন?"
            )
        }
        _state.update {
            it.copy(
                rolePlayScenario = scenario.rawValue,
                rolePlayMessages = listOf(initialMsg)
            )
        }
    }

    fun sendRoleplayUserReply(userBanglaText: String) {
        if (userBanglaText.isBlank()) return

        _state.update { it.copy(isRoleplayLoading = true) }

        viewModelScope.launch {
            runCatching {
                val req = TranslationRequest(
                    banglaText = userBanglaText,
                    scenario = ConversationScenario.fromRawValue(_state.value.rolePlayScenario)
                )
                val res = translation.translate(req)

                val userMsg = RolePlayMessage(
                    sender = "User",
                    englishText = res.english,
                    banglaTranslation = userBanglaText
                )

                val updatedList = _state.value.rolePlayMessages + userMsg
                _state.update { it.copy(rolePlayMessages = updatedList) }

                // Now generate AI response turn
                val aiNextReq = TranslationRequest(
                    banglaText = "In character as ${_state.value.rolePlayScenario} interviewer/partner, reply naturally to user's answer: ${res.english}",
                    scenario = ConversationScenario.fromRawValue(_state.value.rolePlayScenario)
                )
                val aiRes = translation.translate(aiNextReq)

                val aiMsg = RolePlayMessage(
                    sender = "AI",
                    englishText = aiRes.english,
                    banglaTranslation = aiRes.correctedBangla
                )

                _state.update {
                    it.copy(
                        rolePlayMessages = updatedList + aiMsg,
                        isRoleplayLoading = false
                    )
                }

                // Play AI reply
                val prefs = userPrefsRepo.userPreferences.value
                tts.speak(aiRes.english, prefs.speechSpeed, prefs.accent)
            }.onFailure {
                _state.update { it.copy(isRoleplayLoading = false) }
            }
        }
    }

    // Vocabulary notebook
    private fun loadVocabNotebook() {
        viewModelScope.launch {
            vocabDao.getAllFlow().collect { list ->
                _state.update { it.copy(dueVocabList = list) }
            }
        }
    }

    fun flipCard() {
        _state.update { it.copy(isCardFlipped = !it.isCardFlipped) }
    }

    fun rateVocabCard(rating: Int) { // 1 = Easy, 2 = Medium, 3 = Hard
        val currentList = _state.value.dueVocabList
        val index = _state.value.currentVocabIndex
        if (index >= currentList.size) return

        val card = currentList[index]
        val addedDays = when (rating) {
            1 -> 3L * 24 * 3600 * 1000
            2 -> 1L * 24 * 3600 * 1000
            else -> 0L
        }

        viewModelScope.launch(Dispatchers.IO) {
            vocabDao.update(
                card.copy(
                    masteryLevel = (card.masteryLevel + if (rating == 1) 1 else 0).coerceAtMost(5),
                    nextReviewTime = System.currentTimeMillis() + addedDays,
                    lastReviewedTime = System.currentTimeMillis()
                )
            )
            withContext(Dispatchers.Main) {
                _state.update {
                    it.copy(
                        currentVocabIndex = (index + 1) % currentList.size.coerceAtLeast(1),
                        isCardFlipped = false
                    )
                }
            }
        }
    }

    override fun onCleared() {
        tts.shutdown()
        super.onCleared()
    }
}
