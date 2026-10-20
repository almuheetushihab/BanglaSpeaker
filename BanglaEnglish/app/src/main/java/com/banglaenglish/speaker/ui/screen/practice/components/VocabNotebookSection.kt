package com.banglaenglish.speaker.ui.screen.practice.components

import android.content.res.Configuration
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.banglaenglish.speaker.R
import com.banglaenglish.speaker.data.local.VocabularyEntity
import com.banglaenglish.speaker.ui.screen.practice.PracticeUiState
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

    val currentCard = state.dueVocabList[state.currentVocabIndex]

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clickable { onFlipCard() },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (state.isCardFlipped) currentCard.english else currentCard.bangla,
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.vocab_flip),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onRateCard(1) }) {
                Text(stringResource(R.string.vocab_easy))
            }
            OutlinedButton(onClick = { onRateCard(2) }) {
                Text(stringResource(R.string.vocab_medium))
            }
            OutlinedButton(onClick = { onRateCard(3) }) {
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
