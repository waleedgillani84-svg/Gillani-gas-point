package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "slips")
data class Slip(
    @PrimaryKey val id: String, // 6-digit random string
    val name: String,
    val price: String, // Keep exact price entered
    val phone: String,
    val note: String,
    val date: String,
    val time: String,
    val timestamp: Long,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
) : Serializable
