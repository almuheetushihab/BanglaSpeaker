package com.banglaenglish.speaker.ui.components

import android.content.res.Configuration
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.banglaenglish.speaker.R
import com.banglaenglish.speaker.ui.screen.speaker.Phase
import com.banglaenglish.speaker.ui.theme.AppTheme
import com.banglaenglish.speaker.ui.theme.BanglaSpeakerTheme

/**
 * Circular mic button with Primary gradient background, glow and animated pulse rings while listening.
 */
@Composable
fun MicButton(
    phase: Phase,
    level: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    buttonSize: Dp = 60.dp
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

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        label = "micPressScale"
    )

    val buttonBrush = if (listening) AppTheme.colors.warmGradient else AppTheme.colors.primaryGradient

    Box(
        modifier = modifier.size(buttonSize + 12.dp),
        contentAlignment = Alignment.Center
    ) {
        // Glowing animated pulse ring
        if (listening) {
            Box(
                modifier = Modifier
                    .size(buttonSize)
                    .graphicsLayer {
                        val s = 1f + pulse * 0.6f + animatedLevel * 0.3f
                        scaleX = s
                        scaleY = s
                        alpha = (1f - pulse) * 0.45f
                    }
                    .background(AppTheme.colors.intentCommand, CircleShape)
            )
        }

        Box(
            modifier = Modifier
                .scale(pressScale)
                .size(buttonSize)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(buttonBrush)
                .clickable(
                    enabled = !translating,
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            if (translating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(buttonSize * 0.45f),
                    color = Color.White,
                    strokeWidth = 2.5.dp
                )
            } else {
                Icon(
                    imageVector = if (listening) Icons.Rounded.Done else Icons.Rounded.Mic,
                    contentDescription = if (listening) {
                        stringResource(R.string.action_done)
                    } else {
                        stringResource(R.string.action_speak)
                    },
                    tint = Color.White,
                    modifier = Modifier.size(buttonSize * 0.5f)
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
