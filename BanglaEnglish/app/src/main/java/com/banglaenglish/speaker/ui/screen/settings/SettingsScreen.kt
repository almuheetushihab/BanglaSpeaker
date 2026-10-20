package com.banglaenglish.speaker.ui.screen.settings

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.banglaenglish.speaker.R
import com.banglaenglish.speaker.data.AppLanguage
import com.banglaenglish.speaker.data.AppTheme
import com.banglaenglish.speaker.data.ConversationScenario
import com.banglaenglish.speaker.data.VoiceAccent
import com.banglaenglish.speaker.ui.components.AppChip
import com.banglaenglish.speaker.ui.components.ScreenBackground
import com.banglaenglish.speaker.ui.components.TopBarHeader
import com.banglaenglish.speaker.ui.theme.AppTheme as ExtendedAppTheme
import com.banglaenglish.speaker.ui.theme.BanglaSpeakerTheme

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SettingsScreenContent(
        state = state,
        onLanguageSelected = viewModel::setLanguage,
        onScenarioSelected = viewModel::setScenario,
        onAutoTranslateToggle = viewModel::setAutoTranslate,
        onAccentSelected = viewModel::setAccent,
        onSpeedChanged = viewModel::setSpeechSpeed,
        onThemeSelected = viewModel::setTheme,
        onShowClearConfirmation = viewModel::showClearConfirmation,
        onClearAllHistory = viewModel::clearAllHistory,
        modifier = modifier
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreenContent(
    state: SettingsUiState,
    onLanguageSelected: (AppLanguage) -> Unit,
    onScenarioSelected: (ConversationScenario) -> Unit,
    onAutoTranslateToggle: (Boolean) -> Unit,
    onAccentSelected: (VoiceAccent) -> Unit,
    onSpeedChanged: (Float) -> Unit,
    onThemeSelected: (AppTheme) -> Unit,
    onShowClearConfirmation: (Boolean) -> Unit,
    onClearAllHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val prefs = state.userPreferences

    if (state.showClearConfirmation) {
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
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                TopBarHeader(
                    title = stringResource(R.string.settings_title),
                    currentLanguage = prefs.language,
                    onLanguageToggle = onLanguageSelected
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Gemini API Key Status Badge
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (state.isGeminiConfigured) {
                            ExtendedAppTheme.colors.intentAnswer.copy(alpha = 0.15f)
                        } else {
                            ExtendedAppTheme.colors.intentQuestion.copy(alpha = 0.15f)
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (state.isGeminiConfigured) ExtendedAppTheme.colors.intentAnswer else ExtendedAppTheme.colors.intentQuestion
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (state.isGeminiConfigured) Icons.Rounded.CheckCircle else Icons.Rounded.Warning,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                        Column {
                            Text(
                                text = stringResource(R.string.setting_gemini_key_title),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (state.isGeminiConfigured) {
                                    stringResource(R.string.gemini_key_configured)
                                } else {
                                    stringResource(R.string.gemini_key_missing)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // 1. Language Section
                SettingsSectionCard(
                    icon = Icons.Rounded.Language,
                    title = stringResource(R.string.setting_language_title),
                    iconColor = ExtendedAppTheme.colors.intentStatement
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        AppLanguage.entries.forEach { lang ->
                            AppChip(
                                selected = prefs.language == lang,
                                onClick = { onLanguageSelected(lang) },
                                label = stringResource(lang.labelRes),
                                selectedBrush = ExtendedAppTheme.colors.primaryGradient
                            )
                        }
                    }
                }

                // 2. Appearance Section
                SettingsSectionCard(
                    icon = Icons.Rounded.ColorLens,
                    title = stringResource(R.string.setting_theme_title),
                    iconColor = ExtendedAppTheme.colors.intentRequest
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        AppTheme.entries.forEach { theme ->
                            AppChip(
                                selected = prefs.theme == theme,
                                onClick = { onThemeSelected(theme) },
                                label = stringResource(theme.labelRes),
                                selectedBrush = ExtendedAppTheme.colors.primaryGradient
                            )
                        }
                    }
                }

                // 3. Conversation Section
                SettingsSectionCard(
                    icon = Icons.Rounded.Translate,
                    title = stringResource(R.string.setting_scenario_title),
                    iconColor = ExtendedAppTheme.colors.intentGreeting
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            ConversationScenario.entries.forEach { scenario ->
                                AppChip(
                                    selected = prefs.scenario == scenario,
                                    onClick = { onScenarioSelected(scenario) },
                                    label = stringResource(scenario.labelRes),
                                    selectedBrush = ExtendedAppTheme.colors.primaryGradient
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.setting_auto_translate_title),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = stringResource(R.string.setting_auto_translate_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = prefs.autoTranslate,
                                onCheckedChange = onAutoTranslateToggle
                            )
                        }
                    }
                }

                // 4. Voice Section
                SettingsSectionCard(
                    icon = Icons.Rounded.RecordVoiceOver,
                    title = stringResource(R.string.setting_accent_title),
                    iconColor = ExtendedAppTheme.colors.intentAnswer
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            VoiceAccent.entries.forEach { accent ->
                                AppChip(
                                    selected = prefs.accent == accent,
                                    onClick = { onAccentSelected(accent) },
                                    label = stringResource(accent.labelRes),
                                    selectedBrush = ExtendedAppTheme.colors.coolGradient
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(R.string.setting_speech_speed_title),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = String.format("%.1fx", prefs.speechSpeed),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Slider(
                                value = prefs.speechSpeed,
                                onValueChange = onSpeedChanged,
                                valueRange = 0.5f..1.2f,
                                steps = 6
                            )
                        }
                    }
                }

                // 5. About Section
                SettingsSectionCard(
                    icon = Icons.Rounded.Info,
                    title = stringResource(R.string.setting_about_title),
                    iconColor = ExtendedAppTheme.colors.intentOther
                ) {
                    Text(
                        text = stringResource(R.string.setting_about_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Clear History Button
                OutlinedButton(
                    onClick = { onShowClearConfirmation(true) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = stringResource(R.string.action_clear_conversation),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    icon: ImageVector,
    title: String,
    iconColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            content()
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    BanglaSpeakerTheme {
        SettingsScreenContent(
            state = SettingsUiState(),
            onLanguageSelected = {},
            onScenarioSelected = {},
            onAutoTranslateToggle = {},
            onAccentSelected = {},
            onSpeedChanged = {},
            onThemeSelected = {},
            onShowClearConfirmation = {},
            onClearAllHistory = {}
        )
    }
}
