package com.example.data

import android.content.ContentResolver
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File

data class StorageVolumeInfo(
    val isPrimary: Boolean,
    val description: String,
    val state: String,
    val path: String
)


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
                        title = "Lamento Boliviano (High-Res Streams)",
                        artist = "AP Rock Band",
                        album = "Clásicos del Sur",
                        duration = 372000L,
                        path = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                        folder = "Music",
                        mimeType = "audio/mpeg",
                        isVideo = false,
                        dateAdded = System.currentTimeMillis() - 86400000L * 3 // 3 days ago
                    ),
                    MediaItemEntity(
                        id = "demo_musica_ligera",
                        title = "De Música Ligera",
                        artist = "Stereo Soda",
                        album = "Grandes de Hoy",
                        duration = 425000L,
                        path = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
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
                        duration = 344000L,
                        path = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
                        folder = "Downloads",
                        mimeType = "audio/mpeg",
                        isVideo = false,
                        dateAdded = System.currentTimeMillis() - 3600000L // 1 hour ago
                    ),
                    MediaItemEntity(
                        id = "demo_amanecer_carmesi",
                        title = "Amanecer Carmesí",
                        artist = "AP Organization Synth",
                        album = "Crimson Beats",
                        duration = 302000L,
                        path = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
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
                        duration = 653000L,
                        path = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
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
                        duration = 596000L,
                        path = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
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
                val contentUri = "content://media/external/audio/media/$id"
                localMedia.add(
                    MediaItemEntity(
                        id = "local_audio_$id",
                        title = cursor.getString(titleCol) ?: "Pista Desconocida",
                        artist = cursor.getString(artistCol) ?: "<Desconocido>",
                        album = cursor.getString(albumCol) ?: "<Desconocido>",
                        duration = cursor.getLong(durationCol),
                        path = contentUri,
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
                val contentUri = "content://media/external/video/media/$id"
                localMedia.add(
                    MediaItemEntity(
                        id = "local_video_$id",
                        title = cursor.getString(titleCol) ?: "Video Desconocido",
                        artist = cursor.getString(artistCol) ?: "AP Reproductor",
                        album = cursor.getString(albumCol) ?: "Media",
                        duration = cursor.getLong(durationCol),
                        path = contentUri,
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

        // Add physical directories scan for files on external SD memory card & internal standard folders
        deepScanPhysicalVolumes()
    }

    suspend fun deepScanPhysicalVolumes() = withContext(Dispatchers.IO) {
        val storageVolumes = getStorageVolumesInfo()
        val foundItems = mutableListOf<MediaItemEntity>()

        for (volume in storageVolumes) {
            val volumeRoot = File(volume.path)
            if (volumeRoot.exists() && volumeRoot.isDirectory) {
                // Scan typical user entry-points (Music, Video, Movies, Downloads etc.)
                val targetFolders = listOf("Music", "Video", "Movies", "Download", "Downloads", "DCIM")
                for (subName in targetFolders) {
                    val subFolder = File(volumeRoot, subName)
                    if (subFolder.exists() && subFolder.isDirectory) {
                        traverseAndCollectMedia(subFolder, foundItems)
                    }
                }
                // Also scan the root itself (shallowly)
                val rootFiles = volumeRoot.listFiles() ?: continue
                for (f in rootFiles) {
                    if (f.isFile) {
                        collectFileIfMedia(f, foundItems)
                    }
                }
            }
        }

        if (foundItems.isNotEmpty()) {
            mediaDao.insertMediaItems(foundItems)
            Log.d("MediaRepository", "Scanned ${foundItems.size} items from physical paths.")
        }
    }

    private fun traverseAndCollectMedia(dir: File, list: MutableList<MediaItemEntity>) {
        val files = dir.listFiles() ?: return
        for (f in files) {
            if (f.isDirectory) {
                // Exclude system/hidden directories
                if (f.name != "Android" && !f.name.startsWith(".")) {
                    traverseAndCollectMedia(f, list)
                }
            } else if (f.isFile) {
                collectFileIfMedia(f, list)
            }
        }
    }

    private fun collectFileIfMedia(f: File, list: MutableList<MediaItemEntity>) {
        val name = f.name
        val ext = name.substringAfterLast(".", "").lowercase()
        val isAudio = ext in listOf("mp3", "flac", "wav", "m4a", "ogg", "alac")
        val isVideo = ext in listOf("mp4", "mkv", "avi", "3gp", "webm")

        if (isAudio || isVideo) {
            val mimeType = if (isVideo) "video/$ext" else "audio/$ext"
            val parentFolder = f.parentFile?.name ?: "Almacenamiento"
            val duration = getDurationOfFile(f)
            val path = f.absolutePath

            // Create a unique id for physical scanning to prevent duplicates
            val id = "physical_${path.hashCode()}"

            list.add(
                MediaItemEntity(
                    id = id,
                    title = name.substringBeforeLast("."),
                    artist = if (isVideo) "Video Local" else "Artista Local",
                    album = parentFolder,
                    duration = duration,
                    path = path,
                    folder = parentFolder,
                    mimeType = mimeType,
                    isVideo = isVideo,
                    dateAdded = f.lastModified()
                )
            )
        }
    }

    private fun getDurationOfFile(file: File): Long {
        var duration = 150000L // 2.5 minutes fallback
        val retriever = android.media.MediaMetadataRetriever()
        try {
            retriever.setDataSource(file.absolutePath)
            val durationStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
            if (durationStr != null) {
                duration = durationStr.toLong()
            }
        } catch (_: Exception) {
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {}
        }
        return duration
    }

    fun getStorageVolumesInfo(): List<StorageVolumeInfo> {
        val list = mutableListOf<StorageVolumeInfo>()
        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as? StorageManager
        
        if (storageManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val volumes = storageManager.storageVolumes
                for (vol in volumes) {
                    val isPrimary = vol.isPrimary
                    val desc = vol.getDescription(context) ?: if (isPrimary) "Almacenamiento Interno" else "Tarjeta MicroSD Externa"
                    val state = vol.state ?: "MOUNTED"
                    
                    var path = ""
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        path = vol.directory?.absolutePath ?: ""
                    }
                    
                    if (path.isEmpty()) {
                        val dirs = context.getExternalFilesDirs(null)
                        if (isPrimary && dirs.isNotEmpty() && dirs[0] != null) {
                            path = dirs[0]!!.absolutePath.substringBefore("/Android")
                        } else if (!isPrimary && dirs.size > 1 && dirs[1] != null) {
                            path = dirs[1]!!.absolutePath.substringBefore("/Android")
                        } else {
                            path = if (isPrimary) Environment.getExternalStorageDirectory().absolutePath else ""
                        }
                    }
                    
                    if (path.isNotEmpty()) {
                        list.add(StorageVolumeInfo(isPrimary, desc, state, path))
                    }
                }
            }
        }
        
        // Ensure at least internal is listed if StorageManager list fails or is empty
        if (list.isEmpty()) {
            val internalPath = Environment.getExternalStorageDirectory().absolutePath
            list.add(StorageVolumeInfo(true, "Almacenamiento Interno", "MOUNTED", internalPath))
            
            // Check for potential secondary external SD cards from getExternalFilesDirs
            val dirs = context.getExternalFilesDirs(null)
            if (dirs.size > 1 && dirs[1] != null) {
                val sdPath = dirs[1]!!.absolutePath.substringBefore("/Android")
                list.add(StorageVolumeInfo(false, "Tarjeta SD Externa", "MOUNTED", sdPath))
            }
        }
        
        return list
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
