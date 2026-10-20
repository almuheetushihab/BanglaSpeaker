package com.banglaenglish.speaker.ui.components

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.banglaenglish.speaker.ui.theme.BanglaSpeakerTheme

@Composable
fun AudioVisualizerBars(
    modifier: Modifier = Modifier,
    level: Float = 0f,
    isDictating: Boolean = false
) {
    val numBars = 7
    val barWidth = 4.dp
    val maxBarHeight = 28.dp
    val minBarHeight = 6.dp

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(numBars) { index ->
            val factor = if (isDictating) {
                val seed = (index + 1) * 0.15f
                (level * (0.5f + seed)).coerceIn(0.1f, 1.0f)
            } else {
                0.1f
            }

            val targetHeight = minBarHeight + (maxBarHeight - minBarHeight) * factor
            val animatedHeight by animateFloatAsState(
                targetValue = targetHeight.value,
                animationSpec = tween(durationMillis = 100),
                label = "barHeight_$index"
            )

            Box(
                modifier = Modifier
                    .width(barWidth)
                    .height(animatedHeight.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (isDictating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun AudioVisualizerBarsPreview() {
    BanglaSpeakerTheme {
        AudioVisualizerBars(level = 0.6f, isDictating = true)
    }
}
