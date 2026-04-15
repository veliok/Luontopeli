package com.example.luontopeli.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luontopeli.data.remote.firebase.AuthManager
import com.example.luontopeli.data.remote.firebase.FirestoreManager
import com.example.luontopeli.data.repository.WalkRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authManager: AuthManager,
    private val firestoreManager: FirestoreManager,
    private val walkRepository: WalkRepository,
    private val healthConnectManager: com.example.luontopeli.data.remote.health.HealthConnectManager
) : ViewModel() {

    // Expose HealthConnectManager for UI to launch permission flow
    val healthConnect = healthConnectManager

    // Seurataan nykyistä käyttäjää
    private val _currentUser = MutableStateFlow<FirebaseUser?>(authManager.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    // Lisätään laskenta kaikille retkille (askeleet, matka, kalorit)
    data class Stats(val steps: Int, val distance: Double, val calories: Float)

    val totalStats = walkRepository.allSessions.map { sessions ->
        val steps = sessions.sumOf { it.stepCount }
        val distance = sessions.sumOf { it.distanceMeters.toDouble() }
        val calories = sessions.sumOf { it.calories.toDouble() }.toFloat()
        Stats(steps, distance, calories)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Stats(0, 0.0, 0f))

    // Lasketaan löydetyt kohteet reaaliajassa
    @OptIn(ExperimentalCoroutinesApi::class)
    val totalSpots: StateFlow<Int> = _currentUser
        .flatMapLatest { user ->
            if (user != null) {
                firestoreManager.getUserSpots(user.uid).map { it.size }
            } else {
                flowOf(0)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun signInAnonymously() {
        viewModelScope.launch {
            val result = authManager.signInAnonymously()
            if (result.isSuccess) {
                // Päivitetään käyttäjätila onnistumisen jälkeen
                _currentUser.value = authManager.currentUser
            }
        }
    }

    fun signOut() {
        authManager.signOut()
        _currentUser.value = null
    }
}