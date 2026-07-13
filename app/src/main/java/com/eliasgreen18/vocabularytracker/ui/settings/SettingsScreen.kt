package com.eliasgreen18.vocabularytracker.ui.settings

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.eliasgreen18.vocabularytracker.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val keepHistory by viewModel.keepBackupHistory.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val notificationTime by viewModel.notificationTime.collectAsState()
    val googleAccountName by viewModel.googleAccountName.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val autoSyncEnabled by viewModel.autoSyncEnabled.collectAsState()
    val autoSpeakEnabled by viewModel.autoSpeakEnabled.collectAsState()
    val appTheme by viewModel.appTheme.collectAsState()

    val context = LocalContext.current
    var showTimePicker by remember { mutableStateOf(false) }
    var showImportConfirmDialog by remember { mutableStateOf(false) }
    var showNameDialog by remember { mutableStateOf(false) }
    var googleSignInError by remember { mutableStateOf<String?>(null) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.email?.let { 
                    viewModel.onGoogleAccountConnected(it)
                }
            } catch (e: ApiException) {
                googleSignInError = "Error: ${e.statusCode}"
            }
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            pendingImportUri = uri
            showImportConfirmDialog = true
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title), fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // User Identity Section
            SettingsHeader(stringResource(R.string.section_identity))
            Card(shape = MaterialTheme.shapes.large) {
                SettingsClickableItem(
                    title = stringResource(R.string.field_personal_name),
                    description = userName,
                    icon = Icons.Default.Person,
                    onClick = { showNameDialog = true }
                )
            }

            // Notifications Section
            SettingsHeader(stringResource(R.string.section_study_reminders))
            Card(shape = MaterialTheme.shapes.large) {
                Column {
                    SettingsToggleItem(
                        title = stringResource(R.string.item_review_alerts),
                        description = stringResource(R.string.desc_review_alerts),
                        icon = Icons.Default.Notifications,
                        checked = notificationsEnabled,
                        onCheckedChange = { enabled ->
                            viewModel.setNotificationsEnabled(enabled)
                        }
                    )

                    if (notificationsEnabled) {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                        SettingsClickableItem(
                            title = stringResource(R.string.item_reminder_time),
                            description = String.format("%02d:%02d", notificationTime.first, notificationTime.second),
                            icon = Icons.Default.AccessTime,
                            onClick = { showTimePicker = true }
                        )
                    }
                }
            }

            // Cloud Sync Section
            SettingsHeader(stringResource(R.string.section_cloud_backup))
            Card(shape = MaterialTheme.shapes.large) {
                Column {
                    SettingsClickableItem(
                        title = if (googleAccountName == null) stringResource(R.string.item_connect_drive) else stringResource(R.string.item_connected_cloud),
                        description = googleAccountName ?: stringResource(R.string.desc_secure_vocab),
                        icon = if (googleAccountName == null) Icons.Default.CloudQueue else Icons.Default.CloudDone,
                        onClick = { 
                            if (googleAccountName == null) {
                                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestEmail()
                                    .requestScopes(Scope(DriveScopes.DRIVE_FILE))
                                    .build()
                                val client = GoogleSignIn.getClient(context, gso)
                                googleSignInLauncher.launch(client.signInIntent)
                            } else {
                                viewModel.disconnectGoogleAccount()
                            }
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)

                    SettingsToggleItem(
                        title = stringResource(R.string.item_auto_sync),
                        description = stringResource(R.string.desc_auto_sync),
                        icon = Icons.Default.Sync,
                        checked = autoSyncEnabled,
                        enabled = googleAccountName != null,
                        onCheckedChange = { viewModel.setAutoSyncEnabled(it) }
                    )
                }
            }

            // Appearance
            SettingsHeader(stringResource(R.string.section_appearance))
            ThemeSelector(
                selectedTheme = appTheme,
                onThemeSelected = { viewModel.setAppTheme(it) }
            )

            // Audio & Automation
            SettingsHeader(stringResource(R.string.section_automation))
            Card(shape = MaterialTheme.shapes.large) {
                Column {
                    SettingsToggleItem(
                        title = stringResource(R.string.item_auto_pronounce),
                        description = stringResource(R.string.desc_auto_pronounce),
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        checked = autoSpeakEnabled,
                        onCheckedChange = { viewModel.setAutoSpeakEnabled(it) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                    SettingsToggleItem(
                        title = stringResource(R.string.item_keep_history),
                        description = stringResource(R.string.desc_keep_history),
                        icon = Icons.Default.History,
                        checked = keepHistory,
                        onCheckedChange = { viewModel.setKeepBackupHistory(it) }
                    )
                }
            }

            // Data Maintenance
            SettingsHeader(stringResource(R.string.section_data_management))
            Card(shape = MaterialTheme.shapes.large) {
                Column {
                    SettingsClickableItem(
                        title = stringResource(R.string.item_process_translations),
                        description = stringResource(R.string.desc_process_translations),
                        icon = Icons.Default.Translate,
                        onClick = { viewModel.triggerMassTranslation() }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                    SettingsClickableItem(
                        title = stringResource(R.string.item_import_external),
                        description = stringResource(R.string.desc_import_external),
                        icon = Icons.Default.FileUpload,
                        onClick = { filePickerLauncher.launch("*/*") }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }

        if (showTimePicker) {
            val state = rememberTimePickerState(
                initialHour = notificationTime.first,
                initialMinute = notificationTime.second,
                is24Hour = true
            )
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.setNotificationTime(state.hour, state.minute)
                        showTimePicker = false
                    }) { Text(stringResource(R.string.ok)) }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) { Text(stringResource(R.string.cancel)) }
                },
                text = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        TimePicker(state = state)
                    }
                }
            )
        }

        if (showNameDialog) {
            var name by remember { mutableStateOf(userName) }
            AlertDialog(
                onDismissRequest = { showNameDialog = false },
                title = { Text(stringResource(R.string.field_personal_name)) },
                text = {
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(R.string.field_name_label)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.setUserName(name.trim().ifBlank { "Reader" })
                        showNameDialog = false
                    }) { Text(stringResource(R.string.save)) }
                },
                dismissButton = {
                    TextButton(onClick = { showNameDialog = false }) { Text(stringResource(R.string.cancel)) }
                }
            )
        }

        if (showImportConfirmDialog) {
            var overwrite by remember { mutableStateOf(false) }
            AlertDialog(
                onDismissRequest = { showImportConfirmDialog = false },
                title = { Text(stringResource(R.string.import_confirm_title)) },
                text = {
                    Column {
                        Text(stringResource(R.string.import_confirm_desc))
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = overwrite, onCheckedChange = { overwrite = it })
                            Text(text = stringResource(R.string.import_overwrite_label), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        pendingImportUri?.let { uri ->
                            val ext = if (uri.toString().endsWith(".json")) "json" else "csv"
                            viewModel.importData(uri, ext, overwrite)
                        }
                        showImportConfirmDialog = false
                    }) {
                        Text(stringResource(R.string.import_btn))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showImportConfirmDialog = false }) { Text(stringResource(R.string.cancel)) }
                }
            )
        }
    }
}

