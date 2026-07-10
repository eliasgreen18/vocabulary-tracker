package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.domain.model.RelationshipType
import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import javax.inject.Inject

class DeleteWordRelationshipUseCase @Inject constructor(
    private val repository: WordRepository
) {
    suspend operator fun invoke(wordId: Long, relatedId: Long, type: RelationshipType) {
        repository.deleteRelationship(wordId, relatedId, type)
    }
}
