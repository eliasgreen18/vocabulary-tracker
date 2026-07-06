package com.eliasgreen18.vocabularytracker.di

import com.eliasgreen18.vocabularytracker.data.remote.LibreTranslateService
import com.eliasgreen18.vocabularytracker.data.remote.MockTranslationService
import com.eliasgreen18.vocabularytracker.data.remote.api.LibreTranslateApi
import com.eliasgreen18.vocabularytracker.domain.repository.TranslationService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    // Switch to a faster/more reliable public instance
    private const val BASE_URL = "https://translate.terraprint.co/"
    private const val USE_MOCK = false

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideLibreTranslateApi(retrofit: Retrofit): LibreTranslateApi {
        return retrofit.create(LibreTranslateApi::class.java)
    }

    @Provides
    @Singleton
    fun provideTranslationService(
        mockService: MockTranslationService,
        realService: LibreTranslateService
    ): TranslationService {
        return if (USE_MOCK) {
            mockService
        } else {
            realService
        }
    }
}
