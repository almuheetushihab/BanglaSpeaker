package com.banglaenglish.speaker.ui.screen.speaker.components

import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.banglaenglish.speaker.R
import com.banglaenglish.speaker.data.ConversationItem
import com.banglaenglish.speaker.data.KeyPhrase
import com.banglaenglish.speaker.data.TranslationEngineType
import com.banglaenglish.speaker.data.TranslationIntent
import com.banglaenglish.speaker.data.TranslationStyle
import com.banglaenglish.speaker.ui.components.BanglaBubble
import com.banglaenglish.speaker.ui.theme.BanglaSpeakerTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConversationCard(
    item: ConversationItem,
    onStyleSelected: (TranslationStyle) -> Unit,
    onPlay: () -> Unit,
    onBookmarkToggle: () -> Unit,
    onPractice: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val activeText = item.getActiveEnglishText()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // User Bangla speech bubble
        BanglaBubble(text = item.correctedBangla.ifBlank { item.bangla })

        // English Translation Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Top row: Intent Badge + Engine Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Text(
                            text = stringResource(item.intent.labelRes),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = stringResource(item.engineUsed.labelRes),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Active English Text
                Text(
                    text = activeText,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )

                // Style Chips (Natural / Formal / Casual)
                if (item.englishFormal.isNotBlank() && item.englishFormal != item.english) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TranslationStyle.entries.forEach { style ->
                            FilterChip(
                                selected = item.selectedStyle == style,
                                onClick = { onStyleSelected(style) },
                                label = { Text(stringResource(style.labelRes)) }
                            )
                        }
                    }
                }

                // Tip in Bangla (if present)
                if (!item.tipBn.isNullOrBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Lightbulb,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = item.tipBn,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                // Key Phrases Chips (if present)
                if (item.keyPhrases.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stringResource(R.string.label_key_phrases),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            item.keyPhrases.forEach { phrase ->
                                AssistChip(
                                    onClick = { },
                                    label = { Text("${phrase.english} = ${phrase.bangla}") },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }

                // Action Bar: Play, Copy, Share, Bookmark, Practice
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = onPlay) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                                contentDescription = stringResource(R.string.action_play),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { clipboard.setText(AnnotatedString(activeText)) }) {
                            Icon(
                                imageVector = Icons.Rounded.ContentCopy,
                                contentDescription = stringResource(R.string.action_copy),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                putExtra(Intent.EXTRA_TEXT, activeText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, null))
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.Share,
                                contentDescription = stringResource(R.string.action_share),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onBookmarkToggle) {
                            Icon(
                                imageVector = if (item.isBookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                                contentDescription = stringResource(R.string.action_bookmark),
                                tint = if (item.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    AssistChip(
                        onClick = onPractice,
                        label = { Text(stringResource(R.string.action_practice_sentence)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.RecordVoiceOver,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun ConversationCardPreview() {
    BanglaSpeakerTheme {
        ConversationCard(
            item = ConversationItem(
                bangla = "আমি আজ অফিসে যাব না",
                correctedBangla = "আমি আজ অফিসে যাব না।",
                intent = TranslationIntent.STATEMENT,
                english = "I will not go to the office today.",
                englishFormal = "I shall not be attending the office today.",
                englishCasual = "I won't hit the office today.",
                tipBn = "অফিসে যাওয়ার কথা বলতে go to the office ব্যবহার করা হয়।",
                keyPhrases = listOf(KeyPhrase("go to office", "অফিসে যাওয়া")),
                engineUsed = TranslationEngineType.GEMINI
            ),
            onStyleSelected = {},
            onPlay = {},
            onBookmarkToggle = {},
            onPractice = {}
        )
    }
}
