package com.eliasgreen18.vocabularytracker.navigation.graphs

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.eliasgreen18.vocabularytracker.navigation.Screen
import com.eliasgreen18.vocabularytracker.ui.MainViewModel
import com.eliasgreen18.vocabularytracker.ui.books.BookCompletionScreen
import com.eliasgreen18.vocabularytracker.ui.books.BookDetailScreen
import com.eliasgreen18.vocabularytracker.ui.books.BookStatsScreen
import com.eliasgreen18.vocabularytracker.ui.books.BooksScreen
import com.eliasgreen18.vocabularytracker.ui.reader.EpubReaderScreen
import com.eliasgreen18.vocabularytracker.ui.reader.PdfReaderScreen
import com.eliasgreen18.vocabularytracker.ui.session.ChapterDetailScreen

fun NavGraphBuilder.libraryGraph(
    navController: NavController,
    mainViewModel: MainViewModel
) {
    composable(Screen.Books.route) {
        BooksScreen(
            onNavigateToBookDetail = { bookId ->
                navController.navigate(Screen.BookDetail.createRoute(bookId))
            },
            onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
            onNavigateToNotifications = { navController.navigate(Screen.NotificationCenter.route) },
            onBackupClick = { mainViewModel.exportBackup() },
            onSyncClick = { mainViewModel.syncToDrive() },
            onExportCsvClick = { mainViewModel.exportToCsv() },
            onExportJsonClick = { mainViewModel.exportToJson() }
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
            onNavigateToPdfReader = { bookId ->
                navController.navigate(Screen.PdfReader.createRoute(bookId))
            },
            onNavigateToEpubReader = { bookId ->
                navController.navigate(Screen.EpubReader.createRoute(bookId))
            },
            onNavigateToStats = { bookId ->
                navController.navigate(Screen.BookStats.createRoute(bookId))
            }
        )
    }

    composable(
        route = Screen.BookStats.route,
        arguments = listOf(navArgument("bookId") { type = NavType.LongType })
    ) {
        BookStatsScreen(onNavigateBack = { navController.popBackStack() })
    }

    composable(
        route = Screen.PdfReader.route,
        arguments = listOf(navArgument("bookId") { type = NavType.LongType })
    ) {
        PdfReaderScreen(onNavigateBack = { navController.popBackStack() })
    }

    composable(
        route = Screen.EpubReader.route,
        arguments = listOf(navArgument("bookId") { type = NavType.LongType })
    ) {
        EpubReaderScreen(onNavigateBack = { navController.popBackStack() })
    }

    composable(
        route = Screen.BookCompletion.route,
        arguments = listOf(navArgument("bookId") { type = NavType.LongType })
    ) {
        BookCompletionScreen(onNavigateBack = { 
            navController.popBackStack(Screen.Books.route, false) 
        })
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
}
