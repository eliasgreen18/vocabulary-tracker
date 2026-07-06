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
                    when (item) {
                        is NavItem.ScreenItem -> {
                            NavigationBarItem(
                                icon = { Icon(item.icon, contentDescription = item.label) },
                                label = { Text(item.label) },
                                selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true,
                                onClick = {
                                    navController.navigate(item.screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                        is NavItem.ActionItem -> {
                            NavigationBarItem(
                                icon = { 
                                    Icon(
                                        Icons.AutoMirrored.Filled.MenuBook, 
                                        contentDescription = "Read",
                                        tint = if (activeSessionId != null) MaterialTheme.colorScheme.primary else LocalContentColor.current
                                    ) 
                                },
                                label = { Text("Read") },
                                selected = currentDestination?.hierarchy?.any { it.route?.startsWith("session/") == true } == true,
                                onClick = {
                                    activeSessionId?.let { id ->
                                        navController.navigate(Screen.ActiveSession.createRoute(id))
                                    } ?: run {
                                        // If no active session, go to Books to pick one
                                        navController.navigate(Screen.Books.route)
                                    }
                                }
                            )
                        }
                    }
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
                    onNavigateBack = { /* Stay on tab */ },
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
            
            // Details & Flows (NOT part of bottom bar items but in the same NavHost)
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
