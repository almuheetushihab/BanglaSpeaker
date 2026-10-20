package com.banglaenglish.speaker.ui.screen.practice

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.banglaenglish.speaker.R
import com.banglaenglish.speaker.ui.components.ScreenBackground
import com.banglaenglish.speaker.ui.components.TopBarHeader
import com.banglaenglish.speaker.ui.screen.practice.components.RepeatAfterMeSection
import com.banglaenglish.speaker.ui.screen.practice.components.RoleplaySection
import com.banglaenglish.speaker.ui.screen.practice.components.VocabNotebookSection
import com.banglaenglish.speaker.ui.theme.AppTheme
import com.banglaenglish.speaker.ui.theme.BanglaSpeakerTheme

@Composable
fun PracticeScreen(
    initialTargetSentence: String? = null,
    modifier: Modifier = Modifier,
    viewModel: PracticeViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (!initialTargetSentence.isNullOrBlank() && initialTargetSentence != state.targetSentence) {
        viewModel.setTargetSentence(initialTargetSentence)
    }

    PracticeScreenContent(
        state = state,
        onTabSelected = viewModel::setSelectedTab,
        onPlayTarget = viewModel::playTargetSentence,
        onToggleRecording = {
            if (state.isRecording) {
                viewModel.stopListeningUserAttempt()
            } else {
                viewModel.startListeningUserAttempt()
            }
        },
        onScenarioSelected = viewModel::setRoleplayScenario,
        onSendUserReply = viewModel::sendRoleplayUserReply,
        onFlipCard = viewModel::flipCard,
        onRateCard = viewModel::rateVocabCard,
        modifier = modifier
    )
}

@Composable
fun PracticeScreenContent(
    state: PracticeUiState,
    onTabSelected: (Int) -> Unit,
    onPlayTarget: () -> Unit,
    onToggleRecording: () -> Unit,
    onScenarioSelected: (com.banglaenglish.speaker.data.ConversationScenario) -> Unit,
    onSendUserReply: (String) -> Unit,
    onFlipCard: () -> Unit,
    onRateCard: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    ScreenBackground(modifier = modifier) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TopBarHeader(
                    title = stringResource(R.string.practice_title),
                    streakDays = state.streakDays
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                // Warm Gradient Streak Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(AppTheme.colors.warmGradient)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.streak_title, state.streakDays),
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Tabs
                TabRow(selectedTabIndex = state.selectedTab) {
                    Tab(
                        selected = state.selectedTab == 0,
                        onClick = { onTabSelected(0) },
                        text = { Text(stringResource(R.string.tab_repeat_after_me), fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = state.selectedTab == 1,
                        onClick = { onTabSelected(1) },
                        text = { Text(stringResource(R.string.tab_roleplay), fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = state.selectedTab == 2,
                        onClick = { onTabSelected(2) },
                        text = { Text(stringResource(R.string.tab_vocabulary), fontWeight = FontWeight.SemiBold) }
                    )
                }

                when (state.selectedTab) {
                    0 -> RepeatAfterMeSection(
                        state = state,
                        onPlayTarget = onPlayTarget,
                        onToggleRecording = onToggleRecording
                    )
                    1 -> RoleplaySection(
                        state = state,
                        onScenarioSelected = onScenarioSelected,
                        onSendUserReply = onSendUserReply
                    )
                    else -> VocabNotebookSection(
                        state = state,
                        onFlipCard = onFlipCard,
                        onRateCard = onRateCard
                    )
                }
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun PracticeScreenPreview() {
    BanglaSpeakerTheme {
        PracticeScreenContent(
            state = PracticeUiState(),
            onTabSelected = {},
            onPlayTarget = {},
            onToggleRecording = {},
            onScenarioSelected = {},
            onSendUserReply = {},
            onFlipCard = {},
            onRateCard = {}
        )
    }
}
