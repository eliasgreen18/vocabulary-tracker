package com.eliasgreen18.vocabularytracker.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eliasgreen18.vocabularytracker.domain.model.LearningInsight
import com.eliasgreen18.vocabularytracker.domain.usecase.GetLearningInsightsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class NotificationCenterViewModel @Inject constructor(
    getLearningInsightsUseCase: GetLearningInsightsUseCase
) : ViewModel() {

    val insights: StateFlow<List<LearningInsight>> = getLearningInsightsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
