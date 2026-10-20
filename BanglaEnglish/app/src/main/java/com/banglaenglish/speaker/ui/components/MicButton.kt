package com.banglaenglish.speaker.ui.components

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.banglaenglish.speaker.R
import com.banglaenglish.speaker.ui.screen.speaker.Phase
import com.banglaenglish.speaker.ui.theme.BanglaSpeakerTheme

@Composable
fun MicButton(
    phase: Phase,
    level: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listening = phase == Phase.Listening
    val translating = phase == Phase.Translating

    val transition = rememberInfiniteTransition(label = "pulseTransition")
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseValue"
    )
    val animatedLevel by animateFloatAsState(
        targetValue = level,
        label = "levelAnimation"
    )
    val containerColor by animateColorAsState(
        targetValue = if (listening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        label = "micColorAnimation"
    )

    Box(
        modifier = modifier
            .padding(vertical = 8.dp)
            .size(132.dp),
        contentAlignment = Alignment.Center
    ) {
        if (listening) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .graphicsLayer {
                        val s = 1f + pulse * 0.55f + animatedLevel * 0.35f
                        scaleX = s
                        scaleY = s
                        alpha = (1f - pulse) * 0.35f
                    }
                    .background(containerColor, CircleShape)
            )
        }
        FilledIconButton(
            onClick = onClick,
            enabled = !translating,
            modifier = Modifier
                .size(84.dp)
                .shadow(10.dp, CircleShape),
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = containerColor)
        ) {
            if (translating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 3.dp
                )
            } else {
                Icon(
                    imageVector = if (listening) Icons.Rounded.Stop else Icons.Rounded.Mic,
                    contentDescription = if (listening) {
                        stringResource(R.string.action_stop)
                    } else {
                        stringResource(R.string.action_speak)
                    },
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

@Preview(name = "Light Mode - Idle", showBackground = true)
@Preview(name = "Dark Mode - Idle", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun MicButtonIdlePreview() {
    BanglaSpeakerTheme {
        MicButton(
            phase = Phase.Idle,
            level = 0f,
            onClick = {}
        )
    }
}

@Preview(name = "Light Mode - Listening", showBackground = true)
@Preview(name = "Dark Mode - Listening", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun MicButtonListeningPreview() {
    BanglaSpeakerTheme {
        MicButton(
            phase = Phase.Listening,
            level = 0.5f,
            onClick = {}
        )
    }
}
