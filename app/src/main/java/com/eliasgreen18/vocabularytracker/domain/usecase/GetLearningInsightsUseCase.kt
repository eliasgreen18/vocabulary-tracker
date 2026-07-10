package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.model.LearningInsight
import com.eliasgreen18.vocabularytracker.domain.model.InsightType
import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetLearningInsightsUseCase @Inject constructor(
    private val repository: WordRepository,
    private val getGlobalStatsUseCase: GetGlobalStatsUseCase
) {
    operator fun invoke(): Flow<List<LearningInsight>> {
        return combine(
            getGlobalStatsUseCase(),
            repository.getAuthorStats()
        ) { stats, authorStats ->
            val insights = mutableListOf<LearningInsight>()
            
            // 1. Milestones
            if (stats.uniqueWordsCount >= 1000) {
                insights.add(LearningInsight("Linguistic Giant: You've collected over 1,000 unique words!", InsightType.SUCCESS))
            } else if (stats.uniqueWordsCount >= 500) {
                insights.add(LearningInsight("Vocab Master: 500 words reached! Your dictionary is growing fast.", InsightType.SUCCESS))
            } else if (stats.uniqueWordsCount >= 100) {
                insights.add(LearningInsight("Century Club: You've reached your first 100 words!", InsightType.SUCCESS))
            }

            // 2. Author Intelligence
            val toughest = authorStats.firstOrNull()
            if (toughest != null && toughest.uniqueWordsCount > 20) {
                insights.add(
                    LearningInsight(
                        "${toughest.author} is currently your most challenging author (${toughest.uniqueWordsCount} unique words).",
                        InsightType.CHALLENGE
                    )
                )
            }

            // 3. Learning Progress
            if (stats.learnedWordsCount > 0) {
                val percent = (stats.learnedWordsCount * 100) / stats.uniqueWordsCount
                if (percent > 50) {
                    insights.add(LearningInsight("Great efficiency! More than half of your vocabulary is already learned.", InsightType.SUCCESS))
                }
            }

            // 4. Activity
            if (stats.newWordsCount > 30) {
                insights.add(LearningInsight("Backlog Alert: You have ${stats.newWordsCount} new words waiting to be studied.", InsightType.CHALLENGE))
            }

            insights
        }
    }
}
