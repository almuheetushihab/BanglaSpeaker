package com.banglaenglish.speaker.data

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await

/** Bangla -> English using Google ML Kit (offline fallback). */
class MlKitTranslationEngine : TranslationEngine {

    private val translator: Translator = Translation.getClient(
        TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.BENGALI)
            .setTargetLanguage(TranslateLanguage.ENGLISH)
            .build()
    )

    suspend fun prepareModel() {
        translator.downloadModelIfNeeded(DownloadConditions.Builder().build()).await()
    }

    override suspend fun translate(request: TranslationRequest): TranslationResult {
        prepareModel()
        val englishTranslation = translator.translate(request.banglaText).await()

        return TranslationResult(
            correctedBangla = request.banglaText,
            intent = TranslationIntent.STATEMENT,
            english = englishTranslation,
            englishFormal = englishTranslation,
            englishCasual = englishTranslation,
            tipBn = null,
            keyPhrases = emptyList(),
            engineUsed = TranslationEngineType.ML_KIT
        )
    }

    fun close() {
        translator.close()
    }
}
