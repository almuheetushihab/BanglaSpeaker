package com.banglaenglish.speaker.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.banglaenglish.speaker.data.ConversationItem
import com.banglaenglish.speaker.ui.screen.speaker.SpeakerUiState
import com.banglaenglish.speaker.ui.theme.BanglaSpeakerTheme

@Composable
fun ConversationList(
    state: SpeakerUiState,
    onReplay: (ConversationItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val hasPartial = state.partialText.isNotBlank()

    LaunchedEffect(state.conversation.size, hasPartial) {
        val count = listState.layoutInfo.totalItemsCount
        if (count > 0) {
            listState.animateScrollToItem(count - 1)
        }
    }

    if (state.conversation.isEmpty() && !hasPartial) {
        EmptyState(modifier = modifier)
        return
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(
            items = state.conversation,
            key = { it.id }
        ) { item ->
            ConversationCard(
                item = item,
                onReplay = { onReplay(item) }
            )
        }
        if (hasPartial) {
            item(key = "partial") {
                BanglaBubble(
                    text = state.partialText,
                    modifier = Modifier.alpha(0.6f)
                )
            }
        }
    }
}

@Preview(name = "Light Mode - Empty", showBackground = true)
@Preview(name = "Dark Mode - Empty", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun ConversationListEmptyPreview() {
    BanglaSpeakerTheme {
        ConversationList(
            state = SpeakerUiState(),
            onReplay = {}
        )
    }
}

@Preview(name = "Light Mode - Populated", showBackground = true)
@Preview(name = "Dark Mode - Populated", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun ConversationListPopulatedPreview() {
    BanglaSpeakerTheme {
        ConversationList(
            state = SpeakerUiState(
                conversation = listOf(
                    ConversationItem(
                        bangla = "আমি আজ অফিসে যাব না।",
                        english = "I will not go to the office today."
                    )
                )
            ),
            onReplay = {}
        )
    }
}
