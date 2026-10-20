package com.banglaenglish.speaker.data

import com.banglaenglish.speaker.BuildConfig
import kotlinx.coroutines.CancellationException

class TranslationRepository(
    private val geminiEngine: GeminiTranslationEngine = GeminiTranslationEngine(),
    private val mlKitEngine: MlKitTranslationEngine = MlKitTranslationEngine()
) {

    suspend fun prepareOfflineModel() {
        mlKitEngine.prepareModel()
    }

    suspend fun translate(request: TranslationRequest): TranslationResult {
        // Try Gemini AI first if key is configured
        if (BuildConfig.GEMINI_API_KEY.isNotBlank()) {
            try {
                return geminiEngine.translate(request)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Gemini API failed, fall back silently to ML Kit
            }
        }

        // Offline fallback using ML Kit
        return mlKitEngine.translate(request)
    }

    fun close() {
        mlKitEngine.close()
    }
}
