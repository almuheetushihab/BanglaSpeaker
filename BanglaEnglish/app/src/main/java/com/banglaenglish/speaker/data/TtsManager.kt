package com.banglaenglish.speaker.data

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/** Speaks English text using Android's built-in TextToSpeech engine (free). */
class TtsManager(
    context: Context,
    private val onProblem: (String) -> Unit,
) {
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private var tts: TextToSpeech? = null
    private var ready = false
    private var pending: Pair<String, Boolean>? = null

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
            pending?.let { (text, slow) -> speak(text, slow) }
            pending = null
        }
    }

    fun speak(text: String, slow: Boolean) {
        if (!ready) {
            pending = text to slow
            return
        }
        tts?.setSpeechRate(if (slow) 0.65f else 1.0f)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utt-${System.nanoTime()}")
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
