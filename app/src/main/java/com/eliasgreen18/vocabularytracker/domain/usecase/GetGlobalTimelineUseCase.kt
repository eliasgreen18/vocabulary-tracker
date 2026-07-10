package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.model.GlobalTimeline
import com.eliasgreen18.vocabularytracker.domain.model.MonthlySummary
import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

class GetGlobalTimelineUseCase @Inject constructor(
    private val repository: WordRepository
) {
    operator fun invoke(): Flow<GlobalTimeline> {
        return repository.getWordDiscoveries().map { discoveries ->
            val grouped = discoveries.groupBy { 
                YearMonth.from(it.firstSeenAt.atZone(ZoneId.systemDefault()))
            }
            
            val summaries = grouped.entries
                .sortedByDescending { it.key }
                .map { (month, list) ->
                    MonthlySummary(
                        yearMonth = month,
                        uniqueWordsCount = list.size,
                        sampleWords = list.take(5).map { it.wordText }
                    )
                }

            GlobalTimeline(months = summaries)
        }
    }
}
