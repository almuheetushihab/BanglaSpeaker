package com.banglaenglish.speaker.ui.screen.practice.components

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.banglaenglish.speaker.R
import com.banglaenglish.speaker.data.local.VocabularyEntity
import com.banglaenglish.speaker.ui.components.GradientButton
import com.banglaenglish.speaker.ui.screen.practice.PracticeUiState
import com.banglaenglish.speaker.ui.theme.AppTheme
import com.banglaenglish.speaker.ui.theme.BanglaSpeakerTheme

@Composable
fun VocabNotebookSection(
    state: PracticeUiState,
    onFlipCard: () -> Unit,
    onRateCard: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.dueVocabList.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.empty_vocab),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val currentCard = state.dueVocabList[state.currentVocabIndex.coerceIn(0, state.dueVocabList.size - 1)]

    val rotation by animateFloatAsState(
        targetValue = if (state.isCardFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "cardFlipRotation"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Due Count Badge
        Text(
            text = stringResource(R.string.vocab_due_count, state.dueVocabList.size),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 3D Flip Flashcard
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 12 * density
                }
                .clickable { onFlipCard() },
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .graphicsLayer {
                        if (rotation > 90f) {
                            rotationY = 180f
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (rotation > 90f) currentCard.english else currentCard.bangla,
                        style = MaterialTheme.typography.displayMedium,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.vocab_flip),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // Rating Buttons: Easy (Green), Medium (Amber), Hard (Rose)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            GradientButton(
                onClick = { onRateCard(1) },
                brush = AppTheme.colors.successGradient,
                elevation = 4.dp
            ) {
                Text(stringResource(R.string.vocab_easy))
            }

            GradientButton(
                onClick = { onRateCard(2) },
                brush = AppTheme.colors.warmGradient,
                elevation = 4.dp
            ) {
                Text(stringResource(R.string.vocab_medium))
            }

            GradientButton(
                onClick = { onRateCard(3) },
                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                    listOf(AppTheme.colors.intentCommand, Color(0xFFF43F5E))
                ),
                elevation = 4.dp
            ) {
                Text(stringResource(R.string.vocab_hard))
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun VocabNotebookSectionPreview() {
    BanglaSpeakerTheme {
        VocabNotebookSection(
            state = PracticeUiState(
                dueVocabList = listOf(
                    VocabularyEntity(id = 1, english = "Attending office", bangla = "অফিসে যাওয়া")
                ),
                isCardFlipped = false
            ),
            onFlipCard = {},
            onRateCard = {}
        )
    }
}
