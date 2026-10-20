@file:OptIn(ExperimentalMaterial3Api::class)

package com.banglaenglish.speaker.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.banglaenglish.speaker.data.ConversationItem

@Composable
fun SpeakerScreen(viewModel: SpeakerViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboard = LocalSoftwareKeyboardController.current
    var typed by rememberSaveable { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.onMicTapped()
        else viewModel.showMessage("মাইক্রোফোনের অনুমতি ছাড়া বাংলা শোনা সম্ভব নয়।")
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    val onMic: () -> Unit = {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) viewModel.onMicTapped()
        else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    val send: () -> Unit = {
        if (typed.isNotBlank()) {
            viewModel.translateTyped(typed)
            typed = ""
            keyboard?.hide()
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                        MaterialTheme.colorScheme.background,
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
                        Text("বাংলা → English", fontWeight = FontWeight.Bold)
                    },
                    actions = {
                        IconButton(
                            onClick = viewModel::clearConversation,
                            enabled = state.conversation.isNotEmpty(),
                        ) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Clear")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    ),
                )
            },
        ) { padding ->
            Column(
                Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                ModelBanner(state.modelState, onRetry = viewModel::downloadModel)

                Box(Modifier.weight(1f)) {
                    ConversationList(
                        state = state,
                        onReplay = viewModel::replay,
                    )
                }

                BottomPanel(
                    state = state,
                    typed = typed,
                    onTypedChange = { typed = it },
                    onSend = send,
                    onMic = onMic,
                    onToggleSlow = viewModel::toggleSlowSpeech,
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Conversation
// ---------------------------------------------------------------------------

@Composable
private fun ConversationList(
    state: SpeakerUiState,
    onReplay: (ConversationItem) -> Unit,
) {
    val listState = rememberLazyListState()
    val hasPartial = state.partialText.isNotBlank()

    LaunchedEffect(state.conversation.size, hasPartial) {
        val count = listState.layoutInfo.totalItemsCount
        if (count > 0) listState.animateScrollToItem(count - 1)
    }

    if (state.conversation.isEmpty() && !hasPartial) {
        EmptyState()
        return
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items(state.conversation, key = { it.id }) { item ->
            ConversationCard(item = item, onReplay = { onReplay(item) })
        }
        if (hasPartial) {
            item(key = "partial") {
                BanglaBubble(text = state.partialText, modifier = Modifier.alpha(0.6f))
            }
        }
    }
}

@Composable
private fun ConversationCard(item: ConversationItem, onReplay: () -> Unit) {
    val clipboard = LocalClipboardManager.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BanglaBubble(text = item.bangla)

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp),
                shadowElevation = 3.dp,
                modifier = Modifier.widthIn(max = 320.dp),
            ) {
                Column(Modifier.padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 4.dp)) {
                    Text(
                        text = item.english,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 28.sp,
                    )
                    Row {
                        IconButton(onClick = onReplay, modifier = Modifier.size(40.dp)) {
                            Icon(
                                Icons.AutoMirrored.Rounded.VolumeUp,
                                contentDescription = "Play",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                        IconButton(
                            onClick = { clipboard.setText(AnnotatedString(item.english)) },
                            modifier = Modifier.size(40.dp),
                        ) {
                            Icon(
                                Icons.Rounded.ContentCopy,
                                contentDescription = "Copy",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BanglaBubble(text: String, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shape = RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp),
            modifier = Modifier.widthIn(max = 300.dp),
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(88.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Rounded.Translate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp),
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(
            "বাংলায় বলুন, ইংরেজিতে শুনুন",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "নিচের মাইক্রোফোনে ট্যাপ করে বাংলায় কথা বলুন। অ্যাপ ইংরেজিতে অনুবাদ করে শুনিয়ে দেবে। তারপর নিজে মুখে বলে প্র্যাকটিস করুন।",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 2.dp,
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "উদাহরণ",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Spacer(Modifier.height(4.dp))
                Text("আমি আজ অফিসে যাব না।", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "I will not go to the office today.",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Model download banner
// ---------------------------------------------------------------------------

@Composable
private fun ModelBanner(modelState: ModelState, onRetry: () -> Unit) {
    AnimatedVisibility(visible = modelState != ModelState.Ready) {
        Surface(
            color = if (modelState == ModelState.Failed)
                MaterialTheme.colorScheme.errorContainerSafe()
            else MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
        ) {
            Row(
                Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (modelState == ModelState.Downloading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Text(
                        "প্রথমবার অনুবাদের মডেল (~৩০ MB) ডাউনলোড হচ্ছে। ইন্টারনেট চালু রাখুন।",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    Text(
                        "মডেল ডাউনলোড হয়নি। ইন্টারনেট চেক করুন।",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = onRetry) { Text("আবার চেষ্টা") }
                }
            }
        }
    }
}

@Composable
private fun androidx.compose.material3.ColorScheme.errorContainerSafe(): Color =
    this.error.copy(alpha = 0.15f)

// ---------------------------------------------------------------------------
// Bottom panel: status, mic, text input
// ---------------------------------------------------------------------------

@Composable
private fun BottomPanel(
    state: SpeakerUiState,
    typed: String,
    onTypedChange: (String) -> Unit,
    onSend: () -> Unit,
    onMic: () -> Unit,
    onToggleSlow: () -> Unit,
) {
    val statusText = when (state.phase) {
        Phase.Idle -> "মাইক্রোফোনে ট্যাপ করে বাংলায় বলুন"
        Phase.Listening -> "শুনছি… কথা বলুন"
        Phase.Translating -> "ইংরেজিতে অনুবাদ করছি…"
        Phase.Speaking -> "ইংরেজিতে শোনাচ্ছি…"
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        shadowElevation = 12.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                FilterChip(
                    selected = state.slowSpeech,
                    onClick = onToggleSlow,
                    label = { Text("ধীরে বলো") },
                    leadingIcon = {
                        Icon(
                            Icons.Rounded.Speed,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize),
                        )
                    },
                )
            }

            MicButton(
                phase = state.phase,
                level = state.micLevel,
                onClick = onMic,
            )

            OutlinedTextField(
                value = typed,
                onValueChange = onTypedChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.phase != Phase.Translating && state.phase != Phase.Listening,
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                placeholder = { Text("অথবা বাংলায় টাইপ করুন…") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                trailingIcon = {
                    IconButton(onClick = onSend, enabled = typed.isNotBlank()) {
                        Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "Send")
                    }
                },
            )
        }
    }
}

@Composable
private fun MicButton(phase: Phase, level: Float, onClick: () -> Unit) {
    val listening = phase == Phase.Listening
    val translating = phase == Phase.Translating

    val transition = rememberInfiniteTransition(label = "pulse")
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart),
        label = "pulseValue",
    )
    val animatedLevel by animateFloatAsState(targetValue = level, label = "level")
    val container by animateColorAsState(
        targetValue = if (listening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        label = "micColor",
    )

    Box(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .size(132.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (listening) {
            Box(
                Modifier
                    .size(84.dp)
                    .graphicsLayer {
                        val s = 1f + pulse * 0.55f + animatedLevel * 0.35f
                        scaleX = s
                        scaleY = s
                        alpha = (1f - pulse) * 0.35f
                    }
                    .background(container, CircleShape)
            )
        }
        FilledIconButton(
            onClick = onClick,
            enabled = !translating,
            modifier = Modifier
                .size(84.dp)
                .shadow(10.dp, CircleShape),
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = container),
        ) {
            if (translating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 3.dp,
                )
            } else {
                Icon(
                    imageVector = if (listening) Icons.Rounded.Stop else Icons.Rounded.Mic,
                    contentDescription = if (listening) "Stop" else "Speak",
                    modifier = Modifier.size(40.dp),
                )
            }
        }
    }
}
