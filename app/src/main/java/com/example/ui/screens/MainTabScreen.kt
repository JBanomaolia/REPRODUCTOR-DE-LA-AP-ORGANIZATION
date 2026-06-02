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
import com.example.ui.AppLanguage
import com.example.ui.TranslationManager
import com.example.ui.SortOption
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
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
    val musicTracks by viewModel.sortedMusicList.collectAsState()
    val videoTracks by viewModel.sortedVideoList.collectAsState()
    val foldersList by viewModel.videoFolders.collectAsState()
    val albumsList by viewModel.albumsList.collectAsState()
    val recentlyAddedList by viewModel.sortedRecentlyAddedList.collectAsState()
    val smartPlaylistTracks by viewModel.smartPlaylist.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()
    val sortAscending by viewModel.sortAscending.collectAsState()

    // Filter statuses
    var activeFolderFilter by remember { mutableStateOf<String?>(null) }
    var activeAlbumFilter by remember { mutableStateOf<String?>(null) }
    var showConfigDialog by remember { mutableStateOf(false) }

    // Media scan permission states for Audio + Video + Storage volumes
    val permissionsList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_MEDIA_VIDEO
        )
    } else {
        listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    val permissionState = rememberMultiplePermissionsState(permissionsList)

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
                            text = TranslationManager.translate("APP_TITLE", currentLanguage),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = TranslationManager.translate("APP_SUBTITLE", currentLanguage),
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
                            if (permissionState.allPermissionsGranted) {
                                viewModel.scanDevices()
                                Toast.makeText(context, TranslationManager.translate("SCANNING_TOAST", currentLanguage), Toast.LENGTH_SHORT).show()
                            } else {
                                permissionState.launchMultiplePermissionRequest()
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
                            text = TranslationManager.translate("SCAN_BUTTON", currentLanguage),
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
        val tabs = listOf(
            TranslationManager.translate("TAB_MUSIC", currentLanguage),
            TranslationManager.translate("TAB_VIDEO", currentLanguage),
            TranslationManager.translate("TAB_ALBUMS", currentLanguage),
            TranslationManager.translate("TAB_RECENT", currentLanguage),
            TranslationManager.translate("TAB_STORAGE", currentLanguage)
        )
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

        // Sorting options control bar
        if (currentTabIdx in listOf(0, 1, 2, 3)) {
            SortingBar(viewModel = viewModel, onOpenConfig = { showConfigDialog = true })
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
                4 -> {
                    StorageManagementTabScreen(viewModel = viewModel, permissionState = permissionState)
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

        // Configuration Dialog Overlay
        if (showConfigDialog) {
            AlertDialog(
                onDismissRequest = { showConfigDialog = false },
                confirmButton = {
                    TextButton(
                        onClick = { showConfigDialog = false },
                        modifier = Modifier.testTag("dialog_close_button")
                    ) {
                        Text(
                            text = "LISTO",
                            color = ScarletPrimary,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = ScarletPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AP CONFIGURATIONS",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Section 1: Track Sorting Order
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = TranslationManager.translate("SORT_OPTION_TITLE", currentLanguage).uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                letterSpacing = 0.5.sp
                            )
                            
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                SortOption.values().forEach { option ->
                                    val isSelected = sortBy == option
                                    val labelKey = when (option) {
                                        SortOption.TITLE -> "SORT_BY_TITLE"
                                        SortOption.ALBUM -> "SORT_BY_ALBUM"
                                        SortOption.ARTIST -> "SORT_BY_ARTIST"
                                        SortOption.DATE -> "SORT_BY_DATE"
                                        SortOption.DURATION -> "SORT_BY_DURATION"
                                    }
                                    val label = TranslationManager.translate(labelKey, currentLanguage)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) ScarletPrimary.copy(alpha = 0.2f) else Color(0xFF161616))
                                            .border(1.dp, if (isSelected) ScarletPrimary else Color.Transparent, RoundedCornerShape(8.dp))
                                            .clickable { viewModel.setSortBy(option) }
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = label.uppercase(),
                                            color = if (isSelected) Color.White else Color.LightGray,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = ScarletPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF161616))
                                    .clickable { viewModel.toggleSortOrder() }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (sortAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                        contentDescription = null,
                                        tint = ScarletPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (sortAscending) 
                                            TranslationManager.translate("SORT_ORDER_ASC", currentLanguage).uppercase()
                                        else 
                                            TranslationManager.translate("SORT_ORDER_DESC", currentLanguage).uppercase(),
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "TOGGLE",
                                    color = ScarletPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        // Section 2: App Language Selection
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = TranslationManager.translate("CHOOSE_LANGUAGE", currentLanguage).uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                letterSpacing = 0.5.sp
                            )
                            
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                AppLanguage.values().forEach { lang ->
                                    val isSelected = currentLanguage == lang
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) ScarletPrimary.copy(alpha = 0.2f) else Color(0xFF161616))
                                            .border(1.dp, if (isSelected) ScarletPrimary else Color.Transparent, RoundedCornerShape(8.dp))
                                            .clickable { viewModel.setLanguage(lang) }
                                            .testTag("lang_dialog_${lang.code}")
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(lang.flag, fontSize = 14.sp)
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = lang.displayName.uppercase(),
                                                color = if (isSelected) Color.White else Color.LightGray,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = ScarletPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                containerColor = Color(0xFF1E1E1E),
                textContentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
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

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun StorageManagementTabScreen(
    viewModel: MediaViewModel,
    permissionState: com.google.accompanist.permissions.MultiplePermissionsState
) {
    val context = LocalContext.current
    val storageVolumes by viewModel.storageVolumes.collectAsState()
    val musicTracks by viewModel.musicList.collectAsState()
    val videoTracks by viewModel.videoList.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("storage_management_scroll"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Core Storage Info Panel Header
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = "Storage Status",
                        tint = ScarletPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "GESTIÓN DE MEMORIAS Y TARJETA SD",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        letterSpacing = 0.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Control de acceso directo y catalogado de pistas físicas en tarjetas de memoria microSD y almacenamiento local.",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
        }

        // Permissions Status card
        item {
            val allGranted = permissionState.allPermissionsGranted
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (allGranted) Color(0xFF0F1E16) else Color(0xFF221111)
                ),
                border = BorderStroke(
                    1.dp, 
                    if (allGranted) Color(0xFF1E5C3A).copy(alpha = 0.6f) else Color(0xFFDC143C).copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(if (allGranted) Color(0xFF1AA35B) else Color(0xFFDC143C), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (allGranted) Icons.Default.Check else Icons.Default.PriorityHigh,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "PERMISOS DE MEDIOS",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(
                                    if (allGranted) Color(0xFF1AA35B) else Color(0xFFDC143C),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (allGranted) "CONCEDIDO" else "REQUERIDO",
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (allGranted) {
                            "Acceso desbloqueado al sistema de archivos local y tarjetas SD instaladas de forma óptima. El reproductor AP puede leer directamente tus medios en alta definición."
                        } else {
                            "Para buscar pistas de música o documentales de video almacenados físicamente en la memoria interna o externa (micro SD), es necesario autorizar los accesos a los medios."
                        },
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    if (!allGranted) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { permissionState.launchMultiplePermissionRequest() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC143C)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .testTag("grant_permissions_btn")
                        ) {
                            Text("OTORGAR ACCESO A MEDIOS", fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }

        // Storage Drives title
        item {
            Text(
                "VOLÚMENES Y DISPOSITIVOS DIRECTOS",
                style = MaterialTheme.typography.labelSmall,
                color = Color.LightGray,
                fontWeight = FontWeight.Bold
            )
        }

        // List Volumes Dynamically
        if (storageVolumes.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(Color(0xFF141414), RoundedCornerShape(12.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Cargando discos de almacenamiento...", color = Color.Gray, fontSize = 11.sp)
                }
            }
        } else {
            items(storageVolumes) { volume ->
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("volume_card_${volume.path.hashCode()}"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
                    border = BorderStroke(1.dp, CrimsonBorder.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFF222222), CircleShape)
                                    .border(1.dp, if (volume.isPrimary) ScarletPrimary else Color.White.copy(alpha = 0.6f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (volume.isPrimary) Icons.Default.Storage else Icons.Default.SdCard,
                                    contentDescription = null,
                                    tint = if (volume.isPrimary) ScarletPrimary else Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = volume.description.uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = if (volume.isPrimary) "Memoria del Sistema (Interna)" else "Unidad de Almacenamiento Removible / Tarjeta SD",
                                    color = Color.Gray,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Path & Status Details
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1A1A1A), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Ruta física:", color = Color.Gray, fontSize = 10.sp)
                                Text(volume.path, color = Color.White, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Estado de montaje:", color = Color.Gray, fontSize = 10.sp)
                                Text(volume.state, color = ScarletPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Individual Mount point Scan Trigger Button
                        Button(
                            onClick = {
                                if (permissionState.allPermissionsGranted) {
                                    viewModel.scanDevices()
                                    Toast.makeText(context, "Escaneo profundo en curso: ${volume.description}", Toast.LENGTH_SHORT).show()
                                } else {
                                    permissionState.launchMultiplePermissionRequest()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1C1C)),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                                .testTag("scan_vol_btn_${volume.path.hashCode()}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SyncAlt,
                                contentDescription = null,
                                tint = ScarletPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "ESCANEAR ESTA UNIDAD",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Stats section
        item {
            Text(
                "INDICE DE ARCHIVOS GENERAL",
                style = MaterialTheme.typography.labelSmall,
                color = Color.LightGray,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CrimsonBorder.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${musicTracks.size}",
                            color = ScarletPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Canciones",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(Color.White.copy(alpha = 0.1f))
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${videoTracks.size}",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Videos",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(Color.White.copy(alpha = 0.1f))
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val total = musicTracks.size + videoTracks.size
                        Text(
                            text = "$total",
                            color = Color.LightGray,
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = TranslationManager.translate("TOTAL_INDEX_LABEL", currentLanguage),
                            color = Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SortingBar(viewModel: MediaViewModel, onOpenConfig: () -> Unit) {
    val sortBy by viewModel.sortBy.collectAsState()
    val sortAscending by viewModel.sortAscending.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF141414))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.Sort,
                contentDescription = null,
                tint = ScarletPrimary,
                modifier = Modifier.size(16.dp)
            )
            val sortByLabel = when (sortBy) {
                SortOption.TITLE -> "SORT_BY_TITLE"
                SortOption.ALBUM -> "SORT_BY_ALBUM"
                SortOption.ARTIST -> "SORT_BY_ARTIST"
                SortOption.DATE -> "SORT_BY_DATE"
                SortOption.DURATION -> "SORT_BY_DURATION"
            }
            val dirLabel = if (sortAscending) "SORT_ORDER_ASC" else "SORT_ORDER_DESC"
            val textValue = "${TranslationManager.translate("SORT_OPTION_TITLE", currentLanguage)}: " +
                    "${TranslationManager.translate(sortByLabel, currentLanguage)} (${TranslationManager.translate(dirLabel, currentLanguage)})"
            
            Text(
                text = textValue.uppercase(),
                color = Color.LightGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))

        // Gear-shaped button to configure order and language
        IconButton(
            onClick = onOpenConfig,
            modifier = Modifier
                .size(34.dp)
                .background(Color(0xFF222222), RoundedCornerShape(6.dp))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                .testTag("app_config_gear_button")
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Configure Application",
                tint = ScarletPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

