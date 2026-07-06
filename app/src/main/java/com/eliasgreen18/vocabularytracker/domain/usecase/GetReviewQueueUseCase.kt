package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.model.ReviewWord
import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class GetReviewQueueUseCase @Inject constructor(
    private val repository: WordRepository
) {
    operator fun invoke(): Flow<List<ReviewWord>> {
        val startOfToday = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return repository.getReviewQueue(startOfToday)
    }
}
