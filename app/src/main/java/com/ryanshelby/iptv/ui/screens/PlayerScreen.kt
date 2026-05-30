package com.ryanshelby.iptv.ui.screens

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.ryanshelby.iptv.PlaybackService
import com.ryanshelby.iptv.R
import com.ryanshelby.iptv.data.Channel
import com.ryanshelby.iptv.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun PlayerScreen(
    channel: Channel?,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    channelIndex: Int,
    totalChannels: Int,
    isInPipMode: Boolean = false,
    onEnterPip: () -> Unit = {},
    onRegisterPlayPauseCallback: (((() -> Unit)?) -> Unit)? = null,
    onRegisterGoLiveCallback: (((() -> Unit)?) -> Unit)? = null,
    onPlayingStateChanged: ((Boolean) -> Unit)? = null
) {
    val context = LocalContext.current
    val activity = context as? Activity

    // Keep an always-updated reference to channel for use in callbacks
    val currentChannel by rememberUpdatedState(channel)

    // Force landscape only when NOT in PiP
    DisposableEffect(isInPipMode) {
        if (!isInPipMode) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    // Load channel
    LaunchedEffect(channel?.url) {
        channel?.url?.let { url ->
            exoPlayer.setMediaItem(MediaItem.fromUri(url))
            exoPlayer.prepare()
            exoPlayer.play()
        }
    }

    // Stop service and release player on dispose
    DisposableEffect(Unit) {
        onDispose {
            PlaybackService.stop(context)
            exoPlayer.release()
        }
    }

    // Player state
    var isPlaying by remember { mutableStateOf(true) }
    var showControls by remember { mutableStateOf(true) }
    var isLocked by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableFloatStateOf(1f) }
    var showSpeedMenu by remember { mutableStateOf(false) }
    var volumeLevel by remember { mutableFloatStateOf(0.7f) }
    var brightnessLevel by remember { mutableFloatStateOf(0.5f) }
    var showVolumeIndicator by remember { mutableStateOf(false) }
    var showBrightnessIndicator by remember { mutableStateOf(false) }
    var seekIndicator by remember { mutableStateOf<String?>(null) }
    var aspectMode by remember { mutableIntStateOf(0) }
    val aspectModes = listOf("Fit", "Fill", "Zoom")

    // Register play/pause callback for PiP remote actions and notification
    LaunchedEffect(Unit) {
        onRegisterPlayPauseCallback?.invoke {
            if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
        }
    }

    // Register Go Live callback — reloads the stream to get back to real-time
    LaunchedEffect(Unit) {
        onRegisterGoLiveCallback?.invoke {
            currentChannel?.url?.let { url ->
                exoPlayer.setMediaItem(MediaItem.fromUri(url))
                exoPlayer.prepare()
                exoPlayer.play()
            }
        }
    }

    // Request notification permission (Android 13+)
    val notifPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* granted or not, we still try to show notification */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notifPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Start/update notification service when channel loads or changes
    LaunchedEffect(channel?.url, channel?.name) {
        channel?.let {
            PlaybackService.start(context, it.name, it.group, true)
        }
    }

    // Auto-hide controls (disabled in PiP mode)
    LaunchedEffect(showControls, isInPipMode) {
        if (showControls && !isLocked && !isInPipMode) {
            delay(4000)
            showControls = false
        }
    }

    // Hide controls when entering PiP
    LaunchedEffect(isInPipMode) {
        if (isInPipMode) {
            showControls = false
        }
    }

    // Listen for player state changes + update notification
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
                onPlayingStateChanged?.invoke(playing)
                // Update notification with current play/pause state
                currentChannel?.let {
                    PlaybackService.start(context, it.name, it.group, playing)
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose { exoPlayer.removeListener(listener) }
    }

    // Audio manager for volume
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Video view
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                view.resizeMode = when (aspectMode) {
                    1 -> androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    2 -> androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL
                    else -> androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
            }
        )

        // Everything below is hidden in PiP mode for a clean mini player
        if (!isInPipMode) {
            // Watermark branding — semi-transparent logo in top-right corner
            Image(
                painter = painterResource(R.drawable.watermark_logo),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 12.dp, end = 12.dp)
                    .size(28.dp)
                    .alpha(0.12f),
                contentScale = ContentScale.Fit
            )

            // Gesture layer
            Box(
                modifier = Modifier.fillMaxSize()
                    .pointerInput(isLocked) {
                        if (isLocked) {
                            detectTapGestures { showControls = !showControls }
                            return@pointerInput
                        }
                        detectTapGestures(
                            onTap = { showControls = !showControls },
                            onDoubleTap = { offset ->
                                val half = size.width / 2
                                if (offset.x < half) {
                                    exoPlayer.seekBack()
                                    seekIndicator = "-10s"
                                } else {
                                    exoPlayer.seekForward()
                                    seekIndicator = "+10s"
                                }
                            }
                        )
                    }
                    .pointerInput(isLocked) {
                        if (isLocked) return@pointerInput
                        detectVerticalDragGestures { change, dragAmount ->
                            val x = change.position.x
                            val half = size.width / 2f
                            val sensitivity = 0.005f
                            if (x > half) {
                                // Right side: Volume
                                volumeLevel = (volumeLevel - dragAmount * sensitivity).coerceIn(0f, 1f)
                                audioManager.setStreamVolume(
                                    AudioManager.STREAM_MUSIC,
                                    (volumeLevel * maxVolume).toInt(),
                                    0
                                )
                                showVolumeIndicator = true
                            } else {
                                // Left side: Brightness
                                brightnessLevel = (brightnessLevel - dragAmount * sensitivity).coerceIn(0f, 1f)
                                activity?.window?.attributes = activity?.window?.attributes?.apply {
                                    screenBrightness = brightnessLevel
                                }
                                showBrightnessIndicator = true
                            }
                        }
                    }
            )

            // Seek indicator
            AnimatedVisibility(
                visible = seekIndicator != null,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                LaunchedEffect(seekIndicator) {
                    delay(700)
                    seekIndicator = null
                }
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.7f)
                ) {
                    Text(
                        seekIndicator ?: "",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp
                    )
                }
            }

            // Volume indicator
            GestureIndicator(
                visible = showVolumeIndicator,
                icon = Icons.Default.VolumeUp,
                level = volumeLevel,
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 24.dp),
                onHide = { showVolumeIndicator = false }
            )

            // Brightness indicator
            GestureIndicator(
                visible = showBrightnessIndicator,
                icon = Icons.Default.LightMode,
                level = brightnessLevel,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 24.dp),
                onHide = { showBrightnessIndicator = false }
            )

            // Controls overlay
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(300))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Black.copy(0.6f), Color.Transparent, Color.Transparent, Color.Black.copy(0.7f))
                            )
                        )
                ) {
                    if (isLocked) {
                        // Locked - show only unlock button
                        IconButton(
                            onClick = { isLocked = false },
                            modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp)
                        ) {
                            Icon(Icons.Default.LockOpen, null, tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                    } else {
                        // Top bar
                        Row(
                            modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter)
                                .padding(top = 8.dp, start = 8.dp, end = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                            }
                            Column(Modifier.weight(1f).padding(horizontal = 8.dp)) {
                                Text(
                                    channel?.name ?: "", color = Color.White,
                                    fontWeight = FontWeight.SemiBold, fontSize = 16.sp,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    "${channelIndex + 1} / $totalChannels",
                                    color = Color.White.copy(0.6f), fontSize = 12.sp
                                )
                            }
                            // PiP button
                            IconButton(onClick = onEnterPip) {
                                Icon(Icons.Default.PictureInPicture, "Mini Player", tint = Color.White)
                            }
                            // Lock
                            IconButton(onClick = { isLocked = true }) {
                                Icon(Icons.Default.Lock, null, tint = Color.White)
                            }
                            // Aspect
                            TextButton(onClick = { aspectMode = (aspectMode + 1) % 3 }) {
                                Text(aspectModes[aspectMode], color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            // Speed
                            Box {
                                TextButton(onClick = { showSpeedMenu = true }) {
                                    Text("${playbackSpeed}x", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                DropdownMenu(expanded = showSpeedMenu, onDismissRequest = { showSpeedMenu = false }) {
                                    listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f).forEach { speed ->
                                        DropdownMenuItem(
                                            text = { Text("${speed}x") },
                                            onClick = {
                                                playbackSpeed = speed
                                                exoPlayer.setPlaybackSpeed(speed)
                                                showSpeedMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Center controls
                        Row(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalArrangement = Arrangement.spacedBy(32.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Previous
                            IconButton(onClick = onPrev, Modifier.size(48.dp)) {
                                Icon(Icons.Default.SkipPrevious, null, Modifier.size(36.dp), tint = Color.White)
                            }
                            // Rewind
                            IconButton(onClick = { exoPlayer.seekBack() }, Modifier.size(48.dp)) {
                                Icon(Icons.Default.Replay10, null, Modifier.size(32.dp), tint = Color.White)
                            }
                            // Play/Pause
                            FilledIconButton(
                                onClick = { if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play() },
                                modifier = Modifier.size(64.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Purple40)
                            ) {
                                Icon(
                                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    null, Modifier.size(36.dp), tint = Color.White
                                )
                            }
                            // Forward
                            IconButton(onClick = { exoPlayer.seekForward() }, Modifier.size(48.dp)) {
                                Icon(Icons.Default.Forward10, null, Modifier.size(32.dp), tint = Color.White)
                            }
                            // Next
                            IconButton(onClick = onNext, Modifier.size(48.dp)) {
                                Icon(Icons.Default.SkipNext, null, Modifier.size(36.dp), tint = Color.White)
                            }
                        }

                        // Bottom bar
                        Row(
                            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
                                .padding(bottom = 12.dp, start = 16.dp, end = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(8.dp).clip(CircleShape).background(LiveRed))
                                Spacer(Modifier.width(6.dp))
                                Text("LIVE", color = LiveRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Text(
                                channel?.group ?: "", color = Color.White.copy(0.5f), fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GestureIndicator(
    visible: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    level: Float,
    modifier: Modifier,
    onHide: () -> Unit
) {
    AnimatedVisibility(
        visible = visible, enter = fadeIn(), exit = fadeOut(),
        modifier = modifier
    ) {
        LaunchedEffect(level) { delay(1200); onHide() }
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.Black.copy(alpha = 0.65f),
            modifier = Modifier.width(40.dp)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier.height(100.dp).width(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(0.3f))
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .fillMaxHeight(level)
                            .align(Alignment.BottomCenter)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Purple40)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text("${(level * 100).toInt()}%", color = Color.White, fontSize = 10.sp)
            }
        }
    }
}
