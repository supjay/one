package com.raven.app.data.local.dao

import androidx.room.*
import com.raven.app.data.local.entities.ItineraryItemEntity
import com.raven.app.data.local.entities.PackingItemEntity
import com.raven.app.data.local.entities.TripEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TravelDao {

    @Query("SELECT * FROM trips ORDER BY startDateMillis ASC")
    fun getAllTrips(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE status = :status ORDER BY startDateMillis ASC")
    fun getTripsByStatus(status: String): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE id = :id")
    suspend fun getTripById(id: Long): TripEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripEntity): Long

    @Update
    suspend fun updateTrip(trip: TripEntity)

    @Delete
    suspend fun deleteTrip(trip: TripEntity)

    // Itinerary
    @Query("SELECT * FROM itinerary_items WHERE tripId = :tripId ORDER BY dateTimeMillis ASC, sortOrder ASC")
    fun getItineraryForTrip(tripId: Long): Flow<List<ItineraryItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItineraryItem(item: ItineraryItemEntity): Long

    @Update
    suspend fun updateItineraryItem(item: ItineraryItemEntity)

    @Delete
    suspend fun deleteItineraryItem(item: ItineraryItemEntity)

    @Query("DELETE FROM itinerary_items WHERE tripId = :tripId")
    suspend fun deleteAllItineraryForTrip(tripId: Long)

    // Packing
    @Query("SELECT * FROM packing_items WHERE tripId = :tripId ORDER BY category ASC, name ASC")
    fun getPackingListForTrip(tripId: Long): Flow<List<PackingItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackingItem(item: PackingItemEntity): Long

    @Update
    suspend fun updatePackingItem(item: PackingItemEntity)

    @Delete
    suspend fun deletePackingItem(item: PackingItemEntity)

    @Query("UPDATE packing_items SET isPacked = :packed WHERE id = :id")
    suspend fun setPackingItemPacked(id: Long, packed: Boolean)

    @Query("SELECT COUNT(*) FROM packing_items WHERE tripId = :tripId")
    suspend fun getPackingItemCount(tripId: Long): Int

    @Query("SELECT COUNT(*) FROM packing_items WHERE tripId = :tripId AND isPacked = 1")
    suspend fun getPackedItemCount(tripId: Long): Int
}
