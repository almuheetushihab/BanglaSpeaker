package com.banglaenglish.speaker

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.banglaenglish.speaker.data.AppTheme
import com.banglaenglish.speaker.ui.components.BottomNavigationBar
import com.banglaenglish.speaker.ui.components.NavTab
import com.banglaenglish.speaker.ui.screen.history.HistoryScreen
import com.banglaenglish.speaker.ui.screen.practice.PracticeScreen
import com.banglaenglish.speaker.ui.screen.settings.SettingsScreen
import com.banglaenglish.speaker.ui.screen.settings.SettingsViewModel
import com.banglaenglish.speaker.ui.screen.speaker.SpeakerScreen
import com.banglaenglish.speaker.ui.theme.BanglaSpeakerTheme
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val settingsState by settingsViewModel.state.collectAsStateWithLifecycle()

            val darkTheme = when (settingsState.userPreferences.theme) {
                AppTheme.DARK -> true
                AppTheme.LIGHT -> false
                AppTheme.SYSTEM -> isSystemInDarkTheme()
            }

            BanglaSpeakerTheme(darkTheme = darkTheme) {
                MainAppScreen()
            }
        }
    }
}

@Composable
fun MainAppScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: NavTab.Speak.route

    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomNavigationBar(
                currentRoute = currentRoute.substringBefore("?"),
                onTabSelected = { tab ->
                    navController.navigate(tab.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavTab.Speak.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavTab.Speak.route) {
                SpeakerScreen(
                    onNavigateToPractice = { sentence ->
                        val encoded = URLEncoder.encode(sentence, StandardCharsets.UTF_8.toString())
                        navController.navigate("${NavTab.Practice.route}?target=$encoded")
                    }
                )
            }

            composable(NavTab.History.route) {
                HistoryScreen(
                    onNavigateToPractice = { sentence ->
                        val encoded = URLEncoder.encode(sentence, StandardCharsets.UTF_8.toString())
                        navController.navigate("${NavTab.Practice.route}?target=$encoded")
                    }
                )
            }

            composable(
                route = "${NavTab.Practice.route}?target={target}",
                arguments = listOf(
                    navArgument("target") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val encodedTarget = backStackEntry.arguments?.getString("target")
                val decodedTarget = encodedTarget?.let {
                    runCatching { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) }.getOrNull()
                }

                PracticeScreen(initialTargetSentence = decodedTarget)
            }

            composable(NavTab.Settings.route) {
                SettingsScreen()
            }
        }
    }
}
