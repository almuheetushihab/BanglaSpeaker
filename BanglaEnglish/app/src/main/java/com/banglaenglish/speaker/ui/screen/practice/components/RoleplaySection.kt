package com.banglaenglish.speaker.ui.screen.practice.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.SmartToy
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.banglaenglish.speaker.R
import com.banglaenglish.speaker.data.ConversationScenario
import com.banglaenglish.speaker.ui.components.AppChip
import com.banglaenglish.speaker.ui.screen.practice.PracticeUiState
import com.banglaenglish.speaker.ui.screen.practice.RolePlayMessage
import com.banglaenglish.speaker.ui.theme.AppTheme
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
        ConversationScenario.TRAVEL,
        ConversationScenario.OFFICE_MEETING
    )

    Column(modifier = modifier.fillMaxSize()) {
        // Scenario Picker Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            scenarios.forEach { s ->
                AppChip(
                    selected = state.rolePlayScenario == s.rawValue,
                    onClick = { onScenarioSelected(s) },
                    label = stringResource(s.labelRes),
                    selectedBrush = AppTheme.colors.coolGradient
                )
            }
        }

        // Roleplay Chat Messages
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.rolePlayMessages) { msg ->
                val isAi = msg.sender == "AI"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isAi) Arrangement.Start else Arrangement.End,
                    verticalAlignment = Alignment.Top
                ) {
                    if (isAi) {
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AppTheme.colors.primaryGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SmartToy,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Surface(
                        shape = if (isAi) RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp) else RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp),
                        color = if (isAi) MaterialTheme.colorScheme.surface else Color.Unspecified,
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .then(
                                if (!isAi) {
                                    Modifier.clip(RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)).background(AppTheme.colors.primaryGradient)
                                } else {
                                    Modifier
                                }
                            )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = msg.englishText,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isAi) MaterialTheme.colorScheme.onSurface else Color.White
                            )
                            if (msg.banglaTranslation.isNotBlank()) {
                                Text(
                                    text = msg.banglaTranslation,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isAi) MaterialTheme.colorScheme.onSurfaceVariant else Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }

                    if (!isAi) {
                        Box(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AppTheme.colors.coolGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Reply Field
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            shadowElevation = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = userText,
                    onValueChange = { userText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(R.string.roleplay_input_placeholder)) },
                    singleLine = true,
                    shape = CircleShape,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (userText.isNotBlank()) {
                                onSendUserReply(userText)
                                userText = ""
                            }
                        }
                    )
                )

                IconButton(
                    onClick = {
                        if (userText.isNotBlank()) {
                            onSendUserReply(userText)
                            userText = ""
                        }
                    },
                    enabled = userText.isNotBlank() && !state.isRoleplayLoading,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (userText.isNotBlank()) AppTheme.colors.primaryGradient else Brush.linearGradient(listOf(Color.Gray, Color.Gray)))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Send,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
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
                    RolePlayMessage("AI", "Welcome! How can I help you today?", "স্বাগতম! আজ আপনাকে কীভাবে সাহায্য করতে পারি?"),
                    RolePlayMessage("User", "I want to check in for my room.", "আমি আমার রুমের জন্য চেক-ইন করতে চাই।")
                )
            ),
            onScenarioSelected = {},
            onSendUserReply = {}
        )
    }
}
