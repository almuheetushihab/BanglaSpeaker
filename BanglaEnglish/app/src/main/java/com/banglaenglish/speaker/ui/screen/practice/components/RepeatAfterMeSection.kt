package com.banglaenglish.speaker.ui.screen.practice.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.banglaenglish.speaker.R
import com.banglaenglish.speaker.ui.screen.practice.PracticeUiState
import com.banglaenglish.speaker.ui.screen.practice.WordResult
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
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = state.targetSentence,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                IconButton(onClick = onPlayTarget) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                        contentDescription = stringResource(R.string.repeat_listen),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Mic Record Button
        Button(
            onClick = onToggleRecording,
            shape = CircleShape,
            modifier = Modifier.size(72.dp)
        ) {
            Icon(
                imageVector = if (state.isRecording) Icons.Rounded.Stop else Icons.Rounded.Mic,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
        }

        if (state.recognizedSpeech.isNotBlank()) {
            Text(
                text = stringResource(R.string.practice_recorded_label, state.recognizedSpeech),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Comparison Result & Score
        if (state.score != null) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (state.score >= 80) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.repeat_score, state.score),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        state.wordComparison.forEach { wordRes ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (wordRes.isMatched) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.error
                                }
                            ) {
                                Text(
                                    text = wordRes.word,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (wordRes.isMatched) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onError
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
