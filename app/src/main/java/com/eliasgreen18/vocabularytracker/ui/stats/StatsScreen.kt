package com.eliasgreen18.vocabularytracker.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.eliasgreen18.vocabularytracker.R
import com.eliasgreen18.vocabularytracker.domain.model.StreakInfo
import com.eliasgreen18.vocabularytracker.ui.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onNavigateToReview: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToTimeline: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onBackupClick: () -> Unit,
    onSyncClick: () -> Unit,
    onExportCsvClick: () -> Unit,
    onExportJsonClick: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 32.dp, bottom = 32.dp)
                ) {
                    // Integrated Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.nav_me),
                                style = MaterialTheme.typography.headlineMedium,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            var showMenu by remember { mutableStateOf(false) }
                            Box {
                                IconButton(onClick = { showMenu = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                                }
                                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.menu_notifications)) },
                                        onClick = { showMenu = false; onNavigateToNotifications() },
                                        leadingIcon = { Icon(Icons.Default.Notifications, contentDescription = null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.menu_settings)) },
                                        onClick = { showMenu = false; onNavigateToSettings() },
                                        leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) }
                                    )
                                    HorizontalDivider()
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.menu_sync_drive)) },
                                        onClick = { showMenu = false; onSyncClick() },
                                        leadingIcon = { Icon(Icons.Default.CloudSync, contentDescription = null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.menu_export_csv)) },
                                        onClick = { showMenu = false; onExportCsvClick() },
                                        leadingIcon = { Icon(Icons.Default.TableChart, contentDescription = null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.menu_export_json)) },
                                        onClick = { showMenu = false; onExportJsonClick() },
                                        leadingIcon = { Icon(Icons.Default.Code, contentDescription = null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.menu_db_backup)) },
                                        onClick = { showMenu = false; onBackupClick() },
                                        leadingIcon = { Icon(Icons.Default.Backup, contentDescription = null) }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = stringResource(R.string.stats_learning_progress),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }

                    uiState.weeklyStats?.let { data ->
                        item { WeeklyFocusCard(data) }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(onClick = onNavigateToTimeline, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.stats_full_timeline))
                            }
                            TextButton(onClick = onNavigateToProfile, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.stats_reading_profile))
                            }
                        }
                    }

                    uiState.heatmap?.let { data ->
                        item { StreakCard(streakInfo = data.streakInfo) }
                        item {
                            AnalyticsSection(
                                title = stringResource(R.string.stats_activity_intensity),
                                description = stringResource(R.string.stats_activity_desc)
                            ) {
                                ActivityHeatmapChart(activity = data.dailyActivity)
                            }
                        }
                    }

                    uiState.mastery?.let { data ->
                        item {
                            AnalyticsSection(
                                title = stringResource(R.string.stats_vocab_mastery),
                                description = stringResource(R.string.stats_vocab_mastery_desc)
                            ) {
                                MasteryDistributionChart(
                                    newCount = data.distribution.newCount,
                                    learningCount = data.distribution.learningCount,
                                    learnedCount = data.distribution.learnedCount
                                )
                            }
                        }
                        
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    StatColumn(label = stringResource(R.string.stats_accuracy), value = "${data.recallAccuracy}%")
                                    VerticalDivider(modifier = Modifier.height(32.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                                    StatColumn(label = stringResource(R.string.stats_forgotten), value = data.forgottenWordsCount)
                                    VerticalDivider(modifier = Modifier.height(32.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                                    StatColumn(label = stringResource(R.string.stats_velocity), value = "+${data.learningVelocity}")
                                }
                            }
                        }
                    }

                    if (uiState.dueCount > 0) {
                        item {
                            ReviewQueueCard(count = uiState.dueCount, onStartReview = onNavigateToReview)
                        }
                    }

                    uiState.globalStats?.let { stats ->
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                GlobalStatsCard(stats = stats)
                                SrsStatsCard(stats = stats)
                            }
                        }
                    }

                    uiState.analytics?.let { data ->
                        data.mostChallengingChapter?.let { chapter ->
                            item {
                                ToughAuthorCard(
                                    authorName = chapter.bookTitle,
                                    wordCount = chapter.uniqueWordsCount
                                )
                            }
                        }

                        if (data.wordsPerBook.isNotEmpty()) {
                            item {
                                AnalyticsSection(
                                    title = stringResource(R.string.stats_words_per_book),
                                    description = stringResource(R.string.stats_words_per_book_desc)
                                ) {
                                    SimpleBarChart(
                                        items = data.wordsPerBook.map { ChartItem(it.bookTitle, it.uniqueWordsCount) },
                                        barColor = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        if (data.wordsPerChapter.isNotEmpty()) {
                            item {
                                AnalyticsSection(
                                    title = stringResource(R.string.stats_challenging_chapters),
                                    description = stringResource(R.string.stats_challenging_chapters_desc)
                                ) {
                                    SimpleBarChart(
                                        items = data.wordsPerChapter.map { ChartItem("${it.bookTitle} - ${it.chapterNumber}", it.uniqueWordsCount) },
                                        barColor = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }
                    
                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }
        }
    }
}

@Composable
fun WeeklyFocusCard(stats: com.eliasgreen18.vocabularytracker.domain.usecase.WeeklyReadingStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = stringResource(R.string.stats_weekly_focus), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text(text = stringResource(R.string.stats_min_goal, stats.totalMinutesThisWeek, stats.weeklyGoalMinutes), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                }
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(48.dp)) {
                    CircularProgressIndicator(
                        progress = { stats.completionPercentage / 100f },
                        strokeWidth = 6.dp,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(text = "${stats.completionPercentage}%", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth().height(60.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                val last7Days = (0..6).map { java.time.LocalDate.now().minusDays(it.toLong()) }.reversed()
                last7Days.forEach { date ->
                    val mins = stats.dailyMinutes[date] ?: 0
                    val heightFactor = if (stats.weeklyGoalMinutes > 0) (mins.toFloat() / (stats.weeklyGoalMinutes / 7f)).coerceAtMost(1f) else 0f
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .fillMaxHeight(0.1f + (heightFactor * 0.9f))
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(if (heightFactor >= 1f) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = date.dayOfWeek.name.take(1), style = MaterialTheme.typography.labelSmall, fontSize = 8.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ToughAuthorCard(authorName: String, wordCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = stringResource(R.string.stats_challenging_author), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                Text(text = authorName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                Text(text = stringResource(R.string.stats_challenging_chapter_desc, wordCount), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun StreakCard(streakInfo: StreakInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = if (streakInfo.currentStreak > 0) Color(0xFFFF9800) else MaterialTheme.colorScheme.outline, modifier = Modifier.size(32.dp))
                Text(text = streakInfo.currentStreak.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(text = stringResource(R.string.stats_current_streak), style = MaterialTheme.typography.labelSmall)
            }
            
            VerticalDivider(modifier = Modifier.height(48.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(32.dp))
                Text(text = streakInfo.longestStreak.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(text = stringResource(R.string.stats_all_time_best), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun AnalyticsSection(title: String, description: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun ReviewQueueCard(count: Int, onStartReview: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = stringResource(R.string.daily_review), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onTertiaryContainer)
                Text(text = stringResource(R.string.stats_words_ready_review, count), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onTertiaryContainer)
            }
            Button(onClick = onStartReview) { Text(stringResource(R.string.btn_start)) }
        }
    }
}

@Composable
fun GlobalStatsCard(stats: com.eliasgreen18.vocabularytracker.domain.model.GlobalStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = stringResource(R.string.stats_vocab_power), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatColumn(label = stringResource(R.string.stats_unique), value = stats.uniqueWordsCount)
                StatColumn(label = stringResource(R.string.stats_total), value = stats.totalOccurrencesCount)
                StatColumn(label = stringResource(R.string.stats_learned), value = stats.learnedWordsCount)
                StatColumn(label = stringResource(R.string.stats_words_per_hour), value = stats.wordsPerHour)
            }
        }
    }
}

@Composable
fun SrsStatsCard(stats: com.eliasgreen18.vocabularytracker.domain.model.GlobalStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            StatColumn(label = stringResource(R.string.stats_memory_score), value = stats.totalReviewsDone)
            VerticalDivider(modifier = Modifier.height(40.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.2f))
            StatColumn(label = stringResource(R.string.stats_recall_accuracy), value = "${stats.recallAccuracy}%")
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
