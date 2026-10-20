package com.banglaenglish.speaker.ui.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.banglaenglish.speaker.R
import com.banglaenglish.speaker.ui.screen.speaker.ModelState
import com.banglaenglish.speaker.ui.theme.BanglaSpeakerTheme

@Composable
fun ModelBanner(
    modelState: ModelState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = modelState != ModelState.Ready,
        modifier = modifier
    ) {
        val containerColor = if (modelState == ModelState.Failed) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            MaterialTheme.colorScheme.secondaryContainer
        }

        val contentColor = if (modelState == ModelState.Failed) {
            MaterialTheme.colorScheme.onErrorContainer
        } else {
            MaterialTheme.colorScheme.onSecondaryContainer
        }

        Surface(
            color = containerColor,
            contentColor = contentColor,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (modelState == ModelState.Downloading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = contentColor
                    )
                    Text(
                        text = stringResource(R.string.model_downloading),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Text(
                        text = stringResource(R.string.model_download_failed),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onRetry) {
                        Text(
                            text = stringResource(R.string.action_retry),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Light Mode - Downloading", showBackground = true)
@Preview(name = "Dark Mode - Downloading", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun ModelBannerDownloadingPreview() {
    BanglaSpeakerTheme {
        ModelBanner(
            modelState = ModelState.Downloading,
            onRetry = {}
        )
    }
}

@Preview(name = "Light Mode - Failed", showBackground = true)
@Preview(name = "Dark Mode - Failed", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun ModelBannerFailedPreview() {
    BanglaSpeakerTheme {
        ModelBanner(
            modelState = ModelState.Failed,
            onRetry = {}
        )
    }
}
