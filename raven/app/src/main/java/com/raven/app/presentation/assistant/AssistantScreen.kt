package com.raven.app.presentation.assistant

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.raven.app.domain.model.ConversationMessage
import com.raven.app.domain.model.MessageRole
import com.raven.app.presentation.theme.ColorAssistant
import com.raven.app.util.DateTimeUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantScreen(
    viewModel: AssistantViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll to latest message
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(uiState.messages.size - 1)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier.size(32.dp).clip(CircleShape).background(ColorAssistant.copy(0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.AutoAwesome, null, tint = ColorAssistant, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text("Raven", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Personal Assistant", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearHistory() }) {
                        Icon(Icons.Filled.Delete, "Clear history", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
        },
        bottomBar = {
            InputBar(
                text = uiState.inputText,
                onTextChange = { viewModel.setInputText(it) },
                onSend = { viewModel.sendMessage() },
                onVoiceStart = { viewModel.startListening() },
                onVoiceStop = { viewModel.stopListening() },
                isListening = uiState.isListening,
                isProcessing = uiState.isProcessing,
                canSend = uiState.inputText.isNotBlank()
            )
        }
    ) { padding ->
        if (uiState.messages.isEmpty()) {
            WelcomeContent(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.messages, key = { it.id }) { message ->
                    MessageBubble(message)
                }
                if (uiState.isProcessing) {
                    item { ThinkingIndicator() }
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
private fun WelcomeContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(80.dp).clip(CircleShape).background(ColorAssistant.copy(0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.AutoAwesome, null, tint = ColorAssistant, modifier = Modifier.size(40.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text("Hi, I'm Raven", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "Your personal assistant. I can help you with reminders, notes, expenses, travel, and family commitments.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
        Text("Try saying:", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        val suggestions = listOf(
            "Remind me to call John tomorrow at 5pm",
            "Add expense \$45 for food",
            "What's my budget this month?",
            "Show upcoming reminders"
        )
        suggestions.forEach { suggestion ->
            SuggestionChip(
                onClick = {},
                label = { Text(suggestion, style = MaterialTheme.typography.bodySmall) },
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun MessageBubble(message: ConversationMessage) {
    val isUser = message.role == MessageRole.USER
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).background(ColorAssistant.copy(0.15f)).align(Alignment.Bottom),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.AutoAwesome, null, tint = ColorAssistant, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.width(8.dp))
        }

        Column(horizontalAlignment = if (isUser) Alignment.End else Alignment.Start) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp, topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                color = if (isUser) ColorAssistant else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Text(
                    text = message.content,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                DateTimeUtils.formatTime(message.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun ThinkingIndicator() {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(start = 40.dp)) {
        repeat(3) { index ->
            val infiniteTransition = rememberInfiniteTransition(label = "dot")
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.6f, targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(index * 100)
                ),
                label = "scale"
            )
            Box(
                modifier = Modifier.size(8.dp).scale(scale).clip(CircleShape).background(ColorAssistant.copy(0.5f))
            )
        }
    }
}

@Composable
private fun InputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onVoiceStart: () -> Unit,
    onVoiceStop: () -> Unit,
    isListening: Boolean,
    isProcessing: Boolean,
    canSend: Boolean
) {
    Surface(shadowElevation = 8.dp, tonalElevation = 2.dp) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text(if (isListening) "Listening..." else "Ask Raven anything...") },
                shape = RoundedCornerShape(24.dp),
                maxLines = 3
            )

            // Mic button
            AnimatedContent(targetState = isListening, label = "mic") { listening ->
                IconButton(
                    onClick = { if (listening) onVoiceStop() else onVoiceStart() },
                    modifier = Modifier.size(48.dp).clip(CircleShape)
                        .background(if (listening) ColorAssistant.copy(0.3f) else ColorAssistant.copy(0.1f))
                ) {
                    Icon(
                        if (listening) Icons.Filled.MicOff else Icons.Filled.Mic,
                        contentDescription = if (listening) "Stop" else "Speak",
                        tint = ColorAssistant
                    )
                }
            }

            // Send button
            AnimatedVisibility(visible = canSend && !isProcessing) {
                IconButton(
                    onClick = onSend,
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(ColorAssistant)
                ) {
                    Icon(Icons.Filled.Send, "Send", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}
