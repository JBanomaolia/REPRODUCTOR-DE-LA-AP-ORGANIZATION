package com.example.data

import android.content.ContentResolver
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class MediaRepository(
    private val context: Context,
    private val mediaDao: MediaDao
) {
    val allMusic: Flow<List<MediaItemEntity>> = mediaDao.getAllMusic()
    val allVideos: Flow<List<MediaItemEntity>> = mediaDao.getAllVideos()
    val videoFolders: Flow<List<String>> = mediaDao.getVideoFolders()
    val albums: Flow<List<String>> = mediaDao.getAlbums()
    val recentlyAdded: Flow<List<MediaItemEntity>> = mediaDao.getRecentlyAdded()
    val smartPlaylist: Flow<List<MediaItemEntity>> = mediaDao.getSmartPlaylistMostPlayed()
    val equalizerPresets: Flow<List<EqualizerPresetEntity>> = mediaDao.getEqualizerPresets()

    fun getVideosInFolder(folder: String): Flow<List<MediaItemEntity>> = mediaDao.getVideosInFolder(folder)
    fun getMusicInAlbum(album: String): Flow<List<MediaItemEntity>> = mediaDao.getMusicInAlbum(album)

    suspend fun incrementPlayCount(id: String) {
        mediaDao.incrementPlayCount(id)
    }

    suspend fun savePreset(name: String, bands: String) {
        mediaDao.insertPreset(EqualizerPresetEntity(name = name, bands = bands, isCustom = true))
    }

    suspend fun deletePreset(preset: EqualizerPresetEntity) {
        mediaDao.deletePreset(preset)
    }

    suspend fun populateDemoDataIfEmpty() {
        withContext(Dispatchers.IO) {
            val musicEmpty = mediaDao.getAllMusic().first().isEmpty()
            val videoEmpty = mediaDao.getAllVideos().first().isEmpty()

            if (musicEmpty && videoEmpty) {
                Log.d("MediaRepository", "Database or media files empty. Inserting demo tracks...")
                val demoItems = listOf(
                    // Music demo tracks
                    MediaItemEntity(
                        id = "demo_lamento_boliviano",
                        title = "Lamento Boliviano (High-Res)",
                        artist = "AP Rock Band",
                        album = "Clásicos del Sur",
                        duration = 224000L,
                        path = "demo_lamento_boliviano.flac", // mock path or indicator code
                        folder = "Music",
                        mimeType = "audio/flac",
                        isVideo = false,
                        dateAdded = System.currentTimeMillis() - 86400000L * 3 // 3 days ago
                    ),
                    MediaItemEntity(
                        id = "demo_musica_ligera",
                        title = "De Música Ligera",
                        artist = "Stereo Soda",
                        album = "Grandes de Hoy",
                        duration = 208000L,
                        path = "demo_musica_ligera.mp3",
                        folder = "Music",
                        mimeType = "audio/mpeg",
                        isVideo = false,
                        dateAdded = System.currentTimeMillis() - 86400000L * 1 // 1 day ago
                    ),
                    MediaItemEntity(
                        id = "demo_cosmic_voyage",
                        title = "Cosmic Voyage (Lossless)",
                        artist = "Stellar Synth Orchestra",
                        album = "Future Vision",
                        duration = 180000L,
                        path = "demo_cosmic_voyage.alac",
                        folder = "Downloads",
                        mimeType = "audio/x-alac",
                        isVideo = false,
                        dateAdded = System.currentTimeMillis() - 3600000L // 1 hour ago
                    ),
                    MediaItemEntity(
                        id = "demo_amanecer_carmesi",
                        title = "Amanecer Carmesí",
                        artist = "AP Organization Synth",
                        album = "Crimson Beats",
                        duration = 150000L,
                        path = "demo_amanecer_carmesi.mp3",
                        folder = "Music",
                        mimeType = "audio/mpeg",
                        isVideo = false,
                        dateAdded = System.currentTimeMillis()
                    ),
                    // Video demo tracks
                    MediaItemEntity(
                        id = "demo_cataratas_iguazu",
                        title = "Cataratas de Iguazú UHD",
                        artist = "Natural AP Documentales",
                        album = "Maravillas Mundiales",
                        duration = 45000L,
                        path = "demo_cataratas_iguazu.mp4",
                        folder = "Documentales",
                        mimeType = "video/mp4",
                        isVideo = true,
                        dateAdded = System.currentTimeMillis() - 86400000L * 5
                    ),
                    MediaItemEntity(
                        id = "demo_cinematic_neon",
                        title = "Cinematic Neon Street",
                        artist = "AP Cyberpunk Films",
                        album = "Vistas Urbanas",
                        duration = 60000L,
                        path = "demo_cinematic_neon.mp4",
                        folder = "Downloads",
                        mimeType = "video/mp4",
                        isVideo = true,
                        dateAdded = System.currentTimeMillis() - 86400000L * 2
                    )
                )
                mediaDao.insertMediaItems(demoItems)

                // Insert default equalizer presets as well if empty
                val presetsEmpty = mediaDao.getEqualizerPresets().first().isEmpty()
                if (presetsEmpty) {
                    val defaultPresets = listOf(
                        EqualizerPresetEntity(name = "Normal (AP Std)", bands = "0,0,0,0,0", isCustom = false),
                        EqualizerPresetEntity(name = "Rock Carmesí (+Low)", bands = "5,3,-1,2,4", isCustom = false),
                        EqualizerPresetEntity(name = "Pop Brillante", bands = "2,4,1,-2,-1", isCustom = false),
                        EqualizerPresetEntity(name = "Metal Directo", bands = "4,2,3,4,1", isCustom = false),
                        EqualizerPresetEntity(name = "Electro Graves", bands = "6,4,0,0,2", isCustom = false),
                        EqualizerPresetEntity(name = "Voces Claras", bands = "-2,1,4,3,2", isCustom = false)
                    )
                    defaultPresets.forEach { mediaDao.insertPreset(it) }
                }
            }
        }
    }

    suspend fun scanLocalMedia() = withContext(Dispatchers.IO) {
        val resolver: ContentResolver = context.contentResolver
        val localMedia = mutableListOf<MediaItemEntity>()

        // 1. Scan Audio
        val audioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val audioProjection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.DATE_ADDED
        )
        val audioCursor = resolver.query(audioUri, audioProjection, null, null, null)
        audioCursor?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

            while (cursor.moveToNext()) {
                val path = cursor.getString(dataCol)
                val folder = getParentFolderName(path)
                val id = cursor.getString(idCol)
                localMedia.add(
                    MediaItemEntity(
                        id = "local_audio_$id",
                        title = cursor.getString(titleCol) ?: "Pista Desconocida",
                        artist = cursor.getString(artistCol) ?: "<Desconocido>",
                        album = cursor.getString(albumCol) ?: "<Desconocido>",
                        duration = cursor.getLong(durationCol),
                        path = path,
                        folder = folder,
                        mimeType = cursor.getString(mimeCol) ?: "audio/mpeg",
                        isVideo = false,
                        dateAdded = cursor.getLong(dateCol) * 1000L
                    )
                )
            }
        }

        // 2. Scan Video
        val videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val videoProjection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.ARTIST,
            MediaStore.Video.Media.ALBUM,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.DATE_ADDED
        )
        val videoCursor = resolver.query(videoUri, videoProjection, null, null, null)
        videoCursor?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ALBUM)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)

            while (cursor.moveToNext()) {
                val path = cursor.getString(dataCol)
                val folder = getParentFolderName(path)
                val id = cursor.getString(idCol)
                localMedia.add(
                    MediaItemEntity(
                        id = "local_video_$id",
                        title = cursor.getString(titleCol) ?: "Video Desconocido",
                        artist = cursor.getString(artistCol) ?: "AP Reproductor",
                        album = cursor.getString(albumCol) ?: "Media",
                        duration = cursor.getLong(durationCol),
                        path = path,
                        folder = folder,
                        mimeType = cursor.getString(mimeCol) ?: "video/mp4",
                        isVideo = true,
                        dateAdded = cursor.getLong(dateCol) * 1000L
                    )
                )
            }
        }

        if (localMedia.isNotEmpty()) {
            mediaDao.insertMediaItems(localMedia)
            Log.d("MediaRepository", "Scanned and saved ${localMedia.size} local media items.")
        } else {
            // No local media found from scanning (common in emulators, or if permission not yet granted)
            // Just populate demo data
            populateDemoDataIfEmpty()
        }
    }

    private fun getParentFolderName(path: String?): String {
        if (path.isNullOrBlank()) return "Internal"
        return try {
            val parts = path.split("/")
            if (parts.size >= 2) {
                parts[parts.size - 2]
            } else {
                "Downloads"
            }
        } catch (_: Exception) {
            "Downloads"
        }
    }
}
