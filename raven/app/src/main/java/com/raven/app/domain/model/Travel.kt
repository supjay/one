package com.raven.app.domain.model

data class Trip(
    val id: Long = 0,
    val name: String,
    val destination: String,
    val startDateMillis: Long,
    val endDateMillis: Long,
    val budget: Double = 0.0,
    val status: TripStatus = TripStatus.PLANNED,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class ItineraryItem(
    val id: Long = 0,
    val tripId: Long,
    val title: String,
    val description: String = "",
    val location: String = "",
    val dateTimeMillis: Long,
    val type: ItineraryType = ItineraryType.ACTIVITY,
    val estimatedCost: Double = 0.0,
    val isBooked: Boolean = false,
    val bookingReference: String = "",
    val sortOrder: Int = 0
)

data class PackingItem(
    val id: Long = 0,
    val tripId: Long,
    val name: String,
    val category: String = "General",
    val quantity: Int = 1,
    val isPacked: Boolean = false
)

enum class TripStatus(val label: String) {
    PLANNED("Planned"),
    ONGOING("Ongoing"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}

enum class ItineraryType(val label: String) {
    FLIGHT("Flight"),
    HOTEL("Hotel"),
    ACTIVITY("Activity"),
    TRANSPORT("Transport"),
    MEAL("Meal")
}
