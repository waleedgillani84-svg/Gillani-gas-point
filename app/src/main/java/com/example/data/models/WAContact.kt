package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "wa_contacts")
data class WAContact(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val num: String,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
) : Serializable
