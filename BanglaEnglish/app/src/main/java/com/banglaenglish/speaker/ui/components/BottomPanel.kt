package com.banglaenglish.speaker.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.banglaenglish.speaker.R
import com.banglaenglish.speaker.ui.screen.speaker.Phase
import com.banglaenglish.speaker.ui.screen.speaker.SpeakerUiState
import com.banglaenglish.speaker.ui.theme.BanglaSpeakerTheme

@Composable
fun BottomPanel(
    state: SpeakerUiState,
    typed: String,
    onTypedChange: (String) -> Unit,
    onSend: () -> Unit,
    onMic: () -> Unit,
    onToggleSlow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusText = when (state.phase) {
        Phase.Idle -> stringResource(R.string.status_idle)
        Phase.Listening -> stringResource(R.string.status_listening)
        Phase.Translating -> stringResource(R.string.status_translating)
        Phase.Speaking -> stringResource(R.string.status_speaking)
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        shadowElevation = 12.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 12.dp
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = state.slowSpeech,
                    onClick = onToggleSlow,
                    label = { Text(stringResource(R.string.chip_slow_speech)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Speed,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                )
            }

            MicButton(
                phase = state.phase,
                level = state.micLevel,
                onClick = onMic
            )

            OutlinedTextField(
                value = typed,
                onValueChange = onTypedChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = (state.phase != Phase.Translating) && (state.phase != Phase.Listening),
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
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

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun BottomPanelPreview() {
    BanglaSpeakerTheme {
        BottomPanel(
            state = SpeakerUiState(),
            typed = "",
            onTypedChange = {},
            onSend = {},
            onMic = {},
            onToggleSlow = {}
        )
    }
}
