package com.example.luontopeli.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.luontopeli.data.local.entity.WalkSession
import com.example.luontopeli.data.repository.WalkRepository
import com.example.luontopeli.sensor.StepCounterManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class WalkViewModel @Inject constructor(
    application: Application,
    private val walkRepository: WalkRepository,
    private val stepManager: StepCounterManager,
    private val healthConnectManager: com.example.luontopeli.data.remote.health.HealthConnectManager
) : AndroidViewModel(application) {

    // Aktiivisen session tila
    private val _currentSession = MutableStateFlow<WalkSession?>(null)
    val currentSession: StateFlow<WalkSession?> = _currentSession.asStateFlow()

    // Onko kävely käynnissä
    private val _isWalking = MutableStateFlow(false)
    val isWalking: StateFlow<Boolean> = _isWalking.asStateFlow()

    // Aloita uusi kävelysessio
    fun startWalk() {
        if (_isWalking.value) return  // Ei aloiteta uudelleen

        val session = WalkSession()
        _currentSession.value = session
        _isWalking.value = true

        // Rekisteröi askelmittari
        stepManager.startStepCounting {
            // Tämä kutsutaan joka askeleella (taustasäikeessä)
            _currentSession.update { current ->
                current?.copy(
                    stepCount = current.stepCount + 1,
                    distanceMeters = current.distanceMeters + StepCounterManager.STEP_LENGTH_METERS
                )
            }
        }
    }

    // Lopeta kävely ja tallenna sessio
    fun stopWalk() {
        stepManager.stopStepCounting()
        _isWalking.value = false
        // Kopioidaan nykyinen tila, käsitellään tallennus asynkronisesti
        val snapshot = _currentSession.value
        if (snapshot == null) {
            return
        }

        viewModelScope.launch {
            val distance = snapshot.distanceMeters
            // Estimoitu kalorikulutus: n. 60 kcal per km (yksinkertainen arvio)
            var calories = (distance / 1000f) * 60f

            if (healthConnectManager.isAvailable() && snapshot.startTime > 0) {
                try {
                    val start = java.time.Instant.ofEpochMilli(snapshot.startTime)
                    val end = java.time.Instant.ofEpochMilli(System.currentTimeMillis())
                    val hc = healthConnectManager.readStepsAndCalories(start, end)
                    if (hc != null) {
                        calories = hc.second.toFloat()
                    }
                } catch (e: Exception) {

                }
            }

            val finished = snapshot.copy(
                endTime = System.currentTimeMillis(),
                isActive = false,
                calories = calories
            )

            walkRepository.insertSession(finished)
            _currentSession.value = null
        }
    }

    // Siivoaa sensorit kun ViewModel tuhotaan
    override fun onCleared() {
        super.onCleared()
        stepManager.stopAll()
    }
}

// Apufunktiot
fun formatDistance(meters: Float): String {
    return if (meters < 1000f) {
        "${meters.toInt()} m"
    } else {
        "${"%.1f".format(meters / 1000f)} km"
    }
}

fun formatDuration(startTime: Long, endTime: Long = System.currentTimeMillis()): String {
    val seconds = (endTime - startTime) / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    return when {
        hours > 0 -> "${hours}h ${minutes % 60}min"
        minutes > 0 -> "${minutes}min ${seconds % 60}s"
        else -> "${seconds}s"
    }
}