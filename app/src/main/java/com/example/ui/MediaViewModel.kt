package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.EqualizerPresetEntity
import com.example.data.MediaItemEntity
import com.example.data.MediaRepository
import com.example.data.StorageVolumeInfo
import com.example.playback.PlaybackManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class SortOption {
    TITLE, ALBUM, ARTIST, DATE, DURATION
}

class MediaViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = MediaRepository(application, database.mediaDao())
    val playbackManager = PlaybackManager.getInstance(application)

    private val prefs = application.getSharedPreferences("ap_player_preferences", Context.MODE_PRIVATE)

    // Language state
    private val _currentLanguage = MutableStateFlow(
        AppLanguage.values().find { it.code == prefs.getString("APP_CURRENT_LANGUAGE", "es_LA") } ?: AppLanguage.ES_LA
    )
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    // Sorting state
    private val _sortBy = MutableStateFlow(
        SortOption.values().find { it.name == prefs.getString("SORT_OPTION", "TITLE") } ?: SortOption.TITLE
    )
    val sortBy: StateFlow<SortOption> = _sortBy.asStateFlow()

    private val _sortAscending = MutableStateFlow(
        prefs.getBoolean("SORT_ASCENDING", true)
    )
    val sortAscending: StateFlow<Boolean> = _sortAscending.asStateFlow()

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

    // Sorted states derived from base databases
    val sortedMusicList: StateFlow<List<MediaItemEntity>> = combine(musicList, _sortBy, _sortAscending) { list, option, asc ->
        sortList(list, option, asc)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sortedVideoList: StateFlow<List<MediaItemEntity>> = combine(videoList, _sortBy, _sortAscending) { list, option, asc ->
        sortList(list, option, asc)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sortedRecentlyAddedList: StateFlow<List<MediaItemEntity>> = combine(recentlyAdded, _sortBy, _sortAscending) { list, option, asc ->
        sortList(list, option, asc)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active equalizer preset
    private val _activePresetName = MutableStateFlow("Normal (AP Std)")
    val activePresetName: StateFlow<String> = _activePresetName

    // App theme state
    private val _isDarkTheme = MutableStateFlow(true) // Black + Scarlet theme is default dark
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme

    // Storage volumes state
    private val _storageVolumes = MutableStateFlow<List<StorageVolumeInfo>>(emptyList())
    val storageVolumes: StateFlow<List<StorageVolumeInfo>> = _storageVolumes.asStateFlow()

    // Custom sheets state flow
    private val _customSheets = MutableStateFlow<Map<String, String>>(emptyMap())
    val customSheets: StateFlow<Map<String, String>> = _customSheets.asStateFlow()

    init {
        // Pre-populate with our gorgeous high-fidelity tracks
        viewModelScope.launch {
            repository.populateDemoDataIfEmpty()
            refreshStorageVolumes()
            loadCustomSheets()
        }
    }

    private fun loadCustomSheets() {
        val allPrefs = prefs.all
        val sheetsMap = mutableMapOf<String, String>()
        for ((key, value) in allPrefs) {
            if (key.startsWith("custom_sheet_music_") && value is String) {
                val trackId = key.removePrefix("custom_sheet_music_")
                sheetsMap[trackId] = value
            }
        }
        _customSheets.value = sheetsMap
    }

    fun setCustomSheetMusic(trackId: String, uriString: String?) {
        val editor = prefs.edit()
        if (uriString == null) {
            editor.remove("custom_sheet_music_$trackId")
            _customSheets.value = _customSheets.value - trackId
        } else {
            editor.putString("custom_sheet_music_$trackId", uriString)
            _customSheets.value = _customSheets.value + (trackId to uriString)
        }
        editor.apply()
    }

    fun refreshStorageVolumes() {
        viewModelScope.launch {
            _storageVolumes.value = repository.getStorageVolumesInfo()
        }
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun scanDevices() {
        viewModelScope.launch {
            repository.scanLocalMedia()
            refreshStorageVolumes()
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

    // Change language
    fun setLanguage(lang: AppLanguage) {
        _currentLanguage.value = lang
        prefs.edit().putString("APP_CURRENT_LANGUAGE", lang.code).apply()
    }

    // Change sorting field
    fun setSortBy(option: SortOption) {
        _sortBy.value = option
        prefs.edit().putString("SORT_OPTION", option.name).apply()
    }

    // Toggle sorting order direction
    fun toggleSortOrder() {
        val newVal = !_sortAscending.value
        _sortAscending.value = newVal
        prefs.edit().putBoolean("SORT_ASCENDING", newVal).apply()
    }

    // Dynamic sort helper
    private fun sortList(list: List<MediaItemEntity>, option: SortOption, asc: Boolean): List<MediaItemEntity> {
        val comparator = when (option) {
            SortOption.TITLE -> compareBy<MediaItemEntity, String>(String.CASE_INSENSITIVE_ORDER) { it.title }
            SortOption.ALBUM -> compareBy<MediaItemEntity, String>(String.CASE_INSENSITIVE_ORDER) { it.album }
            SortOption.ARTIST -> compareBy<MediaItemEntity, String>(String.CASE_INSENSITIVE_ORDER) { it.artist }
            SortOption.DATE -> compareBy<MediaItemEntity> { it.dateAdded }
            SortOption.DURATION -> compareBy<MediaItemEntity> { it.duration }
        }
        val sorted = list.sortedWith(comparator)
        return if (asc) sorted else sorted.reversed()
    }
}
