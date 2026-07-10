package com.eliasgreen18.vocabularytracker.data.local.dao

import androidx.room.*
import com.eliasgreen18.vocabularytracker.data.local.entity.WordRelationshipEntity
import com.eliasgreen18.vocabularytracker.data.local.entity.WordWithCountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RelationshipDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRelationship(relationship: WordRelationshipEntity)

    @Query("DELETE FROM word_relationships WHERE (wordId = :wordId AND relatedWordId = :relatedId AND type = :type) OR (wordId = :relatedId AND relatedWordId = :wordId AND type = :type)")
    suspend fun deleteRelationship(wordId: Long, relatedId: Long, type: String)

    @Query("""
        SELECT 
            w.id as wordId,
            w.text as wordText,
            0 as sessionCount,
            (SELECT COUNT(*) FROM occurrences o WHERE o.wordId = w.id) as globalCount,
            w.isFocusWord as isFocusWord,
            w.translation as translation,
            w.translationStatus as translationStatus,
            r.type as relationshipType
        FROM words w
        JOIN word_relationships r ON (w.id = r.relatedWordId AND r.wordId = :wordId) OR (w.id = r.wordId AND r.relatedWordId = :wordId)
    """)
    fun getRelatedWordsFlow(wordId: Long): Flow<List<RelatedWordEntity>>
}

data class RelatedWordEntity(
    val wordId: Long,
    val wordText: String,
    val sessionCount: Int,
    val globalCount: Int,
    val isFocusWord: Boolean,
    val translation: String?,
    val translationStatus: String,
    val relationshipType: String
)
