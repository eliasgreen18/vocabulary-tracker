package com.eliasgreen18.vocabularytracker.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.eliasgreen18.vocabularytracker.navigation.Screen
import com.eliasgreen18.vocabularytracker.navigation.graphs.*
import com.eliasgreen18.vocabularytracker.ui.theme.VocabularyTrackerTheme

sealed class NavItem {
    data class ScreenItem(val screen: Screen, val label: String, val icon: ImageVector) : NavItem()
    object ActionItem : NavItem()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContainer(
    viewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val activeSessionId by viewModel.activeSessionId.collectAsState()
    val appTheme by viewModel.appTheme.collectAsState()
    val backupFile by viewModel.backupFile.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showSyncErrorDialog by remember { mutableStateOf<String?>(null) }
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Global Backup Handler
    LaunchedEffect(backupFile) {
        backupFile?.let { file ->
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/octet-stream"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Export Backup..."))
            viewModel.clearBackupState()
        }
    }

    // Sync Status Handler
    LaunchedEffect(syncStatus) {
        syncStatus?.let {
            if (it.startsWith("Sync Failed:")) {
                showSyncErrorDialog = it
            } else {
                snackbarHostState.showSnackbar(it)
            }
            viewModel.clearStatus()
        }
    }

    val items = listOf(
        NavItem.ScreenItem(Screen.Home, "Home", Icons.Default.Home),
        NavItem.ScreenItem(Screen.Books, "Books", Icons.Default.Book),
        NavItem.ActionItem,
        NavItem.ScreenItem(Screen.Search, "Words", Icons.Default.Translate),
        NavItem.ScreenItem(Screen.Stats, "Me", Icons.Default.Person)
    )

    VocabularyTrackerTheme(appTheme = appTheme) {
        val currentRoute = currentDestination?.route ?: ""
        val showBottomBar = !currentRoute.startsWith("epub_reader") && 
                           !currentRoute.startsWith("pdf_reader") && 
                           !currentRoute.startsWith("session/") &&
                           !currentRoute.startsWith("chapter/") &&
                           !currentRoute.startsWith("book/") &&
                           !currentRoute.startsWith("word/") &&
                           !currentRoute.contains("book_completion") &&
                           !currentRoute.contains("review") &&
                           !currentRoute.contains("camera_scanner")

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp
                    ) {
                        items.forEach { item ->
                            val isSelected = when (item) {
                                is NavItem.ScreenItem -> {
                                    when (item.screen.route) {
                                        Screen.Home.route -> currentRoute == Screen.Home.route
                                        Screen.Search.route -> currentRoute == Screen.Search.route || currentRoute.startsWith("word/")
                                        Screen.Books.route -> currentRoute == Screen.Books.route || currentRoute.startsWith("book/")
                                        Screen.Stats.route -> currentRoute == Screen.Stats.route
                                        else -> currentDestination?.hierarchy?.any { it.route == item.screen.route } == true
                                    }
                                }
                                is NavItem.ActionItem -> currentRoute.startsWith("session/")
                            }

                            NavigationBarItem(
                                icon = { 
                                    when (item) {
                                        is NavItem.ScreenItem -> Icon(item.icon, contentDescription = item.label)
                                        is NavItem.ActionItem -> Icon(
                                            Icons.AutoMirrored.Filled.MenuBook, 
                                            contentDescription = "Read",
                                            tint = if (activeSessionId != null) MaterialTheme.colorScheme.primary else LocalContentColor.current
                                        )
                                    }
                                },
                                label = { Text(if (item is NavItem.ScreenItem) item.label else "Read") },
                                selected = isSelected,
                                onClick = {
                                    when (item) {
                                        is NavItem.ScreenItem -> {
                                            val targetRoute = item.screen.route
                                            if (currentRoute == targetRoute) {
                                                navController.navigate(targetRoute) {
                                                    popUpTo(targetRoute) { inclusive = true }
                                                }
                                            } else {
                                                navController.navigate(targetRoute) {
                                                    popUpTo(navController.graph.findStartDestination().id) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        }
                                        is NavItem.ActionItem -> {
                                            activeSessionId?.let { id ->
                                                navController.navigate(Screen.ActiveSession.createRoute(id)) {
                                                    launchSingleTop = true
                                                }
                                            } ?: run {
                                                navController.navigate(Screen.Books.route)
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Box(modifier = Modifier.padding(bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else 0.dp)) {
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        enterTransition = { EnterTransition.None },
                        exitTransition = { ExitTransition.None },
                        popEnterTransition = { EnterTransition.None },
                        popExitTransition = { ExitTransition.None }
                    ) {
                        homeGraph(navController, viewModel)
                        libraryGraph(navController, viewModel)
                        wordsGraph(navController, viewModel)
                        statsGraph(navController, viewModel)
                        settingsGraph(navController)
                    }
                }
            }
        }
        
        if (showSyncErrorDialog != null) {
            AlertDialog(
                onDismissRequest = { showSyncErrorDialog = null },
                title = { Text("Cloud Sync Error") },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text(
                            text = showSyncErrorDialog!!,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = { showSyncErrorDialog = null }) {
                        Text("Dismiss")
                    }
                }
            )
        }
    }
}
