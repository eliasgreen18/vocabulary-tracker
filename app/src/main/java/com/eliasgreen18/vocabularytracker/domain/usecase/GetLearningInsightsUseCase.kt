package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.model.GlobalStats
import com.eliasgreen18.vocabularytracker.domain.model.LearningInsight
import com.eliasgreen18.vocabularytracker.domain.model.InsightType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetLearningInsightsUseCase @Inject constructor(
    private val getGlobalStatsUseCase: GetGlobalStatsUseCase
) {
    operator fun invoke(): Flow<List<LearningInsight>> {
        return getGlobalStatsUseCase().map { stats ->
            val insights = mutableListOf<LearningInsight>()
            
            if (stats.learnedWordsCount > 0) {
                insights.add(LearningInsight("You've mastered ${stats.learnedWordsCount} words!", InsightType.SUCCESS))
            }
            
            if (stats.translatedWordsCount > 0) {
                insights.add(LearningInsight("${stats.translatedWordsCount} words have translations available.", InsightType.INFO))
            }
            
            if (stats.newWordsCount > 10) {
                insights.add(LearningInsight("You have ${stats.newWordsCount} new words to learn. Keep reading!", InsightType.CHALLENGE))
            }
            
            if (stats.uniqueWordsCount > 100) {
                insights.add(LearningInsight("Your vocabulary has grown to over 100 words!", InsightType.SUCCESS))
            }

            insights
        }
    }
}
