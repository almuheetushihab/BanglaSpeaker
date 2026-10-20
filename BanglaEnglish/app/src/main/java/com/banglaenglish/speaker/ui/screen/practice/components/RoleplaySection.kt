package com.banglaenglish.speaker.ui.screen.practice.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.banglaenglish.speaker.R
import com.banglaenglish.speaker.data.ConversationScenario
import com.banglaenglish.speaker.ui.screen.practice.PracticeUiState
import com.banglaenglish.speaker.ui.screen.practice.RolePlayMessage
import com.banglaenglish.speaker.ui.theme.BanglaSpeakerTheme

@Composable
fun RoleplaySection(
    state: PracticeUiState,
    onScenarioSelected: (ConversationScenario) -> Unit,
    onSendUserReply: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var userText by rememberSaveable { mutableStateOf("") }
    val scenarios = listOf(
        ConversationScenario.JOB_INTERVIEW,
        ConversationScenario.SHOPPING,
        ConversationScenario.TRAVEL
    )

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            scenarios.forEach { s ->
                FilterChip(
                    selected = state.rolePlayScenario == s.rawValue,
                    onClick = { onScenarioSelected(s) },
                    label = { Text(stringResource(s.labelRes)) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(state.rolePlayMessages) { msg ->
                val isAi = msg.sender == "AI"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isAi) Arrangement.Start else Arrangement.End
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isAi) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = msg.englishText,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            if (msg.banglaTranslation.isNotBlank()) {
                                Text(
                                    text = msg.banglaTranslation,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = userText,
                onValueChange = { userText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(R.string.roleplay_input_placeholder)) },
                singleLine = true,
                shape = RoundedCornerShape(24.dp)
            )
            IconButton(
                onClick = {
                    if (userText.isNotBlank()) {
                        onSendUserReply(userText)
                        userText = ""
                    }
                },
                enabled = userText.isNotBlank() && !state.isRoleplayLoading
            ) {
                Icon(imageVector = Icons.AutoMirrored.Rounded.Send, contentDescription = null)
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun RoleplaySectionPreview() {
    BanglaSpeakerTheme {
        RoleplaySection(
            state = PracticeUiState(
                rolePlayMessages = listOf(
                    RolePlayMessage("AI", "Welcome! How can I help you?", "স্বাগতম! আপনাকে কীভাবে সাহায্য করতে পারি?"),
                    RolePlayMessage("User", "I want to check in.", "আমি চেক-ইন করতে চাই।")
                )
            ),
            onScenarioSelected = {},
            onSendUserReply = {}
        )
    }
}
