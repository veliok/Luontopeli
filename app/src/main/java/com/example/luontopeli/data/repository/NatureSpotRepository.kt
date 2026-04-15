package com.example.luontopeli.data.repository

import com.example.luontopeli.data.local.dao.NatureSpotDao
import com.example.luontopeli.data.local.entity.NatureSpot
import com.example.luontopeli.data.remote.firebase.AuthManager
import com.example.luontopeli.data.remote.firebase.FirestoreManager
// Storage is disabled: image uploads are not used
import kotlinx.coroutines.flow.Flow

class NatureSpotRepository(
    private val dao: NatureSpotDao,
    private val firestoreManager: FirestoreManager,
    private val authManager: AuthManager
) {
    val allSpots: Flow<List<NatureSpot>> = dao.getAllSpots()

    // Tallenna löytö: ensin Room, sitten Firebase
    suspend fun insertSpot(spot: NatureSpot) {
        val spotWithUser = spot.copy(userId = authManager.currentUserId)

        // 1. Tallenna paikallisesti HETI (toimii offline-tilassakin)
        dao.insert(spotWithUser.copy(synced = false))

        // 2. Yritä synkronoida Firebaseen
        syncSpotToFirebase(spotWithUser)
    }

    // Synkronoi yksittäinen kohde Firebaseen
    private suspend fun syncSpotToFirebase(spot: NatureSpot) {
        try {
            val spotWithUrl = spot.copy(imageFirebaseUrl = null)
            firestoreManager.saveSpot(spotWithUrl).getOrThrow()

            dao.markSynced(spot.id, "")
        } catch (e: Exception) {
            // Synkronointi epäonnistui – yritetään uudelleen myöhemmin
            // synced = false pysyy Room:ssa
        }
    }

    // Synkronoi kaikki odottavat kohteet (kutsutaan yhteyden palautuessa)
    suspend fun syncPendingSpots() {
        val unsyncedSpots = dao.getUnsyncedSpots()
        unsyncedSpots.forEach { spot ->
            syncSpotToFirebase(spot)
        }
    }
}