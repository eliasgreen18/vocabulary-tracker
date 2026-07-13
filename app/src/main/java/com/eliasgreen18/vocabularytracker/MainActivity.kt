package com.eliasgreen18.vocabularytracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.eliasgreen18.vocabularytracker.data.util.SpeechService
import com.eliasgreen18.vocabularytracker.ui.MainContainer
import com.eliasgreen18.vocabularytracker.ui.components.SplashScreen
import com.eliasgreen18.vocabularytracker.ui.theme.VocabularyTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var speechService: SpeechService

    override fun onCreate(savedInstanceState: Bundle?) {
        val systemSplash = installSplashScreen()
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            var showBrandingSplash by remember { mutableStateOf(true) }
            
            // Branding remains visible for a very short time to bridge the gap
            // and show version info without feeling like a "second screen"
            LaunchedEffect(Unit) {
                delay(600) // Optimal for transition
                showBrandingSplash = false
            }
            
            VocabularyTrackerTheme {
                if (showBrandingSplash) {
                    SplashScreen(versionName = BuildConfig.VERSION_NAME)
                } else {
                    MainContainer()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechService.shutdown()
    }
}
