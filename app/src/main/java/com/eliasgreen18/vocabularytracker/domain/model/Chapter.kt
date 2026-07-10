package com.eliasgreen18.vocabularytracker.domain.model

data class Chapter(
    val id: Long = 0,
    val bookId: Long,
    val number: String, // Changed from Int to String
    val title: String? = null
) {
    val displayTitle: String
        get() {
            val prefix = if (number.all { it.isDigit() }) "Chapter $number" else number
            return if (title.isNullOrBlank()) {
                prefix
            } else {
                "$prefix: $title"
            }
        }
}
