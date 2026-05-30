package com.ryanshelby.iptv

import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ryanshelby.iptv.ui.screens.HomeScreen
import com.ryanshelby.iptv.ui.screens.WorldScreen
import com.ryanshelby.iptv.ui.screens.OnboardingScreen
import com.ryanshelby.iptv.ui.screens.PlayerScreen
import com.ryanshelby.iptv.ui.screens.SplashScreen
import com.ryanshelby.iptv.ui.screens.AboutScreen
import com.ryanshelby.iptv.ui.theme.FreeIPTVTheme
import com.ryanshelby.iptv.viewmodel.MainViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val PREFS_NAME = "freeiptv_prefs"
private const val KEY_ONBOARDING_DONE = "onboarding_done"

private const val ACTION_PIP_CONTROL = "com.ryanshelby.iptv.PIP_CONTROL"
private const val EXTRA_CONTROL_TYPE = "control_type"
private const val CONTROL_PLAY_PAUSE = 1

class MainActivity : ComponentActivity() {

    private val _isInPipMode = MutableStateFlow(false)
    private val isInPipMode = _isInPipMode.asStateFlow()

    // Track whether the player is currently showing so we can auto-PiP on Home press
    private var isPlayerActive = false

    // Callback to toggle play/pause from PiP remote action or notification
    private var onPipPlayPause: (() -> Unit)? = null

    // Callback to reload the channel (Go Live from notification)
    private var onGoLive: (() -> Unit)? = null

    // Track current playing state for PiP action icon updates
    private var isCurrentlyPlaying = true

