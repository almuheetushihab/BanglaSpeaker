package com.banglaenglish.speaker.data

/** One finished round: what the user said in Bangla and its English translation. */
data class ConversationItem(
    val id: Long = System.nanoTime(),
    val bangla: String,
    val english: String,
)
