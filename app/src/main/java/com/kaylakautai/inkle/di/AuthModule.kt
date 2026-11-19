package com.kaylakautai.inkle.di

import com.kaylakautai.inkle.auth.AuthRepository
import com.kaylakautai.inkle.auth.CredentialManagerGoogleIdTokenProvider
import com.kaylakautai.inkle.auth.FirebaseAuthRepository
import com.kaylakautai.inkle.auth.GoogleIdTokenProvider
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
