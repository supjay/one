package com.raven.app.domain.repository

import com.raven.app.domain.model.ItineraryItem
import com.raven.app.domain.model.PackingItem
import com.raven.app.domain.model.Trip
import kotlinx.coroutines.flow.Flow

interface TravelRepository {
    fun getAllTrips(): Flow<List<Trip>>
    fun getTripsByStatus(status: String): Flow<List<Trip>>
    suspend fun getTripById(id: Long): Trip?
    suspend fun saveTrip(trip: Trip): Long
    suspend fun deleteTrip(trip: Trip)

    fun getItineraryForTrip(tripId: Long): Flow<List<ItineraryItem>>
    suspend fun saveItineraryItem(item: ItineraryItem): Long
    suspend fun deleteItineraryItem(item: ItineraryItem)

    fun getPackingListForTrip(tripId: Long): Flow<List<PackingItem>>
    suspend fun savePackingItem(item: PackingItem): Long
    suspend fun deletePackingItem(item: PackingItem)
    suspend fun setPackingItemPacked(id: Long, packed: Boolean)
    suspend fun getPackingProgress(tripId: Long): Pair<Int, Int> // packed, total
}
