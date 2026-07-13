package com.eliasgreen18.vocabularytracker.ui.reader

import androidx.compose.ui.graphics.Color
import com.eliasgreen18.vocabularytracker.domain.model.ReaderTheme
import com.eliasgreen18.vocabularytracker.ui.theme.LibraryPaper

data class ReaderThemeColors(
    val backgroundColor: Color,
    val textColor: Color,
    val displayName: String
)

fun ReaderTheme.toColors(): ReaderThemeColors {
    return when (this) {
        ReaderTheme.WHITE -> ReaderThemeColors(Color.White, Color.Black, "White")
        ReaderTheme.PAPER -> ReaderThemeColors(LibraryPaper, Color(0xFF322E63), "Paper")
        ReaderTheme.DARK -> ReaderThemeColors(Color(0xFF121212), Color(0xFFE0E0E0), "Dark")
        ReaderTheme.SEPIA -> ReaderThemeColors(Color(0xFFF4ECD8), Color(0xFF5B4636), "Sepia")
    }
}
