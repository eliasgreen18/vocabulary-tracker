package com.eliasgreen18.vocabularytracker.ui.reader

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpubReaderScreen(
    onNavigateBack: () -> Unit,
    viewModel: EpubReaderViewModel = hiltViewModel()
) {
    val epubContent by viewModel.epubContent.collectAsState()
    val currentIndex by viewModel.currentChapterIndex.collectAsState()
    val initialScrollOffset by viewModel.initialScrollOffset.collectAsState()
    val highlights by viewModel.currentHighlights.collectAsState()
    
    val scrollState = rememberScrollState()

    // Sync scroll position to ViewModel
    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.value }
            .collect { offset ->
                if (offset > 0) {
                    viewModel.onScrollPositionChanged(offset)
                }
            }
    }

    // Restore scroll position
    LaunchedEffect(currentIndex, epubContent) {
        if (epubContent != null) {
            scrollState.scrollTo(initialScrollOffset)
        }
    }
    
    // Reset selection when chapter changes
    var textFieldValue by remember(currentIndex, epubContent) { 
        val plainText = epubContent?.chapters?.getOrNull(currentIndex)?.plainText ?: ""
        mutableStateOf(TextFieldValue(text = plainText)) 
    }

    var fontSize by remember { mutableStateOf(18.sp) }
    
    // Transform plain text into AnnotatedString with highlights
    val annotatedContent = remember(epubContent, currentIndex, highlights) {
        val plainText = epubContent?.chapters?.getOrNull(currentIndex)?.plainText ?: ""
        buildAnnotatedString {
            append(plainText)
            highlights.forEach { hl ->
                if (hl.startOffset >= 0 && hl.startOffset < plainText.length && hl.endOffset <= plainText.length) {
                    addStyle(
                        style = SpanStyle(background = Color(android.graphics.Color.parseColor(hl.colorHex)).copy(alpha = 0.4f)),
                        start = hl.startOffset,
                        end = hl.endOffset
                    )
                }
            }
        }
    }

    LaunchedEffect(annotatedContent) {
        textFieldValue = textFieldValue.copy(annotatedString = annotatedContent)
    }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val emptyTextToolbar = object : TextToolbar {
        override val status: TextToolbarStatus = TextToolbarStatus.Hidden
        override fun hide() {}
        override fun showMenu(
            rect: Rect,
            onCopyRequested: (() -> Unit)?,
            onPasteRequested: (() -> Unit)?,
            onCutRequested: (() -> Unit)?,
            onSelectAllRequested: (() -> Unit)?
        ) {}
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = epubContent?.title ?: "Loading...", 
                        maxLines = 1,
                        style = MaterialTheme.typography.titleMedium
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { fontSize = (fontSize.value - 2).coerceAtLeast(12f).sp }) {
                        Icon(Icons.Default.TextDecrease, contentDescription = "Decrease font")
                    }
                    IconButton(onClick = { fontSize = (fontSize.value + 2).coerceAtMost(32f).sp }) {
                        Icon(Icons.Default.TextIncrease, contentDescription = "Increase font")
                    }
                }
            )
        },
        bottomBar = {
            epubContent?.let { content ->
                Surface(
                    tonalElevation = 1.dp,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp) // Even more compact
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.onChapterSelected(currentIndex - 1) },
                            enabled = currentIndex > 0,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous", modifier = Modifier.size(16.dp))
                        }
                        
                        Text(
                            text = "${currentIndex + 1} / ${content.chapters.size}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                        
                        IconButton(
                            onClick = { viewModel.onChapterSelected(currentIndex + 1) },
                            enabled = currentIndex < content.chapters.size - 1,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            val selection = textFieldValue.selection
            AnimatedVisibility(
                visible = !selection.collapsed,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Eraser Action
                    FloatingActionButton(
                        onClick = {
                            viewModel.removeHighlightsInRange(selection.start, selection.end)
                            textFieldValue = textFieldValue.copy(selection = TextRange(selection.end))
                        },
                        containerColor = Color.White,
                        contentColor = Color.Black,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
                    ) {
                        Icon(Icons.Default.FormatColorReset, contentDescription = "Remove highlight")
                    }

                    // Highlight Action
                    FloatingActionButton(
                        onClick = {
                            val text = textFieldValue.text.substring(selection.start, selection.end)
                            viewModel.addHighlight(selection.start, selection.end, text)
                            textFieldValue = textFieldValue.copy(selection = TextRange(selection.end))
                        },
                        containerColor = Color(0xFFFFF176),
                        contentColor = Color.Black,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Highlight")
                    }

                    // Add Word Action
                    FloatingActionButton(
                        onClick = { 
                            val word = textFieldValue.text.substring(selection.start, selection.end).trim()
                            if (word.isNotEmpty()) {
                                viewModel.addWordToVocabulary(word, word)
                                textFieldValue = textFieldValue.copy(selection = TextRange(selection.end))
                                scope.launch {
                                    snackbarHostState.showSnackbar("Added: $word")
                                }
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add word")
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            val content = epubContent
            if (content == null) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Preparing book...", style = MaterialTheme.typography.labelMedium)
                }
            } else if (content.chapters.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("This book format is not fully supported.")
                }
            } else {
                val chapter = content.chapters[currentIndex]
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp)
                ) {
                    Text(
                        text = chapter.title,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    
                    CompositionLocalProvider(LocalTextToolbar provides emptyTextToolbar) {
                        TextField(
                            value = textFieldValue,
                            onValueChange = { textFieldValue = it },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent
                            ),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                lineHeight = (fontSize.value * 1.6f).sp,
                                fontFamily = FontFamily.Serif,
                                fontSize = fontSize
                            )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}
