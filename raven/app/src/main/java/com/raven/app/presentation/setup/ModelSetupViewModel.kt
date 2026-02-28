package com.raven.app.presentation.setup

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raven.app.util.DownloadState
import com.raven.app.util.LlmInferenceManager
import com.raven.app.util.LlmState
import com.raven.app.util.ModelDownloadManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SetupUiState {
    object Checking : SetupUiState()
    data class NeedsDownload(val modelSizeMb: Int = ModelDownloadManager.MODEL_SIZE_MB) : SetupUiState()
    data class Downloading(val progressPercent: Int, val downloadedMb: Int, val totalMb: Int) : SetupUiState()
    object Initializing : SetupUiState()
    object Ready : SetupUiState()
    data class Error(val message: String) : SetupUiState()
    object Skipped : SetupUiState()
}

@HiltViewModel
class ModelSetupViewModel @Inject constructor(
    private val downloadManager: ModelDownloadManager,
    private val llmManager: LlmInferenceManager,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    companion object {
        val KEY_MODEL_READY = booleanPreferencesKey("model_ready")
        val KEY_LLM_SKIPPED = booleanPreferencesKey("llm_skipped")
    }

    private val _state = MutableStateFlow<SetupUiState>(SetupUiState.Checking)
    val state: StateFlow<SetupUiState> = _state.asStateFlow()

    val isSetupNeeded: Flow<Boolean> = dataStore.data.map { prefs ->
        val modelReady = prefs[KEY_MODEL_READY] ?: false
        val skipped = prefs[KEY_LLM_SKIPPED] ?: false
        !modelReady && !skipped
    }

    init {
        checkModelStatus()
    }

    private fun checkModelStatus() {
        viewModelScope.launch {
            val prefs = dataStore.data.first()
            val modelReady = prefs[KEY_MODEL_READY] ?: false
            val skipped = prefs[KEY_LLM_SKIPPED] ?: false

            when {
                modelReady -> {
                    // Model already downloaded — initialize it
                    _state.value = SetupUiState.Initializing
                    initializeModel()
                }
                skipped -> _state.value = SetupUiState.Skipped
                downloadManager.modelExists() -> {
                    // File exists but flag not set — initialize
                    _state.value = SetupUiState.Initializing
                    initializeModel()
                }
                else -> _state.value = SetupUiState.NeedsDownload()
            }
        }
    }

    fun startDownload(customUrl: String? = null) {
        viewModelScope.launch {
            val url = customUrl ?: ModelDownloadManager.MODEL_DOWNLOAD_URL
            downloadManager.downloadModel(url).collect { downloadState ->
                when (downloadState) {
                    is DownloadState.Downloading -> {
                        val downloadedMb = (downloadState.downloadedBytes / 1_000_000).toInt()
                        val totalMb = (downloadState.totalBytes / 1_000_000).toInt()
                        _state.value = SetupUiState.Downloading(
                            progressPercent = downloadState.progressPercent,
                            downloadedMb = downloadedMb,
                            totalMb = totalMb.coerceAtLeast(1)
                        )
                    }
                    is DownloadState.Complete -> {
                        _state.value = SetupUiState.Initializing
                        initializeModel()
                    }
                    is DownloadState.Error -> {
                        _state.value = SetupUiState.Error(downloadState.message)
                    }
                    else -> {}
                }
            }
        }
    }

    fun copyModelFromPath(path: String) {
        viewModelScope.launch {
            _state.value = SetupUiState.Initializing
            val success = downloadManager.copyFromPath(path)
            if (success) {
                initializeModel()
            } else {
                _state.value = SetupUiState.Error("Could not copy model from $path")
            }
        }
    }

    private suspend fun initializeModel() {
        llmManager.initializeModel(downloadManager.getModelPath())
        when (val llmState = llmManager.state.value) {
            is LlmState.Ready -> {
                dataStore.edit { it[KEY_MODEL_READY] = true }
                _state.value = SetupUiState.Ready
            }
            is LlmState.Error -> {
                _state.value = SetupUiState.Error(llmState.message)
            }
            else -> {}
        }
    }

    fun skip() {
        viewModelScope.launch {
            dataStore.edit { it[KEY_LLM_SKIPPED] = true }
            _state.value = SetupUiState.Skipped
        }
    }

    fun retry() {
        _state.value = SetupUiState.NeedsDownload()
    }
}
