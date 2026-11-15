package com.example.readingapp.di

import com.example.readingapp.auth.AuthRepository
import com.example.readingapp.auth.CredentialManagerGoogleIdTokenProvider
import com.example.readingapp.auth.FirebaseAuthRepository
import com.example.readingapp.auth.GoogleIdTokenProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    abstract fun bindGoogleIdTokenProvider(
        impl: CredentialManagerGoogleIdTokenProvider
    ): GoogleIdTokenProvider

    @Binds
    abstract fun bindAuthRepository(impl: FirebaseAuthRepository): AuthRepository
}
