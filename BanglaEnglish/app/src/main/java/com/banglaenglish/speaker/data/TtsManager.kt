package com.banglaenglish.speaker.data

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/** Speaks English text sentence-by-sentence with accent & speed control using TextToSpeech. */
class TtsManager(
    context: Context,
    private val onProblem: (String) -> Unit
) {
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private var tts: TextToSpeech? = null
    private var ready = false
    private var pending: Triple<String, Float, VoiceAccent>? = null

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status != TextToSpeech.SUCCESS) {
                onProblem("Text-to-speech চালু করা যায়নি।")
                return@TextToSpeech
            }
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                onProblem("English voice নেই। Settings > Text-to-speech এ গিয়ে English ইনস্টল করুন।")
                return@TextToSpeech
            }
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) { _isSpeaking.value = true }
                override fun onDone(utteranceId: String?) { _isSpeaking.value = false }
                override fun onStop(utteranceId: String?, interrupted: Boolean) { _isSpeaking.value = false }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) { _isSpeaking.value = false }
            })
            ready = true
            pending?.let { (text, speed, accent) -> speak(text, speed, accent) }
            pending = null
        }
    }

    /**
     * Speaks the text sentence by sentence.
     * @param text The string to speak.
     * @param speed Speech rate multiplier (0.5x to 1.2x).
     * @param accent Accent selection (VoiceAccent.US or VoiceAccent.UK).
     */
    fun speak(text: String, speed: Float = 1.0f, accent: VoiceAccent = VoiceAccent.US) {
        if (!ready) {
            pending = Triple(text, speed, accent)
            return
        }

        stop()

        val locale = if (accent == VoiceAccent.UK) Locale.UK else Locale.US
        tts?.setLanguage(locale)

        // Find best voice for selected locale
        runCatching {
            val voices = tts?.voices
            val bestVoice = voices?.find { v ->
                v.locale.language == locale.language &&
                        v.locale.country == locale.country &&
                        !v.isNetworkConnectionRequired
            } ?: voices?.find { v -> v.locale.language == locale.language }

            if (bestVoice != null) {
                tts?.voice = bestVoice
            }
        }

        tts?.setSpeechRate(speed.coerceIn(0.5f, 1.2f))

        // Split long sentences by punctuation to speak smoothly sentence-by-sentence
        val sentences = text.split(Regex("(?<=[.!?\\n])\\s+"))
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (sentences.isEmpty()) return

        sentences.forEachIndexed { index, sentence ->
            val queueMode = if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            val utteranceId = "utt_${System.nanoTime()}_$index"
            tts?.speak(sentence, queueMode, null, utteranceId)
        }
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
