package com.example.readingapp.di

import com.example.readingapp.network.BookApi
import com.example.readingapp.repo.BookRepo
import com.example.readingapp.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ReadingAppModule {

    @Singleton
    @Provides
    fun provideBookApi(): BookApi {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create()
    }

    @Singleton
    @Provides
    fun providesBookRepo(bookApi: BookApi): BookRepo {
        return BookRepo(bookApi)
    }
}