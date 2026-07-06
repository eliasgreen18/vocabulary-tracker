package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.model.HomeDashboard
import com.eliasgreen18.vocabularytracker.domain.repository.BookRepository
import com.eliasgreen18.vocabularytracker.domain.repository.ReadingSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetHomeDashboardUseCase @Inject constructor(
    private val sessionRepository: ReadingSessionRepository,
    private val bookRepository: BookRepository
) {
    operator fun invoke(): Flow<HomeDashboard> {
        return combine(
            sessionRepository.getAllActiveSessionsWithDetails(),
            bookRepository.getAllBooks()
        ) { activeInfos, books ->
            HomeDashboard(
                activeSessions = activeInfos,
                recentBooks = books
            )
        }
    }
}
