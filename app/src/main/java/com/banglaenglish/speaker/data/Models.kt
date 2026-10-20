package com.banglaenglish.speaker.data

import androidx.annotation.StringRes
import com.banglaenglish.speaker.R
import kotlinx.serialization.Serializable

/** Intent classifications for translated utterances. */
enum class TranslationIntent(@param:StringRes val labelRes: Int, val rawValue: String) {
    QUESTION(R.string.intent_question, "question"),
    ANSWER(R.string.intent_answer, "answer"),
    STATEMENT(R.string.intent_statement, "statement"),
    REQUEST(R.string.intent_request, "request"),
    COMMAND(R.string.intent_command, "command"),
    GREETING(R.string.intent_greeting, "greeting"),
    OTHER(R.string.intent_other, "other");

    companion object {
        fun fromRawValue(value: String): TranslationIntent {
            return entries.find { it.rawValue.equals(value, ignoreCase = true) } ?: STATEMENT
        }
    }
}

/** Formality & style variations for translated sentences. */
enum class TranslationStyle(@param:StringRes val labelRes: Int, val rawValue: String) {
    NATURAL(R.string.style_natural, "english"),
    FORMAL(R.string.style_formal, "formal"),
    CASUAL(R.string.style_casual, "casual");

    companion object {
        fun fromRawValue(value: String): TranslationStyle {
            return entries.find { it.rawValue.equals(value, ignoreCase = true) } ?: NATURAL
        }
    }
}

/** Translation engines supported by the application. */
enum class TranslationEngineType(@param:StringRes val labelRes: Int, val rawValue: String) {
    GEMINI(R.string.engine_gemini, "Gemini"),
    ML_KIT(R.string.engine_mlkit, "ML Kit");

    companion object {
        fun fromRawValue(value: String): TranslationEngineType {
            return if (value.contains("ML", ignoreCase = true)) ML_KIT else GEMINI
        }
    }
}

/** Conversation scenarios for context-aware vocabulary adaptation. */
enum class ConversationScenario(@param:StringRes val labelRes: Int, val rawValue: String) {
    CASUAL_CHAT(R.string.scenario_casual, "Casual chat"),
    OFFICE_MEETING(R.string.scenario_office, "Office/Tech meeting"),
    JOB_INTERVIEW(R.string.scenario_interview, "Job interview"),
    TRAVEL(R.string.scenario_travel, "Travel"),
    SHOPPING(R.string.scenario_shopping, "Shopping"),
    STUDY(R.string.scenario_study, "Study");

    companion object {
        fun fromRawValue(value: String): ConversationScenario {
            return entries.find { it.rawValue.equals(value, ignoreCase = true) } ?: CASUAL_CHAT
        }
    }
}

/** Voice accents for Text-To-Speech output. */
enum class VoiceAccent(@param:StringRes val labelRes: Int, val rawValue: String) {
    US(R.string.accent_us, "US"),
    UK(R.string.accent_uk, "UK");

    companion object {
        fun fromRawValue(value: String): VoiceAccent {
            return entries.find { it.rawValue.equals(value, ignoreCase = true) } ?: US
        }
    }
}

/** Application UI theme selection. */
enum class AppTheme(@param:StringRes val labelRes: Int, val rawValue: String) {
    SYSTEM(R.string.theme_system, "System"),
    LIGHT(R.string.theme_light, "Light"),
    DARK(R.string.theme_dark, "Dark");

    companion object {
        fun fromRawValue(value: String): AppTheme {
            return entries.find { it.rawValue.equals(value, ignoreCase = true) } ?: SYSTEM
        }
    }
}

/** Application UI language selection. */
enum class AppLanguage(@param:StringRes val labelRes: Int, val rawValue: String, val localeCode: String) {
    SYSTEM(R.string.lang_system, "System", ""),
    ENGLISH(R.string.lang_english, "English", "en"),
    BANGLA(R.string.lang_bangla, "বাংলা", "bn");

    companion object {
        fun fromRawValue(value: String): AppLanguage {
            return entries.find { it.rawValue.equals(value, ignoreCase = true) } ?: SYSTEM
        }
    }
}

/** Roleplay conversation turn senders. */
enum class RoleplaySender {
    AI, USER
}

/** Spaced repetition review difficulty ratings. */
enum class VocabRating(val daysAdd: Long) {
    EASY(3L),
    MEDIUM(1L),
    HARD(0L)
}

/** A single key phrase translation pair extracted from AI response. */
@Serializable
data class KeyPhrase(
    val english: String = "",
    val bangla: String = ""
)

/** One finished round: what the user said in Bangla and its English translations. */
data class ConversationItem(
    val id: Long = 0L,
    val bangla: String,
    val correctedBangla: String = bangla,
    val intent: TranslationIntent = TranslationIntent.STATEMENT,
    val english: String,
    val englishFormal: String = english,
    val englishCasual: String = english,
    val selectedStyle: TranslationStyle = TranslationStyle.NATURAL,
    val tipBn: String? = null,
    val keyPhrases: List<KeyPhrase> = emptyList(),
    val isBookmarked: Boolean = false,
    val engineUsed: TranslationEngineType = TranslationEngineType.GEMINI,
    val timestamp: Long = System.currentTimeMillis()
) {
    /** Returns the text corresponding to the currently selected style. */
    fun getActiveEnglishText(): String = when (selectedStyle) {
        TranslationStyle.FORMAL -> englishFormal.ifBlank { english }
        TranslationStyle.CASUAL -> englishCasual.ifBlank { english }
        TranslationStyle.NATURAL -> english
    }
}

/** Request payload for translation engine. */
data class TranslationRequest(
    val banglaText: String,
    val history: List<ConversationItem> = emptyList(),
    val scenario: ConversationScenario = ConversationScenario.CASUAL_CHAT,
    val appLanguageCode: String = "bn"
)

/** Result output from translation engine. */
data class TranslationResult(
    val correctedBangla: String,
    val intent: TranslationIntent,
    val english: String,
    val englishFormal: String,
    val englishCasual: String,
    val tipBn: String? = null,
    val keyPhrases: List<KeyPhrase> = emptyList(),
    val engineUsed: TranslationEngineType
)
