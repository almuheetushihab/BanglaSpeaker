package com.banglaenglish.speaker.ui.screen.speaker

import com.banglaenglish.speaker.data.ConversationItem
import com.banglaenglish.speaker.ui.UiText

enum class Phase {
    Idle,
    Listening,
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
    val partialText: String = "",
    val micLevel: Float = 0f,
    val slowSpeech: Boolean = false,
    val conversation: List<ConversationItem> = emptyList(),
    val message: UiText? = null
)
