package com.example.ui.screens

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MediaItemEntity
import com.example.ui.MediaViewModel
import com.example.ui.theme.CrimsonBorder
import com.example.ui.theme.DarkGreyBase
import com.example.ui.theme.ScarletPrimary
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainTabScreen(
    viewModel: MediaViewModel,
    onOpenMusicPlayer: () -> Unit,
    onOpenVideoPlayer: (MediaItemEntity) -> Unit,
    onOpenEqualizer: () -> Unit
) {
    val context = LocalContext.current
    var currentTabIdx by remember { mutableStateOf(0) } // 0: Música, 1: Video, 2: Álbumes, 3: Añadido Recientemente
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()

    // Query states
    val musicTracks by viewModel.musicList.collectAsState()
    val videoTracks by viewModel.videoList.collectAsState()
    val foldersList by viewModel.videoFolders.collectAsState()
    val albumsList by viewModel.albumsList.collectAsState()
    val recentlyAddedList by viewModel.recentlyAdded.collectAsState()
    val smartPlaylistTracks by viewModel.smartPlaylist.collectAsState()

    // Filter statuses
    var activeFolderFilter by remember { mutableStateOf<String?>(null) }
    var activeAlbumFilter by remember { mutableStateOf<String?>(null) }

    // Media scan permission state
    val permissionString = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    val permissionState = rememberPermissionState(permissionString)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Custom Logo & Header Navigation Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF121212))
                .padding(top = 40.dp, bottom = 12.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // AP custom brand Logo with base bg-[#990000] border border-white rounded-lg
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { viewModel.toggleTheme() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFF990000), RoundedCornerShape(8.dp))
                            .border(width = 1.dp, color = Color.White, shape = RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "AP",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontStyle = FontStyle.Italic,
                            fontSize = 15.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.graphicsLayer {
                                rotationZ = -5f
                            }
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "AP ORGANIZATION",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "HIGH-RESOLUTION PLAYER",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Equalizer Launcher Button (translucent sleek style)
                    IconButton(
                        onClick = onOpenEqualizer,
                        modifier = Modifier
                            .testTag("main_equalizer_shortcut")
                            .size(36.dp)
                            .background(Color(0x0DFFFFFF), RoundedCornerShape(8.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Ecualizador",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Scanner sync button (Crimson Red with subtle white border opacity)
                    Button(
                        onClick = {
                            if (permissionState.status.isGranted) {
                                viewModel.scanDevices()
                                Toast.makeText(context, "Buscando pistas locales de sonido y video...", Toast.LENGTH_SHORT).show()
                            } else {
                                permissionState.launchPermissionRequest()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC143C)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("scan_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "ESCANEAR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
        
        // Header separator bottom outline
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0x1AFFFFFF))
        )

        // Swipe Tabs Title row
        val tabs = listOf("Música", "Video", "Álbumes", "Reciente")
        TabRow(
            selectedTabIndex = currentTabIdx,
            containerColor = Color(0xFF121212),
            contentColor = Color(0xFFDC143C),
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[currentTabIdx]),
                    color = Color(0xFFDC143C),
                    height = 2.dp
                )
            },
            divider = {}
        ) {
            tabs.forEachIndexed { index, name ->
                Tab(
                    selected = currentTabIdx == index,
                    onClick = { currentTabIdx = index },
                    modifier = Modifier.testTag("tab_$index"),
                    text = {
                        Text(
                            text = name.uppercase(),
                            color = if (currentTabIdx == index) Color(0xFFDC143C) else Color.White.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                    }
                )
            }
        }

        // Core visual lists split screens
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentTabIdx) {
                0 -> {
                    // TABS 0: MUSIC SCREEN VIEW
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        // Intelligent Playlists Frequency Row (Lista inteligente)
                        item {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Smart",
                                        tint = ScarletPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "LISTA INTELIGENTE (Frecuencia activa)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.LightGray,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                if (smartPlaylistTracks.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp)
                                            .border(1.dp, CrimsonBorder.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                            .background(Color(0xFF141414)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Escucha más canciones para poblar frecuencia...", color = Color.Gray, fontSize = 11.sp)
                                    }
                                } else {
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(smartPlaylistTracks) { smartTrack ->
                                            Card(
                                                modifier = Modifier
                                                    .width(130.dp)
                                                    .clickable { viewModel.playTrack(smartTrack, musicTracks) },
                                                colors = CardDefaults.cardColors(containerColor = Color(0xFF161616)),
                                                border = BorderStroke(1.dp, CrimsonBorder.copy(alpha = 0.5f)),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(8.dp)) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(70.dp)
                                                            .background(Color(0xFF222222), RoundedCornerShape(8.dp))
                                                            .padding(4.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            "AP",
                                                            color = ScarletPrimary,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 18.sp
                                                        )
                                                        Text(
                                                            "🔥 x${smartTrack.playCount}",
                                                            color = Color.White,
                                                            fontSize = 7.sp,
                                                            fontWeight = FontWeight.Black,
                                                            modifier = Modifier
                                                                .align(Alignment.BottomEnd)
                                                                .background(Color.Black.copy(alpha = 0.6f))
                                                                .padding(horizontal = 3.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = smartTrack.title,
                                                        color = Color.White,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = smartTrack.artist,
                                                        color = Color.Gray,
                                                        fontSize = 9.sp,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Music Catalog List Header
                        item {
                            Text(
                                text = "NUESTRAS PISTAS DISPONIBLES",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }

                        // Tracks Grid Items
                        if (musicTracks.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Sin pistas en base de datos. Haz clic en Escuchar Demo.", color = Color.Gray)
                                }
                            }
                        } else {
                            items(musicTracks) { musicItem ->
                                MusicTrackItem(
                                    track = musicItem,
                                    onClick = { viewModel.playTrack(musicItem, musicTracks) }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // TABS 1: VIDEO SCREEN VIEW (Includes Folder locator selector)
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        item {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "LOCALIZADOR DE CARPETAS DE VIDEOS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.LightGray,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                // Folder row selectors list
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // "Show all" button
                                    item {
                                        Button(
                                            onClick = { activeFolderFilter = null },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (activeFolderFilter == null) ScarletPrimary else Color(0xFF1E1E1E)
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, if (activeFolderFilter == null) Color.White else Color.DarkGray)
                                        ) {
                                            Text("TODOS", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // Device and External directories listed
                                    items(foldersList) { folderName ->
                                        val isCurrent = activeFolderFilter == folderName
                                        Button(
                                            onClick = { activeFolderFilter = folderName },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isCurrent) ScarletPrimary else Color(0xFF1E1E1E)
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, if (isCurrent) Color.White else Color.DarkGray)
                                        ) {
                                            tintedFolderIcon(isCurrent)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(folderName.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // Display list of videos matching the physical folder
                        val filteredVideos = if (activeFolderFilter != null) {
                            videoTracks.filter { it.folder == activeFolderFilter }
                        } else {
                            videoTracks
                        }

                        if (filteredVideos.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Ningún archivo de video cargado en esta sección.", color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                        } else {
                            items(filteredVideos) { videoItem ->
                                VideoTrackItem(
                                    video = videoItem,
                                    onClick = {
                                        // Play and open full screen player
                                        viewModel.playTrack(videoItem, filteredVideos)
                                        onOpenVideoPlayer(videoItem)
                                    }
                                )
                            }
                        }
                    }
                }
                2 -> {
                    // TABS 2: ALBUMS SCREEN VIEW (With custom inside tracks search)
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        item {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "ÁLBUMES DISPONIBLES EN SISTEMA",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.LightGray,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                // Grid of albums
                                if (albumsList.isEmpty()) {
                                    Text("Cargando álbumes...", color = Color.Gray)
                                } else {
                                    albumsList.forEach { albumName ->
                                        val isCurrent = activeAlbumFilter == albumName
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                                .clickable {
                                                    activeAlbumFilter = if (isCurrent) null else albumName
                                                },
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF151515)),
                                            border = BorderStroke(1.dp, if (isCurrent) ScarletPrimary else CrimsonBorder.copy(alpha = 0.3f)),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(44.dp)
                                                        .background(Color(0xFF252525), CircleShape)
                                                        .border(1.dp, CrimsonBorder, CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(imageVector = Icons.Default.Album, contentDescription = null, tint = ScarletPrimary)
                                                }
                                                Spacer(modifier = Modifier.width(16.dp))
                                                Text(
                                                    text = albumName,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                            }
                                        }

                                        // Expand inner music tracks of selected album
                                        if (isCurrent) {
                                            val albumTracks = musicTracks.filter { it.album == albumName }
                                            Column(modifier = Modifier.padding(start = 24.dp)) {
                                                albumTracks.forEach { trackItem ->
                                                    MusicTrackItem(
                                                        track = trackItem,
                                                        onClick = { viewModel.playTrack(trackItem, albumTracks) }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // TABS 3: RECENTLY ADDED SCREEN VIEW
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text(
                                text = "NUEVOS ARCHIVOS DE REPRODUCCIÓN DETECTADOS",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.LightGray,
                                modifier = Modifier.padding(top = 16.dp, start = 16.dp, bottom = 8.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (recentlyAddedList.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("No se detectaron añadidos recientes...", color = Color.Gray)
                                }
                            }
                        } else {
                            items(recentlyAddedList) { addedItem ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .clickable {
                                            if (addedItem.isVideo) {
                                                viewModel.playTrack(addedItem, recentlyAddedList)
                                                onOpenVideoPlayer(addedItem)
                                            } else {
                                                viewModel.playTrack(addedItem, recentlyAddedList)
                                            }
                                        },
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
                                    border = BorderStroke(1.dp, CrimsonBorder.copy(alpha = 0.4f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(if (addedItem.isVideo) ScarletPrimary else Color(0xFF222222), CircleShape)
                                                .border(1.dp, Color.White, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (addedItem.isVideo) Icons.Default.PlayCircle else Icons.Default.MusicNote,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(addedItem.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                                            Text("Añadido recientemente • ${addedItem.artist}", color = Color.Gray, fontSize = 10.sp)
                                        }
                                        Text(
                                            text = formatTime(addedItem.duration),
                                            color = Color.LightGray,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // PERSISTENT BOTTOM DIRECT PLAYER SHEET (Sleek Obsidian Brand Panel)
        val currentPlaybackTrack by viewModel.playbackManager.currentTrack.collectAsState()
        val isPlaybackPlaying by viewModel.playbackManager.isPlaying.collectAsState()

        if (currentPlaybackTrack != null && (currentTabIdx != 1 || !currentPlaybackTrack!!.isVideo)) {
            val playingItem = currentPlaybackTrack!!
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(Color(0xFF111111))
                    .border(width = 1.dp, color = Color(0xFFDC143C).copy(alpha = 0.3f))
                    .clickable { onOpenMusicPlayer() }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Custom Crimson audio album badge
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color(0xFFDC143C), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "AP",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            fontStyle = FontStyle.Italic
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = playingItem.title.uppercase(),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // HI-RES active indicator label
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFDC143C), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    "HI-RES",
                                    color = Color.White,
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${playingItem.artist} · ${if (playingItem.path.endsWith(".mp3")) "MP3" else "FLAC"}",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Skip Previous control
                    IconButton(
                        onClick = { viewModel.playbackManager.playPrevious() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Anterior",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Play/Pause circular control
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFDC143C), CircleShape)
                            .clickable { viewModel.playbackManager.togglePlayPause() }
                            .testTag("mini_play_pause"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaybackPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaybackPlaying) "Pausar" else "Reproducir",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Skip Next control
                    IconButton(
                        onClick = { viewModel.playbackManager.playNext() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Siguiente",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MusicTrackItem(
    track: MediaItemEntity,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFF181818), RoundedCornerShape(8.dp))
                    .border(1.dp, CrimsonBorder.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = ScarletPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = track.title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = track.artist,
                    color = Color.Gray,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (track.mimeType.contains("flac") || track.mimeType.contains("alac")) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF1E1E1E), RoundedCornerShape(4.dp))
                        .border(1.dp, ScarletPrimary, RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text("HI-RES", color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                text = formatTime(track.duration),
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun VideoTrackItem(
    video: MediaItemEntity,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = video.title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Carpeta: ${video.folder.uppercase()}",
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }
        }

        Text(
            text = formatTime(video.duration),
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}

@Composable
fun tintedFolderIcon(isCurrent: Boolean) {
    Icon(
        imageVector = Icons.Default.Folder,
        contentDescription = null,
        tint = if (isCurrent) Color.White else ScarletPrimary,
        modifier = Modifier.size(14.dp)
    )
}

// Helpers for line borders
fun BottomBorder(width: androidx.compose.ui.unit.Dp) = width
fun TopBorder(width: androidx.compose.ui.unit.Dp) = width
