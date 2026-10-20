package com.banglaenglish.speaker.data

/** Interface for Bangla to English translation engines (Gemini AI, ML Kit fallback). */
interface TranslationEngine {
    suspend fun translate(request: TranslationRequest): TranslationResult
}
