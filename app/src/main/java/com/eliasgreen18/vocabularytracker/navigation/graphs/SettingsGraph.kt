package com.eliasgreen18.vocabularytracker.navigation.graphs

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.eliasgreen18.vocabularytracker.navigation.Screen
import com.eliasgreen18.vocabularytracker.ui.notifications.NotificationCenterScreen
import com.eliasgreen18.vocabularytracker.ui.settings.SettingsScreen

fun NavGraphBuilder.settingsGraph(
    navController: NavController
) {
    composable(Screen.Settings.route) {
        SettingsScreen(onNavigateBack = { navController.popBackStack() })
    }
    
    composable(Screen.NotificationCenter.route) {
        NotificationCenterScreen(onNavigateBack = { navController.popBackStack() })
    }
}
