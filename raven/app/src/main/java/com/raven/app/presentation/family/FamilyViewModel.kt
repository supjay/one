package com.raven.app.presentation.family

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raven.app.domain.model.*
import com.raven.app.domain.repository.FamilyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FamilyUiState(
    val members: List<FamilyMember> = emptyList(),
    val commitments: List<Commitment> = emptyList(),
    val typeFilter: CommitmentType? = null,
    val showCompleted: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FamilyViewModel @Inject constructor(
    private val repository: FamilyRepository
) : ViewModel() {

    private val _typeFilter = MutableStateFlow<CommitmentType?>(null)
    private val _showCompleted = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<FamilyUiState> = combine(
        repository.getAllMembers(),
        combine(_typeFilter, _showCompleted) { type, showCompleted -> Pair(type, showCompleted) }
            .flatMapLatest { (type, showCompleted) ->
                when {
                    type != null -> repository.getCommitmentsByType(type.name)
                    !showCompleted -> repository.getUpcomingCommitments()
                    else -> repository.getAllCommitments()
                }
            },
        _typeFilter,
        _showCompleted,
        _error
    ) { members, commitments, typeFilter, showCompleted, error ->
        FamilyUiState(members, commitments, typeFilter, showCompleted, false, error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FamilyUiState())

    fun setTypeFilter(type: CommitmentType?) { _typeFilter.value = type }
    fun toggleShowCompleted() { _showCompleted.value = !_showCompleted.value }

    fun saveMember(member: FamilyMember) {
        viewModelScope.launch {
            try { repository.saveMember(member) }
            catch (e: Exception) { _error.value = e.message }
        }
    }

    fun deleteMember(member: FamilyMember) {
        viewModelScope.launch {
            try { repository.deleteMember(member) }
            catch (e: Exception) { _error.value = e.message }
        }
    }

    fun saveCommitment(commitment: Commitment) {
        viewModelScope.launch {
            try { repository.saveCommitment(commitment) }
            catch (e: Exception) { _error.value = e.message }
        }
    }

    fun deleteCommitment(commitment: Commitment) {
        viewModelScope.launch {
            try { repository.deleteCommitment(commitment) }
            catch (e: Exception) { _error.value = e.message }
        }
    }

    fun setCommitmentCompleted(id: Long, completed: Boolean) {
        viewModelScope.launch { repository.setCommitmentCompleted(id, completed) }
    }

    fun clearError() { _error.value = null }
}
