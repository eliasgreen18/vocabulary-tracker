package com.eliasgreen18.vocabularytracker.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Books : Screen("books")
    object BookDetail : Screen("book/{bookId}") {
        fun createRoute(bookId: Long) = "book/$bookId"
    }
    object ActiveSession : Screen("session/{sessionId}") {
        fun createRoute(sessionId: Long) = "session/$sessionId"
    }
    object ChapterDetail : Screen("chapter/{chapterId}") {
        fun createRoute(chapterId: Long) = "chapter/$chapterId"
    }
    object Search : Screen("search")
    object FocusWords : Screen("focus_words")
    object Review : Screen("review")
    object WordDetail : Screen("word/{wordId}") {
        fun createRoute(wordId: Long) = "word/$wordId"
    }
}
