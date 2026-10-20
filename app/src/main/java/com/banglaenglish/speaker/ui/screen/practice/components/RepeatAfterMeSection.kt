package com.banglaenglish.speaker.ui.screen.practice.components

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.banglaenglish.speaker.R
import com.banglaenglish.speaker.ui.screen.practice.PracticeUiState
import com.banglaenglish.speaker.ui.screen.practice.WordResult
import com.banglaenglish.speaker.ui.theme.AppTheme
import com.banglaenglish.speaker.ui.theme.BanglaSpeakerTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RepeatAfterMeSection(
    state: PracticeUiState,
    onPlayTarget: () -> Unit,
    onToggleRecording: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.repeat_instruction),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Target Sentence Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = state.targetSentence,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onPlayTarget,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                            contentDescription = stringResource(R.string.repeat_listen),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Hero Record Button
        Box(
            modifier = Modifier
                .padding(vertical = 12.dp)
                .size(96.dp)
                .shadow(10.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    if (state.isRecording) AppTheme.colors.warmGradient else AppTheme.colors.primaryGradient
                )
                .clickable(onClick = onToggleRecording),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (state.isRecording) Icons.Rounded.Stop else Icons.Rounded.Mic,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(44.dp)
            )
        }

        if (state.recognizedSpeech.isNotBlank()) {
            Text(
                text = stringResource(R.string.practice_recorded_label, state.recognizedSpeech),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Animated Score Ring & Word Match Chips
        if (state.score != null) {
            val animatedScoreProgress by animateFloatAsState(
                targetValue = (state.score.coerceIn(0, 100) / 100f),
                animationSpec = tween(durationMillis = 800),
                label = "scoreProgress"
            )

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Score Circular Progress
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(110.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { animatedScoreProgress },
                            modifier = Modifier.fillMaxSize(),
                            color = if (state.score >= 80) AppTheme.colors.intentAnswer else AppTheme.colors.intentCommand,
                            strokeWidth = 10.dp,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Text(
                            text = "${state.score}%",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Word Chips (Green = Correct, Rose = Missed)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.wordComparison.forEach { wordRes ->
                            val chipBg = if (wordRes.isMatched) AppTheme.colors.intentAnswer else AppTheme.colors.intentCommand
                            Surface(
                                shape = CircleShape,
                                color = chipBg.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = wordRes.word,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = chipBg,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
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
private fun RepeatAfterMeSectionPreview() {
    BanglaSpeakerTheme {
        RepeatAfterMeSection(
            state = PracticeUiState(
                targetSentence = "I would like to order a coffee, please.",
                score = 85,
                wordComparison = listOf(
                    WordResult("i", true),
                    WordResult("would", true),
                    WordResult("like", true),
                    WordResult("to", true),
                    WordResult("order", true),
                    WordResult("a", true),
                    WordResult("coffee", false)
                )
            ),
            onPlayTarget = {},
            onToggleRecording = {}
        )
    }
}
