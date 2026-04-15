package com.example.luontopeli.di

import com.example.luontopeli.data.remote.firebase.AuthManager
import com.example.luontopeli.data.remote.firebase.FirestoreManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideAuthManager(): AuthManager = AuthManager()

    @Provides
    @Singleton
    fun provideFirestoreManager(): FirestoreManager = FirestoreManager()

    @Suppress("unused")
    @Provides
    @Singleton
    fun provideHealthConnectManager(@dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context): com.example.luontopeli.data.remote.health.HealthConnectManager {
        return com.example.luontopeli.data.remote.health.HealthConnectManager(context)
    }
}