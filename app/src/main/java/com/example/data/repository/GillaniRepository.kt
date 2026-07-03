package com.example.data.repository

import com.example.data.dao.SlipDao
import com.example.data.dao.WAContactDao
import com.example.data.models.Slip
import com.example.data.models.WAContact
import kotlinx.coroutines.flow.Flow

class GillaniRepository(
    private val slipDao: SlipDao,
    private val waContactDao: WAContactDao
) {
    val activeSlips: Flow<List<Slip>> = slipDao.getAllActiveSlips()
    val deletedSlips: Flow<List<Slip>> = slipDao.getAllDeletedSlips()

    suspend fun insertSlip(slip: Slip) {
        slipDao.insertSlip(slip)
    }

    suspend fun updateSlip(slip: Slip) {
        slipDao.updateSlip(slip)
    }

    suspend fun deleteSlipPermanently(id: String) {
        slipDao.deleteSlipPermanently(id)
    }

    suspend fun emptyDeletedSlips() {
        slipDao.emptyDeletedSlips()
    }

    suspend fun deleteDeletedSlipsOlderThan(timeLimit: Long) {
        slipDao.deleteDeletedSlipsOlderThan(timeLimit)
    }

    val activeContacts: Flow<List<WAContact>> = waContactDao.getAllActiveContacts()
    val deletedContacts: Flow<List<WAContact>> = waContactDao.getAllDeletedContacts()

    suspend fun insertContact(contact: WAContact) {
        waContactDao.insertContact(contact)
    }

    suspend fun updateContact(contact: WAContact) {
        waContactDao.updateContact(contact)
    }

    suspend fun deleteContactPermanently(id: Long) {
        waContactDao.deleteContactPermanently(id)
    }

    suspend fun emptyDeletedContacts() {
        waContactDao.emptyDeletedContacts()
    }

    suspend fun deleteDeletedContactsOlderThan(timeLimit: Long) {
        waContactDao.deleteDeletedContactsOlderThan(timeLimit)
    }
}
