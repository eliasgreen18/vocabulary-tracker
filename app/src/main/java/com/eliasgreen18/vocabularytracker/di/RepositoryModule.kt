package com.eliasgreen18.vocabularytracker.di

import com.eliasgreen18.vocabularytracker.data.repository.BookRepositoryImpl
import com.eliasgreen18.vocabularytracker.data.repository.ChapterRepositoryImpl
import com.eliasgreen18.vocabularytracker.data.repository.ReadingSessionRepositoryImpl
import com.eliasgreen18.vocabularytracker.data.repository.WordRepositoryImpl
import com.eliasgreen18.vocabularytracker.domain.repository.BookRepository
import com.eliasgreen18.vocabularytracker.domain.repository.ChapterRepository
import com.eliasgreen18.vocabularytracker.domain.repository.ReadingSessionRepository
import com.eliasgreen18.vocabularytracker.domain.repository.WordRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBookRepository(
        bookRepositoryImpl: BookRepositoryImpl
    ): BookRepository

    @Binds
    @Singleton
    abstract fun bindReadingSessionRepository(
        readingSessionRepositoryImpl: ReadingSessionRepositoryImpl
    ): ReadingSessionRepository

    @Binds
    @Singleton
    abstract fun bindChapterRepository(
        chapterRepositoryImpl: ChapterRepositoryImpl
    ): ChapterRepository

    @Binds
    @Singleton
    abstract fun bindWordRepository(
        wordRepositoryImpl: WordRepositoryImpl
    ): WordRepository
}
