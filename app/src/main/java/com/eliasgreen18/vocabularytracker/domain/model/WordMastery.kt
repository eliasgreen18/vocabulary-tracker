package com.eliasgreen18.vocabularytracker.domain.model

enum class WordMastery(val label: String) {
    NEW("New"),
    LEARNING("Learning"),
    LEARNED("Learned");

    companion object {
        fun fromCount(count: Int): WordMastery {
            return when {
                count >= 5 -> LEARNED
                count >= 3 -> LEARNING
                else -> NEW
            }
        }
    }
}
