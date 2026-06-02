package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.example.ui.MediaViewModel
import com.example.ui.theme.CrimsonBorder
import com.example.ui.theme.ScarletPrimary
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

    val track = currentTrack ?: return

    var currentViewTab by remember { mutableStateOf(PlayerSubView.LYRICS) } // LYRICS or SHEET_MUSIC
    val lyrics = remember(track.id) { SyncedLyricsAndMusicSheet.getLyricsForTrack(track.id) }
    val sheetMusic = remember(track.id) { SyncedLyricsAndMusicSheet.getSheetMusicForTrack(track.id) }

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
                    contentDescription = "Minimizar",
                    tint = Color.White
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "REPRODUCIENDO",
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
                    contentDescription = "Ecualizador",
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
                // Real-time sheet music (Partituras) staff drawing view
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
                            fontSize = 16.sp
                        )
                        Text(
                            text = activeBeat?.instruction ?: "Ritmo Estándar",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

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

                            // Draw the 5 standard lines of sheet music staff (Contornos de lineales blancos)
                            val lineSpacing = 16.dp.toPx()
                            val topOffset = mid - (2 * lineSpacing)

                            for (i in 0 until 5) {
                                val y = topOffset + i * lineSpacing
                                drawLine(
                                    color = Color.White.copy(alpha = 0.4f),
                                    start = Offset(20f, y),
                                    end = Offset(w - 20f, y),
                                    strokeWidth = 2f
                                )
                            }

                            // Draw treble clef G symbol mockup
                            val clefX = 50f
                            val clefY = mid
                            drawCircle(
                                color = ScarletPrimary,
                                radius = 6.dp.toPx(),
                                center = Offset(clefX, clefY),
                                style = Stroke(width = 3f)
                            )
                            drawLine(
                                color = ScarletPrimary,
                                start = Offset(clefX, clefY - 35.dp.toPx()),
                                end = Offset(clefX, clefY + 35.dp.toPx()),
                                strokeWidth = 4f
                            )

                            // Draw dynamic moving musical notes of current beats
                            val activeNotes = activeBeat?.notes ?: listOf(4, 7, 11)
                            val drawOffsetY = topOffset + 4 * lineSpacing // baseline G

                            activeNotes.forEachIndexed { noteIdx, midiOffset ->
                                // Calculate drawing heights on staff lines
                                val noteY = drawOffsetY - ((midiOffset * (lineSpacing / 2f)))
                                val noteX = 150f + (noteIdx * 70f)

                                // Draw glowing scarlet note head
                                drawOval(
                                    color = ScarletPrimary,
                                    topLeft = Offset(noteX - 10f, noteY - 7f),
                                    size = Size(20f, 14f)
                                )
                                drawOval(
                                    color = Color.White,
                                    topLeft = Offset(noteX - 4f, noteY - 3f),
                                    size = Size(8f, 6f)
                                )
                                // Stem
                                drawLine(
                                    color = Color.White,
                                    start = Offset(noteX + 9f, noteY - 1f),
                                    end = Offset(noteX + 9f, noteY - 45f),
                                    strokeWidth = 3f
                                )
                                // Beam
                                if (noteIdx == activeNotes.lastIndex) {
                                    drawLine(
                                        color = Color.White,
                                        start = Offset(150f + 9f, drawOffsetY - ((activeNotes[0] * (lineSpacing / 2f))) - 45f),
                                        end = Offset(noteX + 9f, noteY - 45f),
                                        strokeWidth = 4f
                                    )
                                }
                            }

                            // Moving beat measure bar line
                            val progressPercent = if (duration > 0) position.toFloat() / duration.toFloat() else 0f
                            val scannerX = 120f + (progressPercent * (w - 250f)).coerceAtLeast(0f)
                            drawLine(
                                color = Color.White.copy(alpha = 0.8f),
                                start = Offset(scannerX, topOffset - 10f),
                                end = Offset(scannerX, topOffset + 4 * lineSpacing + 10f),
                                strokeWidth = 2f
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

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
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                // Mock string dots
                                Canvas(modifier = Modifier.size(24.dp)) {
                                    for (dot in 0..3) {
                                        drawCircle(
                                            color = if (isThisChord) CrimsonBorder else Color.Black,
                                            radius = 3f,
                                            center = Offset(size.width / 4f * dot + 2f, size.height / 2f)
                                        )
                                    }
                                }
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
