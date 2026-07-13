package com.eliasgreen18.vocabularytracker.ui.util

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.eliasgreen18.vocabularytracker.R

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
        title = { 
            Text(
                text = title, 
                style = MaterialTheme.typography.titleLarge,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold
            ) 
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        ),
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
                        text = { Text(stringResource(R.string.menu_notifications)) },
                        onClick = { 
                            showMenu = false
                            onNavigateToNotifications() 
                        },
                        leadingIcon = { Icon(Icons.Default.Notifications, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_settings)) },
                        onClick = { 
                            showMenu = false
                            onNavigateToSettings() 
                        },
                        leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) }
                    )
                    
                    HorizontalDivider()

                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_sync_drive)) },
                        onClick = { 
                            showMenu = false
                            onSyncClick() 
                        },
                        leadingIcon = { Icon(Icons.Default.CloudSync, contentDescription = null) }
                    )

                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_export_csv)) },
                        onClick = { 
                            showMenu = false
                            onExportCsvClick() 
                        },
                        leadingIcon = { Icon(Icons.Default.TableChart, contentDescription = null) }
                    )

                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_export_json)) },
                        onClick = { 
                            showMenu = false
                            onExportJsonClick() 
                        },
                        leadingIcon = { Icon(Icons.Default.Code, contentDescription = null) }
                    )

                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_db_backup)) },
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
