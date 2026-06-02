package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_items")
data class MediaItemEntity(
    @PrimaryKey val id: String, // typically physical path or Content ID
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val path: String,
    val folder: String,
    val mimeType: String,
    val isVideo: Boolean,
    val playCount: Int = 0,
    val lastPlayedTimestamp: Long = 0L,
    val dateAdded: Long = System.currentTimeMillis()
)

@Entity(tableName = "equalizer_presets")
data class EqualizerPresetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val bands: String, // comma-separated float gains, e.g., "0.0,0.0,0.0,0.0,0.0"
    val isCustom: Boolean = true
)
