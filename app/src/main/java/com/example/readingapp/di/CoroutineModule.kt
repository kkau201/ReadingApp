package com.example.readingapp.di

import com.example.readingapp.coroutines.CoroutineDispatcherProvider
import com.example.readingapp.coroutines.RealCoroutineDispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
class CoroutineModule {
    @Provides
    fun provideCoroutineDispatcherProvider(provider: RealCoroutineDispatcherProvider): CoroutineDispatcherProvider {
        return provider
    }
}