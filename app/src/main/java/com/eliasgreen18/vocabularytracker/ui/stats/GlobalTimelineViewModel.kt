package com.eliasgreen18.vocabularytracker.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eliasgreen18.vocabularytracker.domain.model.GlobalTimeline
import com.eliasgreen18.vocabularytracker.domain.usecase.GetGlobalTimelineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class GlobalTimelineViewModel @Inject constructor(
    getGlobalTimelineUseCase: GetGlobalTimelineUseCase
) : ViewModel() {

    val timelineState: StateFlow<GlobalTimeline?> = getGlobalTimelineUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
}
