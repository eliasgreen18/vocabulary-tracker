package com.eliasgreen18.vocabularytracker.ui.words

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.eliasgreen18.vocabularytracker.domain.model.JourneyEvent
import com.eliasgreen18.vocabularytracker.domain.model.RelationshipType
import com.eliasgreen18.vocabularytracker.domain.model.WordDetailUiState
import com.eliasgreen18.vocabularytracker.domain.model.WordMastery
import com.eliasgreen18.vocabularytracker.ui.util.ExternalTranslationHelper
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WordDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToWordDetail: (Long) -> Unit,
    viewModel: WordDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showEditWordDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showEditTranslationDialog by remember { mutableStateOf(false) }
    var showEditIpaDialog by remember { mutableStateOf(false) }
    var showEditNotesDialog by remember { mutableStateOf(false) }
    var showAddRelationshipDialog by remember { mutableStateOf(false) }
    
    var wordTextToEdit by remember { mutableStateOf("") }
    var translationToEdit by remember { mutableStateOf("") }
    var ipaToEdit by remember { mutableStateOf("") }
    var notesToEdit by remember { mutableStateOf("") }

    val dateTimeFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
        .withZone(ZoneId.systemDefault())

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is WordDetailUiEvent.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        withDismissAction = true
                    )
                }
                WordDetailUiEvent.InsightsGenerated -> {
                    snackbarHostState.showSnackbar("AI Insights generated!")
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { 
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    containerColor = if (data.visuals.message.contains("Error", ignoreCase = true) || data.visuals.message.contains("fail", ignoreCase = true)) 
                        MaterialTheme.colorScheme.errorContainer 
                    else 
                        MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = if (data.visuals.message.contains("Error", ignoreCase = true) || data.visuals.message.contains("fail", ignoreCase = true)) 
                        MaterialTheme.colorScheme.onErrorContainer 
                    else 
                        MaterialTheme.colorScheme.onSecondaryContainer,
                    snackbarData = data
                )
            }
        },
        topBar = {
            TopAppBar(
                title = { Text("Word Detail") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    uiState?.let { state ->
                        IconButton(onClick = { 
                            wordTextToEdit = state.word.text
                            showEditWordDialog = true 
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Word")
                        }
                        IconButton(onClick = { viewModel.toggleFocus(!state.word.isFocusWord) }) {
                            Icon(
                                imageVector = if (state.word.isFocusWord) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Favorite",
                                tint = if (state.word.isFocusWord) MaterialTheme.colorScheme.primary else LocalContentColor.current
                            )
                        }
                        IconButton(onClick = { showDeleteConfirmDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        uiState?.let { state ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Header: Word + Mastery + Language
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = state.word.text,
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            IconButton(onClick = { viewModel.speak(state.word.text) }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.VolumeUp, 
                                    contentDescription = "Speak",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            MasteryBadge(mastery = state.mastery)
                            state.mainLanguage?.let { lang ->
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = lang.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }

                // Quick Stats Grid
                item {
                    QuickStatsGrid(state)
                }

                // Memory Performance (SRS)
                item {
                    MemoryPerformanceCard(state)
                }

                // Translation Card (Editable)
                item {
                    EditableDataCard(
                        label = "Translation",
                        value = state.word.translation,
                        placeholder = "No translation available",
                        onEdit = { 
                            translationToEdit = state.word.translation ?: ""
                            showEditTranslationDialog = true 
                        }
                    )
                }

                // IPA Card (Editable)
                item {
                    EditableDataCard(
                        label = "Pronunciation (IPA)",
                        value = state.word.ipa,
                        placeholder = "No IPA available",
                        onEdit = {
                            ipaToEdit = state.word.ipa ?: ""
                            showEditIpaDialog = true
                        },
                        icon = Icons.AutoMirrored.Filled.VolumeUp
                    )
                }

                // Action Hub (External)
                item {
                    ExternalResourcesSection(state.word.text, context)
                }

                // Personal Notes Section
                item {
                    EditableDataCard(
                        label = "Personal Notes & Mnemonics",
                        value = state.word.notes,
                        placeholder = "Add your own tricks or rules here...",
                        onEdit = {
                            notesToEdit = state.word.notes ?: ""
                            showEditNotesDialog = true
                        },
                        icon = Icons.AutoMirrored.Filled.NoteAdd
                    )
                }

                // AI Tutor Section
                item {
                    AiTutorSection(
                        word = state.word,
                        isLoading = isAiLoading,
                        onAskAi = { viewModel.generateAiInsights() }
                    )
                }

                // Related Words Section
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Related Words",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(onClick = { showAddRelationshipDialog = true }) {
                                Icon(Icons.Default.AddLink, contentDescription = "Add relationship")
                            }
                        }
                        
                        if (state.relatedWords.isEmpty()) {
                            Text(
                                text = "No words linked yet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        } else {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                state.relatedWords.forEach { related ->
                                    val (color, label) = when (related.relationshipType) {
                                        RelationshipType.SYNONYM -> Color(0xFF4CAF50) to "Synonym"
                                        RelationshipType.ANTONYM -> MaterialTheme.colorScheme.error to "Antonym"
                                        RelationshipType.RELATED -> MaterialTheme.colorScheme.secondary to "Related"
                                    }
                                    
                                    FilterChip(
                                        selected = true,
                                        onClick = { onNavigateToWordDetail(related.wordId) },
                                        label = { Text("${related.wordText} ($label)") },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = color.copy(alpha = 0.1f),
                                            selectedLabelColor = color
                                        ),
                                        trailingIcon = {
                                            IconButton(
                                                onClick = { viewModel.deleteRelationship(related.wordId, related.relationshipType) },
                                                modifier = Modifier.size(16.dp)
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(12.dp))
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Vocabulary Journey (Timeline)
                item {
                    Column {
                        Text(
                            text = "Vocabulary Journey",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        JourneyTimeline(
                            events = state.journey, 
                            dateTimeFormatter = dateTimeFormatter,
                            onSpeak = { viewModel.speak(it) }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        // Dialogs
        if (showEditWordDialog) {
            EditValueDialog(
                title = "Edit Word",
                label = "Word text",
                initialValue = wordTextToEdit,
                onDismiss = { showEditWordDialog = false },
                onConfirm = { 
                    viewModel.updateWordText(it)
                    showEditWordDialog = false
                }
            )
        }

        if (showEditTranslationDialog) {
            EditValueDialog(
                title = "Edit Translation",
                label = "Spanish translation",
                initialValue = translationToEdit,
                onDismiss = { showEditTranslationDialog = false },
                onConfirm = {
                    viewModel.saveManualTranslation(it)
                    showEditTranslationDialog = false
                }
            )
        }

        if (showEditIpaDialog) {
            EditValueDialog(
                title = "Edit Pronunciation",
                label = "IPA",
                initialValue = ipaToEdit,
                isIpa = true,
                onDismiss = { showEditIpaDialog = false },
                onConfirm = {
                    viewModel.saveManualIpa(it)
                    showEditIpaDialog = false
                }
            )
        }

        if (showEditNotesDialog) {
            EditValueDialog(
                title = "Personal Notes",
                label = "Rules or mnemonics",
                initialValue = notesToEdit,
                onDismiss = { showEditNotesDialog = false },
                onConfirm = {
                    viewModel.saveManualNotes(it)
                    showEditNotesDialog = false
                }
            )
        }

        if (showAddRelationshipDialog) {
            AddRelationshipDialog(
                viewModel = viewModel,
                onDismiss = { showAddRelationshipDialog = false }
            )
        }

        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { Text("Delete Word?") },
                text = { Text("This will permanently remove the word and all its recorded occurrences. This action cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteWord(onDeleted = onNavigateBack)
                            showDeleteConfirmDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddRelationshipDialog(
    viewModel: WordDetailViewModel,
    onDismiss: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val results by viewModel.searchResults.collectAsState()
    var selectedType by remember { mutableStateOf(RelationshipType.SYNONYM) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Link Related Word") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Link as:", style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RelationshipType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    label = { Text("Search word to link") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Box(modifier = Modifier.heightIn(max = 200.dp)) {
                    LazyColumn {
                        items(results) { word ->
                            ListItem(
                                headlineContent = { Text(word.wordText) },
                                modifier = Modifier.clickable {
                                    viewModel.addRelationship(word.wordId, selectedType)
                                    onDismiss()
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun JourneyTimeline(events: List<JourneyEvent>, dateTimeFormatter: DateTimeFormatter, onSpeak: (String) -> Unit) {
    if (events.isEmpty()) {
        Text(text = "The journey hasn't started yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
    } else {
        Column(modifier = Modifier.fillMaxWidth()) {
            events.forEachIndexed { index, event ->
                JourneyEventItem(
                    event = event,
                    isLast = index == events.size - 1,
                    formatter = dateTimeFormatter,
                    onSpeak = onSpeak
                )
            }
        }
    }
}

@Composable
fun JourneyEventItem(event: JourneyEvent, isLast: Boolean, formatter: DateTimeFormatter, onSpeak: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(48.dp)) {
            val (icon, color) = when (event) {
                is JourneyEvent.Discovery -> Icons.Default.Search to MaterialTheme.colorScheme.primary
                is JourneyEvent.Encounter -> Icons.AutoMirrored.Filled.MenuBook to MaterialTheme.colorScheme.secondary
                is JourneyEvent.Reviewed -> Icons.Default.History to Color(0xFF4CAF50)
                is JourneyEvent.Mastered -> Icons.Default.Stars to Color(0xFFFFD700)
            }
            
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            
            if (!isLast) {
                Box(
                    modifier = Modifier.width(2.dp).weight(1f).background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }
        
        Column(modifier = Modifier.padding(start = 12.dp, bottom = 24.dp).weight(1f)) {
            val title = when (event) {
                is JourneyEvent.Discovery -> "First Discovery"
                is JourneyEvent.Encounter -> "Encountered again"
                is JourneyEvent.Reviewed -> "Knowledge Check"
                is JourneyEvent.Mastered -> "Mastery Achieved!"
            }
            
            val description = when (event) {
                is JourneyEvent.Discovery -> "${event.bookTitle}\n${event.chapterDisplay}"
                is JourneyEvent.Encounter -> "${event.bookTitle}\n${event.chapterDisplay}"
                is JourneyEvent.Reviewed -> "Studied with ${event.nextInterval} day interval"
                is JourneyEvent.Mastered -> "Word officially learned"
            }

            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            val snippet = when (event) {
                is JourneyEvent.Discovery -> event.snippet
                is JourneyEvent.Encounter -> event.snippet
                else -> null
            }
            
            if (!snippet.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "\"$snippet\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { onSpeak(snippet) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Speak", modifier = Modifier.size(14.dp))
                    }
                }
            }

            Text(
                text = formatter.format(event.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun EditableDataCard(
    label: String,
    value: String?,
    placeholder: String,
    onEdit: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (icon != null) {
                        Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value ?: placeholder,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (value != null) FontWeight.Bold else FontWeight.Normal,
                fontStyle = if (value == null) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal
            )
        }
    }
}

@Composable
fun EditValueDialog(
    title: String,
    label: String,
    initialValue: String,
    isIpa: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var value by remember { mutableStateOf(initialValue) }
    
    val ipaSymbols = listOf("/", "ə", "ɪ", "æ", "θ", "ð", "ʃ", "ʒ", "ŋ", "ʌ", "ɔ", "ɒ", "ɜ", "ʊ", "u", "i", "e", "a")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                TextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text(label) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                if (isIpa) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Quick Symbols", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(ipaSymbols) { symbol ->
                            AssistChip(
                                onClick = { value += symbol },
                                label = { Text(symbol, style = MaterialTheme.typography.bodyLarge) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(value) }, enabled = value.isNotBlank()) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun QuickStatsGrid(state: WordDetailUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        DetailStatItem(label = "Appearances", value = state.totalOccurrences.toString(), icon = Icons.Default.BarChart)
        DetailStatItem(label = "Books", value = state.bookCount.toString(), icon = Icons.AutoMirrored.Filled.MenuBook)
        DetailStatItem(label = "Chapters", value = state.chapterCount.toString(), icon = Icons.Default.Bookmarks)
    }
}

@Composable
fun MemoryPerformanceCard(state: WordDetailUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Memory Performance", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Next Review", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    val nextText = state.nextReviewDate?.let {
                        val days = ChronoUnit.DAYS.between(Instant.now(), it)
                        when {
                            days < 0 -> "Overdue"
                            days == 0L -> "Today"
                            days == 1L -> "Tomorrow"
                            else -> "In $days days"
                        }
                    } ?: "Not Scheduled"
                    Text(text = nextText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Memory Strength", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(text = "${state.currentInterval} day interval", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { state.recallAccuracy / 100f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = if (state.recallAccuracy > 70) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${state.recallAccuracy}% Recall Accuracy (${state.successCount} hits / ${state.failCount} misses)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun AiTutorSection(
    word: com.eliasgreen18.vocabularytracker.domain.model.Word,
    isLoading: Boolean,
    onAskAi: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AutoAwesome, 
                        contentDescription = null, 
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "AI Tutor Insights", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
                
                if (word.aiExplanation == null) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        TextButton(onClick = onAskAi) {
                            Text("✨ Generate")
                        }
                    }
                }
            }

            if (word.aiExplanation != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = word.aiExplanation!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (!word.aiExamples.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Usage Examples:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = word.aiExamples!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            } else if (!isLoading) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Unlock deep linguistic insights, nuances, and natural examples with Gemini AI.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun MasteryBadge(mastery: WordMastery) {
    val (color, icon) = when (mastery) {
        WordMastery.NEW -> MaterialTheme.colorScheme.outline to Icons.Default.FiberNew
        WordMastery.LEARNING -> MaterialTheme.colorScheme.primary to Icons.Default.AutoStories
        WordMastery.LEARNED -> Color(0xFF4CAF50) to Icons.Default.CheckCircle
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        contentColor = color,
        shape = CircleShape,
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = mastery.label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DetailStatItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            shape = CircleShape,
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
fun ExternalResourcesSection(text: String, context: android.content.Context) {
    Column {
        Text("Quick Lookup", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { ExternalTranslationHelper.openGoogleTranslate(context, text) },
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(8.dp)
            ) {
                Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Google", style = MaterialTheme.typography.labelLarge)
            }
            Button(
                onClick = { ExternalTranslationHelper.openReversoContext(context, text) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                contentPadding = PaddingValues(8.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reverso", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
