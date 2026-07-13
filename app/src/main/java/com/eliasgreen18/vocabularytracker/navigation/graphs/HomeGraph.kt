package com.eliasgreen18.vocabularytracker.navigation.graphs

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.eliasgreen18.vocabularytracker.navigation.Screen
import com.eliasgreen18.vocabularytracker.ui.MainViewModel
import com.eliasgreen18.vocabularytracker.ui.home.HomeScreen

fun NavGraphBuilder.homeGraph(
    navController: NavController,
    mainViewModel: MainViewModel
) {
    composable(Screen.Home.route) {
        HomeScreen(
            onNavigateToReview = { navController.navigate(Screen.Review.route) },
            onNavigateToBooks = { navController.navigate(Screen.Books.route) },
            onNavigateToBookDetail = { bookId ->
                navController.navigate(Screen.BookDetail.createRoute(bookId))
            },
            onNavigateToPdfReader = { bookId ->
                navController.navigate(Screen.PdfReader.createRoute(bookId))
            },
            onNavigateToEpubReader = { bookId ->
                navController.navigate(Screen.EpubReader.createRoute(bookId))
            },
            onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
            onNavigateToNotifications = { navController.navigate(Screen.NotificationCenter.route) },
            onBackupClick = { mainViewModel.exportBackup() },
            onSyncClick = { mainViewModel.syncToDrive() },
            onExportCsvClick = { mainViewModel.exportToCsv() },
            onExportJsonClick = { mainViewModel.exportToJson() }
        )
    }
}
