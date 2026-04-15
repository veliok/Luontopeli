package com.example.luontopeli.data.local.dao

import androidx.room.*
import com.example.luontopeli.data.local.entity.WalkSession
import kotlinx.coroutines.flow.Flow

@Dao
interface WalkSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(walk: WalkSession): Long

    @Query("SELECT * FROM walk_sessions WHERE isActive = 0 ORDER BY startTime DESC LIMIT 1")
    fun getLatestFinishedSession(): Flow<WalkSession?>

    @Query("SELECT * FROM walk_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<WalkSession>>

    @Update
    suspend fun update(walk: WalkSession)
}