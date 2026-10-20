package com.banglaenglish.speaker.ui.components

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.banglaenglish.speaker.R
import com.banglaenglish.speaker.ui.theme.BanglaSpeakerTheme

enum class NavTab(val route: String, @param:StringRes val titleRes: Int, val icon: ImageVector) {
    Speak("speak", R.string.nav_speak, Icons.Rounded.Mic),
    History("history", R.string.nav_history, Icons.Rounded.History),
    Practice("practice", R.string.nav_practice, Icons.Rounded.RecordVoiceOver),
    Settings("settings", R.string.nav_settings, Icons.Rounded.Settings)
}

@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        NavTab.entries.forEach { tab ->
            val selected = currentRoute == tab.route
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = stringResource(tab.titleRes)
                    )
                },
                label = { Text(stringResource(tab.titleRes)) }
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun BottomNavigationBarPreview() {
    BanglaSpeakerTheme {
        BottomNavigationBar(
            currentRoute = NavTab.Speak.route,
            onTabSelected = {}
        )
    }
}
