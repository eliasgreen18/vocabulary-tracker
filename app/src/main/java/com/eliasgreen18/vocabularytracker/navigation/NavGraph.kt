package com.eliasgreen18.vocabularytracker.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.eliasgreen18.vocabularytracker.ui.home.HomeScreen
import com.eliasgreen18.vocabularytracker.ui.books.BooksScreen
import com.eliasgreen18.vocabularytracker.ui.books.BookViewModel
import com.eliasgreen18.vocabularytracker.ui.books.BookDetailScreen
import com.eliasgreen18.vocabularytracker.ui.session.ActiveSessionScreen
import com.eliasgreen18.vocabularytracker.ui.session.ChapterDetailScreen
import com.eliasgreen18.vocabularytracker.ui.words.FocusWordsScreen
import com.eliasgreen18.vocabularytracker.ui.words.SearchWordsScreen
import com.eliasgreen18.vocabularytracker.ui.words.WordDetailScreen
import com.eliasgreen18.vocabularytracker.ui.review.ReviewScreen

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

@Composable
fun VocabularyNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToBooks = {
                    navController.navigate(Screen.Books.route)
                },
                onNavigateToSearch = {
                    navController.navigate(Screen.Search.route)
                },
                onNavigateToFocusWords = {
                    navController.navigate(Screen.FocusWords.route)
                },
                onNavigateToReview = {
                    navController.navigate(Screen.Review.route)
                },
                onContinueSession = { sessionId ->
                    navController.navigate(Screen.ActiveSession.createRoute(sessionId))
                },
                onBookClick = { book ->
                    navController.navigate(Screen.BookDetail.createRoute(book.id))
                }
            )
        }
        composable(Screen.Books.route) {
            BooksScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToSession = { sessionId ->
                    navController.navigate(Screen.ActiveSession.createRoute(sessionId))
                }
            )
        }
        composable(
            route = Screen.BookDetail.route,
            arguments = listOf(navArgument("bookId") { type = NavType.LongType })
        ) {
            BookDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSession = { sessionId ->
                    navController.navigate(Screen.ActiveSession.createRoute(sessionId))
                },
                onNavigateToChapterDetail = { chapterId ->
                    navController.navigate(Screen.ChapterDetail.createRoute(chapterId))
                },
                onNavigateToWordDetail = { wordId ->
                    navController.navigate(Screen.WordDetail.createRoute(wordId))
                }
            )
        }
        composable(
            route = Screen.ActiveSession.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) {
            ActiveSessionScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToWordDetail = { wordId ->
                    navController.navigate(Screen.WordDetail.createRoute(wordId))
                }
            )
        }
        composable(
            route = Screen.ChapterDetail.route,
            arguments = listOf(navArgument("chapterId") { type = NavType.LongType })
        ) {
            ChapterDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToWordDetail = { wordId ->
                    navController.navigate(Screen.WordDetail.createRoute(wordId))
                }
            )
        }
        composable(Screen.Search.route) {
            SearchWordsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToWordDetail = { wordId ->
                    navController.navigate(Screen.WordDetail.createRoute(wordId))
                }
            )
        }
        composable(Screen.FocusWords.route) {
            FocusWordsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToWordDetail = { wordId ->
                    navController.navigate(Screen.WordDetail.createRoute(wordId))
                }
            )
        }
        composable(Screen.Review.route) {
            ReviewScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = Screen.WordDetail.route,
            arguments = listOf(navArgument("wordId") { type = NavType.LongType })
        ) {
            WordDetailScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
