package com.example.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MediaItemEntity
import com.example.playback.PlaybackLoopMode
import com.example.playback.SyncedLyricsAndMusicSheet
import com.example.ui.AppLanguage
import com.example.ui.MediaViewModel
import com.example.ui.theme.CrimsonBorder
import com.example.ui.theme.ScarletPrimary
import com.example.ui.TranslationManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

@Composable
fun MusicPlayerScreen(
    viewModel: MediaViewModel,
    onOpenEqualizer: () -> Unit,
    onMinimize: () -> Unit
) {
    val currentTrack by viewModel.playbackManager.currentTrack.collectAsState()
    val isPlaying by viewModel.playbackManager.isPlaying.collectAsState()
    val position by viewModel.playbackManager.currentPosition.collectAsState()
    val duration by viewModel.playbackManager.currentDuration.collectAsState()
    val loopMode by viewModel.playbackManager.loopMode.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()

    val track = currentTrack ?: return

    var currentViewTab by remember { mutableStateOf(PlayerSubView.LYRICS) } // LYRICS or SHEET_MUSIC
    val lyrics = remember(track.id) { SyncedLyricsAndMusicSheet.getLyricsForTrack(track.id) }
    val sheetMusic = remember(track.id) { SyncedLyricsAndMusicSheet.getSheetMusicForTrack(track.id) }
    
    val customSheets by viewModel.customSheets.collectAsState()
    val customSheetUriString = customSheets[track.id]
    var viewFullSheetMusic by remember { mutableStateOf(false) }
    var sheetDarkMode by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val docLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            try {
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(it, takeFlags)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            viewModel.setCustomSheetMusic(track.id, it.toString())
        }
    }

    val activeLyricIdx = lyrics.indexOfLast { position >= it.timestampMs }.coerceAtLeast(0)
    val activeSheetBeat = sheetMusic.indexOfLast { position >= it.timestampMs }.coerceAtLeast(0)

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Smooth scroll lyrics to active line
    LaunchedEffect(activeLyricIdx) {
        if (lyrics.isNotEmpty()) {
            listState.animateScrollToItem(activeLyricIdx)
        }
    }

    // Interactive waveform simulator state
    val waveAnim = rememberInfiniteTransition("waveform")
    val waveOffset by waveAnim.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .padding(top = 16.dp, bottom = 24.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Player Header Navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
             IconButton(onClick = onMinimize, modifier = Modifier.testTag("back_to_list")) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = TranslationManager.translate("PLAYER_PREVIOUS", currentLanguage),
                    tint = Color.White
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = TranslationManager.translate("PLAYER_NOW_PLAYING", currentLanguage),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.LightGray,
                    letterSpacing = 2.sp
                )
                Text(
                    text = if (track.mimeType.contains("flac") || track.mimeType.contains("alac")) "ALTA RESOLUCIÓN LOSSLESS 🎧" else "REDUCTOR DURAL AD-FREE",
                    style = MaterialTheme.typography.bodySmall,
                    color = ScarletPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = onOpenEqualizer, modifier = Modifier.testTag("equalizer_shortcut")) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = TranslationManager.translate("EQUALIZER_HEADER", currentLanguage),
                    tint = ScarletPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Center Album Art & Track Meta
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dynamic Art with rotating crimson border vinyl effect
            val vinylRotatingAngle by rememberInfiniteTransition("rotation").animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(6000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "vinyl"
            )

            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF131313))
                    .border(2.dp, CrimsonBorder, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                // AP organization logo visualization
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.minDimension / 2f
                    // Draw outer crimson circle
                    drawCircle(
                        color = CrimsonBorder,
                        radius = radius,
                        style = Stroke(width = 2f)
                    )
                    // Draw glowing center AP letters
                    drawCircle(
                        color = ScarletPrimary.copy(alpha = 0.2f),
                        radius = radius - 10f
                    )
                }
                Text(
                    "AP",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = track.artist,
                    color = Color.LightGray,
                    fontSize = 14.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(ScarletPrimary, RoundedCornerShape(4.dp))
                            .border(1.dp, Color.White, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = track.mimeType.substringAfter("/").uppercase(),
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    if (track.mimeType.contains("flac") || track.mimeType.contains("alac")) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF222222), RoundedCornerShape(4.dp))
                                .border(1.dp, CrimsonBorder, RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "HIGH-RES",
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Synced Lyrics & Music Sheet Toggle Tabs
        TabRow(
            selectedTabIndex = if (currentViewTab == PlayerSubView.LYRICS) 0 else 1,
            containerColor = Color.Transparent,
            contentColor = ScarletPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[if (currentViewTab == PlayerSubView.LYRICS) 0 else 1]),
                    color = ScarletPrimary,
                    height = 2.dp
                )
            },
            divider = {}
        ) {
            Tab(
                selected = currentViewTab == PlayerSubView.LYRICS,
                onClick = { currentViewTab = PlayerSubView.LYRICS },
                text = { Text("LETRAS SINCRONIZADAS", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = currentViewTab == PlayerSubView.SHEET_MUSIC,
                onClick = { currentViewTab = PlayerSubView.SHEET_MUSIC },
                text = { Text("PARTITURAS ACTIVAS", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Large Display Content Container (Saves screen space and displays content on dark cards)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF101010), RoundedCornerShape(16.dp))
                .border(1.dp, CrimsonBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            if (currentViewTab == PlayerSubView.LYRICS) {
                // Interactive scrolling lyrics view
                if (lyrics.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Letras de alta definición sincronizándose...", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(vertical = 40.dp)
                    ) {
                        itemsIndexed(lyrics) { idx, line ->
                            val isActive = idx == activeLyricIdx
                            val scale by animateFloatAsState(if (isActive) 1.2f else 0.95f)
                            val color by animateColorAsState(if (isActive) Color.White else Color.Gray.copy(alpha = 0.6f))
                            val glow = if (isActive) Brush.horizontalGradient(listOf(Color.Transparent, ScarletPrimary.copy(alpha = 0.15f), Color.Transparent)) else null

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(if (glow != null) Modifier.background(glow) else Modifier)
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = line.text,
                                    fontSize = 16.sp,
                                    color = color,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().graphicsLayer(
                                        scaleX = scale,
                                        scaleY = scale
                                    )
                                )
                            }
                        }
                    }
                }
            } else {
                // Multilingual definitions for action labels
                val (toggleRealtime, toggleFull, uploadLabel, clearLabel, modeLabel) = when (currentLanguage) {
                    AppLanguage.ES_LA -> listOf("VER EN TIEMPO REAL", "NOTACIÓN COMPLETA / PDF", "CARGAR PDF / IMAGEN", "QUITAR", "MODO NOCTURNO")
                    AppLanguage.JA_JP -> listOf("リアルタイム表示", "楽譜 / PDF 表示", "PDF / 画像の読み込み", "消去", "ダークモード")
                    AppLanguage.PT_BR -> listOf("VER EM TEMPO REAL", "NOTAÇÃO COMPLETA / PDF", "CARREGAR PDF / IMAGEM", "LIMPAR", "MODO NOTURNO")
                    AppLanguage.RU_RU -> listOf("РЕАЛ-ТАЙМ", "ПОЛНЫЕ НОТЫ / PDF", "ЗАГРУЗИТЬ PDF / ИЗОбр.", "СБРОСИТЬ", "ТЕМНЫЙ РЕЖИМ")
                    else -> listOf("REAL-TIME STAFF", "FULL SHEET MUSIC / PDF", "LOAD PDF / IMAGE", "CLEAR", "DARK MODE")
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    // Controls Row for Mode Toggles, SAF PDF/Image loading, and Contrast Themes
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Switch style button (Realtime Staff vs Full sheets)
                        Button(
                            onClick = { viewFullSheetMusic = !viewFullSheetMusic },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (viewFullSheetMusic) ScarletPrimary.copy(alpha = 0.2f) else ScarletPrimary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .testTag("toggle_sheet_music_mode_button")
                        ) {
                            Text(
                                text = if (viewFullSheetMusic) toggleRealtime else toggleFull,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }

                        // Upload button
                        IconButton(
                            onClick = {
                                docLauncher.launch(arrayOf("application/pdf", "image/*"))
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF222222), RoundedCornerShape(8.dp))
                                .testTag("upload_sheet_music_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = uploadLabel,
                                tint = ScarletPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Reset custom sheet button (Visible only when custom exists)
                        if (customSheetUriString != null) {
                            IconButton(
                                onClick = {
                                    viewModel.setCustomSheetMusic(track.id, null)
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFF331111), RoundedCornerShape(8.dp))
                                    .testTag("clear_sheet_music_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = clearLabel,
                                    tint = Color.Red,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Contrast toggle button (Visible only when showing full page)
                        if (viewFullSheetMusic) {
                            IconButton(
                                onClick = { sheetDarkMode = !sheetDarkMode },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFF222222), RoundedCornerShape(8.dp))
                                    .testTag("toggle_contrast_button")
                            ) {
                                Icon(
                                    imageVector = if (sheetDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = modeLabel,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        if (!viewFullSheetMusic) {
                            // Render original standard interactive live staff player
                            val activeBeat = if (sheetMusic.isNotEmpty()) sheetMusic[activeSheetBeat] else null

                            Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Acompañamiento: ${activeBeat?.chordName ?: "Base"}",
                                        color = ScarletPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = activeBeat?.instruction ?: "Ritmo Estándar",
                                        color = Color.LightGray,
                                        fontSize = 10.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Treble Clef staff music canvas drawing
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                        .background(Color(0xFF090909))
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val w = size.width
                                        val h = size.height
                                        val mid = h / 2f

                                        // Draw the 5 standard lines of sheet music staff
                                        val lineSpacing = 14.dp.toPx()
                                        val topOffset = mid - (2 * lineSpacing)

                                        for (i in 0 until 5) {
                                            val y = topOffset + i * lineSpacing
                                            drawLine(
                                                color = Color.White.copy(alpha = 0.3f),
                                                start = Offset(20f, y),
                                                end = Offset(w - 20f, y),
                                                strokeWidth = 1.5f
                                            )
                                        }

                                        // Draw treble clef G symbol mockup
                                        val clefX = 40f
                                        val clefY = mid
                                        drawCircle(
                                            color = ScarletPrimary,
                                            radius = 5.dp.toPx(),
                                            center = Offset(clefX, clefY),
                                            style = Stroke(width = 2.5f)
                                        )
                                        drawLine(
                                            color = ScarletPrimary,
                                            start = Offset(clefX, clefY - 30.dp.toPx()),
                                            end = Offset(clefX, clefY + 30.dp.toPx()),
                                            strokeWidth = 3f
                                        )

                                        // Draw dynamic moving musical notes of current beats
                                        val activeNotes = activeBeat?.notes ?: listOf(4, 7, 11)
                                        val drawOffsetY = topOffset + 4 * lineSpacing // baseline G

                                        activeNotes.forEachIndexed { noteIdx, midiOffset ->
                                            val noteY = drawOffsetY - ((midiOffset * (lineSpacing / 2f)))
                                            val noteX = 130f + (noteIdx * 60f)

                                            // Draw glowing scarlet note head
                                            drawOval(
                                                color = ScarletPrimary,
                                                topLeft = Offset(noteX - 8f, noteY - 5f),
                                                size = Size(16f, 10f)
                                            )
                                            drawOval(
                                                color = Color.White,
                                                topLeft = Offset(noteX - 3.5f, noteY - 2f),
                                                size = Size(7f, 4f)
                                            )
                                            // Stem
                                            drawLine(
                                                color = Color.White,
                                                start = Offset(noteX + 7.5f, noteY - 1f),
                                                end = Offset(noteX + 7.5f, noteY - 25f),
                                                strokeWidth = 2.5f
                                            )
                                            // Beam
                                            if (noteIdx == activeNotes.lastIndex) {
                                                drawLine(
                                                    color = Color.White,
                                                    start = Offset(130f + 7.5f, drawOffsetY - ((activeNotes[0] * (lineSpacing / 2f))) - 35f),
                                                    end = Offset(noteX + 7.5f, noteY - 35f),
                                                    strokeWidth = 3f
                                                )
                                            }
                                        }

                                        // Moving beat measure bar line
                                        val progressPercent = if (duration > 0) position.toFloat() / duration.toFloat() else 0f
                                        val scannerX = 100f + (progressPercent * (w - 200f)).coerceAtLeast(0f)
                                        drawLine(
                                            color = Color.White.copy(alpha = 0.7f),
                                            start = Offset(scannerX, topOffset - 10f),
                                            end = Offset(scannerX, topOffset + 4 * lineSpacing + 10f),
                                            strokeWidth = 1.5f
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Chords representations fret diagram placeholders
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    listOf("A", "C", "D", "E", "F", "G").forEach { chord ->
                                        val isThisChord = activeBeat?.chordName?.contains(chord) == true
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isThisChord) ScarletPrimary else Color.Transparent,
                                                    shape = RoundedCornerShape(4.dp)
                                                )
                                                .padding(4.dp)
                                        ) {
                                            Text(
                                                text = chord,
                                                color = if (isThisChord) ScarletPrimary else Color.Gray,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Canvas(modifier = Modifier.size(20.dp)) {
                                                for (dot in 0..3) {
                                                    drawCircle(
                                                        color = if (isThisChord) CrimsonBorder else Color.Black,
                                                        radius = 2.5f,
                                                        center = Offset(size.width / 4f * dot + 2f, size.height / 2f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // SHOW FULL NOTATION SHEETS (Scrollable Pages / PDF / Image files)
                            if (customSheetUriString != null) {
                                if (customSheetUriString.lowercase().contains("pdf")) {
                                    PdfSheetMusicViewer(
                                        uri = Uri.parse(customSheetUriString),
                                        darkMode = sheetDarkMode
                                    )
                                } else {
                                    // Custom user Image loaded safely from SAF stream
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        var imgBitmap by remember(customSheetUriString) { mutableStateOf<android.graphics.Bitmap?>(null) }
                                        var loadError by remember(customSheetUriString) { mutableStateOf(false) }

                                        LaunchedEffect(customSheetUriString) {
                                            try {
                                                val stream = context.contentResolver.openInputStream(Uri.parse(customSheetUriString))
                                                imgBitmap = BitmapFactory.decodeStream(stream)
                                                stream?.close()
                                                loadError = false
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                                loadError = true
                                            }
                                        }

                                        if (loadError) {
                                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                Text("Restricción de acceso o formato no soportado.", color = Color.Gray, fontSize = 11.sp, textAlign = TextAlign.Center)
                                            }
                                        } else if (imgBitmap != null) {
                                            val filter = if (sheetDarkMode) ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
                                                -1f, 0f, 0f, 0f, 255f,
                                                0f, -1f, 0f, 0f, 255f,
                                                0f,  0f, -1f, 0f, 255f,
                                                0f,  0f, 0f, 1f, 0f
                                            ))) else null

                                            Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                                                Image(
                                                    bitmap = imgBitmap!!.asImageBitmap(),
                                                    contentDescription = "Custom Sheet Image",
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                                                    colorFilter = filter
                                                )
                                            }
                                        } else {
                                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                CircularProgressIndicator(color = ScarletPrimary)
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Fallback to our stunning vector-rendered book sheets
                                DefaultBuiltInSheetMusicViewer(
                                    trackId = track.id,
                                    title = track.title,
                                    artist = track.artist,
                                    darkMode = sheetDarkMode
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dynamic Waveforms Visualizer (Bouncing audio levels simulation)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(horizontal = 8.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val barWidth = 6.dp.toPx()
                val spacing = 4.dp.toPx()
                val barsCount = (width / (barWidth + spacing)).toInt()

                for (i in 0 until barsCount) {
                    // Sine-wave simulator combined with play-pause logic
                    val volumeMultiplier = if (isPlaying) 1.2f else 0.1f
                    val waveValue = sin((i.toFloat() * 0.15f) + waveOffset) * 0.5f + 0.5f
                    val randomNoise = if (isPlaying) (Math.random().toFloat() * 0.3f) else 0f
                    val finalPercent = (waveValue * 0.7f + randomNoise) * volumeMultiplier

                    val barHeight = (height * finalPercent).coerceAtLeast(3.dp.toPx())
                    val x = i * (barWidth + spacing)
                    val y = height - barHeight

                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(ScarletPrimary, CrimsonBorder)
                        ),
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Custom Slider Progress Seek bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatTime(position),
                color = Color.LightGray,
                style = MaterialTheme.typography.labelSmall
            )

            Slider(
                value = position.toFloat(),
                onValueChange = { newValue ->
                    viewModel.playbackManager.seekTo(newValue.toLong())
                },
                valueRange = 0f..duration.toFloat().coerceAtLeast(1f),
                colors = SliderDefaults.colors(
                    activeTrackColor = ScarletPrimary,
                    inactiveTrackColor = Color(0xFF2E2E2E),
                    thumbColor = Color.White
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )

            Text(
                text = formatTime(duration),
                color = Color.LightGray,
                style = MaterialTheme.typography.labelSmall
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Core playback controllers buttons (Targes with test tags for visual verifications)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Loop Mode choice
            Column(
                modifier = Modifier
                    .size(44.dp)
                    .border(
                        1.dp,
                        if (loopMode == PlaybackLoopMode.ONE) ScarletPrimary else Color.Transparent,
                        CircleShape
                    )
                    .clip(CircleShape)
                    .clickable {
                        val nextMode = when (loopMode) {
                            PlaybackLoopMode.ALL -> PlaybackLoopMode.ONE
                            PlaybackLoopMode.ONE -> PlaybackLoopMode.FOLDER
                            PlaybackLoopMode.FOLDER -> PlaybackLoopMode.ALL
                        }
                        viewModel.playbackManager.setLoopMode(nextMode)
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = when (loopMode) {
                        PlaybackLoopMode.ONE -> Icons.Default.RepeatOne
                        PlaybackLoopMode.FOLDER -> Icons.Default.FolderZip
                        PlaybackLoopMode.ALL -> Icons.Default.Repeat
                    },
                    contentDescription = "Bucle",
                    tint = if (loopMode == PlaybackLoopMode.ALL) Color.White else ScarletPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = when (loopMode) {
                        PlaybackLoopMode.ONE -> "1"
                        PlaybackLoopMode.FOLDER -> " Carp"
                        PlaybackLoopMode.ALL -> "Todo"
                    },
                    fontSize = 6.sp,
                    color = Color.White
                )
            }

            // Previous
            FilledIconButton(
                onClick = { viewModel.playbackManager.playPrevious() },
                modifier = Modifier
                    .size(50.dp)
                    .testTag("prev_track_button"),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color(0xFF222222),
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Atrás"
                )
            }

            // Play / Pause Huge scarlet action circle
            Button(
                onClick = { viewModel.playbackManager.togglePlayPause() },
                modifier = Modifier
                    .size(68.dp)
                    .border(2.dp, Color.White, CircleShape)
                    .testTag("play_pause_button"),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ScarletPrimary,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Reproducir/Pausar",
                    modifier = Modifier.size(36.dp)
                )
            }

            // Next
            FilledIconButton(
                onClick = { viewModel.playbackManager.playNext() },
                modifier = Modifier
                    .size(50.dp)
                    .testTag("next_track_button"),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color(0xFF222222),
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Siguiente"
                )
            }

            // High Resolution / Equalizer icon launcher
            Column(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable { onOpenEqualizer() },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = "Visualizer EQ",
                    tint = ScarletPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Text("EQ", fontSize = 8.sp, color = Color.White)
            }
        }
    }
}

fun formatTime(milliseconds: Long): String {
    val totalSeconds = (milliseconds / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

enum class PlayerSubView {
    LYRICS, SHEET_MUSIC
}

// ==========================================
// NEW SHEET MUSIC RENDERERS AND VIEWERS
// ==========================================

@Composable
fun PdfSheetMusicViewer(
    uri: Uri,
    darkMode: Boolean
) {
    val context = LocalContext.current
    var pageCount by remember(uri) { mutableStateOf(0) }
    var pdfRenderer by remember(uri) { mutableStateOf<PdfRenderer?>(null) }
    var fileDescriptor by remember(uri) { mutableStateOf<ParcelFileDescriptor?>(null) }
    var errorMsg by remember(uri) { mutableStateOf<String?>(null) }

    DisposableEffect(uri) {
        try {
            val fd = context.contentResolver.openFileDescriptor(uri, "r")
            if (fd != null) {
                fileDescriptor = fd
                val renderer = PdfRenderer(fd)
                pdfRenderer = renderer
                pageCount = renderer.pageCount
            } else {
                errorMsg = "No se pudo abrir el archivo PDF."
            }
        } catch (e: Exception) {
            errorMsg = "Error: ${e.localizedMessage}"
            e.printStackTrace()
        }

        onDispose {
            try {
                pdfRenderer?.close()
                fileDescriptor?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    if (errorMsg != null) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Text(errorMsg!!, color = Color.Red.copy(alpha = 0.8f), fontSize = 11.sp, textAlign = TextAlign.Center)
        }
    } else if (pageCount == 0) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = ScarletPrimary)
        }
    } else {
        val listState = rememberLazyListState()
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(pageCount) { pageIndex ->
                pdfRenderer?.let { renderer ->
                    val pageBitmap = remember(renderer, pageIndex, darkMode) {
                        try {
                            val page = renderer.openPage(pageIndex)
                            val scale = 1.8f
                            val width = (page.width * scale).toInt()
                            val height = (page.height * scale).toInt()
                            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                            
                            val canvas = android.graphics.Canvas(bitmap)
                            canvas.drawColor(if (darkMode) android.graphics.Color.parseColor("#141414") else android.graphics.Color.parseColor("#FAF6EE"))
                            
                            page.render(
                                bitmap,
                                null,
                                null,
                                PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                            )
                            page.close()
                            bitmap
                        } catch (e: Exception) {
                            null
                        }
                    }

                    if (pageBitmap != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "PÁGINA ${pageIndex + 1} DE $pageCount",
                                color = Color.Gray,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Image(
                                bitmap = pageBitmap.asImageBitmap(),
                                contentDescription = "PDF Sheet notation page",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                                colorFilter = if (darkMode) ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
                                    -1f, 0f, 0f, 0f, 255f,
                                    0f, -1f, 0f, 0f, 255f,
                                    0f,  0f, -1f, 0f, 255f,
                                    0f,  0f, 0f, 1f, 0f
                                ))) else null
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DefaultBuiltInSheetMusicViewer(
    trackId: String,
    title: String,
    artist: String,
    darkMode: Boolean
) {
    val listState = rememberLazyListState()
    val paperBg = if (darkMode) Color(0xFF141414) else Color(0xFFFAF6EE)
    val inkColor = if (darkMode) Color.White else Color(0xFF1E1A15)
    val brandSymbol = if (darkMode) ScarletPrimary else Color(0xFFC72C41)

    val notesPage1 = remember(trackId) {
        if (trackId.contains("boliviano")) {
            listOf(
                NotationStaveData("I. INTRO (RIFF)", listOf("Em", "Am", "B7", "Em"), listOf(listOf(4, 7, 11), listOf(9, 0, 4), listOf(11, 2, 6, 9), listOf(4, 7, 11)), "Intro de guitarra acústica y flauta mansa"),
                NotationStaveData("II. ESTROFA INICIO", listOf("Em", "Am", "D", "G"), listOf(listOf(4, 7, 11), listOf(9, 0, 4), listOf(2, 6, 9), listOf(7, 11, 2)), "Me quieren agitar / Me incitan a gritar / palabras no me tocan")
            )
        } else if (trackId.contains("ligera")) {
            listOf(
                NotationStaveData("I. INTRO INTENSA", listOf("Bm", "G", "D", "A"), listOf(listOf(11, 2, 6), listOf(7, 11, 2), listOf(2, 6, 9), listOf(9, 1, 4)), "Riff legendario de guitarra eléctrica distorsionada"),
                NotationStaveData("II. VERSO 1", listOf("Bm", "G", "D", "A"), listOf(listOf(11, 2, 6), listOf(7, 11, 2), listOf(2, 6, 9), listOf(9, 1, 4)), "Ella durmió al calor de las masas / y yo desperté soñándola")
            )
        } else {
            listOf(
                NotationStaveData("I. PRELUDIO", listOf("Am", "F", "C", "G"), listOf(listOf(9, 0, 4), listOf(5, 9, 0), listOf(0, 4, 7), listOf(7, 11, 2)), "Armónicos suspendidos activos de la AP Organization"),
                NotationStaveData("II. COMPÁS GENERAL", listOf("Am", "Dm", "G", "C"), listOf(listOf(9, 0, 4), listOf(2, 5, 9), listOf(7, 11, 2), listOf(0, 4, 7)), "Sintonizador balanceado y acompañamiento")
            )
        }
    }

    val notesPage2 = remember(trackId) {
        if (trackId.contains("boliviano")) {
            listOf(
                NotationStaveData("III. ESTRIBILLO - CORO", listOf("Em", "Am", "C", "D"), listOf(listOf(4, 7, 11), listOf(9, 0, 4), listOf(0, 4, 7), listOf(2, 6, 9)), "Y yo estoy aquí, borracho y loco / y mi corazón idiota brillará"),
                NotationStaveData("IV. CODA OUTRO", listOf("Em", "C", "D", "Em"), listOf(listOf(4, 7, 11), listOf(0, 4, 7), listOf(2, 6, 9), listOf(4, 7, 11)), "Lamento boliviano que empezó y no va a terminar nunca")
            )
        } else if (trackId.contains("ligera")) {
            listOf(
                NotationStaveData("III. ESTRIBILLO MÚSICA LIGERA", listOf("Bm", "G", "D", "A"), listOf(listOf(11, 2, 6), listOf(7, 11, 2), listOf(2, 6, 9), listOf(9, 1, 4)), "De aquel amor de música ligera / Nada nos libra, nada más queda"),
                NotationStaveData("IV. RETORNO DE RIFF", listOf("Bm", "G", "D", "A"), listOf(listOf(11, 2, 6), listOf(7, 11, 2), listOf(2, 6, 9), listOf(9, 1, 4)), "Solo maestral de guitarra / ¡Gracias Totales!")
            )
        } else {
            listOf(
                NotationStaveData("III. ÁPICE ARMÓNICO", listOf("F", "G", "Am", "E7"), listOf(listOf(5, 9, 0), listOf(7, 11, 2), listOf(9, 0, 4), listOf(11, 2, 6)), "Frecuencia con compás alterado ascendente"),
                NotationStaveData("IV. CODA Y ENLACE", listOf("Dm", "G", "Am", "Am"), listOf(listOf(2, 5, 9), listOf(7, 11, 2), listOf(9, 0, 4), listOf(9, 0, 4)), "Salida balanceada de audio de alta resolución")
            )
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        item {
            SheetPaperPage(
                title = title,
                artist = artist,
                subtitle = "EDICIÓN DE INTRODUCCIÓN Y COMPASES PRINCIPALES",
                paperBg = paperBg,
                inkColor = inkColor,
                brandSymbol = brandSymbol,
                pageIndex = 1,
                pageCount = 2
            ) {
                notesPage1.forEachIndexed { i, stave ->
                    NotationStaveBlock(
                        titleText = stave.titleText,
                        chordList = stave.chordList,
                        notesBlock = stave.notesBlock,
                        inkColor = inkColor,
                        brandSymbol = brandSymbol,
                        paperBg = paperBg,
                        lyrics = stave.lyrics
                    )
                    if (i < notesPage1.lastIndex) {
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }
            }
        }

        item {
            SheetPaperPage(
                title = title,
                artist = artist,
                subtitle = "EDICIÓN DE DESARROLLO, CLÍMAX Y CODA",
                paperBg = paperBg,
                inkColor = inkColor,
                brandSymbol = brandSymbol,
                pageIndex = 2,
                pageCount = 2
            ) {
                notesPage2.forEachIndexed { i, stave ->
                    NotationStaveBlock(
                        titleText = stave.titleText,
                        chordList = stave.chordList,
                        notesBlock = stave.notesBlock,
                        inkColor = inkColor,
                        brandSymbol = brandSymbol,
                        paperBg = paperBg,
                        lyrics = stave.lyrics
                    )
                    if (i < notesPage2.lastIndex) {
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }
            }
        }
    }
}

data class NotationStaveData(
    val titleText: String,
    val chordList: List<String>,
    val notesBlock: List<List<Int>>,
    val lyrics: String
)

@Composable
fun SheetPaperPage(
    title: String,
    artist: String,
    subtitle: String,
    paperBg: Color,
    inkColor: Color,
    brandSymbol: Color,
    pageIndex: Int,
    pageCount: Int,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = paperBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, inkColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title.uppercase(),
                        color = inkColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )
                    Text(
                        text = artist.uppercase(),
                        color = inkColor.copy(alpha = 0.6f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
                Box(
                    modifier = Modifier
                        .background(brandSymbol.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .border(1.dp, brandSymbol.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "AP NOTACIÓN",
                        color = brandSymbol,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Text(
                text = subtitle.uppercase(),
                color = inkColor.copy(alpha = 0.4f),
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            HorizontalDivider(color = inkColor.copy(alpha = 0.15f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // Body content
            content()

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = inkColor.copy(alpha = 0.1f), thickness = 1.dp)

            // Footer info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "© AP Music Sheets Organiser",
                    color = inkColor.copy(alpha = 0.3f),
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "PÁG. $pageIndex / $pageCount",
                    color = inkColor.copy(alpha = 0.5f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun NotationStaveBlock(
    titleText: String,
    chordList: List<String>,
    notesBlock: List<List<Int>>,
    inkColor: Color,
    brandSymbol: Color,
    paperBg: Color,
    lyrics: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = titleText.uppercase(),
            color = brandSymbol,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
        ) {
            // Draw 5 staff lines
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val mid = h / 2f
                val lineSpacing = 6.dp.toPx()
                val topOffset = mid - (2 * lineSpacing)

                for (i in 0 until 5) {
                    val y = topOffset + i * lineSpacing
                    drawLine(
                        color = inkColor.copy(alpha = 0.2f),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1.2f
                    )
                }

                // G-Clef mockup
                val clefX = 24f
                drawCircle(
                    color = brandSymbol,
                    radius = 2.5.dp.toPx(),
                    center = Offset(clefX, mid),
                    style = Stroke(width = 1.5f)
                )
                drawLine(
                    color = brandSymbol,
                    start = Offset(clefX, mid - 14.dp.toPx()),
                    end = Offset(clefX, mid + 14.dp.toPx()),
                    strokeWidth = 2f
                )
            }

            // Placed text overlays to be beautifully readable
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 38.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                chordList.forEach { chord ->
                    Text(
                        text = chord,
                        color = brandSymbol,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Draw notes
            Canvas(modifier = Modifier.fillMaxSize().padding(start = 38.dp)) {
                val w = size.width
                val h = size.height
                val mid = h / 2f
                val lineSpacing = 6.dp.toPx()
                val topOffset = mid - (1 * lineSpacing)

                val cols = notesBlock.size
                val colWidth = w / cols

                notesBlock.forEachIndexed { colIdx, notes ->
                    val noteX = (colIdx * colWidth) + (colWidth / 2f)
                    val baselineY = topOffset + 3 * lineSpacing

                    notes.forEachIndexed { noteIdx, offset ->
                        val noteY = baselineY - (offset * (lineSpacing / 2f))

                        // Filled oval note head
                        drawOval(
                            color = inkColor,
                            topLeft = Offset(noteX - 5f, noteY - 3f),
                            size = Size(10f, 6f)
                        )
                        // Light core center inside note head
                        drawOval(
                            color = paperBg,
                            topLeft = Offset(noteX - 2f, noteY - 1.5f),
                            size = Size(4f, 3f)
                        )
                        // Note stem
                        drawLine(
                            color = inkColor,
                            start = Offset(noteX + 4.5f, noteY - 1f),
                            end = Offset(noteX + 4.5f, noteY - 18f),
                            strokeWidth = 1.2f
                        )
                    }
                }
            }
        }

        Text(
            text = "LÍRICA: \"$lyrics\"",
            color = inkColor.copy(alpha = 0.6f),
            fontSize = 8.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
        )
    }
}
