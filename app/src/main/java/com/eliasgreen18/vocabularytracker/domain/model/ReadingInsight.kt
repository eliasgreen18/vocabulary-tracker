package com.eliasgreen18.vocabularytracker.domain.model

sealed class ReadingInsight {
    abstract val text: String
    abstract val type: InsightType

    data class ToughAuthor(
        val authorName: String,
        val wordCount: Int,
        override val text: String,
        override val type: InsightType = InsightType.CHALLENGE
    ) : ReadingInsight()

    data class LearningMilestone(
        val milestoneValue: Int,
        override val text: String,
        override val type: InsightType = InsightType.SUCCESS
    ) : ReadingInsight()

    data class GeneralInfo(
        override val text: String,
        override val type: InsightType = InsightType.INFO
    ) : ReadingInsight()
}
