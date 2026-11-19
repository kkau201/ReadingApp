package com.kaylakautai.inkle.di

import com.kaylakautai.inkle.coroutines.CoroutineDispatcherProvider
import com.kaylakautai.inkle.coroutines.RealCoroutineDispatcherProvider
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