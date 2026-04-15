package com.example.luontopeli.data.repository

import com.example.luontopeli.data.local.dao.WalkSessionDao
import com.example.luontopeli.data.local.entity.WalkSession
import com.example.luontopeli.data.remote.firebase.AuthManager
import com.example.luontopeli.data.remote.firebase.FirestoreManager
import kotlinx.coroutines.flow.Flow

class WalkRepository(
    private val dao: WalkSessionDao,
    private val firestoreManager: FirestoreManager,
    private val authManager: AuthManager
) {
    // Kaikki retket (historia-näkymää varten)
    val allSessions: Flow<List<WalkSession>> = dao.getAllSessions()

    // TÄRKEÄ: Helppo haku edelliselle retkelle
    val latestFinishedSession: Flow<WalkSession?> = dao.getLatestFinishedSession()

    // Aloita uusi retki paikallisesti
    suspend fun insertSession(session: WalkSession) {
        val uid = authManager.currentUserId ?: "offline_user"
        val sessionWithUser = session.copy(userId = uid)
        dao.insert(sessionWithUser)
    }

    // Päivitä retken tiedot (esim. askeleet kävelyn aikana)
    suspend fun updateSession(session: WalkSession) {
        dao.update(session)
    }

    // Lopeta retki ja synkronoi Firebaseen
    suspend fun endAndSyncSession(session: WalkSession) {
        val finishedSession = session.copy(
            isActive = false,
            endTime = System.currentTimeMillis()
        )

        // 1. Tallenna lopetustieto heti Roomiin
        dao.update(finishedSession)

        // 2. Yritä synkronoida Firestoreen
        try {
            firestoreManager.saveWalkSession(finishedSession).getOrThrow()
            // Jos haluat seurata synkronointitilaa kuten havainnoissa:
            // dao.markSynced(finishedSession.id)
        } catch (e: Exception) {
            // Offline-tila: jää vain paikalliseen kantaan
        }
    }
}