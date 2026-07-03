package com.example.data.dao

import androidx.room.*
import com.example.data.models.WAContact
import kotlinx.coroutines.flow.Flow

@Dao
interface WAContactDao {
    @Query("SELECT * FROM wa_contacts WHERE isDeleted = 0 ORDER BY id DESC")
    fun getAllActiveContacts(): Flow<List<WAContact>>

    @Query("SELECT * FROM wa_contacts WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getAllDeletedContacts(): Flow<List<WAContact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: WAContact)

    @Update
    suspend fun updateContact(contact: WAContact)

    @Query("DELETE FROM wa_contacts WHERE id = :id")
    suspend fun deleteContactPermanently(id: Long)

    @Query("DELETE FROM wa_contacts WHERE isDeleted = 1")
    suspend fun emptyDeletedContacts()

    @Query("DELETE FROM wa_contacts WHERE isDeleted = 1 AND deletedAt < :timeLimit")
    suspend fun deleteDeletedContactsOlderThan(timeLimit: Long)
}