@Composable
fun ThemeSelector(
    selectedTheme: com.eliasgreen18.vocabularytracker.domain.model.AppTheme,
    onThemeSelected: (com.eliasgreen18.vocabularytracker.domain.model.AppTheme) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        com.eliasgreen18.vocabularytracker.domain.model.AppTheme.entries.forEach { theme ->
            val color = when(theme) {
                com.eliasgreen18.vocabularytracker.domain.model.AppTheme.SYSTEM -> Color.Gray
                com.eliasgreen18.vocabularytracker.domain.model.AppTheme.LIGHT -> Color(0xFFFDFBF7)
                com.eliasgreen18.vocabularytracker.domain.model.AppTheme.DARK -> Color.Black
                com.eliasgreen18.vocabularytracker.domain.model.AppTheme.SEPIA -> Color(0xFFF4ECD8)
                com.eliasgreen18.vocabularytracker.domain.model.AppTheme.OLED -> Color.Black
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f).clickable { onThemeSelected(theme) }
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(color)
                        .border(
                            width = if (selectedTheme == theme) 2.dp else 1.dp,
                            color = if (selectedTheme == theme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(12.dp)
                        )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = theme.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selectedTheme == theme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (selectedTheme == theme) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun SettingsHeader(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.2.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
    )
}

@Composable
fun SettingsToggleItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), shape = CircleShape, modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title, 
                    style = MaterialTheme.typography.bodyLarge, 
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                )
                Text(
                    text = description, 
                    style = MaterialTheme.typography.bodySmall, 
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

@Composable
fun SettingsClickableItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), shape = CircleShape, modifier = Modifier.size(40.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
        }
    }
}
