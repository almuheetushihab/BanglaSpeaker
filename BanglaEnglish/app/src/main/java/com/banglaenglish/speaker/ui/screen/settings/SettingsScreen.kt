package com.banglaenglish.speaker.ui.screen.settings

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.banglaenglish.speaker.R
import com.banglaenglish.speaker.data.AppTheme
import com.banglaenglish.speaker.data.ConversationScenario
import com.banglaenglish.speaker.data.VoiceAccent
import com.banglaenglish.speaker.ui.theme.BanglaSpeakerTheme

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SettingsScreenContent(
        state = state,
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreenContent(
    state: SettingsUiState,
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
            title = { Text(stringResource(R.string.dialog_clear_title)) },
            text = { Text(stringResource(R.string.dialog_clear_message)) },
            confirmButton = {
                TextButton(onClick = onClearAllHistory) {
                    Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { onShowClearConfirmation(false) }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
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
            // Gemini API Key Status Line
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (state.isGeminiConfigured) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = if (state.isGeminiConfigured) Icons.Rounded.CheckCircle else Icons.Rounded.Warning,
                        contentDescription = null,
                        tint = if (state.isGeminiConfigured) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.setting_gemini_key_title),
                            style = MaterialTheme.typography.titleSmall
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

            // Scenario Selector
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.setting_scenario_title),
                        style = MaterialTheme.typography.titleSmall
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ConversationScenario.entries.forEach { scenario ->
                            FilterChip(
                                selected = prefs.scenario == scenario,
                                onClick = { onScenarioSelected(scenario) },
                                label = { Text(stringResource(scenario.labelRes)) }
                            )
                        }
                    }
                }
            }

            // Auto-translate Toggle
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.setting_auto_translate_title),
                            style = MaterialTheme.typography.titleSmall
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

            // Accent & Speed
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.setting_accent_title),
                            style = MaterialTheme.typography.titleSmall
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            VoiceAccent.entries.forEach { accent ->
                                FilterChip(
                                    selected = prefs.accent == accent,
                                    onClick = { onAccentSelected(accent) },
                                    label = { Text(stringResource(accent.labelRes)) }
                                )
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.setting_speech_speed_title),
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = String.format("%.1fx", prefs.speechSpeed),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
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

            // App Theme
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.setting_theme_title),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppTheme.entries.forEach { theme ->
                            FilterChip(
                                selected = prefs.theme == theme,
                                onClick = { onThemeSelected(theme) },
                                label = { Text(stringResource(theme.labelRes)) }
                            )
                        }
                    }
                }
            }

            // Clear History Button
            OutlinedButton(
                onClick = { onShowClearConfirmation(true) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = stringResource(R.string.action_clear_conversation),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
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
