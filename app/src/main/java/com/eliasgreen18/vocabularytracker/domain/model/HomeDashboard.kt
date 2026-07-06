package com.eliasgreen18.vocabularytracker.domain.model

data class HomeDashboard(
    val activeSessions: List<ActiveSessionInfo>,
    val recentBooks: List<Book>
)
