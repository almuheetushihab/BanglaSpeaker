package com.banglaenglish.speaker.ui.screen.history

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.banglaenglish.speaker.R
import com.banglaenglish.speaker.data.ConversationItem
import com.banglaenglish.speaker.data.TranslationStyle
import com.banglaenglish.speaker.ui.components.ScreenBackground
import com.banglaenglish.speaker.ui.components.TopBarHeader
import com.banglaenglish.speaker.ui.screen.speaker.components.ConversationCard
import com.banglaenglish.speaker.ui.theme.BanglaSpeakerTheme

@Composable
fun HistoryScreen(
    onNavigateToPractice: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    HistoryScreenContent(
        state = state,
        onSearchQueryChange = viewModel::setSearchQuery,
        onTabSelected = viewModel::setSelectedTab,
        onShowClearConfirmation = viewModel::showClearConfirmation,
        onClearAllHistory = viewModel::clearAllHistory,
        onStyleSelected = { _, _ -> },
        onPlayItem = viewModel::playItem,
        onBookmarkToggle = viewModel::toggleBookmark,
        onNavigateToPractice = onNavigateToPractice,
        modifier = modifier
    )
}

@Composable
fun HistoryScreenContent(
    state: HistoryUiState,
    onSearchQueryChange: (String) -> Unit,
    onTabSelected: (Int) -> Unit,
    onShowClearConfirmation: (Boolean) -> Unit,
    onClearAllHistory: () -> Unit,
    onStyleSelected: (ConversationItem, TranslationStyle) -> Unit,
    onPlayItem: (ConversationItem) -> Unit,
    onBookmarkToggle: (ConversationItem) -> Unit,
    onNavigateToPractice: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.showClearDialog) {
        AlertDialog(
            onDismissRequest = { onShowClearConfirmation(false) },
            title = { Text(stringResource(R.string.dialog_clear_title), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.dialog_clear_message)) },
            confirmButton = {
                TextButton(onClick = onClearAllHistory) {
                    Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { onShowClearConfirmation(false) }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    ScreenBackground(modifier = modifier) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TopBarHeader(
                    title = stringResource(R.string.history_title),
                    actions = {
                        if (state.items.isNotEmpty()) {
                            IconButton(onClick = { onShowClearConfirmation(true) }) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteSweep,
                                    contentDescription = stringResource(R.string.action_clear_conversation_desc),
                                    tint = Color.White
                                )
                            }
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                // Search Bar
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    placeholder = { Text(stringResource(R.string.search_placeholder)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        if (state.searchQuery.isNotBlank()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Rounded.Clear,
                                    contentDescription = null
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = CircleShape
                )

                // Tabs: All History vs Saved Phrases
                TabRow(selectedTabIndex = state.selectedTab) {
                    Tab(
                        selected = state.selectedTab == 0,
                        onClick = { onTabSelected(0) },
                        text = { Text(stringResource(R.string.tab_all_history), fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = state.selectedTab == 1,
                        onClick = { onTabSelected(1) },
                        text = { Text(stringResource(R.string.tab_saved_phrases), fontWeight = FontWeight.SemiBold) }
                    )
                }

                if (state.items.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (state.selectedTab == 1) {
                                stringResource(R.string.empty_saved)
                            } else {
                                stringResource(R.string.empty_history)
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(
                            items = state.items,
                            key = { it.id }
                        ) { item ->
                            ConversationCard(
                                item = item,
                                onStyleSelected = { style -> onStyleSelected(item, style) },
                                onPlay = { onPlayItem(item) },
                                onBookmarkToggle = { onBookmarkToggle(item) },
                                onPractice = { onNavigateToPractice(item.getActiveEnglishText()) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun HistoryScreenPreview() {
    BanglaSpeakerTheme {
        HistoryScreenContent(
            state = HistoryUiState(
                items = listOf(
                    ConversationItem(
                        bangla = "আমি আজ অফিসে যাব না",
                        english = "I will not go to the office today."
                    )
                )
            ),
            onSearchQueryChange = {},
            onTabSelected = {},
            onShowClearConfirmation = {},
            onClearAllHistory = {},
            onStyleSelected = { _, _ -> },
            onPlayItem = {},
            onBookmarkToggle = {},
            onNavigateToPractice = {}
        )
    }
}
