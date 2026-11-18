package com.example.readingapp.di

import android.content.Context
import com.example.readingapp.ReadingApp
import com.example.readingapp.auth.AuthRepository
import com.example.readingapp.common.ActivityProvider
import com.example.readingapp.network.BookApi
import com.example.readingapp.network.BooksApiKeyInterceptor
import com.example.readingapp.network.CacheForOfflineInterceptor
import com.example.readingapp.repo.BookRepository
import com.example.readingapp.config.Constants
import com.example.readingapp.network.AndroidConnectivityObserver
import com.example.readingapp.network.ConnectivityObserver
import com.example.readingapp.network.NetworkInterceptor
import com.example.readingapp.repo.FireRepository
import com.example.readingapp.repo.FireRepository.Companion.BOOKS_COLLECTION
import com.example.readingapp.repo.FireRepository.Companion.SESSIONS_COLLECTION
import com.example.readingapp.repo.FireRepository.Companion.USERS_COLLECTION
import com.example.readingapp.utils.hasInternet
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ReadingAppModule {

    private const val BOOKS_CACHE_BYTES = 10L * 1024 * 1024

    @Provides
    fun provideActivityProvider(
        @ApplicationContext context: Context
    ): ActivityProvider {
        return context as ReadingApp
    }

    @Singleton
    @Provides
    fun provideBookApi(
        @ApplicationContext context: Context,
        booksApiKeyInterceptor: BooksApiKeyInterceptor,
        networkInterceptor: NetworkInterceptor
    ): BookApi {
        val client = OkHttpClient.Builder()
            // Keeps Google Books responses so books seen before still show in full offline.
            .cache(Cache(File(context.cacheDir, "google_books"), BOOKS_CACHE_BYTES))
            .addInterceptor(booksApiKeyInterceptor)
            .addInterceptor(networkInterceptor)
            .addNetworkInterceptor(CacheForOfflineInterceptor())
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
    fun provideConnectivityObserver(
        @ApplicationContext context: Context
    ): ConnectivityObserver = AndroidConnectivityObserver(context)

    @Singleton
    @Provides
    fun providesBookRepo(bookApi: BookApi): BookRepository {
        return BookRepository(bookApi)
    }

    @Singleton
    @Provides
    fun providesFireBookRepo(
        authRepository: AuthRepository,
        @ApplicationContext context: Context
    ): FireRepository {
        val firestore = FirebaseFirestore.getInstance()
        return FireRepository(
            queryBook = firestore.collection(BOOKS_COLLECTION),
            queryUser = firestore.collection(USERS_COLLECTION),
            querySession = firestore.collection(SESSIONS_COLLECTION),
            currentUserId = { authRepository.currentUserId },
            hasConnection = { context.hasInternet() }
        )
    }
}