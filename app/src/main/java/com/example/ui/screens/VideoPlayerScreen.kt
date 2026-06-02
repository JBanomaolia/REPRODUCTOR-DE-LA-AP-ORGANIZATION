package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView
import com.example.data.MediaItemEntity
import com.example.playback.PlaybackLoopMode
import com.example.playback.VideoRotationMode
import com.example.ui.MediaViewModel
import com.example.ui.theme.CrimsonBorder
import com.example.ui.theme.ScarletPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun VideoPlayerScreen(
    viewModel: MediaViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentTrack by viewModel.playbackManager.currentTrack.collectAsState()
    val isPlaying by viewModel.playbackManager.isPlaying.collectAsState()
    val position by viewModel.playbackManager.currentPosition.collectAsState()
    val duration by viewModel.playbackManager.currentDuration.collectAsState()
    val loopMode by viewModel.playbackManager.loopMode.collectAsState()

    val rotationMode by viewModel.playbackManager.videoRotation.collectAsState()
    val availableAudioTracks by viewModel.playbackManager.availableAudioTracks.collectAsState()
    val selectedAudioIndex by viewModel.playbackManager.selectedAudioTrackIndex.collectAsState()

    val track = currentTrack ?: return

    var showControls by remember { mutableStateOf(true) }
    var showTrackChooser by remember { mutableStateOf(false) }

    // Toggle indicator effect overlays for double taps
    var leftRippleVisible by remember { mutableStateOf(false) }
    var rightRippleVisible by remember { mutableStateOf(false) }
    var centerPlayPauseVisible by remember { mutableStateOf(false) }

    // Auto-dismiss controls after 3 seconds of inactivity
    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying) {
            delay(4000L)
            showControls = false
        }
    }

    // Apply Screen rotation dynamically based on rotationMode config
    LaunchedEffect(rotationMode) {
        val activity = context as? Activity ?: return@LaunchedEffect
        when (rotationMode) {
            VideoRotationMode.AUTO -> {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
            }
            VideoRotationMode.MANUAL_PORTRAIT -> {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
            VideoRotationMode.MANUAL_LANDSCAPE -> {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
        }
    }

    // Reset rotation orientation when leaving player screen
    DisposableEffect(Unit) {
        onDispose {
            val activity = context as? Activity
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // ACTUAL ExoPlayer View (for videos)
        if (!track.id.startsWith("demo_")) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = viewModel.playbackManager.player
                        useController = false // We draw our custom Scarlet controls instead!
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Gorgeous Cinematic Neon Fallback Canvas Loop (renders high contrast tech grids)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F0F0F)),
                contentAlignment = Alignment.Center
            ) {
                // Background visual indicator
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.MovieFilter,
                        contentDescription = null,
                        tint = ScarletPrimary.copy(alpha = 0.15f),
                        modifier = Modifier.size(200.dp)
                    )
                    Text(
                        text = "REPRODUCIENDO CONTENIDO CINEMÁTICO\nAP ORGANIZATION MEDIA SYSTEM",
                        color = CrimsonBorder.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Ambient glow circle
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .border(1.dp, CrimsonBorder, CircleShape)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Videocam else Icons.Default.VideocamOff,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }

        // THREE-COLUMN DETECTOR FOR GESTURES (Double Taps to Seek / Single Taps to Show UI)
        Row(modifier = Modifier.fillMaxSize()) {
            // LEFT COLUMN (30% WIDTH) -> Double Tap to Rewind 10s
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.3f)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { showControls = !showControls },
                            onDoubleTap = {
                                viewModel.playbackManager.rewind10s()
                                leftRippleVisible = true
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (leftRippleVisible) {
                    LaunchedEffect(Unit) {
                        delay(600)
                        leftRippleVisible = false
                    }
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            .border(1.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.Replay10, contentDescription = null, tint = Color.White)
                            Text("-10s", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // CENTER COLUMN (40% WIDTH) -> Double Tap to Pause/Play
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.4f)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { showControls = !showControls },
                            onDoubleTap = {
                                viewModel.playbackManager.togglePlayPause()
                                centerPlayPauseVisible = true
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (centerPlayPauseVisible) {
                    LaunchedEffect(Unit) {
                        delay(600)
                        centerPlayPauseVisible = false
                    }
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .background(ScarletPrimary.copy(alpha = 0.3f), CircleShape)
                            .border(2.dp, CrimsonBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            // RIGHT COLUMN (30% WIDTH) -> Double Tap to Fast Forward 10s
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.3f)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { showControls = !showControls },
                            onDoubleTap = {
                                viewModel.playbackManager.fastForward10s()
                                rightRippleVisible = true
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (rightRippleVisible) {
                    LaunchedEffect(Unit) {
                        delay(600)
                        rightRippleVisible = false
                    }
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            .border(1.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.Forward10, contentDescription = null, tint = Color.White)
                            Text("+10s", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // TOP CONTROLS OVERLAY (White base outlines & letters as requested)
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(animationSpec = tween(300)) + slideInVertically(),
            exit = fadeOut(animationSpec = tween(300)) + slideOutVertically(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("exit_video_player")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = track.title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1
                        )
                        Text(
                            text = "Carpeta: ${track.folder}",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Audio track selector button (Analyze multiple tracks)
                    IconButton(
                        onClick = { showTrackChooser = true },
                        modifier = Modifier
                            .border(1.dp, CrimsonBorder, RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.5f))
                            .testTag("audio_track_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Audiotrack,
                            contentDescription = "Pistas de Audio",
                            tint = ScarletPrimary
                        )
                    }

                    // Rotation lock options toggle
                    IconButton(
                        onClick = {
                            val nextRot = when (rotationMode) {
                                VideoRotationMode.AUTO -> VideoRotationMode.MANUAL_LANDSCAPE
                                VideoRotationMode.MANUAL_LANDSCAPE -> VideoRotationMode.MANUAL_PORTRAIT
                                VideoRotationMode.MANUAL_PORTRAIT -> VideoRotationMode.AUTO
                            }
                            viewModel.playbackManager.setVideoRotation(nextRot)
                        },
                        modifier = Modifier
                            .border(1.dp, Color.White, RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.5f))
                            .testTag("rotation_toggle")
                    ) {
                        Icon(
                            imageVector = when (rotationMode) {
                                VideoRotationMode.AUTO -> Icons.Default.ScreenRotation
                                VideoRotationMode.MANUAL_LANDSCAPE -> Icons.Default.ScreenLockLandscape
                                VideoRotationMode.MANUAL_PORTRAIT -> Icons.Default.ScreenLockPortrait
                            },
                            contentDescription = "Giro de Pantalla",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // BOTTOM CONTROLS OVERLAY
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(animationSpec = tween(300)) + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut(animationSpec = tween(300)) + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                // Seeking progress Slider bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTime(position),
                        color = Color.White,
                        fontSize = 11.sp
                    )
                    Slider(
                        value = position.toFloat(),
                        onValueChange = { viewModel.playbackManager.seekTo(it.toLong()) },
                        valueRange = 0f..duration.toFloat().coerceAtLeast(1f),
                        colors = SliderDefaults.colors(
                            activeTrackColor = ScarletPrimary,
                            inactiveTrackColor = Color.DarkGray,
                            thumbColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )
                    Text(
                        text = formatTime(duration),
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Core video buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Loop style: repeat single video OR repeat full folder list
                    IconButton(
                        onClick = {
                            val nextMode = when (loopMode) {
                                PlaybackLoopMode.ONE -> PlaybackLoopMode.FOLDER
                                else -> PlaybackLoopMode.ONE
                            }
                            viewModel.playbackManager.setLoopMode(nextMode)
                        }
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (loopMode == PlaybackLoopMode.ONE) Icons.Default.RepeatOne else Icons.Default.FolderOpen,
                                contentDescription = "Repetir",
                                tint = ScarletPrimary
                            )
                            Text(
                                text = if (loopMode == PlaybackLoopMode.ONE) "Bucle Inf" else "Lista Carp",
                                fontSize = 8.sp,
                                color = Color.White
                            )
                        }
                    }

                    // Seek backward 10s back
                    IconButton(onClick = { viewModel.playbackManager.rewind10s() }) {
                        Icon(imageVector = Icons.Default.Replay10, contentDescription = "Retroceder 10s", tint = Color.White)
                    }

                    // Large Scarlett Play/Pause center toggle
                    FloatingActionButton(
                        onClick = { viewModel.playbackManager.togglePlayPause() },
                        containerColor = ScarletPrimary,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(54.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Pausar/Reproducir"
                        )
                    }

                    // Seek forward 10s forward
                    IconButton(onClick = { viewModel.playbackManager.fastForward10s() }) {
                        Icon(imageVector = Icons.Default.Forward10, contentDescription = "Adelantar 10s", tint = Color.White)
                    }

                    // Information label ad-free
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                        Text("SIN COMPRAS", fontSize = 8.sp, color = Color.White)
                    }
                }
            }
        }

        // Multiple Audio Track source Selector bottom sheet dialog
        if (showTrackChooser) {
            AlertDialog(
                onDismissRequest = { showTrackChooser = false },
                confirmButton = {
                    TextButton(onClick = { showTrackChooser = false }) {
                        Text("LISTO", color = ScarletPrimary, fontWeight = FontWeight.Bold)
                    }
                },
                title = {
                    Text(
                        "FUENTES DE AUDIO ORIGINAL",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Se han detectado ${availableAudioTracks.size} pistas integradas en este archivo de video. Seleccione su pista de procedencia preferida:",
                            fontSize = 12.sp,
                            color = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        availableAudioTracks.forEachIndexed { index, trackName ->
                            val isSelected = index == selectedAudioIndex
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) ScarletPrimary.copy(alpha = 0.2f) else Color.Transparent)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) ScarletPrimary else Color.White.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        viewModel.playbackManager.selectAudioTrackSource(index)
                                    }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = null,
                                        tint = if (isSelected) ScarletPrimary else Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = trackName,
                                        color = if (isSelected) Color.White else Color.LightGray,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Check",
                                        tint = ScarletPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
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
