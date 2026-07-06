package com.eliasgreen18.vocabularytracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.eliasgreen18.vocabularytracker.domain.usecase.ProcessPendingTranslationsUseCase
import com.eliasgreen18.vocabularytracker.navigation.VocabularyNavGraph
import com.eliasgreen18.vocabularytracker.ui.theme.VocabularyTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var processPendingTranslationsUseCase: ProcessPendingTranslationsUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        lifecycleScope.launch {
            processPendingTranslationsUseCase()
        }

        enableEdgeToEdge()
        setContent {
            VocabularyTrackerTheme {
                VocabularyNavGraph()
            }
        }
    }
}
