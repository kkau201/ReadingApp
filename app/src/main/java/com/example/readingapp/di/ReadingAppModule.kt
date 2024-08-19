package com.example.readingapp.di

import android.content.Context
import com.example.readingapp.ReadingApp
import com.example.readingapp.common.ActivityProvider
import com.example.readingapp.network.BookApi
import com.example.readingapp.repo.BookRepository
import com.example.readingapp.config.Constants
import com.example.readingapp.network.NetworkInterceptor
import com.example.readingapp.repo.FireRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ReadingAppModule {

    @Provides
    fun provideActivityProvider(
        @ApplicationContext context: Context
    ): ActivityProvider {
        return context as ReadingApp
    }

    @Singleton
    @Provides
    fun provideBookApi(
        networkInterceptor: NetworkInterceptor
    ): BookApi {
        val client = OkHttpClient.Builder()
            .addInterceptor(networkInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create()
    }

    @Singleton
    @Provides
    fun providesBookRepo(bookApi: BookApi): BookRepository {
        return BookRepository(bookApi)
    }

    @Singleton
    @Provides
    fun providesFireBookRepo(): FireRepository {
        val firestore = FirebaseFirestore.getInstance()
        return FireRepository(
            queryBook = firestore.collection("books"),
            queryUser = firestore.collection("users")
        )
    }
}