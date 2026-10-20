package com.banglaenglish.speaker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.banglaenglish.speaker.data.ConversationItem
import com.banglaenglish.speaker.data.KeyPhrase
import com.banglaenglish.speaker.data.TranslationEngineType
import com.banglaenglish.speaker.data.TranslationIntent
import com.banglaenglish.speaker.data.TranslationStyle
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val bangla: String,
    val correctedBangla: String,
    val intent: String,
    val english: String,
    val englishFormal: String,
    val englishCasual: String,
    val selectedStyle: String = "english",
    val tipBn: String? = null,
    val keyPhrasesJson: String = "[]",
    val isBookmarked: Boolean = false,
    val engineUsed: String = "Gemini",
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toDomainModel(): ConversationItem {
        val parsedPhrases = runCatching {
            Json.decodeFromString<List<KeyPhrase>>(keyPhrasesJson)
        }.getOrDefault(emptyList())

        return ConversationItem(
            id = id,
            bangla = bangla,
            correctedBangla = correctedBangla,
            intent = TranslationIntent.fromRawValue(intent),
            english = english,
            englishFormal = englishFormal,
            englishCasual = englishCasual,
            selectedStyle = TranslationStyle.fromRawValue(selectedStyle),
            tipBn = tipBn,
            keyPhrases = parsedPhrases,
            isBookmarked = isBookmarked,
            engineUsed = TranslationEngineType.fromRawValue(engineUsed),
            timestamp = timestamp
        )
    }

    companion object {
        fun fromDomainModel(item: ConversationItem): ConversationEntity {
            val jsonPhrases = runCatching {
                Json.encodeToString(item.keyPhrases)
            }.getOrDefault("[]")

            return ConversationEntity(
                id = item.id,
                bangla = item.bangla,
                correctedBangla = item.correctedBangla,
                intent = item.intent.rawValue,
                english = item.english,
                englishFormal = item.englishFormal,
                englishCasual = item.englishCasual,
                selectedStyle = item.selectedStyle.rawValue,
                tipBn = item.tipBn,
                keyPhrasesJson = jsonPhrases,
                isBookmarked = item.isBookmarked,
                engineUsed = item.engineUsed.rawValue,
                timestamp = item.timestamp
            )
        }
    }
}
