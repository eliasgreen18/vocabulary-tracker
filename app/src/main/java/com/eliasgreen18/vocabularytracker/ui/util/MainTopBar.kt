package com.eliasgreen18.vocabularytracker.ui.util

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.eliasgreen18.vocabularytracker.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(
    title: String,
    onNavigateToSettings: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onBackupClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text(title) },
        actions = {
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Notifications") },
                        onClick = { 
                            showMenu = false
                            onNavigateToNotifications() 
                        },
                        leadingIcon = { Icon(Icons.Default.Notifications, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Settings") },
                        onClick = { 
                            showMenu = false
                            onNavigateToSettings() 
                        },
                        leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Create Backup") },
                        onClick = { 
                            showMenu = false
                            onBackupClick() 
                        },
                        leadingIcon = { Icon(Icons.Default.Backup, contentDescription = null) }
                    )
                }
            }
        }
    )
}
