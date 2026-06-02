package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.ui.MediaViewModel
import com.example.ui.screens.EqualizerScreen
import com.example.ui.screens.MainTabScreen
import com.example.ui.screens.MusicPlayerScreen
import com.example.ui.screens.VideoPlayerScreen
import com.example.ui.theme.MyApplicationTheme

enum class AppScreen {
    MAIN, MUSIC_PLAYER, VIDEO_PLAYER, EQUALIZER
}

class MainActivity : ComponentActivity() {

    private val viewModel: MediaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge-to-edge support with safe area drawing in Compose
        enableEdgeToEdge()

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()

            MyApplicationTheme(darkTheme = isDarkTheme) {
                var currentScreen by remember { mutableStateOf(AppScreen.MAIN) }
                val backstack = remember { mutableStateListOf(AppScreen.MAIN) }

                fun navigateTo(screen: AppScreen) {
                    if (backstack.lastOrNull() != screen) {
                        backstack.add(screen)
                    }
                    currentScreen = screen
                }

                fun navigateBack() {
                    if (backstack.size > 1) {
                        backstack.removeAt(backstack.lastIndex)
                        currentScreen = backstack.last()
                    } else {
                        finish()
                    }
                }

                // Handle system back press
                BackHandler {
                    navigateBack()
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "screen_transition"
                        ) { targetScreen ->
                            when (targetScreen) {
                                AppScreen.MAIN -> {
                                    MainTabScreen(
                                        viewModel = viewModel,
                                        onOpenMusicPlayer = { navigateTo(AppScreen.MUSIC_PLAYER) },
                                        onOpenVideoPlayer = { _ -> navigateTo(AppScreen.VIDEO_PLAYER) },
                                        onOpenEqualizer = { navigateTo(AppScreen.EQUALIZER) }
                                    )
                                }
                                AppScreen.MUSIC_PLAYER -> {
                                    MusicPlayerScreen(
                                        viewModel = viewModel,
                                        onOpenEqualizer = { navigateTo(AppScreen.EQUALIZER) },
                                        onMinimize = { navigateBack() }
                                    )
                                }
                                AppScreen.VIDEO_PLAYER -> {
                                    VideoPlayerScreen(
                                        viewModel = viewModel,
                                        onBack = { navigateBack() }
                                    )
                                }
                                AppScreen.EQUALIZER -> {
                                    EqualizerScreen(
                                        viewModel = viewModel,
                                        onBack = { navigateBack() }
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
