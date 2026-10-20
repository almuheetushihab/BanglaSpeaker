package com.banglaenglish.speaker.data

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn

sealed interface SpeechEvent {
    data object Ready : SpeechEvent
    data class Level(val value: Float) : SpeechEvent      // 0f..1f
    data class Partial(val text: String) : SpeechEvent
    data class FinalChunk(val text: String) : SpeechEvent
    data class Failure(val message: String, val isTransientSilence: Boolean) : SpeechEvent
}

/**
 * Wraps Android's SpeechRecognizer as a cold Flow.
 * Emits events including RMS level, partial text, final speech chunks, and errors.
 */
class SpeechRecognizerManager(private val context: Context) {

    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    fun listen(languageTag: String = "bn-BD"): Flow<SpeechEvent> = callbackFlow {
        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)

        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                trySend(SpeechEvent.Ready)
            }

            override fun onRmsChanged(rmsdB: Float) {
                // rmsdB is roughly -2..10
                trySend(SpeechEvent.Level(((rmsdB + 2f) / 12f).coerceIn(0f, 1f)))
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val text = partialResults
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull().orEmpty()
                if (text.isNotBlank()) trySend(SpeechEvent.Partial(text))
            }

            override fun onResults(results: Bundle?) {
                val text = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull().orEmpty()
                if (text.isNotBlank()) {
                    trySend(SpeechEvent.FinalChunk(text))
                } else {
                    trySend(SpeechEvent.Failure("কিছু শুনতে পাইনি।", isTransientSilence = true))
                }
                close()
            }

            override fun onError(error: Int) {
                val isTransient = error == SpeechRecognizer.ERROR_NO_MATCH ||
                        error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT ||
                        error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY
                trySend(SpeechEvent.Failure(errorToMessage(error), isTransientSilence = isTransient))
                close()
            }

            override fun onBeginningOfSpeech() {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)

            // Length hints to prevent cutting off early
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2500L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 5000L)
        }

        recognizer.startListening(intent)

        awaitClose { recognizer.destroy() }
    }.flowOn(Dispatchers.Main)

    private fun errorToMessage(error: Int): String = when (error) {
        SpeechRecognizer.ERROR_NO_MATCH,
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "কিছু শুনতে পাইনি। আবার কথা বলুন।"

        SpeechRecognizer.ERROR_NETWORK,
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT,
        SpeechRecognizer.ERROR_SERVER -> "ইন্টারনেট সংযোগ চেক করুন।"

        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "মাইক্রোফোনের অনুমতি দিন।"
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "রিকগনাইজার ব্যস্ত, পুনঃচেষ্টা করা হচ্ছে..."
        SpeechRecognizer.ERROR_AUDIO -> "অডিও রেকর্ড করতে সমস্যা হয়েছে।"
        12, 13 -> "এই ফোনে বাংলা ভয়েস সাপোর্ট নেই। Google অ্যাপ আপডেট করুন।"
        else -> "কিছু একটা সমস্যা হয়েছে। আবার চেষ্টা করুন।"
    }
}
