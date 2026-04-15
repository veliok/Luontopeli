package com.example.luontopeli.data.remote.health

import android.content.Context
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant

class HealthConnectManager(private val context: Context) {

    private val healthConnectClient by lazy {
        val status = HealthConnectClient.getSdkStatus(context)

        when (status) {
            HealthConnectClient.SDK_AVAILABLE -> Log.d("LUONTOPELI", "Health Connect: Saatavilla")
            HealthConnectClient.SDK_UNAVAILABLE -> Log.e("LUONTOPELI", "Health Connect: Ei tuettu tällä laitteella")
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> Log.e("LUONTOPELI", "Health Connect: Vaatii päivityksen")
        }

        if (status == HealthConnectClient.SDK_AVAILABLE) {
            HealthConnectClient.getOrCreate(context)
        } else {
            null
        }
    }

    fun isAvailable(): Boolean = healthConnectClient != null

    fun getRequiredPermissions(): Set<String> {
        return setOf(
            HealthPermission.getReadPermission(StepsRecord::class),
            HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
            //HealthPermission.getWritePermission(ActiveCaloriesBurnedRecord::class)
        )
    }

    suspend fun hasAllPermissions(): Boolean {
        val client = healthConnectClient ?: return false
        val granted = client.permissionController.getGrantedPermissions()
        return granted.containsAll(getRequiredPermissions())
    }

    @WorkerThread
    suspend fun readStepsAndCalories(start: Instant, end: Instant): Pair<Long, Double>? {
        val client = healthConnectClient ?: return null

        return withContext(Dispatchers.IO) {
            try {
                val filter = TimeRangeFilter.between(start, end)

                // Rakennetaan pyynnöt
                val stepsRequest = ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = filter
                )
                val caloriesRequest = ReadRecordsRequest(
                    recordType = ActiveCaloriesBurnedRecord::class,
                    timeRangeFilter = filter
                )

                // Suoritetaan kyselyt
                val stepsResponse = client.readRecords(stepsRequest)
                val caloriesResponse = client.readRecords(caloriesRequest)

                // Lasketaan summat
                val totalSteps = stepsResponse.records.sumOf { it.count }
                val totalCalories = caloriesResponse.records.sumOf { it.energy.inKilocalories }

                Log.d("HealthConnect", "Haettu: $totalSteps askelta, $totalCalories kcal")
                Pair(totalSteps, totalCalories)

            } catch (e: Exception) {
                Log.e("HealthConnect", "Virhe datan lukemisessa: ${e.message}")
                null
            }
        }
    }
}