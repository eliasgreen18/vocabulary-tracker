package com.eliasgreen18.vocabularytracker.domain.repository

interface PhoneticService {
    suspend fun getIpa(text: String): String?
}
