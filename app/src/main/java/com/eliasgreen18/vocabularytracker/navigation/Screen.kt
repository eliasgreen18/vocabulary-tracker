package com.eliasgreen18.vocabularytracker.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Stats : Screen("stats")
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
    object NotificationCenter : Screen("notifications")
    object Settings : Screen("settings")
    object GlobalTimeline : Screen("global_timeline")
    object ReadingProfile : Screen("reading_profile")
    object BookCompletion : Screen("book_completion/{bookId}") {
        fun createRoute(bookId: Long) = "book_completion/$bookId"
    }
    object CameraScanner : Screen("camera_scanner")
    object PdfReader : Screen("pdf_reader/{bookId}") {
        fun createRoute(bookId: Long) = "pdf_reader/$bookId"
    }
    object EpubReader : Screen("epub_reader/{bookId}") {
        fun createRoute(bookId: Long) = "epub_reader/$bookId"
    }
    object BookStats : Screen("book_stats/{bookId}") {
        fun createRoute(bookId: Long) = "book_stats/$bookId"
    }
}
