package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.EqualizerPresetEntity
import com.example.data.MediaItemEntity
import com.example.data.MediaRepository
import com.example.playback.PlaybackManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MediaViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = MediaRepository(application, database.mediaDao())
    val playbackManager = PlaybackManager.getInstance(application)

    // State flows from Room
    val musicList: StateFlow<List<MediaItemEntity>> = repository.allMusic
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val videoList: StateFlow<List<MediaItemEntity>> = repository.allVideos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val videoFolders: StateFlow<List<String>> = repository.videoFolders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val albumsList: StateFlow<List<String>> = repository.albums
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyAdded: StateFlow<List<MediaItemEntity>> = repository.recentlyAdded
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val smartPlaylist: StateFlow<List<MediaItemEntity>> = repository.smartPlaylist
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val equalizerPresets: StateFlow<List<EqualizerPresetEntity>> = repository.equalizerPresets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active equalizer preset
    private val _activePresetName = MutableStateFlow("Normal (AP Std)")
    val activePresetName: StateFlow<String> = _activePresetName

    // App theme state
    private val _isDarkTheme = MutableStateFlow(true) // Black + Scarlet theme is default dark
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme

    init {
        // Pre-populate with our gorgeous high-fidelity tracks
        viewModelScope.launch {
            repository.populateDemoDataIfEmpty()
        }
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun scanDevices() {
        viewModelScope.launch {
            repository.scanLocalMedia()
        }
    }

    fun selectPreset(preset: EqualizerPresetEntity) {
        _activePresetName.value = preset.name
        val bands = preset.bands.split(",").map { it.toFloatOrNull() ?: 0f }.toFloatArray()
        playbackManager.setEqualizerBands(bands)
    }

    fun saveCustomPreset(name: String, bands: FloatArray) {
        viewModelScope.launch {
            val bandsStr = bands.joinToString(",") { it.toString() }
            repository.savePreset(name, bandsStr)
            _activePresetName.value = name
        }
    }

    fun deletePreset(preset: EqualizerPresetEntity) {
        viewModelScope.launch {
            repository.deletePreset(preset)
            if (_activePresetName.value == preset.name) {
                _activePresetName.value = "Normal (AP Std)"
                playbackManager.setEqualizerBands(floatArrayOf(0f, 0f, 0f, 0f, 0f))
            }
        }
    }

    fun playTrack(track: MediaItemEntity, contextList: List<MediaItemEntity>) {
        playbackManager.playTrack(track, contextList)
        viewModelScope.launch {
            repository.incrementPlayCount(track.id)
        }
    }

    fun getVideosInFolder(folder: String): Flow<List<MediaItemEntity>> {
        return repository.getVideosInFolder(folder)
    }

    fun getMusicInAlbum(album: String): Flow<List<MediaItemEntity>> {
        return repository.getMusicInAlbum(album)
    }
}
