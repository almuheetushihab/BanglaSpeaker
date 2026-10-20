package com.banglaenglish.speaker.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.banglaenglish.speaker.data.ConversationItem
import com.banglaenglish.speaker.data.TranslationStyle
import com.banglaenglish.speaker.ui.screen.speaker.components.ConversationCard as SpeakerConversationCard

@Composable
fun ConversationCard(
    item: ConversationItem,
    onStyleSelected: (TranslationStyle) -> Unit,
    onPlay: () -> Unit,
    onBookmarkToggle: () -> Unit,
    onPractice: () -> Unit,
    modifier: Modifier = Modifier
) {
    SpeakerConversationCard(
        item = item,
        onStyleSelected = onStyleSelected,
        onPlay = onPlay,
        onBookmarkToggle = onBookmarkToggle,
        onPractice = onPractice,
        modifier = modifier
    )
}
