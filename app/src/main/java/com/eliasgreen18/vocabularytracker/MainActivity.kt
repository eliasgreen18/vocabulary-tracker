package com.eliasgreen18.vocabularytracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.eliasgreen18.vocabularytracker.data.local.dictionary.DictionaryInitializer
import com.eliasgreen18.vocabularytracker.domain.usecase.ProcessPendingTranslationsUseCase
import com.eliasgreen18.vocabularytracker.ui.MainContainer
import com.eliasgreen18.vocabularytracker.ui.theme.VocabularyTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var dictionaryInitializer: DictionaryInitializer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        lifecycleScope.launch {
            dictionaryInitializer.initializeIfNeeded(applicationContext)
        }

        // Auto-translation processing disabled for stabilization
        /*
        lifecycleScope.launch {
            processPendingTranslationsUseCase()
        }
        */

        enableEdgeToEdge()
        setContent {
            VocabularyTrackerTheme {
                MainContainer()
            }
        }
    }
}
