package com.eliasgreen18.vocabularytracker.domain.model

data class LearningInsight(
    val text: String,
    val type: InsightType = InsightType.INFO
)

enum class InsightType {
    INFO,
    SUCCESS,
    CHALLENGE
}
