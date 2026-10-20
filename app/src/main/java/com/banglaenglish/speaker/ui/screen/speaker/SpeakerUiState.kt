package com.banglaenglish.speaker.ui.screen.speaker

import com.banglaenglish.speaker.data.ConversationItem
import com.banglaenglish.speaker.ui.UiText

enum class Phase {
    Idle,
    Listening,
    EditingTranscript,
    Translating,
    Speaking
}

enum class ModelState {
    Downloading,
    Ready,
    Failed
}

data class SpeakerUiState(
    val phase: Phase = Phase.Idle,
    val modelState: ModelState = ModelState.Downloading,
    val accumulatedText: String = "",
    val livePartialText: String = "",
    val editableTranscript: String = "",
    val recordingSeconds: Long = 0L,
    val micLevel: Float = 0f,
    val slowSpeech: Boolean = false,
    val conversation: List<ConversationItem> = emptyList(),
    val message: UiText? = null
) {
    /** Combined text shown in real-time during dictation. */
    fun getDisplayTranscript(): String {
        return when {
            accumulatedText.isNotBlank() && livePartialText.isNotBlank() -> "$accumulatedText $livePartialText"
            accumulatedText.isNotBlank() -> accumulatedText
            else -> livePartialText
        }
    }
}
