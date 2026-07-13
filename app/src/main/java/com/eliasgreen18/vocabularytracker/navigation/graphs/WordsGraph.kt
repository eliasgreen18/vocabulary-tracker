package com.eliasgreen18.vocabularytracker.navigation.graphs

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.eliasgreen18.vocabularytracker.navigation.Screen
import com.eliasgreen18.vocabularytracker.ui.MainViewModel
import com.eliasgreen18.vocabularytracker.ui.review.ReviewScreen
import com.eliasgreen18.vocabularytracker.ui.session.ActiveSessionScreen
import com.eliasgreen18.vocabularytracker.ui.words.WordDetailScreen
import com.eliasgreen18.vocabularytracker.ui.words.WordsScreen
import com.eliasgreen18.vocabularytracker.ui.scanner.CameraScannerScreen

fun NavGraphBuilder.wordsGraph(
    navController: NavController,
    mainViewModel: MainViewModel
) {
    composable(Screen.Search.route) {
        WordsScreen(
            onNavigateToWordDetail = { wordId ->
                navController.navigate(Screen.WordDetail.createRoute(wordId))
            },
            onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
            onNavigateToNotifications = { navController.navigate(Screen.NotificationCenter.route) },
            onBackupClick = { mainViewModel.exportBackup() },
            onSyncClick = { mainViewModel.syncToDrive() },
            onExportCsvClick = { mainViewModel.exportToCsv() },
            onExportJsonClick = { mainViewModel.exportToJson() },
            onExportAnkiClick = { words -> mainViewModel.exportToAnki(words) }
        )
    }

    composable(Screen.Review.route) {
        ReviewScreen(
            onNavigateBack = {
                navController.popBackStack()
            },
            onNavigateToWordDetail = { wordId ->
                navController.navigate(Screen.WordDetail.createRoute(wordId))
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
        route = Screen.WordDetail.route,
        arguments = listOf(navArgument("wordId") { type = NavType.LongType })
    ) {
        WordDetailScreen(
            onNavigateBack = { navController.popBackStack() },
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
            onNavigateBack = { navController.popBackStack() },
            onNavigateToWordDetail = { wordId ->
                navController.navigate(Screen.WordDetail.createRoute(wordId))
            },
            onNavigateToScanner = {
                navController.navigate(Screen.CameraScanner.route)
            }
        )
    }

    composable(Screen.CameraScanner.route) {
        CameraScannerScreen(
            onNavigateBack = { navController.popBackStack() },
            onTextSelected = { text ->
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("scanned_text", text)
                navController.popBackStack()
            }
        )
    }
}
