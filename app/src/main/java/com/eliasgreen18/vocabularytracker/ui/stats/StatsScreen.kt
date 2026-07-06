package com.eliasgreen18.vocabularytracker.ui.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.eliasgreen18.vocabularytracker.domain.model.GlobalStats
import com.eliasgreen18.vocabularytracker.domain.model.InsightType
import com.eliasgreen18.vocabularytracker.domain.model.LearningInsight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onNavigateToReview: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val globalStats by viewModel.globalStats.collectAsState()
    val insights by viewModel.insights.collectAsState()
    val dueCount by viewModel.dueCount.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Learning Dashboard") })
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
                Text(
                    text = "Your Progress",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (dueCount > 0) {
                item {
                    ReviewQueueCard(count = dueCount, onStartReview = onNavigateToReview)
                }
            }

            item {
                globalStats?.let { stats ->
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        GlobalStatsCard(stats = stats)
                        SrsStatsCard(stats = stats)
                    }
                }
            }

            if (insights.isNotEmpty()) {
                item {
                    Text(text = "Insights", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                items(insights.size) { index ->
                    InsightItem(insight = insights[index])
                }
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
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
fun GlobalStatsCard(stats: GlobalStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Vocabulary Power", style = MaterialTheme.typography.titleMedium)
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
fun SrsStatsCard(stats: GlobalStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatColumn(label = "Memory Score", value = stats.totalReviewsDone)
            VerticalDivider(modifier = Modifier.height(40.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.2f))
            StatColumn(label = "Recall Accuracy", value = "${stats.recallAccuracy}%")
        }
    }
}

@Composable
fun StatColumn(label: String, value: Any) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(text = label.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun InsightItem(insight: LearningInsight) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val (icon, color) = when(insight.type) {
                InsightType.SUCCESS -> Icons.Default.EmojiEvents to Color(0xFF4CAF50)
                InsightType.CHALLENGE -> Icons.Default.Psychology to Color(0xFFFF9800)
                InsightType.INFO -> Icons.Default.Info to MaterialTheme.colorScheme.primary
            }
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = insight.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
