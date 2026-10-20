package com.banglaenglish.speaker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.banglaenglish.speaker.R
import com.banglaenglish.speaker.data.AppLanguage
import com.banglaenglish.speaker.ui.theme.AppTheme
import com.banglaenglish.speaker.ui.theme.HeaderBottomCornersShape

/**
 * Top bar header with primary gradient background, 28dp bottom corners, app title,
 * quick language EN|বাং toggle, and streak flame chip.
 */
@Composable
fun TopBarHeader(
    title: String,
    subtitle: String? = null,
    currentLanguage: AppLanguage = AppLanguage.BANGLA,
    onLanguageToggle: (AppLanguage) -> Unit = {},
    streakDays: Int = 3,
    actions: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = HeaderBottomCornersShape,
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.colors.primaryGradient)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title and Subtitle
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                    }
                }

                // Controls: Streak flame + Language toggle + Extra actions
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Streak Pill
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "🔥 $streakDays",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Quick EN | বাং Toggle Pill
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.22f))
                            .padding(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isEn = currentLanguage == AppLanguage.ENGLISH
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isEn) Color.White else Color.Transparent)
                                .clickable { onLanguageToggle(AppLanguage.ENGLISH) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.lang_english_short),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isEn) MaterialTheme.colorScheme.primary else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (!isEn) Color.White else Color.Transparent)
                                .clickable { onLanguageToggle(AppLanguage.BANGLA) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.lang_bangla_short),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (!isEn) MaterialTheme.colorScheme.primary else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (actions != null) {
                        actions()
                    }
                }
            }
        }
    }
}
