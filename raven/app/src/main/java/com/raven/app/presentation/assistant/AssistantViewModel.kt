package com.raven.app.presentation.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raven.app.domain.model.ConversationMessage
import com.raven.app.domain.model.MessageRole
import com.raven.app.domain.repository.AssistantRepository
import com.raven.app.domain.repository.ReminderRepository
import com.raven.app.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AssistantUiState(
    val messages: List<ConversationMessage> = emptyList(),
    val isProcessing: Boolean = false,
    val isListening: Boolean = false,
    val inputText: String = "",
    val streamingText: String = "",   // live token accumulation during LLM generation
    val llmReady: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AssistantViewModel @Inject constructor(
    private val assistantRepository: AssistantRepository,
    private val reminderRepository: ReminderRepository,
    private val contextProvider: AppContextProvider,
    private val llmManager: LlmInferenceManager,
    val voiceManager: VoiceRecognitionManager,
    val ttsManager: RavenTtsManager
) : ViewModel() {

    private val _isProcessing = MutableStateFlow(false)
    private val _inputText = MutableStateFlow("")
    private val _streamingText = MutableStateFlow("")
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AssistantUiState> = combine(
        assistantRepository.getAllMessages(),
        _isProcessing,
        voiceManager.state.map { it is VoiceRecognitionState.Listening },
        _inputText,
        _streamingText,
        llmManager.state.map { it is LlmState.Ready },
        _error
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        AssistantUiState(
            messages = args[0] as List<ConversationMessage>,
            isProcessing = args[1] as Boolean,
            isListening = args[2] as Boolean,
            inputText = args[3] as String,
            streamingText = args[4] as String,
            llmReady = args[5] as Boolean,
            error = args[6] as String?
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AssistantUiState())

    init {
        // Auto-fill text field when voice recognition produces a result
        viewModelScope.launch {
            voiceManager.state.collect { state ->
                when (state) {
                    is VoiceRecognitionState.Result -> _inputText.value = state.text
                    is VoiceRecognitionState.Error -> _error.value = state.message
                    else -> {}
                }
            }
        }
    }

    fun setInputText(text: String) { _inputText.value = text }

    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isBlank() || _isProcessing.value) return
        _inputText.value = ""
        // Use streaming for LLM, synchronous for Phase 1 fallback
        if (llmManager.isReady) {
            processInputStreaming(text)
        } else {
            processInput(text)
        }
    }

    fun startListening() { voiceManager.startListening() }
    fun stopListening() { voiceManager.stopListening() }
    fun clearHistory() { viewModelScope.launch { assistantRepository.clearHistory() } }
    fun clearError() { _error.value = null }
    fun resetLlmSession() { llmManager.resetSession() }

    /**
     * Synchronous processing path — used for Phase 1 fallback.
     */
    private fun processInput(userText: String) {
        viewModelScope.launch {
            _isProcessing.value = true
            _streamingText.value = ""

            assistantRepository.addMessage(
                ConversationMessage(role = MessageRole.USER, content = userText)
            )

            val response = try {
                // Phase 1 fallback: rule-based command parsing
                val command = VoiceCommandParser.parse(userText)
                handleCommand(command, userText)
            } catch (e: Exception) {
                "I ran into an issue: ${e.message}"
            }

            assistantRepository.addMessage(
                ConversationMessage(role = MessageRole.ASSISTANT, content = response)
            )
            ttsManager.speak(response)
            _isProcessing.value = false
        }
    }

    /**
     * Streaming processing path — used when LLM is ready.
     * Tokens stream live into _streamingText; final message saved to Room on completion.
     */
    fun processInputStreaming(userText: String) {
        viewModelScope.launch {
            _isProcessing.value = true
            _streamingText.value = ""

            assistantRepository.addMessage(
                ConversationMessage(role = MessageRole.USER, content = userText)
            )

            if (!llmManager.isReady) {
                val fallback = handleCommand(VoiceCommandParser.parse(userText), userText)
                assistantRepository.addMessage(ConversationMessage(role = MessageRole.ASSISTANT, content = fallback))
                ttsManager.speak(fallback)
                _isProcessing.value = false
                return@launch
            }

            try {
                // Build context-enriched prompt
                val appContext = contextProvider.buildContext()
                val history = assistantRepository.getRecentMessages(10)
                val prompt = RavenPromptBuilder.buildFullPrompt(appContext, userText, history)
                val accumulated = StringBuilder()

                // Stream tokens token-by-token into the UI
                llmManager.generateStreamingWithCallback(prompt) { token, isDone ->
                    if (token.isNotEmpty()) {
                        accumulated.append(token)
                        viewModelScope.launch { _streamingText.value = accumulated.toString() }
                    }
                    if (isDone) {
                        viewModelScope.launch {
                            val finalResponse = accumulated.toString().ifBlank {
                                "I couldn't generate a response. Please try again."
                            }
                            assistantRepository.addMessage(
                                ConversationMessage(role = MessageRole.ASSISTANT, content = finalResponse)
                            )
                            ttsManager.speak(finalResponse)
                            _streamingText.value = ""
                            _isProcessing.value = false
                        }
                    }
                }
            } catch (e: Exception) {
                // Fallback on any LLM error
                val fallback = "I ran into an issue. Let me use basic mode.\n" +
                        handleCommand(VoiceCommandParser.parse(userText), userText)
                assistantRepository.addMessage(ConversationMessage(role = MessageRole.ASSISTANT, content = fallback))
                ttsManager.speak(fallback)
                _streamingText.value = ""
                _isProcessing.value = false
            }
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
                if (command.amount > 0)
                    "I'll log $${"%.2f".format(command.amount)} under ${command.category}. Tap 'Add Expense' to confirm."
                else
                    "I caught an expense but couldn't detect the amount. Tap 'Add Expense' to enter it manually."
            }
            is VoiceCommand.AddNote -> {
                "Creating a note titled \"${command.title}\". Tap 'Add Note' to review and save it."
            }
            is VoiceCommand.QueryBudget -> {
                val context = contextProvider.buildContext()
                val budgetLines = context.lines()
                    .filter { it.contains("Budget") || it.contains("%") || (it.contains("$") && it.contains("/")) }
                if (budgetLines.isNotEmpty())
                    "Here's your budget status:\n" + budgetLines.take(5).joinToString("\n")
                else
                    "You haven't set up any budgets yet. Go to Expenses → Budgets to create one."
            }
            is VoiceCommand.QueryReminders -> {
                val reminders = reminderRepository.getActiveReminders().first().take(3)
                if (reminders.isEmpty())
                    "You have no upcoming reminders. You're all clear!"
                else
                    "You have ${reminders.size} upcoming reminder${if (reminders.size > 1) "s" else ""}:\n" +
                            reminders.joinToString("\n") { "• ${it.title}" }
            }
            is VoiceCommand.QueryTrips -> {
                "Check the Travel tab to see your upcoming trips and itineraries."
            }
            is VoiceCommand.Unknown -> {
                if (!llmManager.isReady)
                    "I heard: \"$rawText\". To enable full AI conversation, download the Raven AI model from the assistant screen."
                else
                    "I heard: \"$rawText\". Let me think about that..."
            }
        }
    }
}
