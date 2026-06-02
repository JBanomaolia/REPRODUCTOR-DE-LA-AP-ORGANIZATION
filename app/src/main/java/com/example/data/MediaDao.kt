package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Query("SELECT * FROM media_items WHERE isVideo = 0 ORDER BY title ASC")
    fun getAllMusic(): Flow<List<MediaItemEntity>>

    @Query("SELECT * FROM media_items WHERE isVideo = 1 ORDER BY title ASC")
    fun getAllVideos(): Flow<List<MediaItemEntity>>

    @Query("SELECT DISTINCT folder FROM media_items WHERE isVideo = 1 ORDER BY folder ASC")
    fun getVideoFolders(): Flow<List<String>>

    @Query("SELECT * FROM media_items WHERE isVideo = 1 AND folder = :folderName ORDER BY title ASC")
    fun getVideosInFolder(folderName: String): Flow<List<MediaItemEntity>>

    @Query("SELECT DISTINCT album FROM media_items WHERE isVideo = 0 ORDER BY album ASC")
    fun getAlbums(): Flow<List<String>>

    @Query("SELECT * FROM media_items WHERE isVideo = 0 AND album = :albumName ORDER BY title ASC")
    fun getMusicInAlbum(albumName: String): Flow<List<MediaItemEntity>>

    @Query("SELECT * FROM media_items ORDER BY dateAdded DESC LIMIT 30")
    fun getRecentlyAdded(): Flow<List<MediaItemEntity>>

    @Query("SELECT * FROM media_items WHERE isVideo = 0 ORDER BY playCount DESC, lastPlayedTimestamp DESC LIMIT 30")
    fun getSmartPlaylistMostPlayed(): Flow<List<MediaItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaItems(items: List<MediaItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaItem(item: MediaItemEntity)

    @Query("UPDATE media_items SET playCount = playCount + 1, lastPlayedTimestamp = :timestamp WHERE id = :id")
    suspend fun incrementPlayCount(id: String, timestamp: Long = System.currentTimeMillis())

    // Equalizer Presets
    @Query("SELECT * FROM equalizer_presets")
    fun getEqualizerPresets(): Flow<List<EqualizerPresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: EqualizerPresetEntity)

    @Delete
    suspend fun deletePreset(preset: EqualizerPresetEntity)
}
