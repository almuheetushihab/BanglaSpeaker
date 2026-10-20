package com.banglaenglish.speaker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "practice_sessions")
data class PracticeSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val targetSentence: String,
    val spokenText: String,
    val score: Int, // 0 to 100%
    val mode: String = "repeat_after_me", // "repeat_after_me" or "roleplay"
    val timestamp: Long = System.currentTimeMillis()
)
