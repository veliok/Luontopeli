package com.example.luontopeli.di

import com.example.luontopeli.data.local.dao.NatureSpotDao
import com.example.luontopeli.data.local.dao.WalkSessionDao
import com.example.luontopeli.data.remote.firebase.AuthManager
import com.example.luontopeli.data.remote.firebase.FirestoreManager
import com.example.luontopeli.data.repository.NatureSpotRepository
import com.example.luontopeli.data.repository.WalkRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideNatureSpotRepository(
        dao: NatureSpotDao,
        firestoreManager: FirestoreManager,
        authManager: AuthManager
    ): NatureSpotRepository {
        return NatureSpotRepository(dao, firestoreManager, authManager)
    }

    @Provides
    @Singleton
    fun provideWalkRepository(
        dao: WalkSessionDao,
        firestoreManager: FirestoreManager,
        authManager: AuthManager
    ): WalkRepository {
        return WalkRepository(dao, firestoreManager, authManager)
    }
}