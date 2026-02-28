package com.raven.app.data.repository

import com.raven.app.data.local.dao.TravelDao
import com.raven.app.data.local.entities.ItineraryItemEntity
import com.raven.app.data.local.entities.PackingItemEntity
import com.raven.app.data.local.entities.TripEntity
import com.raven.app.domain.model.*
import com.raven.app.domain.repository.TravelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TravelRepositoryImpl @Inject constructor(
    private val dao: TravelDao
) : TravelRepository {

    override fun getAllTrips(): Flow<List<Trip>> =
        dao.getAllTrips().map { list -> list.map { it.toDomain() } }

    override fun getTripsByStatus(status: String): Flow<List<Trip>> =
        dao.getTripsByStatus(status).map { list -> list.map { it.toDomain() } }

    override suspend fun getTripById(id: Long): Trip? =
        dao.getTripById(id)?.toDomain()

    override suspend fun saveTrip(trip: Trip): Long =
        dao.insertTrip(trip.toEntity())

    override suspend fun deleteTrip(trip: Trip) =
        dao.deleteTrip(trip.toEntity())

    override fun getItineraryForTrip(tripId: Long): Flow<List<ItineraryItem>> =
        dao.getItineraryForTrip(tripId).map { list -> list.map { it.toDomain() } }

    override suspend fun saveItineraryItem(item: ItineraryItem): Long =
        dao.insertItineraryItem(item.toEntity())

    override suspend fun deleteItineraryItem(item: ItineraryItem) =
        dao.deleteItineraryItem(item.toEntity())

    override fun getPackingListForTrip(tripId: Long): Flow<List<PackingItem>> =
        dao.getPackingListForTrip(tripId).map { list -> list.map { it.toDomain() } }

    override suspend fun savePackingItem(item: PackingItem): Long =
        dao.insertPackingItem(item.toEntity())

    override suspend fun deletePackingItem(item: PackingItem) =
        dao.deletePackingItem(item.toEntity())

    override suspend fun setPackingItemPacked(id: Long, packed: Boolean) =
        dao.setPackingItemPacked(id, packed)

    override suspend fun getPackingProgress(tripId: Long): Pair<Int, Int> {
        val packed = dao.getPackedItemCount(tripId)
        val total = dao.getPackingItemCount(tripId)
        return Pair(packed, total)
    }

    private fun TripEntity.toDomain() = Trip(
        id = id, name = name, destination = destination,
        startDateMillis = startDateMillis, endDateMillis = endDateMillis,
        budget = budget, status = TripStatus.valueOf(status),
        notes = notes, createdAt = createdAt
    )

    private fun Trip.toEntity() = TripEntity(
        id = id, name = name, destination = destination,
        startDateMillis = startDateMillis, endDateMillis = endDateMillis,
        budget = budget, status = status.name,
        notes = notes, createdAt = createdAt
    )

    private fun ItineraryItemEntity.toDomain() = ItineraryItem(
        id = id, tripId = tripId, title = title, description = description,
        location = location, dateTimeMillis = dateTimeMillis,
        type = ItineraryType.valueOf(type), estimatedCost = estimatedCost,
        isBooked = isBooked, bookingReference = bookingReference, sortOrder = sortOrder
    )

    private fun ItineraryItem.toEntity() = ItineraryItemEntity(
        id = id, tripId = tripId, title = title, description = description,
        location = location, dateTimeMillis = dateTimeMillis,
        type = type.name, estimatedCost = estimatedCost,
        isBooked = isBooked, bookingReference = bookingReference, sortOrder = sortOrder
    )

    private fun PackingItemEntity.toDomain() = PackingItem(
        id = id, tripId = tripId, name = name,
        category = category, quantity = quantity, isPacked = isPacked
    )

    private fun PackingItem.toEntity() = PackingItemEntity(
        id = id, tripId = tripId, name = name,
        category = category, quantity = quantity, isPacked = isPacked
    )
}
