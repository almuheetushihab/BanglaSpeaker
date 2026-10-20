@file:OptIn(ExperimentalMaterial3Api::class)

package com.banglaenglish.speaker.ui.screen.speaker

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.banglaenglish.speaker.R
import com.banglaenglish.speaker.data.ConversationItem
import com.banglaenglish.speaker.ui.UiText
import com.banglaenglish.speaker.ui.components.BottomPanel
import com.banglaenglish.speaker.ui.components.ConversationList
import com.banglaenglish.speaker.ui.components.ModelBanner
import com.banglaenglish.speaker.ui.theme.BanglaSpeakerTheme

@Composable
fun SpeakerScreen(
    modifier: Modifier = Modifier,
    viewModel: SpeakerViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboard = LocalSoftwareKeyboardController.current
    var typed by rememberSaveable { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
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
        if (granted) {
            viewModel.onMicTapped()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val send: () -> Unit = {
        if (typed.isNotBlank()) {
            viewModel.translateTyped(typed)
            typed = ""
            keyboard?.hide()
        }
    }

    SpeakerScreenContent(
        state = state,
        typed = typed,
        snackbarHostState = snackbarHostState,
        onTypedChange = { typed = it },
        onSend = send,
        onMic = onMic,
        onToggleSlow = viewModel::toggleSlowSpeech,
        onReplay = viewModel::replay,
        onRetryModel = viewModel::downloadModel,
        onClearConversation = viewModel::clearConversation,
        modifier = modifier
    )
}

@Composable
fun SpeakerScreenContent(
    state: SpeakerUiState,
    typed: String,
    snackbarHostState: SnackbarHostState,
    onTypedChange: (String) -> Unit,
    onSend: () -> Unit,
    onMic: () -> Unit,
    onToggleSlow: () -> Unit,
    onReplay: (ConversationItem) -> Unit,
    onRetryModel: () -> Unit,
    onClearConversation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.top_app_bar_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = onClearConversation,
                            enabled = state.conversation.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = stringResource(R.string.action_clear_conversation_desc)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                ModelBanner(
                    modelState = state.modelState,
                    onRetry = onRetryModel
                )

                Box(modifier = Modifier.weight(1f)) {
                    ConversationList(
                        state = state,
                        onReplay = onReplay
                    )
                }

                BottomPanel(
                    state = state,
                    typed = typed,
                    onTypedChange = onTypedChange,
                    onSend = onSend,
                    onMic = onMic,
                    onToggleSlow = onToggleSlow
                )
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
            snackbarHostState = remember { SnackbarHostState() },
            onTypedChange = {},
            onSend = {},
            onMic = {},
            onToggleSlow = {},
            onReplay = {},
            onRetryModel = {},
            onClearConversation = {}
        )
    }
}
