package com.banglaenglish.speaker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vocabulary")
data class VocabularyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val english: String,
    val bangla: String,
    val masteryLevel: Int = 0, // 0 (New) to 5 (Mastered)
    val nextReviewTime: Long = System.currentTimeMillis(),
    val lastReviewedTime: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)
