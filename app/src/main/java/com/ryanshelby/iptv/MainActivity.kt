package com.ryanshelby.iptv

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import com.ryanshelby.iptv.ui.screens.OnboardingScreen
import com.ryanshelby.iptv.ui.screens.PlayerScreen
import com.ryanshelby.iptv.ui.screens.SplashScreen
import com.ryanshelby.iptv.ui.screens.AboutScreen
import com.ryanshelby.iptv.ui.theme.FreeIPTVTheme
import com.ryanshelby.iptv.viewmodel.MainViewModel

private const val PREFS_NAME = "freeiptv_prefs"
private const val KEY_ONBOARDING_DONE = "onboarding_done"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FreeIPTVTheme {
                val viewModel: MainViewModel = viewModel()
                val state by viewModel.state.collectAsState()

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
                                        totalChannels = state.filteredChannels.size
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
                                        onOpenAbout = { currentScreen = AppScreen.ABOUT }
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
}

private enum class AppScreen {
    SPLASH, ONBOARDING, MAIN, ABOUT
}
