package com.eliasgreen18.vocabularytracker.domain.model

import java.time.YearMonth

data class GlobalTimeline(
    val months: List<MonthlySummary>
)

data class MonthlySummary(
    val yearMonth: YearMonth,
    val uniqueWordsCount: Int,
    val sampleWords: List<String>,
    val growthPercentage: Int? = null
)
