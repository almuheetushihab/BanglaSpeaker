package com.banglaenglish.speaker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.banglaenglish.speaker.ui.screen.speaker.SpeakerScreen
import com.banglaenglish.speaker.ui.theme.BanglaSpeakerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BanglaSpeakerTheme {
                SpeakerScreen()
            }
        }
    }
}
