package com.eliasgreen18.vocabularytracker.di

import android.content.Context
import androidx.room.Room
import com.eliasgreen18.vocabularytracker.data.local.dictionary.DictionaryDao
import com.eliasgreen18.vocabularytracker.data.local.dictionary.LocalDictionaryDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DictionaryModule {

    @Provides
    @Singleton
    fun provideDictionaryDatabase(@ApplicationContext context: Context): LocalDictionaryDatabase {
        return Room.databaseBuilder(
            context,
            LocalDictionaryDatabase::class.java,
            "dictionary_db"
        ).build()
    }

    @Provides
    fun provideDictionaryDao(database: LocalDictionaryDatabase): DictionaryDao {
        return database.dictionaryDao()
    }
}
