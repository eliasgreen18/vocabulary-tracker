package com.eliasgreen18.vocabularytracker.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eliasgreen18.vocabularytracker.domain.model.ReadingProfile
import com.eliasgreen18.vocabularytracker.domain.usecase.GetReadingProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ReadingProfileViewModel @Inject constructor(
    getReadingProfileUseCase: GetReadingProfileUseCase
) : ViewModel() {

    val profileState: StateFlow<ReadingProfile?> = getReadingProfileUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
}
