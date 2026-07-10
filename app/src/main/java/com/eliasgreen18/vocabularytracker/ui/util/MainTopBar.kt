package com.eliasgreen18.vocabularytracker.ui.util

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(
    title: String,
    onNavigateToSettings: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onBackupClick: () -> Unit,
    onSyncClick: () -> Unit,
    onExportCsvClick: () -> Unit,
    onExportJsonClick: () -> Unit
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
                    
                    HorizontalDivider()

                    DropdownMenuItem(
                        text = { Text("Sync to Google Drive") },
                        onClick = { 
                            showMenu = false
                            onSyncClick() 
                        },
                        leadingIcon = { Icon(Icons.Default.CloudSync, contentDescription = null) }
                    )

                    DropdownMenuItem(
                        text = { Text("Export to CSV") },
                        onClick = { 
                            showMenu = false
                            onExportCsvClick() 
                        },
                        leadingIcon = { Icon(Icons.Default.TableChart, contentDescription = null) }
                    )

                    DropdownMenuItem(
                        text = { Text("Export to JSON") },
                        onClick = { 
                            showMenu = false
                            onExportJsonClick() 
                        },
                        leadingIcon = { Icon(Icons.Default.Code, contentDescription = null) }
                    )

                    DropdownMenuItem(
                        text = { Text("Create .db Backup") },
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
