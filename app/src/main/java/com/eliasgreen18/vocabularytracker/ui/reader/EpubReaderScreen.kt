package com.eliasgreen18.vocabularytracker.ui.reader

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.eliasgreen18.vocabularytracker.R
import com.eliasgreen18.vocabularytracker.domain.model.ReaderTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpubReaderScreen(
    onNavigateBack: () -> Unit,
    viewModel: EpubReaderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentChapterIndex by viewModel.currentChapterIndex.collectAsState()
    val highlights by viewModel.currentHighlights.collectAsState()
    val initialOffset by viewModel.initialScrollOffset.collectAsState()
    
    val context = LocalContext.current
    val listState = rememberLazyListState()
    var showChapterPicker by remember { mutableStateOf(false) }
    var showThemePicker by remember { mutableStateOf(false) }
    
    var selectedWord by remember { mutableStateOf<String?>(null) }
    var selectedSentence by remember { mutableStateOf<String?>(null) }
    var selectedOffsets by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var clickedHighlightId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(currentChapterIndex, uiState.content) {
        if (uiState.content != null && initialOffset > 0) {
            delay(100.milliseconds)
            listState.scrollToItem(0, initialOffset)
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemScrollOffset }
            .collect { offset ->
                if (listState.firstVisibleItemIndex == 0) {
                    viewModel.onScrollPositionChanged(offset)
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    uiState.content?.chapters?.getOrNull(currentChapterIndex)?.let { chapter ->
                        Text(
                            text = chapter.title,
                            maxLines = 1,
                            style = MaterialTheme.typography.titleMedium
                        )
                    } ?: Text(stringResource(R.string.reader_loading))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    if (uiState.content != null) {
                        IconButton(onClick = { showThemePicker = true }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                        IconButton(onClick = { showChapterPicker = true }) {
                            Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Chapters")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        val colors = uiState.readerTheme.toColors()
        
        Box(modifier = Modifier.fillMaxSize().background(colors.backgroundColor)) {
            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(uiState.error!!, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(onClick = onNavigateBack) { Text(stringResource(R.string.back)) }
                        }
                    }
                }
                uiState.content != null -> {
                    val chapter = uiState.content!!.chapters.getOrNull(currentChapterIndex) ?: uiState.content!!.chapters.first()
                    
                    Box(modifier = Modifier.fillMaxSize()) {
                        SelectionContainer {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                                    .padding(horizontal = 20.dp)
                            ) {
                                item {
                                    Spacer(modifier = Modifier.height(20.dp))
                                    val annotatedText = buildAnnotatedString {
                                        val text = chapter.plainText
                                        append(text)
                                        highlights.forEach { highlight ->
                                            if (highlight.startOffset < text.length && highlight.endOffset <= text.length) {
                                                addStyle(
                                                    style = SpanStyle(background = Color(highlight.colorHex.toColorInt()).copy(alpha = 0.4f)),
                                                    start = highlight.startOffset,
                                                    end = highlight.endOffset
                                                )
                                            }
                                        }
                                    }

                                    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
                                    Text(
                                        text = annotatedText,
                                        style = TextStyle(
                                            fontFamily = FontFamily.Serif,
                                            fontSize = uiState.fontSize.sp,
                                            lineHeight = (uiState.fontSize * 1.6).sp,
                                            color = colors.textColor
                                        ),
                                        onTextLayout = { layoutResult = it },
                                        modifier = Modifier.pointerInput(chapter.plainText) {
                                            detectTapGestures { offset ->
                                                layoutResult?.let { result ->
                                                    val charOffset = result.getOffsetForPosition(offset)
                                                    
                                                    val existingHighlight = highlights.find { 
                                                        charOffset >= it.startOffset && charOffset <= it.endOffset 
                                                    }
                                                    
                                                    if (existingHighlight != null) {
                                                        clickedHighlightId = existingHighlight.id
                                                    } else {
                                                        val text = chapter.plainText
                                                        val word = extractWordAt(text, charOffset)
                                                        val sentence = extractSentenceAt(text, charOffset)
                                                        
                                                        if (word.isNotBlank()) {
                                                            selectedWord = word
                                                            selectedSentence = sentence
                                                            
                                                            val start = text.lastIndexOf('.', charOffset).let { if (it == -1) 0 else it + 1 }
                                                            val end = text.indexOf('.', charOffset).let { if (it == -1) text.length else it + 1 }
                                                            selectedOffsets = start to end
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    )
                                }
                                item { Spacer(modifier = Modifier.height(120.dp)) }
                            }
                        }
                        
                        // Navigation Arrows
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (currentChapterIndex > 0) {
                                LargeFloatingActionButton(
                                    onClick = { viewModel.navigateToPrevChapter() },
                                    containerColor = colors.backgroundColor.copy(alpha = 0.8f),
                                    contentColor = colors.textColor,
                                    shape = CircleShape,
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(Icons.Default.ChevronLeft, contentDescription = "Prev")
                                }
                            } else {
                                Spacer(modifier = Modifier.width(48.dp))
                            }
                            
                            if (currentChapterIndex < (uiState.content?.chapters?.size?.minus(1) ?: 0)) {
                                LargeFloatingActionButton(
                                    onClick = { viewModel.navigateToNextChapter() },
                                    containerColor = colors.backgroundColor.copy(alpha = 0.8f),
                                    contentColor = colors.textColor,
                                    shape = CircleShape,
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(Icons.Default.ChevronRight, contentDescription = "Next")
                                }
                            } else {
                                Spacer(modifier = Modifier.width(48.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedWord != null || clickedHighlightId != null) {
        val snackbarAddedText = stringResource(R.string.added_snackbar, selectedWord ?: "")
        ModalBottomSheet(
            onDismissRequest = { 
                selectedWord = null
                selectedSentence = null
                clickedHighlightId = null
            },
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = uiState.readerTheme.toColors().backgroundColor,
            contentColor = uiState.readerTheme.toColors().textColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (clickedHighlightId != null) {
                    Text(
                        text = "Highlight Actions",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            viewModel.deleteHighlight(clickedHighlightId!!)
                            clickedHighlightId = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete Highlight")
                    }
                } else {
                    Text(
                        text = selectedWord!!,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Serif,
                        textAlign = TextAlign.Center
                    )
                    
                    if (!selectedSentence.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = selectedSentence!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = uiState.readerTheme.toColors().textColor.copy(alpha = 0.6f),
                            fontStyle = FontStyle.Italic,
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.addWordToVocabulary(selectedWord!!, null)
                                    Toast.makeText(context, snackbarAddedText, Toast.LENGTH_SHORT).show()
                                    selectedWord = null
                                    selectedSentence = null
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add Word")
                            }
                            
                            OutlinedButton(
                                onClick = {
                                    selectedOffsets?.let { (start, end) ->
                                        viewModel.addHighlight(start, end, selectedSentence ?: selectedWord!!)
                                    }
                                    selectedWord = null
                                    selectedSentence = null
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Highlight")
                            }
                        }
                        
                        Button(
                            onClick = {
                                viewModel.addWordToVocabulary(selectedWord!!, selectedSentence)
                                Toast.makeText(context, snackbarAddedText, Toast.LENGTH_SHORT).show()
                                selectedWord = null
                                selectedSentence = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.LibraryAdd, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.reader_add_word_context))
                        }
                    }
                }
            }
        }
    }

    if (showChapterPicker && uiState.content != null) {
        AlertDialog(
            onDismissRequest = { showChapterPicker = false },
            title = { Text("Go to Chapter") },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    itemsIndexed(uiState.content!!.chapters) { index, item ->
                        ListItem(
                            headlineContent = { Text(item.title) },
                            modifier = Modifier.clickable {
                                viewModel.onChapterSelected(index)
                                showChapterPicker = false
                            },
                            trailingContent = {
                                if (index == currentChapterIndex) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showChapterPicker = false }) { Text(stringResource(R.string.dismiss)) }
            }
        )
    }

    if (showThemePicker) {
        AlertDialog(
            onDismissRequest = { showThemePicker = false },
            title = { Text("Reader Settings") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    // Theme row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ReaderTheme.entries.forEach { theme ->
                            val themeColors = theme.toColors()
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { 
                                    viewModel.setReaderTheme(theme)
                                }
                            ) {
                                Surface(
                                    modifier = Modifier.size(48.dp),
                                    color = themeColors.backgroundColor,
                                    shape = CircleShape,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
                                ) {
                                    if (uiState.readerTheme == theme) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = themeColors.textColor)
                                        }
                                    }
                                }
                                Text(themeColors.displayName, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    // Font size row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Font Size", style = MaterialTheme.typography.titleMedium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.updateFontSize(-2) }) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease")
                            }
                            Text(
                                text = uiState.fontSize.toString(),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            IconButton(onClick = { viewModel.updateFontSize(2) }) {
                                Icon(Icons.Default.Add, contentDescription = "Increase")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemePicker = false }) { Text(stringResource(R.string.dismiss)) }
            }
        )
    }
}

private fun extractSentenceAt(text: String, offset: Int): String {
    if (text.isBlank()) return ""
    
    val abbreviations = listOf("Mr.", "Mrs.", "Ms.", "Dr.", "Prof.", "St.", "Jr.", "Sr.", "vs.")
    
    var start = text.lastIndexOf('.', offset).let { if (it == -1) 0 else it + 1 }
    while (start > 3) {
        val snippet = text.substring((start - 5).coerceAtLeast(0), start).trim()
        if (abbreviations.any { snippet.endsWith(it, ignoreCase = true) }) {
            start = text.lastIndexOf('.', start - 2).let { if (it == -1) 0 else it + 1 }
        } else {
            break
        }
    }
    
    var end = text.indexOf('.', offset).let { if (it == -1) text.length else it + 1 }
    while (end < text.length - 1) {
        val snippet = text.substring(0, end).trim()
        if (abbreviations.any { snippet.endsWith(it.dropLast(1), ignoreCase = true) }) {
            end = text.indexOf('.', end + 1).let { if (it == -1) text.length else it + 1 }
        } else {
            break
        }
    }

    return text.substring(start, end).trim()
}

private fun extractWordAt(text: String, offset: Int): String {
    if (offset < 0 || offset >= text.length) return ""
    var start = offset
    while (start > 0 && text[start - 1].isLetterOrDigit()) {
        start--
    }
    var end = offset
    while (end < text.length && text[end].isLetterOrDigit()) {
        end++
    }
    return text.substring(start, end).trim()
}
