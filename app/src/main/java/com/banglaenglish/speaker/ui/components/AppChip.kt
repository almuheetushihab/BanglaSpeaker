package com.banglaenglish.speaker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.banglaenglish.speaker.ui.theme.AppTheme

/**
 * Reusable chip with vibrant gradient selected state and smooth color transition.
 */
@Composable
fun AppChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    selectedBrush: Brush = AppTheme.colors.primaryGradient,
    icon: (@Composable () -> Unit)? = null
) {
    val unselectedColor = MaterialTheme.colorScheme.surfaceVariant
    val unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    val backgroundColor by animateColorAsState(
        targetValue = if (selected) Color.Transparent else unselectedColor,
        label = "chipBgColor"
    )

    Box(
        modifier = modifier
            .clip(CircleShape)
            .then(
                if (selected) {
                    Modifier.background(selectedBrush, CircleShape)
                } else {
                    Modifier.background(backgroundColor, CircleShape)
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)
        ) {
            if (icon != null) {
                icon()
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    color = if (selected) Color.White else unselectedTextColor,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp
                )
            )
        }
    }
}
