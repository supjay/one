package com.raven.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val destination: String,
    val startDateMillis: Long,
    val endDateMillis: Long,
    val budget: Double = 0.0,
    val status: String = "PLANNED", // PLANNED, ONGOING, COMPLETED, CANCELLED
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "itinerary_items")
data class ItineraryItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tripId: Long,
    val title: String,
    val description: String = "",
    val location: String = "",
    val dateTimeMillis: Long,
    val type: String = "ACTIVITY", // FLIGHT, HOTEL, ACTIVITY, TRANSPORT, MEAL
    val estimatedCost: Double = 0.0,
    val isBooked: Boolean = false,
    val bookingReference: String = "",
    val sortOrder: Int = 0
)

@Entity(tableName = "packing_items")
data class PackingItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tripId: Long,
    val name: String,
    val category: String = "General",
    val quantity: Int = 1,
    val isPacked: Boolean = false
)
