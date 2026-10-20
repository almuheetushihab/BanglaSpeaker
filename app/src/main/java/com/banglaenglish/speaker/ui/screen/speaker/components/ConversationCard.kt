package com.banglaenglish.speaker.ui.screen.speaker.components

import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.banglaenglish.speaker.ui.components.AppChip
import com.banglaenglish.speaker.ui.components.BanglaBubble
import com.banglaenglish.speaker.ui.theme.AppTheme
import com.banglaenglish.speaker.ui.theme.BanglaSpeakerTheme
import com.banglaenglish.speaker.ui.theme.EnglishTranslationTextStyle

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

    val intentColor = when (item.intent) {
        TranslationIntent.QUESTION -> AppTheme.colors.intentQuestion
        TranslationIntent.ANSWER -> AppTheme.colors.intentAnswer
        TranslationIntent.STATEMENT -> AppTheme.colors.intentStatement
        TranslationIntent.REQUEST -> AppTheme.colors.intentRequest
        TranslationIntent.COMMAND -> AppTheme.colors.intentCommand
        TranslationIntent.GREETING -> AppTheme.colors.intentGreeting
        TranslationIntent.OTHER -> AppTheme.colors.intentOther
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // User Bangla speech bubble
        BanglaBubble(text = item.correctedBangla.ifBlank { item.bangla })

        // English Translation Card with Gradient Left Bar
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                // Gradient Accent Bar on the Left
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .fillMaxHeight()
                        .background(AppTheme.colors.primaryGradient)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Top row: Colored Intent Badge + Engine Label
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = intentColor.copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = stringResource(item.intent.labelRes),
                                style = MaterialTheme.typography.labelSmall,
                                color = intentColor,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = stringResource(item.engineUsed.labelRes),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Active English Translation Hero Text (22sp SemiBold)
                    Text(
                        text = activeText,
                        style = EnglishTranslationTextStyle,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Style segmented chips (Natural, Formal, Casual)
                    if (item.englishFormal.isNotBlank() && item.englishFormal != item.english) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TranslationStyle.entries.forEach { style ->
                                AppChip(
                                    selected = item.selectedStyle == style,
                                    onClick = { onStyleSelected(style) },
                                    label = stringResource(style.labelRes)
                                )
                            }
                        }
                    }

                    // Tip Banner in Bangla
                    if (!item.tipBn.isNullOrBlank()) {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Lightbulb,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = stringResource(R.string.label_tip),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.tertiary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = item.tipBn,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }
                    }

                    // Key Phrases Chips
                    if (item.keyPhrases.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = stringResource(R.string.label_key_phrases),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                item.keyPhrases.forEach { phrase ->
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "${phrase.english} = ${phrase.bangla}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Action Icons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
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
                                    tint = if (item.isBookmarked) AppTheme.colors.intentQuestion else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Practice Button Chip
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(AppTheme.colors.coolGradient)
                                .clickable(onClick = onPractice)
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.RecordVoiceOver,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = stringResource(R.string.action_practice_sentence),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
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
