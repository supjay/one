package com.raven.app.util

import java.util.Calendar

/**
 * Phase 1: Rule-based NLP for structured voice commands.
 * Phase 2: Replace processCommand() body with LLM call (Gemini Nano via MediaPipe).
 */
sealed class VoiceCommand {
    data class AddReminder(val title: String, val dateTimeMillis: Long?) : VoiceCommand()
    data class AddExpense(val amount: Double, val category: String, val description: String) : VoiceCommand()
    data class AddNote(val title: String, val content: String) : VoiceCommand()
    data class QueryBudget(val category: String?) : VoiceCommand()
    data class QueryReminders(val upcoming: Boolean = true) : VoiceCommand()
    data class QueryTrips(val upcoming: Boolean = true) : VoiceCommand()
    data class Unknown(val rawText: String) : VoiceCommand()
}

object VoiceCommandParser {

    fun parse(input: String): VoiceCommand {
        val text = input.lowercase().trim()

        return when {
            // Reminder patterns
            text.contains("remind") || text.startsWith("set a reminder") || text.startsWith("add reminder") -> {
                parseReminderCommand(input)
            }

            // Expense patterns
            text.contains("spent") || text.contains("add expense") ||
                    text.contains("paid") || text.matches(Regex(".*\\$?\\d+(\\.\\d+)?.*")) -> {
                parseExpenseCommand(input)
            }

            // Note patterns
            text.startsWith("note") || text.startsWith("add note") ||
                    text.startsWith("write") || text.startsWith("save note") -> {
                parseNoteCommand(input)
            }

            // Budget query
            text.contains("budget") || text.contains("how much") && text.contains("left") -> {
                val category = extractCategory(text)
                VoiceCommand.QueryBudget(category)
            }

            // Reminder query
            text.contains("what") && text.contains("reminder") ||
                    text.contains("upcoming reminder") -> {
                VoiceCommand.QueryReminders()
            }

            // Trip query
            text.contains("trip") || text.contains("travel") || text.contains("journey") -> {
                VoiceCommand.QueryTrips()
            }

            else -> VoiceCommand.Unknown(input)
        }
    }

    private fun parseReminderCommand(input: String): VoiceCommand {
        // Extract: "remind me to <title> [at/on <time/date>]"
        val lower = input.lowercase()
        val titlePatterns = listOf("remind me to ", "set a reminder to ", "add reminder to ", "add a reminder to ")
        var title = input
        for (pattern in titlePatterns) {
            if (lower.contains(pattern)) {
                title = input.substring(lower.indexOf(pattern) + pattern.length)
                break
            }
        }

        // Strip trailing time qualifiers for title
        val timeQualifiers = listOf(" at ", " on ", " tomorrow", " today", " tonight")
        for (qualifier in timeQualifiers) {
            val idx = title.lowercase().indexOf(qualifier)
            if (idx > 0) {
                title = title.substring(0, idx).trim()
                break
            }
        }

        val dateTime = extractDateTime(lower)
        return VoiceCommand.AddReminder(title.trim().replaceFirstChar { it.uppercase() }, dateTime)
    }

    private fun parseExpenseCommand(input: String): VoiceCommand {
        val lower = input.lowercase()
        val amountRegex = Regex("\\$?([0-9]+(?:\\.[0-9]{1,2})?)")
        val amount = amountRegex.find(lower)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0

        val category = when {
            lower.contains("food") || lower.contains("lunch") ||
                    lower.contains("dinner") || lower.contains("breakfast") -> "Food"
            lower.contains("transport") || lower.contains("taxi") ||
                    lower.contains("grab") || lower.contains("uber") -> "Transport"
            lower.contains("groceri") || lower.contains("supermarket") -> "Groceries"
            lower.contains("entertainment") || lower.contains("movie") -> "Entertainment"
            lower.contains("health") || lower.contains("doctor") ||
                    lower.contains("medicine") || lower.contains("pharmacy") -> "Health"
            lower.contains("shopping") || lower.contains("clothes") -> "Shopping"
            lower.contains("bill") || lower.contains("utility") -> "Bills"
            else -> "Other"
        }

        return VoiceCommand.AddExpense(amount, category, input)
    }

    private fun parseNoteCommand(input: String): VoiceCommand {
        val lower = input.lowercase()
        val prefixes = listOf("note:", "note ", "add note: ", "add note ", "write ", "save note ")
        var content = input
        for (prefix in prefixes) {
            if (lower.startsWith(prefix)) {
                content = input.substring(prefix.length).trim()
                break
            }
        }
        val title = content.take(50).trimEnd { it != ' ' }.trim().ifBlank { "Voice Note" }
        return VoiceCommand.AddNote(title, content)
    }

    private fun extractDateTime(lower: String): Long? {
        val cal = Calendar.getInstance()
        return when {
            lower.contains("tomorrow") -> {
                cal.add(Calendar.DAY_OF_YEAR, 1)
                extractTime(lower, cal)
                cal.timeInMillis
            }
            lower.contains("today") || lower.contains("tonight") -> {
                extractTime(lower, cal)
                cal.timeInMillis
            }
            lower.contains("next week") -> {
                cal.add(Calendar.WEEK_OF_YEAR, 1)
                cal.timeInMillis
            }
            else -> null
        }
    }

    private fun extractTime(lower: String, cal: Calendar) {
        val timeRegex = Regex("(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?")
        timeRegex.find(lower)?.let { match ->
            var hour = match.groupValues[1].toIntOrNull() ?: return
            val minute = match.groupValues[2].toIntOrNull() ?: 0
            val meridiem = match.groupValues[3]
            if (meridiem == "pm" && hour < 12) hour += 12
            if (meridiem == "am" && hour == 12) hour = 0
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            cal.set(Calendar.SECOND, 0)
        }
    }

    private fun extractCategory(text: String): String? {
        val categories = listOf("food", "transport", "groceries", "entertainment", "health", "shopping", "bills")
        return categories.firstOrNull { text.contains(it) }?.replaceFirstChar { it.uppercase() }
    }
}
