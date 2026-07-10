package com.eliasgreen18.vocabularytracker.ui.review

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.eliasgreen18.vocabularytracker.ui.util.MainTopBar
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    onNavigateBack: () -> Unit,
    onNavigateToWordDetail: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onBackupClick: () -> Unit,
    onSyncClick: () -> Unit,
    onExportCsvClick: () -> Unit,
    onExportJsonClick: () -> Unit,
    viewModel: ReviewViewModel = hiltViewModel()
) {
    val currentWord by viewModel.currentWord.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val totalToReview by viewModel.totalToReview.collectAsState()
    val isRevealed by viewModel.isRevealed.collectAsState()
    val feedback by viewModel.lastReviewFeedback.collectAsState()

    var showFeedback by remember { mutableStateOf(false) }

    LaunchedEffect(feedback) {
        if (feedback != null) {
            showFeedback = true
            delay(1500)
            showFeedback = false
        }
    }

    Scaffold(
        topBar = {
            MainTopBar(
                title = "Daily Review",
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToNotifications = onNavigateToNotifications,
                onBackupClick = onBackupClick,
                onSyncClick = onSyncClick,
                onExportCsvClick = onExportCsvClick,
                onExportJsonClick = onExportJsonClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Session Progress Header
            if (totalToReview > 0) {
                Column(modifier = Modifier.padding(16.dp)) {
                    LinearProgressIndicator(
                        progress = { currentIndex.toFloat() / totalToReview },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${totalToReview - currentIndex} cards remaining",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                currentWord?.let { word ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Word Text (Always Visible)
                        Text(
                            text = word.wordText,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(8.dp)
                                .clickable { onNavigateToWordDetail(word.wordId) }
                        )
                        
                        Text(
                            text = "Tap word for full history",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )

                        if (!word.lastSnippet.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "\"${word.lastSnippet}\"",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(48.dp))

                        AnimatedContent(
                            targetState = isRevealed,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "reveal_content"
                        ) { revealed ->
                            if (!revealed) {
                                // UNREVEALED STATE
                                Button(
                                    onClick = { viewModel.reveal() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    shape = MaterialTheme.shapes.large
                                ) {
                                    Icon(Icons.Default.Visibility, contentDescription = null)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Reveal Translation", style = MaterialTheme.typography.titleMedium)
                                }
                            } else {
                                // REVEALED STATE
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    // Translation
                                    Text(
                                        text = word.translation ?: "No translation loaded",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Center
                                    )
                                    
                                    // IPA
                                    if (!word.ipa.isNullOrBlank()) {
                                        Text(
                                            text = word.ipa,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))

                                    // Last seen context
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        shape = MaterialTheme.shapes.medium
                                    ) {
                                        Text(
                                            text = "Last seen in:\n${word.lastContext}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }
                                    
                                    // Personal Notes (Mnemonic)
                                    if (!word.notes.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "💡 ${word.notes}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.secondary,
                                            fontWeight = FontWeight.Medium,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(48.dp))
                                    
                                    Text(
                                        text = "Did you remember?",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Button(
                                            onClick = { viewModel.onForgotten() },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                                            ),
                                            modifier = Modifier.weight(1f).height(56.dp),
                                            shape = MaterialTheme.shapes.medium
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("No")
                                        }
                                        
                                        Button(
                                            onClick = { viewModel.onRemembered() },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF4CAF50),
                                                contentColor = Color.White
                                            ),
                                            modifier = Modifier.weight(1f).height(56.dp),
                                            shape = MaterialTheme.shapes.medium
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Yes")
                                        }
                                    }
                                }
                            }
                        }
                    }
                } ?: run {
                    // Empty State
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "All caught up!",
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "You've reviewed all pending words for today.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = onNavigateBack) {
                            Text("Back to Dashboard")
                        }
                    }
                }
            }
        }

        // Transient Feedback Overlay
        Box(modifier = Modifier.fillMaxSize().padding(bottom = 120.dp), contentAlignment = Alignment.BottomCenter) {
            AnimatedVisibility(
                visible = showFeedback,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 6.dp
                ) {
                    Text(
                        text = feedback ?: "",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
