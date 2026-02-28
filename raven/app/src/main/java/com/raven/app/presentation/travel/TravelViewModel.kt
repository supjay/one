package com.raven.app.presentation.travel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raven.app.domain.model.*
import com.raven.app.domain.repository.TravelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TravelUiState(
    val trips: List<Trip> = emptyList(),
    val selectedTrip: Trip? = null,
    val itinerary: List<ItineraryItem> = emptyList(),
    val packingList: List<PackingItem> = emptyList(),
    val packingProgress: Pair<Int, Int> = Pair(0, 0),
    val statusFilter: TripStatus? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TravelViewModel @Inject constructor(
    private val repository: TravelRepository
) : ViewModel() {

    private val _selectedTripId = MutableStateFlow<Long?>(null)
    private val _statusFilter = MutableStateFlow<TripStatus?>(null)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<TravelUiState> = combine(
        _statusFilter.flatMapLatest { status ->
            if (status != null) repository.getTripsByStatus(status.name)
            else repository.getAllTrips()
        },
        _selectedTripId.flatMapLatest { tripId ->
            if (tripId != null) repository.getItineraryForTrip(tripId)
            else flowOf(emptyList())
        },
        _selectedTripId.flatMapLatest { tripId ->
            if (tripId != null) repository.getPackingListForTrip(tripId)
            else flowOf(emptyList())
        },
        _statusFilter,
        _error
    ) { trips, itinerary, packingList, statusFilter, error ->
        TravelUiState(
            trips = trips,
            itinerary = itinerary,
            packingList = packingList,
            statusFilter = statusFilter,
            error = error
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TravelUiState())

    fun selectTrip(tripId: Long?) {
        _selectedTripId.value = tripId
    }

    fun setStatusFilter(status: TripStatus?) {
        _statusFilter.value = status
    }

    fun saveTrip(trip: Trip) {
        viewModelScope.launch {
            try {
                repository.saveTrip(trip)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun deleteTrip(trip: Trip) {
        viewModelScope.launch {
            try {
                repository.deleteTrip(trip)
                if (_selectedTripId.value == trip.id) _selectedTripId.value = null
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun saveItineraryItem(item: ItineraryItem) {
        viewModelScope.launch { repository.saveItineraryItem(item) }
    }

    fun deleteItineraryItem(item: ItineraryItem) {
        viewModelScope.launch { repository.deleteItineraryItem(item) }
    }

    fun savePackingItem(item: PackingItem) {
        viewModelScope.launch { repository.savePackingItem(item) }
    }

    fun deletePackingItem(item: PackingItem) {
        viewModelScope.launch { repository.deletePackingItem(item) }
    }

    fun togglePackingItem(item: PackingItem) {
        viewModelScope.launch { repository.setPackingItemPacked(item.id, !item.isPacked) }
    }

    fun clearError() { _error.value = null }
}
