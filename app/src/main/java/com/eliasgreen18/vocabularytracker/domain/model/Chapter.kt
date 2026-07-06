package com.eliasgreen18.vocabularytracker.domain.model

data class Chapter(
    val id: Long = 0,
    val bookId: Long,
    val number: Int,
    val title: String? = null
) {
    val displayTitle: String
        get() = if (title.isNullOrBlank()) {
            "Chapter $number"
        } else {
            "Chapter $number: $title"
        }
}
