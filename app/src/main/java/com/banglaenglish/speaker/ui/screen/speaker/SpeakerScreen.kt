package com.banglaenglish.speaker.ui.screen.speaker

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.banglaenglish.speaker.R
import com.banglaenglish.speaker.data.AppLanguage
import com.banglaenglish.speaker.data.ConversationItem
import com.banglaenglish.speaker.data.ConversationScenario
import com.banglaenglish.speaker.data.TranslationStyle
import com.banglaenglish.speaker.ui.UiText
import com.banglaenglish.speaker.ui.components.AppChip
import com.banglaenglish.speaker.ui.components.AudioVisualizerBars
import com.banglaenglish.speaker.ui.components.EmptyState
import com.banglaenglish.speaker.ui.components.MicButton
import com.banglaenglish.speaker.ui.components.ModelBanner
import com.banglaenglish.speaker.ui.components.RecordingTimer
import com.banglaenglish.speaker.ui.components.ScreenBackground
import com.banglaenglish.speaker.ui.components.ShimmerLoadingCard
import com.banglaenglish.speaker.ui.components.TopBarHeader
import com.banglaenglish.speaker.ui.screen.speaker.components.ConversationCard
import com.banglaenglish.speaker.ui.screen.speaker.components.EditTranscriptBox
import com.banglaenglish.speaker.ui.theme.AppTheme
import com.banglaenglish.speaker.ui.theme.BanglaSpeakerTheme

