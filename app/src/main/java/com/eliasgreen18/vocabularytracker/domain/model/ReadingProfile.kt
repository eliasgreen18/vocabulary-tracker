package com.eliasgreen18.vocabularytracker.domain.model

data class ReadingProfile(
    val topBookTeacher: String?,
    val topAuthorTeacher: String?,
    val avgDaysToMaster: Int,
    val totalChaptersRead: Int,
    val learningEfficiency: Int, // Mastery %
    val vocabOrigins: List<BookContribution>
)
