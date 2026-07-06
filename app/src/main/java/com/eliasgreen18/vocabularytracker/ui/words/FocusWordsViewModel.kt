package com.eliasgreen18.vocabularytracker.ui.words

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eliasgreen18.vocabularytracker.domain.model.WordWithCount
import com.eliasgreen18.vocabularytracker.domain.usecase.GetFocusWordsUseCase
import com.eliasgreen18.vocabularytracker.domain.usecase.ToggleFocusWordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FocusWordsViewModel @Inject constructor(
    getFocusWordsUseCase: GetFocusWordsUseCase,
    private val toggleFocusWordUseCase: ToggleFocusWordUseCase
) : ViewModel() {

    val focusWords: StateFlow<List<WordWithCount>> = getFocusWordsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun removeFocus(wordId: Long) {
        viewModelScope.launch {
            toggleFocusWordUseCase(wordId, false)
        }
    }
}
