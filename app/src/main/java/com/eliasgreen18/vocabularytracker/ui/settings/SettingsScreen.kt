package com.eliasgreen18.vocabularytracker.ui.settings

import android.Manifest
import android.app.Activity
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
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
    val autoScrollEnabled by viewModel.autoScrollEnabled.collectAsState()
    val importStatus by viewModel.importStatus.collectAsState()
    val geminiApiKey by viewModel.geminiApiKey.collectAsState()
    val googleAccountName by viewModel.googleAccountName.collectAsState()
    val autoSyncEnabled by viewModel.autoSyncEnabled.collectAsState()
    val autoSpeakEnabled by viewModel.autoSpeakEnabled.collectAsState()
    val appTheme by viewModel.appTheme.collectAsState()

    val context = LocalContext.current
    var showTimePicker by remember { mutableStateOf(false) }
    var showImportConfirmDialog by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var googleSignInError by remember { mutableStateOf<String?>(null) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Notification Permission Launcher (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                viewModel.setNotificationsEnabled(true)
            }
        }
    )

    // Google Sign-In Launcher (IMPROVED FEEDBACK)
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                Log.d("SettingsScreen", "Sign-in success: ${account?.email}")
                account?.email?.let { 
                    viewModel.onGoogleAccountConnected(it)
                    Toast.makeText(context, "Cloud Sync: Connected to $it", Toast.LENGTH_LONG).show()
                }
            } catch (e: ApiException) {
                val statusMsg = "Connection Error (${e.statusCode}): ${e.message}"
                Log.e("SettingsScreen", statusMsg, e)
                googleSignInError = statusMsg
            }
        } else {
            val cancelledMsg = "Google Sign-In was cancelled or failed (Code: ${result.resultCode})"
            Log.w("SettingsScreen", cancelledMsg)
            googleSignInError = cancelledMsg
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

    // Reset import status on entry
    LaunchedEffect(Unit) {
        viewModel.clearImportStatus()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Notifications Section
            SettingsHeader("Daily Reviews")
            
            SettingsToggleItem(
                title = "Review Reminders",
                description = "Get notified when you have words to study.",
                icon = Icons.Default.Notifications,
                checked = notificationsEnabled,
                onCheckedChange = { enabled ->
                    if (enabled && android.os.Build.VERSION.SDK_INT >= 33) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        viewModel.setNotificationsEnabled(enabled)
                    }
                }
            )

            if (notificationsEnabled) {
                SettingsClickableItem(
                    title = "Reminder Time",
                    description = String.format("%02d:%02d", notificationTime.first, notificationTime.second),
                    icon = Icons.Default.AccessTime,
                    onClick = { showTimePicker = true }
                )
            }

            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

            // Cloud Sync Section (MOVED UP FOR VISIBILITY)
            SettingsHeader("Cloud Synchronization")
            
            SettingsClickableItem(
                title = if (googleAccountName == null) "Connect to Google Drive" else "Connected to Google",
                description = googleAccountName ?: "Secure your vocabulary in the cloud automatically.",
                icon = if (googleAccountName == null) Icons.Default.Cloud else Icons.Default.CloudDone,
                onClick = { 
                    if (googleAccountName == null) {
                        Log.d("SettingsScreen", "Starting Google Sign-In Flow")
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestEmail()
                            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
                            .build()
                        val client = GoogleSignIn.getClient(context, gso)
                        googleSignInLauncher.launch(client.signInIntent)
                    } else {
                        viewModel.disconnectGoogleAccount()
                        Toast.makeText(context, "Disconnected from Google Drive", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            SettingsToggleItem(
                title = "Auto-sync data",
                description = "Upload backup after every reading session.",
                icon = Icons.Default.CloudSync,
                checked = autoSyncEnabled,
                enabled = googleAccountName != null,
                onCheckedChange = { viewModel.setAutoSyncEnabled(it) }
            )

            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

            // Reading Experience
            SettingsHeader("Reading Session")

            SettingsToggleItem(
                title = "Auto-Scroll to Top",
                description = "Automatically show the latest word added.",
                icon = Icons.Default.Mouse,
                checked = autoScrollEnabled,
                onCheckedChange = { viewModel.setAutoScrollEnabled(it) }
            )

            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

            // Look & Feel
            SettingsHeader("Appearance")
            
            ThemeSelector(
                selectedTheme = appTheme,
                onThemeSelected = { viewModel.setAppTheme(it) }
            )

            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

            // Audio Experience
            SettingsHeader("Audio & Learning Automation")

            SettingsToggleItem(
                title = "Magic Auto-Fill",
                description = "Automatically fill Translation and IPA when adding words.",
                icon = Icons.Default.AutoFixHigh,
                checked = true,
                onCheckedChange = { /* Always enabled for now */ }
            )

            SettingsToggleItem(
                title = "Auto-pronounce during review",
                description = "Speak words automatically when revealed.",
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                checked = autoSpeakEnabled,
                onCheckedChange = { viewModel.setAutoSpeakEnabled(it) }
            )

            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

            // Data Section
            SettingsHeader("Backup & Data")

            SettingsClickableItem(
                title = "Import Vocabulary",
                description = "Load words from a CSV or JSON file.",
                icon = Icons.Default.FileUpload,
                onClick = { filePickerLauncher.launch("*/*") }
            )

            SettingsToggleItem(
                title = "Keep backup history",
                description = "Store multiple dated versions of your data.",
                icon = Icons.Default.History,
                checked = keepHistory,
                onCheckedChange = { viewModel.setKeepBackupHistory(it) }
            )

            SettingsClickableItem(
                title = "Process Missing Translations",
                description = "Run auto-translation for existing words.",
                icon = Icons.Default.Translate,
                onClick = { viewModel.triggerMassTranslation() }
            )

            SettingsClickableItem(
                title = "Test Reminder",
                description = "Trigger a notification in 5 seconds.",
                icon = Icons.Default.NotificationsActive,
                onClick = { viewModel.sendTestNotification() }
            )

            if (importStatus != null) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = importStatus!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = { viewModel.clearImportStatus() }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // App Info
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Vocabulary Tracker v1.0",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "High-performance reading companion",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                )
            }
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
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
                },
                text = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        TimePicker(state = state)
                    }
                }
            )
        }

        if (showImportConfirmDialog) {
            var overwrite by remember { mutableStateOf(false) }
            AlertDialog(
                onDismissRequest = { showImportConfirmDialog = false },
                title = { Text("Confirm Import") },
                text = {
                    Column {
                        Text("Do you want to import words from the selected file?")
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = overwrite, onCheckedChange = { overwrite = it })
                            Text(text = "Overwrite existing translations", style = MaterialTheme.typography.bodyMedium)
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
                        Text("Import")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showImportConfirmDialog = false }) { Text("Cancel") }
                }
            )
        }

        if (showApiKeyDialog) {
            var key by remember { mutableStateOf(geminiApiKey ?: "") }
            AlertDialog(
                onDismissRequest = { showApiKeyDialog = false },
                title = { Text("Gemini API Key") },
                text = {
                    Column {
                        Text("Get a free key from Google AI Studio to power your AI Tutor.", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = key,
                            onValueChange = { key = it },
                            label = { Text("API Key") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.setGeminiApiKey(key.trim().ifBlank { null })
                        showApiKeyDialog = false
                    }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showApiKeyDialog = false }) { Text("Cancel") }
                }
            )
        }
        
        if (googleSignInError != null) {
            AlertDialog(
                onDismissRequest = { googleSignInError = null },
                title = { Text("Google Drive Error") },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text(
                            text = googleSignInError!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "This usually means your app signature (SHA-1) is not registered in the Google Cloud Console for this Client ID.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = { googleSignInError = null }) {
                        Text("Dismiss")
                    }
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
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        com.eliasgreen18.vocabularytracker.domain.model.AppTheme.entries.forEach { theme ->
            val color = when(theme) {
                com.eliasgreen18.vocabularytracker.domain.model.AppTheme.SYSTEM -> MaterialTheme.colorScheme.outline
                com.eliasgreen18.vocabularytracker.domain.model.AppTheme.LIGHT -> Color.White
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
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(color)
                        .border(
                            width = if (selectedTheme == theme) 2.dp else 1.dp,
                            color = if (selectedTheme == theme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(8.dp)
                        )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = theme.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selectedTheme == theme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun SettingsHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 4.dp)
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
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title, 
                    style = MaterialTheme.typography.bodyLarge, 
                    fontWeight = FontWeight.Medium,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                )
                Text(
                    text = description, 
                    style = MaterialTheme.typography.bodySmall, 
                    color = MaterialTheme.colorScheme.outline
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
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}
