package com.eliasgreen18.vocabularytracker.ui.util

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

@Composable
fun SimpleBarChart(
    items: List<ChartItem>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary
) {
    if (items.isEmpty()) return

    val maxValue = items.maxOf { it.value }.toFloat()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items.forEach { item ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = item.value.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = barColor
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(if (maxValue > 0) item.value.toFloat() / maxValue else 0f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(barColor)
                    )
                }
            }
        }
    }
}

@Composable
fun MasteryDistributionChart(
    newCount: Int,
    learningCount: Int,
    learnedCount: Int,
    modifier: Modifier = Modifier
) {
    val total = (newCount + learningCount + learnedCount).toFloat()
    if (total == 0f) return

    val newWeight = if (newCount > 0) newCount / total else 0f
    val learningWeight = if (learningCount > 0) learningCount / total else 0f
    val learnedWeight = if (learnedCount > 0) learnedCount / total else 0f

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            if (newWeight > 0) {
                Box(
                    modifier = Modifier
                        .weight(newWeight)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
            if (learningWeight > 0) {
                Box(
                    modifier = Modifier
                        .weight(learningWeight)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
            if (learnedWeight > 0) {
                Box(
                    modifier = Modifier
                        .weight(learnedWeight)
                        .fillMaxHeight()
                        .background(Color(0xFF4CAF50))
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MasteryLegendItem("New", newCount, MaterialTheme.colorScheme.outlineVariant)
            MasteryLegendItem("Learning", learningCount, MaterialTheme.colorScheme.primary)
            MasteryLegendItem("Learned", learnedCount, Color(0xFF4CAF50))
        }
    }
}

@Composable
private fun MasteryLegendItem(label: String, count: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$label: $count",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
fun ActivityHeatmapChart(
    activity: Map<LocalDate, Int>,
    modifier: Modifier = Modifier,
    daysToShow: Int = 84 // 12 weeks
) {
    val today = LocalDate.now()
    val startDate = today.minusDays(daysToShow.toLong() - 1)
    
    // Group into weeks (7 days each)
    val weeks = (0 until (daysToShow / 7)).map { weekIndex ->
        (0 until 7).map { dayIndex ->
            startDate.plusDays((weekIndex * 7 + dayIndex).toLong())
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            weeks.forEach { week ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    week.forEach { date ->
                        val count = activity[date] ?: 0
                        val color = when {
                            count == 0 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            count < 3 -> Color(0xFFC6E48B)
                            count < 7 -> Color(0xFF7BC96F)
                            count < 15 -> Color(0xFF239A3B)
                            else -> Color(0xFF196127)
                        }
                        
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(color)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Month labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val months = weeks.map { it.first().month }.distinct()
            months.forEach { month ->
                Text(
                    text = month.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

data class ChartItem(
    val label: String,
    val value: Int
)
