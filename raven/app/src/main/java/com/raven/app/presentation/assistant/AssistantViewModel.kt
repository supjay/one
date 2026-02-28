package com.raven.app.presentation.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raven.app.domain.model.ConversationMessage
import com.raven.app.domain.model.MessageRole
import com.raven.app.domain.repository.AssistantRepository
import com.raven.app.domain.repository.ReminderRepository
import com.raven.app.util.AppContextProvider
import com.raven.app.util.RavenTtsManager
import com.raven.app.util.VoiceCommand
import com.raven.app.util.VoiceCommandParser
import com.raven.app.util.VoiceRecognitionManager
import com.raven.app.util.VoiceRecognitionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AssistantUiState(
    val messages: List<ConversationMessage> = emptyList(),
    val isProcessing: Boolean = false,
    val isListening: Boolean = false,
    val inputText: String = "",
    val error: String? = null
)

@HiltViewModel
class AssistantViewModel @Inject constructor(
    private val assistantRepository: AssistantRepository,
    private val reminderRepository: ReminderRepository,
    private val contextProvider: AppContextProvider,
    val voiceManager: VoiceRecognitionManager,
    val ttsManager: RavenTtsManager
) : ViewModel() {

    private val _isProcessing = MutableStateFlow(false)
    private val _inputText = MutableStateFlow("")
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AssistantUiState> = combine(
        assistantRepository.getAllMessages(),
        _isProcessing,
        voiceManager.state.map { it is VoiceRecognitionState.Listening },
        _inputText,
        _error
    ) { messages, processing, listening, input, error ->
        AssistantUiState(messages, processing, listening, input, error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AssistantUiState())

    init {
        // Populate text field when voice recognition produces a result
        viewModelScope.launch {
            voiceManager.state.collect { state ->
                when (state) {
                    is VoiceRecognitionState.Result -> {
                        _inputText.value = state.text
                    }
                    is VoiceRecognitionState.Error -> {
                        _error.value = state.message
                    }
                    else -> {}
                }
            }
        }
    }

    fun setInputText(text: String) {
        _inputText.value = text
    }

    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isBlank() || _isProcessing.value) return
        _inputText.value = ""
        processInput(text)
    }

    fun startListening() {
        voiceManager.startListening()
    }

    fun stopListening() {
        voiceManager.stopListening()
    }

    fun clearHistory() {
        viewModelScope.launch { assistantRepository.clearHistory() }
    }

    fun clearError() { _error.value = null }

    private fun processInput(userText: String) {
        viewModelScope.launch {
            _isProcessing.value = true

            // Save user message
            assistantRepository.addMessage(
                ConversationMessage(role = MessageRole.USER, content = userText)
            )

            val response = try {
                // Phase 1: Rule-based command parsing
                val command = VoiceCommandParser.parse(userText)
                handleCommand(command, userText)

                // ============================================================
                // TODO (Phase 2): Replace rule-based parsing with LLM call:
                //
                // val context = contextProvider.buildContext()
                // val prompt = "$context\n\nUser: $userText\nRaven:"
                // val llmResponse = geminiNanoEngine.generate(prompt)  // MediaPipe LLM Inference API
                // llmResponse
                // ============================================================
            } catch (e: Exception) {
                "I'm sorry, I ran into an issue: ${e.message}"
            }

            // Save assistant response
            assistantRepository.addMessage(
                ConversationMessage(role = MessageRole.ASSISTANT, content = response)
            )

            // Speak the response
            ttsManager.speak(response)

            _isProcessing.value = false
        }
    }

    private suspend fun handleCommand(command: VoiceCommand, rawText: String): String {
        return when (command) {
            is VoiceCommand.AddReminder -> {
                "Got it! I'll help you set a reminder for \"${command.title}\". " +
                        if (command.dateTimeMillis != null) "I detected a time — tap 'Add Reminder' to confirm."
                        else "Please tap 'Add Reminder' to pick a date and time."
            }
            is VoiceCommand.AddExpense -> {
                if (command.amount > 0) {
                    "I'll log $${"%.2f".format(command.amount)} under ${command.category}. Tap 'Add Expense' to confirm."
                } else {
                    "I caught an expense but couldn't detect the amount. Please tap 'Add Expense' to enter it manually."
                }
            }
            is VoiceCommand.AddNote -> {
                "Creating a note titled \"${command.title}\". Tap 'Add Note' to review and save it."
            }
            is VoiceCommand.QueryBudget -> {
                val context = contextProvider.buildContext()
                val budgetLine = context.lines().filter { it.contains("Budget") || it.contains("%") }
                if (budgetLine.isNotEmpty()) {
                    "Here's your budget status:\n" + budgetLine.joinToString("\n")
                } else {
                    "You haven't set up any budgets yet. Go to Expenses → Budgets to create one."
                }
            }
            is VoiceCommand.QueryReminders -> {
                val reminders = reminderRepository.getActiveReminders().first().take(3)
                if (reminders.isEmpty()) {
                    "You have no upcoming reminders. You're all clear!"
                } else {
                    "You have ${reminders.size} upcoming reminder${if (reminders.size > 1) "s" else ""}:\n" +
                            reminders.joinToString("\n") { "• ${it.title}" }
                }
            }
            is VoiceCommand.QueryTrips -> {
                "Check the Travel tab to see your upcoming trips and itineraries."
            }
            is VoiceCommand.Unknown -> {
                "I heard you say: \"$rawText\". I'm still learning to understand complex requests. " +
                        "Try commands like \"Remind me to...\", \"Add expense $50 for food\", or \"What's my budget?\""
            }
        }
    }
}
