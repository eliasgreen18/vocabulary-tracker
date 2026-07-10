package com.eliasgreen18.vocabularytracker.di

import com.eliasgreen18.vocabularytracker.data.repository.UserPreferencesRepositoryImpl
import com.eliasgreen18.vocabularytracker.domain.repository.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PreferencesModule {

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        impl: UserPreferencesRepositoryImpl
    ): UserPreferencesRepository
}
