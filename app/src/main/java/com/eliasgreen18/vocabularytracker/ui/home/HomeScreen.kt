package com.eliasgreen18.vocabularytracker.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.eliasgreen18.vocabularytracker.domain.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToBooks: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToFocusWords: () -> Unit,
    onNavigateToReview: () -> Unit,
    onContinueSession: (Long) -> Unit,
    onBookClick: (Book) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val dashboard by viewModel.dashboardState.collectAsState()
    val progress by viewModel.learningProgress.collectAsState()
    val globalStats by viewModel.globalStats.collectAsState()
    val insights by viewModel.insights.collectAsState()
    val reviewCount by viewModel.reviewQueueCount.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vocabulary Tracker") },
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Default.Search, contentDescription = "Search Words")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(
                        text = "Welcome back!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    progress?.let { p ->
                        Text(
                            text = "You discovered ${p.wordsAddedToday} new words today!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Global Analytics Card
            item {
                globalStats?.let { stats ->
                    GlobalStatsCard(stats = stats)
                }
            }

            // Learning Insights Marquee
            if (insights.isNotEmpty()) {
                item {
                    InsightsCarousel(insights = insights)
                }
            }

            // Daily Review Section
            if (reviewCount > 0) {
                item {
                    ReviewQueueCard(count = reviewCount, onStartReview = onNavigateToReview)
                }
            }

            // Featured Active Session
            item {
                val activeSession = dashboard?.activeSessions?.firstOrNull()
                if (activeSession != null) {
                    ActiveSessionFeaturedCard(
                        info = activeSession,
                        onContinue = { onContinueSession(activeSession.session.id) }
                    )
                } else {
                    NoActiveSessionCard(onStart = onNavigateToBooks)
                }
            }

            // Quick Access to Focus Words
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onNavigateToFocusWords)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "Focus Words", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }
                }
            }

            // Recent Books
            val recentBooks = dashboard?.recentBooks ?: emptyList()
            if (recentBooks.isNotEmpty()) {
                item {
                    Text(
                        text = "Recent Books",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                items(recentBooks) { book ->
                    RecentBookItem(book = book, onClick = { onBookClick(book) })
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun GlobalStatsCard(stats: GlobalStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Overall Progress", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatColumn(label = "Unique", value = stats.uniqueWordsCount)
                StatColumn(label = "Total", value = stats.totalOccurrencesCount)
                StatColumn(label = "Learned", value = stats.learnedWordsCount)
                StatColumn(label = "Translated", value = stats.translatedWordsCount)
            }
        }
    }
}

@Composable
fun StatColumn(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun InsightsCarousel(insights: List<LearningInsight>) {
    var currentIndex by remember { mutableStateOf(0) }
    
    // Simple auto-advance for demo
    LaunchedEffect(Unit) {
        while(true) {
            kotlinx.coroutines.delay(5000)
            currentIndex = (currentIndex + 1) % insights.size
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val insight = insights[currentIndex]
            val (icon, color) = when(insight.type) {
                InsightType.SUCCESS -> Icons.Default.EmojiEvents to Color(0xFF4CAF50)
                InsightType.CHALLENGE -> Icons.Default.Psychology to Color(0xFFFF9800)
                InsightType.INFO -> Icons.Default.Info to MaterialTheme.colorScheme.primary
            }
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = insight.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun ReviewQueueCard(count: Int, onStartReview: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Daily Review",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = "$count words ready to review",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            Button(onClick = onStartReview) {
                Text("Start")
            }
        }
    }
}

@Composable
fun ActiveSessionFeaturedCard(
    info: ActiveSessionInfo,
    onContinue: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Currently Reading",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            info.book?.let { book ->
                Text(text = book.title, style = MaterialTheme.typography.headlineSmall)
                Text(text = "by ${book.author}", style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = info.chapter.displayTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
                Text("Continue Reading")
            }
        }
    }
}

@Composable
fun NoActiveSessionCard(onStart: () -> Unit) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No active reading session",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onStart) {
                Text("Start Reading")
            }
        }
    }
}

@Composable
fun RecentBookItem(book: Book, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick).padding(vertical = 4.dp),
        headlineContent = { Text(book.title) },
        supportingContent = { Text("${book.author} • ${book.language}") },
        leadingContent = { Icon(Icons.Default.Book, contentDescription = null) }
    )
}
