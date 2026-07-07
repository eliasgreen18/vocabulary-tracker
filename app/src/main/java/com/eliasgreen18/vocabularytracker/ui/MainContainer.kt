package com.eliasgreen18.vocabularytracker.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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

sealed class NavItem {
    data class ScreenItem(val screen: Screen, val label: String, val icon: ImageVector) : NavItem()
    object ActionItem : NavItem() // The central READ action
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContainer(
    viewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val activeSessionId by viewModel.activeSessionId.collectAsState()
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = listOf(
        NavItem.ScreenItem(Screen.Search, "Words", Icons.Default.Translate),
        NavItem.ScreenItem(Screen.Books, "Books", Icons.Default.Book),
        NavItem.ActionItem, // Central position
        NavItem.ScreenItem(Screen.Review, "Review", Icons.Default.History),
        NavItem.ScreenItem(Screen.Home, "Stats", Icons.Default.Leaderboard)
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    val isSelected = when (item) {
                        is NavItem.ScreenItem -> {
                            val currentRoute = currentDestination?.route ?: ""
                            when (item.screen.route) {
                                Screen.Search.route -> currentRoute == Screen.Search.route || currentRoute.startsWith("word/")
                                Screen.Books.route -> currentRoute == Screen.Books.route || currentRoute.startsWith("book/") || currentRoute.startsWith("chapter/")
                                Screen.Review.route -> currentRoute == Screen.Review.route
                                Screen.Home.route -> currentRoute == Screen.Home.route
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
                                        // Reset the tab: navigate back to its own route
                                        navController.navigate(item.screen.route) {
                                            popUpTo(item.screen.route) { inclusive = true }
                                        }
                                    } else {
                                        // Standard cross-tab navigation WITHOUT state restoration
                                        // This ensures we always land on the root of the tab
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
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Search.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Search.route) {
                WordsScreen(
                    onNavigateToWordDetail = { wordId ->
                        navController.navigate(Screen.WordDetail.createRoute(wordId))
                    }
                )
            }
            composable(Screen.Books.route) {
                BooksScreen(
                    onNavigateToBookDetail = { bookId ->
                        navController.navigate(Screen.BookDetail.createRoute(bookId))
                    }
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
                    }
                )
            }
            composable(Screen.Home.route) {
                StatsScreen(
                    onNavigateToReview = {
                        navController.navigate(Screen.Review.route)
                    }
                )
            }
            
            // Details & Flows
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
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToWordDetail = { wordId ->
                        navController.navigate(Screen.WordDetail.createRoute(wordId))
                    }
                )
            }
            composable(
                route = Screen.WordDetail.route,
                arguments = listOf(navArgument("wordId") { type = NavType.LongType })
            ) {
                WordDetailScreen(
                    onNavigateBack = { navController.popBackStack() }
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
