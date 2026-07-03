package com.example.data.dao

import androidx.room.*
import com.example.data.models.Slip
import kotlinx.coroutines.flow.Flow

@Dao
interface SlipDao {
    @Query("SELECT * FROM slips WHERE isDeleted = 0 ORDER BY timestamp DESC")
    fun getAllActiveSlips(): Flow<List<Slip>>

    @Query("SELECT * FROM slips WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getAllDeletedSlips(): Flow<List<Slip>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlip(slip: Slip)

    @Update
    suspend fun updateSlip(slip: Slip)

    @Query("DELETE FROM slips WHERE id = :id")
    suspend fun deleteSlipPermanently(id: String)

    @Query("DELETE FROM slips WHERE isDeleted = 1")
    suspend fun emptyDeletedSlips()

    @Query("DELETE FROM slips WHERE isDeleted = 1 AND deletedAt < :timeLimit")
    suspend fun deleteDeletedSlipsOlderThan(timeLimit: Long)
}
