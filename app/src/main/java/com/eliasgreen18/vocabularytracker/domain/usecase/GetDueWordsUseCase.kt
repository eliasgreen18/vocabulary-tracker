package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.model.ReviewWord
import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import javax.inject.Inject

class GetDueWordsUseCase @Inject constructor(
    private val repository: WordRepository
) {
    operator fun invoke(): Flow<List<ReviewWord>> {
        val now = Instant.now().toEpochMilli()
        return repository.getDueWords(now)
    }
}
