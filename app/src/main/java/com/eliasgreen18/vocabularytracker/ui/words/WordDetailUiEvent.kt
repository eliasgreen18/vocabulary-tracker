package com.eliasgreen18.vocabularytracker.ui.words

sealed class WordDetailUiEvent {
    data class ShowError(val message: String) : WordDetailUiEvent()
    object InsightsGenerated : WordDetailUiEvent()
}