@Composable
fun SpeakerScreen(
    onNavigateToPractice: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SpeakerViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val prefs by viewModel.userPreferences.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboard = LocalSoftwareKeyboardController.current
    var typed by rememberSaveable { mutableStateOf("") }

    // Keep Screen On during dictation
    DisposableEffect(state.phase) {
        val window = (context as? Activity)?.window
        if (state.phase == Phase.Listening) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.onMicTapped()
        } else {
            viewModel.showMessage(UiText.StringResource(R.string.permission_mic_required))
        }
    }

    val currentMessage = state.message
    LaunchedEffect(currentMessage) {
        currentMessage?.let {
            snackbarHostState.showSnackbar(it.asString(context))
            viewModel.consumeMessage()
        }
    }

    val onMic: () -> Unit = {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        if (granted) {
            viewModel.onMicTapped()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val sendTyped: () -> Unit = {
        if (typed.isNotBlank()) {
            viewModel.translateTyped(typed)
            typed = ""
            keyboard?.hide()
        }
    }

    SpeakerScreenContent(
        state = state,
        typed = typed,
        selectedScenario = prefs.scenario,
        currentLanguage = prefs.language,
        snackbarHostState = snackbarHostState,
        onScenarioSelected = viewModel::setScenario,
        onLanguageToggle = viewModel::setLanguage,
        onTypedChange = { typed = it },
        onSend = sendTyped,
        onMic = onMic,
        onFinishDictation = viewModel::finishDictation,
        onStyleSelected = viewModel::selectStyle,
        onPlay = viewModel::speakItem,
        onBookmarkToggle = viewModel::toggleBookmark,
        onPractice = onNavigateToPractice,
        onRetryModel = viewModel::downloadModel,
        onClearConversation = viewModel::clearConversation,
        onUpdateEditableTranscript = viewModel::updateEditableTranscript,
        onConfirmEdit = viewModel::confirmEditAndTranslate,
        onReRecord = viewModel::cancelEditAndReRecord,
        onExampleClick = { example -> viewModel.translateTyped(example) },
        modifier = modifier
    )
}

@Composable
fun SpeakerScreenContent(
    state: SpeakerUiState,
    typed: String,
    selectedScenario: ConversationScenario,
    currentLanguage: AppLanguage,
    snackbarHostState: SnackbarHostState,
    onScenarioSelected: (ConversationScenario) -> Unit,
    onLanguageToggle: (AppLanguage) -> Unit,
    onTypedChange: (String) -> Unit,
    onSend: () -> Unit,
    onMic: () -> Unit,
    onFinishDictation: () -> Unit,
    onStyleSelected: (ConversationItem, TranslationStyle) -> Unit,
    onPlay: (ConversationItem) -> Unit,
    onBookmarkToggle: (ConversationItem) -> Unit,
    onPractice: (String) -> Unit,
    onRetryModel: () -> Unit,
    onClearConversation: () -> Unit,
    onUpdateEditableTranscript: (String) -> Unit,
    onConfirmEdit: () -> Unit,
    onReRecord: () -> Unit,
    onExampleClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(state.conversation.size, state.phase) {
        if (state.conversation.isNotEmpty()) {
            listState.animateScrollToItem(state.conversation.size - 1)
        }
    }

    ScreenBackground(modifier = modifier) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopBarHeader(
                    title = stringResource(R.string.top_app_bar_title),
                    subtitle = stringResource(R.string.top_app_bar_subtitle),
                    currentLanguage = currentLanguage,
                    onLanguageToggle = onLanguageToggle,
                    actions = {
                        IconButton(
                            onClick = onClearConversation,
                            enabled = state.conversation.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = stringResource(R.string.action_clear_conversation_desc),
                                tint = Color.White
                            )
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                // Model Download / Error Banner
                ModelBanner(
                    modelState = state.modelState,
                    onRetry = onRetryModel
                )

                // Top Horizontal Scenario Chips Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ConversationScenario.entries.forEach { scenario ->
                        AppChip(
                            selected = scenario == selectedScenario,
                            onClick = { onScenarioSelected(scenario) },
                            label = stringResource(scenario.labelRes),
                            selectedBrush = AppTheme.colors.primaryGradient
                        )
                    }
                }

                // Phase Status Chip
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val statusText = when (state.phase) {
                        Phase.Idle -> stringResource(R.string.status_ready)
                        Phase.Listening -> stringResource(R.string.status_listening)
                        Phase.Translating -> stringResource(R.string.status_translating)
                        Phase.Speaking -> stringResource(R.string.status_speaking)
                        Phase.EditingTranscript -> stringResource(R.string.status_editing)
                    }

                    val statusColor = when (state.phase) {
                        Phase.Idle -> AppTheme.colors.intentStatement
                        Phase.Listening -> AppTheme.colors.intentCommand
                        Phase.Translating -> AppTheme.colors.intentQuestion
                        Phase.Speaking -> AppTheme.colors.intentAnswer
                        Phase.EditingTranscript -> AppTheme.colors.intentRequest
                    }

                    Surface(
                        shape = CircleShape,
                        color = statusColor.copy(alpha = 0.18f)
                    ) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }

                // Conversation List / Empty State
                Box(modifier = Modifier.weight(1f)) {
                    if (state.conversation.isEmpty() && state.phase == Phase.Idle) {
                        EmptyState(onExampleClick = onExampleClick)
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(
                                items = state.conversation,
                                key = { it.id }
                            ) { item ->
                                ConversationCard(
                                    item = item,
                                    onStyleSelected = { style -> onStyleSelected(item, style) },
                                    onPlay = { onPlay(item) },
                                    onBookmarkToggle = { onBookmarkToggle(item) },
                                    onPractice = { onPractice(item.getActiveEnglishText()) }
                                )
                            }

                            if (state.phase == Phase.Translating) {
                                item(key = "shimmer") {
                                    ShimmerLoadingCard()
                                }
                            }
                        }
                    }
                }

                // Edit Transcript Box (shown when autoTranslate is OFF)
                if (state.phase == Phase.EditingTranscript) {
                    EditTranscriptBox(
                        transcriptText = state.editableTranscript,
                        onTranscriptChange = onUpdateEditableTranscript,
                        onTranslate = onConfirmEdit,
                        onReRecord = onReRecord,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                // Bottom Panel & Dictation Session Controls
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    shadowElevation = 16.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Real-time dictation text & visualizer
                        if (state.phase == Phase.Listening) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RecordingTimer(seconds = state.recordingSeconds)
                                AudioVisualizerBars(
                                    level = state.micLevel,
                                    isDictating = true
                                )
                            }

                            val liveDisplay = state.getDisplayTranscript()
                            Text(
                                text = liveDisplay.ifBlank { stringResource(R.string.status_listening) },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            Button(
                                onClick = onFinishDictation,
                                modifier = Modifier.fillMaxWidth(),
                                shape = CircleShape
                            ) {
                                Icon(imageVector = Icons.Rounded.Done, contentDescription = null)
                                Text(
                                    text = stringResource(R.string.action_done),
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        } else if (state.phase != Phase.EditingTranscript) {
                            // Mic Button & Text Field when Idle
                            MicButton(
                                phase = state.phase,
                                level = state.micLevel,
                                onClick = onMic
                            )

                            OutlinedTextField(
                                value = typed,
                                onValueChange = onTypedChange,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = state.phase != Phase.Translating,
                                singleLine = true,
                                shape = CircleShape,
                                placeholder = { Text(stringResource(R.string.text_input_placeholder)) },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(onSend = { onSend() }),
                                trailingIcon = {
                                    IconButton(
                                        onClick = onSend,
                                        enabled = typed.isNotBlank()
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.Send,
                                            contentDescription = stringResource(R.string.action_send)
                                        )
                                    }
                                }
                            )
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
private fun SpeakerScreenPreview() {
    BanglaSpeakerTheme {
        SpeakerScreenContent(
            state = SpeakerUiState(),
            typed = "",
            selectedScenario = ConversationScenario.CASUAL_CHAT,
            currentLanguage = AppLanguage.BANGLA,
            snackbarHostState = remember { SnackbarHostState() },
            onScenarioSelected = {},
            onLanguageToggle = {},
            onTypedChange = {},
            onSend = {},
            onMic = {},
            onFinishDictation = {},
            onStyleSelected = { _, _ -> },
            onPlay = {},
            onBookmarkToggle = {},
            onPractice = {},
            onRetryModel = {},
            onClearConversation = {},
            onUpdateEditableTranscript = {},
            onConfirmEdit = {},
            onReRecord = {},
            onExampleClick = {}
        )
    }
}