    private val mediaReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_PIP_CONTROL -> {
                    when (intent.getIntExtra(EXTRA_CONTROL_TYPE, -1)) {
                        CONTROL_PLAY_PAUSE -> onPipPlayPause?.invoke()
                    }
                }
                PlaybackService.ACTION_PLAY_PAUSE -> onPipPlayPause?.invoke()
                PlaybackService.ACTION_GO_LIVE -> onGoLive?.invoke()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register media control receiver (PiP + notification actions)
        val mediaFilter = IntentFilter().apply {
            addAction(ACTION_PIP_CONTROL)
            addAction(PlaybackService.ACTION_PLAY_PAUSE)
            addAction(PlaybackService.ACTION_GO_LIVE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(mediaReceiver, mediaFilter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(mediaReceiver, mediaFilter)
        }

        enableEdgeToEdge()
        setContent {
            FreeIPTVTheme {
                val viewModel: MainViewModel = viewModel()
                val state by viewModel.state.collectAsState()
                val pipMode by isInPipMode.collectAsState()

                val prefs = remember { getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
                val onboardingDone = remember { prefs.getBoolean(KEY_ONBOARDING_DONE, false) }
                val context = LocalContext.current

                val currentVersion = remember {
                    try {
                        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                        pInfo.versionName ?: "1.0.0"
                    } catch (e: Exception) {
                        "1.0.0"
                    }
                }

                LaunchedEffect(Unit) {
                    viewModel.checkForUpdates(currentVersion)
                }

                if (state.updateRequiredUrl != null) {
                    AlertDialog(
                        onDismissRequest = { /* Empty to prevent dismiss */ },
                        confirmButton = {
                            Button(onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(state.updateRequiredUrl))
                                    context.startActivity(intent)
                                } catch (e: Exception) {}
                            }) {
                                Text("Update Now")
                            }
                        },
                        title = { Text("Update Required") },
                        text = { Text("A new version of FreeIPTV is available. You must update to continue using the app.") },
                        properties = DialogProperties(
                            dismissOnBackPress = false,
                            dismissOnClickOutside = false
                        )
                    )
                }

                // Navigation state: splash -> onboarding -> main app
                var currentScreen by remember {
                    mutableStateOf(
                        if (onboardingDone) AppScreen.SPLASH else AppScreen.SPLASH
                    )
                }
                var showPlayer by remember { mutableStateOf(false) }

                // Sync showPlayer state to the Activity-level flag for PiP
                LaunchedEffect(showPlayer) {
                    isPlayerActive = showPlayer
                }

                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        fadeIn(tween(400)) + scaleIn(tween(400), initialScale = 0.95f) togetherWith
                            fadeOut(tween(300))
                    },
                    label = "screen_nav"
                ) { screen ->
                    when (screen) {
                        AppScreen.SPLASH -> {
                            SplashScreen(
                                onFinished = {
                                    currentScreen = if (onboardingDone) AppScreen.MAIN
                                    else AppScreen.ONBOARDING
                                }
                            )
                        }
                        AppScreen.ONBOARDING -> {
                            OnboardingScreen(
                                onFinished = {
                                    prefs.edit().putBoolean(KEY_ONBOARDING_DONE, true).apply()
                                    currentScreen = AppScreen.MAIN
                                }
                            )
                        }
                        AppScreen.MAIN -> {
                            AnimatedContent(
                                targetState = showPlayer,
                                transitionSpec = {
                                    if (targetState) {
                                        slideInHorizontally(tween(300)) { it } + fadeIn(tween(300)) togetherWith
                                            slideOutHorizontally(tween(300)) { -it } + fadeOut(tween(200))
                                    } else {
                                        slideInHorizontally(tween(300)) { -it } + fadeIn(tween(300)) togetherWith
                                            slideOutHorizontally(tween(300)) { it } + fadeOut(tween(200))
                                    }
                                },
                                label = "nav"
                            ) { isPlayer ->
                                if (isPlayer) {
                                    PlayerScreen(
                                        channel = viewModel.getCurrentChannel(),
                                        onBack = { showPlayer = false },
                                        onNext = { viewModel.nextChannel() },
                                        onPrev = { viewModel.prevChannel() },
                                        channelIndex = state.currentIndex,
                                        totalChannels = state.filteredChannels.size,
                                        isInPipMode = pipMode,
                                        onEnterPip = { enterPipMode() },
                                        onRegisterPlayPauseCallback = { callback ->
                                            onPipPlayPause = callback
                                        },
                                        onRegisterGoLiveCallback = { callback ->
                                            onGoLive = callback
                                        },
                                        onPlayingStateChanged = { playing ->
                                            isCurrentlyPlaying = playing
                                            if (_isInPipMode.value) {
                                                updatePipActions(playing)
                                            }
                                        }
                                    )
                                } else {
                                    HomeScreen(
                                        state = state,
                                        onChannelClick = { index ->
                                            viewModel.selectChannel(index)
                                            showPlayer = true
                                        },
                                        onSearch = { viewModel.search(it) },
                                        onGroupSelect = { viewModel.selectGroup(it) },
                                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                                        onOpenPlayer = { showPlayer = true },
                                        onOpenAbout = { currentScreen = AppScreen.ABOUT },
                                        onOpenWorld = { currentScreen = AppScreen.WORLD }
                                    )
                                }
                            }
                        }
                        AppScreen.WORLD -> {
                            AnimatedContent(
                                targetState = showPlayer,
                                transitionSpec = {
                                    if (targetState) {
                                        slideInHorizontally(tween(300)) { it } + fadeIn(tween(300)) togetherWith
                                            slideOutHorizontally(tween(300)) { -it } + fadeOut(tween(200))
                                    } else {
                                        slideInHorizontally(tween(300)) { -it } + fadeIn(tween(300)) togetherWith
                                            slideOutHorizontally(tween(300)) { it } + fadeOut(tween(200))
                                    }
                                },
                                label = "world_nav"
                            ) { isPlayer ->
                                if (isPlayer) {
                                    PlayerScreen(
                                        channel = viewModel.getCurrentChannel(),
                                        onBack = { showPlayer = false },
                                        onNext = { viewModel.nextChannel() },
                                        onPrev = { viewModel.prevChannel() },
                                        channelIndex = state.currentWorldIndex,
                                        totalChannels = state.filteredWorldChannels.size,
                                        isInPipMode = pipMode,
                                        onEnterPip = { enterPipMode() },
                                        onRegisterPlayPauseCallback = { callback ->
                                            onPipPlayPause = callback
                                        },
                                        onRegisterGoLiveCallback = { callback ->
                                            onGoLive = callback
                                        },
                                        onPlayingStateChanged = { playing ->
                                            isCurrentlyPlaying = playing
                                            if (_isInPipMode.value) {
                                                updatePipActions(playing)
                                            }
                                        }
                                    )
                                } else {
                                    WorldScreen(
                                        state = state,
                                        viewModel = viewModel,
                                        onChannelClick = { index ->
                                            viewModel.selectChannel(index, isWorld = true)
                                            showPlayer = true
                                        },
                                        onOpenPlayer = { showPlayer = true },
                                        onBack = { 
                                            viewModel.setIsViewingWorld(false)
                                            currentScreen = AppScreen.MAIN 
                                        }
                                    )
                                }
                            }
                        }
                        AppScreen.ABOUT -> {
                            AboutScreen(viewModel = viewModel, onBack = { currentScreen = AppScreen.MAIN })
                        }
                    }
                }
            }
        }
    }

    fun enterPipMode() {
        val builder = PictureInPictureParams.Builder()
            .setAspectRatio(Rational(16, 9))
            .setActions(buildPipActions(isCurrentlyPlaying))
        // Android 14+: enable double-tap to toggle between mini and expanded PiP (YouTube-style)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            builder.setExpandedAspectRatio(Rational(16, 9))
        }
        enterPictureInPictureMode(builder.build())
    }

    private fun updatePipActions(isPlaying: Boolean) {
        val builder = PictureInPictureParams.Builder()
            .setAspectRatio(Rational(16, 9))
            .setActions(buildPipActions(isPlaying))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            builder.setExpandedAspectRatio(Rational(16, 9))
        }
        setPictureInPictureParams(builder.build())
    }

    private fun buildPipActions(isPlaying: Boolean): List<RemoteAction> {
        val intent = Intent(ACTION_PIP_CONTROL).apply {
            putExtra(EXTRA_CONTROL_TYPE, CONTROL_PLAY_PAUSE)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this, CONTROL_PLAY_PAUSE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val iconRes = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        val title = if (isPlaying) "Pause" else "Play"
        val action = RemoteAction(
            Icon.createWithResource(this, iconRes),
            title, title, pendingIntent
        )
        return listOf(action)
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // Auto-enter PiP when user presses Home while watching video
        if (isPlayerActive) {
            enterPipMode()
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        _isInPipMode.value = isInPictureInPictureMode
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(mediaReceiver)
        } catch (_: Exception) {}
    }
}

private enum class AppScreen {
    SPLASH, ONBOARDING, MAIN, WORLD, ABOUT
}
