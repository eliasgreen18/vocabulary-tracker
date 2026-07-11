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
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.eliasgreen18.vocabularytracker.navigation.Screen
import com.eliasgreen18.vocabularytracker.ui.books.BookDetailScreen
import com.eliasgreen18.vocabularytracker.ui.books.BooksScreen
import com.eliasgreen18.vocabularytracker.ui.review.ReviewScreen
import com.eliasgreen18.vocabularytracker.ui.session.ActiveSessionScreen
import com.eliasgreen18.vocabularytracker.ui.session.ChapterDetailScreen
import com.eliasgreen18.vocabularytracker.ui.stats.StatsScreen
import com.eliasgreen18.vocabularytracker.ui.words.WordsScreen
import com.eliasgreen18.vocabularytracker.ui.words.WordDetailScreen
import com.eliasgreen18.vocabularytracker.ui.settings.SettingsScreen
import com.eliasgreen18.vocabularytracker.ui.notifications.NotificationCenterScreen
import com.eliasgreen18.vocabularytracker.ui.stats.GlobalTimelineScreen
import com.eliasgreen18.vocabularytracker.ui.stats.ReadingProfileScreen
import com.eliasgreen18.vocabularytracker.ui.books.BookCompletionScreen
import com.eliasgreen18.vocabularytracker.ui.scanner.CameraScannerScreen
import com.eliasgreen18.vocabularytracker.ui.reader.PdfReaderScreen
import com.eliasgreen18.vocabularytracker.ui.reader.EpubReaderScreen
import com.eliasgreen18.vocabularytracker.ui.home.HomeScreen
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
        NavItem.ScreenItem(Screen.Stats, "Stats", Icons.Default.Leaderboard)
    )

    VocabularyTrackerTheme(appTheme = appTheme) {
        val showBottomBar = currentDestination?.route?.let { route ->
            !route.startsWith("epub_reader") && !route.startsWith("pdf_reader")
        } ?: true

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        items.forEach { item ->
                            val isSelected = when (item) {
                                is NavItem.ScreenItem -> {
                                    val currentRoute = currentDestination?.route ?: ""
                                    when (item.screen.route) {
                                        Screen.Home.route -> currentRoute == Screen.Home.route
                                        Screen.Search.route -> currentRoute == Screen.Search.route || currentRoute.startsWith("word/")
                                        Screen.Books.route -> currentRoute == Screen.Books.route || currentRoute.startsWith("book/") || currentRoute.startsWith("chapter/")
                                        Screen.Stats.route -> currentRoute == Screen.Stats.route
                                        else -> currentDestination?.hierarchy?.any { it.route == item.screen.route } == true
                                    }
                                }
                                is NavItem.ActionItem -> currentDestination?.hierarchy?.any { it.route?.startsWith("session/") == true } == true
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
                                label = { 
                                    Text(if (item is NavItem.ScreenItem) item.label else "Read") 
                                },
                                selected = isSelected,
                                onClick = {
                                    when (item) {
                                        is NavItem.ScreenItem -> {
                                            if (isSelected) {
                                                navController.navigate(item.screen.route) {
                                                    popUpTo(item.screen.route) { inclusive = true }
                                                }
                                            } else {
                                                navController.navigate(item.screen.route) {
                                                    popUpTo(navController.graph.findStartDestination().id) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = false 
                                                }
                                            }
                                        }
                                        is NavItem.ActionItem -> {
                                            activeSessionId?.let { id ->
                                                navController.navigate(Screen.ActiveSession.createRoute(id))
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                color = MaterialTheme.colorScheme.background
            ) {
                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route,
                    enterTransition = { EnterTransition.None },
                    exitTransition = { ExitTransition.None },
                    popEnterTransition = { EnterTransition.None },
                    popExitTransition = { ExitTransition.None }
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
                            onBackupClick = { viewModel.exportBackup() },
                            onSyncClick = { viewModel.syncToDrive() },
                            onExportCsvClick = { viewModel.exportToCsv() },
                            onExportJsonClick = { viewModel.exportToJson() }
                        )
                    }
                    composable(Screen.Search.route) {
                        WordsScreen(
                            onNavigateToWordDetail = { wordId ->
                                navController.navigate(Screen.WordDetail.createRoute(wordId))
                            },
                            onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                            onNavigateToNotifications = { navController.navigate(Screen.NotificationCenter.route) },
                            onBackupClick = { viewModel.exportBackup() },
                            onSyncClick = { viewModel.syncToDrive() },
                            onExportCsvClick = { viewModel.exportToCsv() },
                            onExportJsonClick = { viewModel.exportToJson() },
                            onExportAnkiClick = { words -> viewModel.exportToAnki(words) },
                            onExportQuizletClick = { words -> viewModel.exportToQuizlet(words) }
                        )
                    }
                    composable(Screen.Books.route) {
                        BooksScreen(
                            onNavigateToBookDetail = { bookId ->
                                navController.navigate(Screen.BookDetail.createRoute(bookId))
                            },
                            onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                            onNavigateToNotifications = { navController.navigate(Screen.NotificationCenter.route) },
                            onBackupClick = { viewModel.exportBackup() },
                            onSyncClick = { viewModel.syncToDrive() },
                            onExportCsvClick = { viewModel.exportToCsv() },
                            onExportJsonClick = { viewModel.exportToJson() }
                        )
                    }
                    composable(Screen.Review.route) {
                        ReviewScreen(
                            onNavigateBack = {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onNavigateToWordDetail = { wordId ->
                                navController.navigate(Screen.WordDetail.createRoute(wordId))
                            },
                            onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                            onNavigateToNotifications = { navController.navigate(Screen.NotificationCenter.route) },
                            onBackupClick = { viewModel.exportBackup() },
                            onSyncClick = { viewModel.syncToDrive() },
                            onExportCsvClick = { viewModel.exportToCsv() },
                            onExportJsonClick = { viewModel.exportToJson() }
                        )
                    }
                    composable(Screen.Stats.route) {
                        StatsScreen(
                            onNavigateToReview = {
                                navController.navigate(Screen.Review.route)
                            },
                            onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                            onNavigateToNotifications = { navController.navigate(Screen.NotificationCenter.route) },
                            onNavigateToTimeline = { navController.navigate(Screen.GlobalTimeline.route) },
                            onNavigateToProfile = { navController.navigate(Screen.ReadingProfile.route) },
                            onBackupClick = { viewModel.exportBackup() },
                            onSyncClick = { viewModel.syncToDrive() },
                            onExportCsvClick = { viewModel.exportToCsv() },
                            onExportJsonClick = { viewModel.exportToJson() }
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
                            },
                            onNavigateToCompletion = { bookId ->
                                navController.navigate(Screen.BookCompletion.createRoute(bookId))
                            },
                            onNavigateToPdfReader = { bookId ->
                                navController.navigate(Screen.PdfReader.createRoute(bookId))
                            },
                            onNavigateToEpubReader = { bookId ->
                                navController.navigate(Screen.EpubReader.createRoute(bookId))
                            }
                        )
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
