package com.example.luontopeli.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

// WalkSession tallentaa yhden kävelylenkin tiedot
@Entity(tableName = "walk_sessions")
data class WalkSession(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val stepCount: Int = 0,
    val distanceMeters: Float = 0f,  // Laskettu: stepCount * STEP_LENGTH_METERS
    val spotsFound: Int = 0,         // Tällä kävelylenkillä löydetyt luontokohteet
    val calories: Float = 0f,
    val isActive: Boolean = true
)