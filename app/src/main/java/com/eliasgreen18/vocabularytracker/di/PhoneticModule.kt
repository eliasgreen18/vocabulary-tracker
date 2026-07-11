package com.eliasgreen18.vocabularytracker.di

import com.eliasgreen18.vocabularytracker.data.remote.phonetics.LocalPhoneticService
import com.eliasgreen18.vocabularytracker.domain.repository.PhoneticService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PhoneticModule {

    @Binds
    @Singleton
    abstract fun bindPhoneticService(
        impl: LocalPhoneticService
    ): PhoneticService
}
