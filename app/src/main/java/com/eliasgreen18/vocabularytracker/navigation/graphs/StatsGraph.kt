package com.eliasgreen18.vocabularytracker.navigation.graphs

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.eliasgreen18.vocabularytracker.navigation.Screen
import com.eliasgreen18.vocabularytracker.ui.MainViewModel
import com.eliasgreen18.vocabularytracker.ui.stats.GlobalTimelineScreen
import com.eliasgreen18.vocabularytracker.ui.stats.ReadingProfileScreen
import com.eliasgreen18.vocabularytracker.ui.stats.StatsScreen

fun NavGraphBuilder.statsGraph(
    navController: NavController,
    mainViewModel: MainViewModel
) {
    composable(Screen.Stats.route) {
        StatsScreen(
            onNavigateToReview = {
                navController.navigate(Screen.Review.route)
            },
            onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
            onNavigateToNotifications = { navController.navigate(Screen.NotificationCenter.route) },
            onNavigateToTimeline = { navController.navigate(Screen.GlobalTimeline.route) },
            onNavigateToProfile = { navController.navigate(Screen.ReadingProfile.route) },
            onBackupClick = { mainViewModel.exportBackup() },
            onSyncClick = { mainViewModel.syncToDrive() },
            onExportCsvClick = { mainViewModel.exportToCsv() },
            onExportJsonClick = { mainViewModel.exportToJson() }
        )
    }

    composable(Screen.GlobalTimeline.route) {
        GlobalTimelineScreen(onNavigateBack = { navController.popBackStack() })
    }

    composable(Screen.ReadingProfile.route) {
        ReadingProfileScreen(onNavigateBack = { navController.popBackStack() })
    }
}
