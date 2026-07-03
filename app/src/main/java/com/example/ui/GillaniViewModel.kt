package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.models.Slip
import com.example.data.models.WAContact
import com.example.data.repository.GillaniRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class GillaniViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GillaniRepository
    private val sharedPrefs = application.getSharedPreferences("gillani_gas_prefs", Context.MODE_PRIVATE)

    // Room Database Observables
    val activeSlips: StateFlow<List<Slip>>
    val deletedSlips: StateFlow<List<Slip>>
    val activeContacts: StateFlow<List<WAContact>>
    val deletedContacts: StateFlow<List<WAContact>>

    // General App Settings Flow
    private val _showShopDetails = MutableStateFlow(sharedPrefs.getBoolean("show_shop_details", true))
    val showShopDetails: StateFlow<Boolean> = _showShopDetails.asStateFlow()

    private val _slipDesign = MutableStateFlow(sharedPrefs.getString("slip_design", "thermal-mega") ?: "thermal-mega")
    val slipDesign: StateFlow<String> = _slipDesign.asStateFlow()

    // Custom Design Configs Flow
    private val _customShopName = MutableStateFlow(sharedPrefs.getString("custom_shop_name", "GILLANI GAS POINT") ?: "GILLANI GAS POINT")
    val customShopName: StateFlow<String> = _customShopName.asStateFlow()

    private val _customSubtitle = MutableStateFlow(sharedPrefs.getString("custom_subtitle", "China Scheme Rana Liaqat Chowk Lahore") ?: "China Scheme Rana Liaqat Chowk Lahore")
    val customSubtitle: StateFlow<String> = _customSubtitle.asStateFlow()

    private val _customFooter = MutableStateFlow(sharedPrefs.getString("custom_footer", "App Creator Junaid Gillani") ?: "App Creator Junaid Gillani")
    val customFooter: StateFlow<String> = _customFooter.asStateFlow()

    private val _customFont = MutableStateFlow(sharedPrefs.getString("custom_font", "monospace") ?: "monospace")
    val customFont: StateFlow<String> = _customFont.asStateFlow()

    private val _customBorder = MutableStateFlow(sharedPrefs.getString("custom_border", "dashed") ?: "dashed")
    val customBorder: StateFlow<String> = _customBorder.asStateFlow()

    private val _customSize = MutableStateFlow(sharedPrefs.getString("custom_size", "big") ?: "big")
    val customSize: StateFlow<String> = _customSize.asStateFlow()

    private val _customBgColor = MutableStateFlow(sharedPrefs.getString("custom_bg_color", "#ffffff") ?: "#ffffff")
    val customBgColor: StateFlow<String> = _customBgColor.asStateFlow()

    // Navigation & UI state
    private val _currentTab = MutableStateFlow("home") // "home", "history", "whatsapp", "settings"
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    private val _historySearchQuery = MutableStateFlow("")
    val historySearchQuery: StateFlow<String> = _historySearchQuery.asStateFlow()

    private val _contactsSearchQuery = MutableStateFlow("")
    val contactsSearchQuery: StateFlow<String> = _contactsSearchQuery.asStateFlow()

    private val _previewSlip = MutableStateFlow<Slip?>(null)
    val previewSlip: StateFlow<Slip?> = _previewSlip.asStateFlow()

    private val _showTrashDialog = MutableStateFlow(false)
    val showTrashDialog: StateFlow<Boolean> = _showTrashDialog.asStateFlow()

    private val _trashTab = MutableStateFlow("slips") // "slips", "contacts"
    val trashTab: StateFlow<String> = _trashTab.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = GillaniRepository(database.slipDao(), database.waContactDao())

        // Collect Flows and keep active states
        activeSlips = repository.activeSlips
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        deletedSlips = repository.deletedSlips
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        activeContacts = repository.activeContacts
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        deletedContacts = repository.deletedContacts
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Run automatic 30 days trash cleanup
        cleanupOldTrash()
    }

    private fun cleanupOldTrash() {
        viewModelScope.launch {
            val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
            repository.deleteDeletedSlipsOlderThan(thirtyDaysAgo)
            repository.deleteDeletedContactsOlderThan(thirtyDaysAgo)
        }
    }

    // Tab Navigation
    fun setTab(tab: String) {
        _currentTab.value = tab
    }

    fun setHistorySearchQuery(query: String) {
        _historySearchQuery.value = query
    }

    fun setContactsSearchQuery(query: String) {
        _contactsSearchQuery.value = query
    }

    fun showReceiptPreview(slip: Slip?) {
        _previewSlip.value = slip
    }

    fun setShowTrashDialog(show: Boolean) {
        _showTrashDialog.value = show
    }

    fun setTrashTab(tab: String) {
        _trashTab.value = tab
    }

    // Settings modifiers
    fun setShowShopDetails(show: Boolean) {
        _showShopDetails.value = show
        sharedPrefs.edit().putBoolean("show_shop_details", show).apply()
    }

    fun setSlipDesign(design: String) {
        _slipDesign.value = design
        sharedPrefs.edit().putString("slip_design", design).apply()
    }

    fun saveCustomDesign(
        shopName: String,
        subtitle: String,
        footer: String,
        font: String,
        border: String,
        size: String,
        bgColor: String
    ) {
        _customShopName.value = shopName
        _customSubtitle.value = subtitle
        _customFooter.value = footer
        _customFont.value = font
        _customBorder.value = border
        _customSize.value = size
        _customBgColor.value = bgColor

        sharedPrefs.edit().apply {
            putString("custom_shop_name", shopName)
            putString("custom_subtitle", subtitle)
            putString("custom_footer", footer)
            putString("custom_font", font)
            putString("custom_border", border)
            putString("custom_size", size)
            putString("custom_bg_color", bgColor)
        }.apply()
    }

    // Slip Business Logic
    fun generateSlip(name: String, price: String, phone: String, note: String) {
        viewModelScope.launch {
            val randomId = (100000 + Random().nextInt(900000)).toString()
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            
            val dateStr = dateFormat.format(calendar.time)
            val timeStr = timeFormat.format(calendar.time)

            val newSlip = Slip(
                id = randomId,
                name = name.trim(),
                price = price.trim(),
                phone = phone.trim(),
                note = note.trim(),
                date = dateStr,
                time = timeStr,
                timestamp = System.currentTimeMillis()
            )

            repository.insertSlip(newSlip)
            _previewSlip.value = newSlip
        }
    }

    fun deleteSlipToTrash(slip: Slip) {
        viewModelScope.launch {
            val trashedSlip = slip.copy(
                isDeleted = true,
                deletedAt = System.currentTimeMillis()
            )
            repository.updateSlip(trashedSlip)
        }
    }

    fun restoreSlipFromTrash(slip: Slip) {
        viewModelScope.launch {
            val restoredSlip = slip.copy(
                isDeleted = false,
                deletedAt = null
            )
            repository.updateSlip(restoredSlip)
        }
    }

    fun deleteSlipPermanently(id: String) {
        viewModelScope.launch {
            repository.deleteSlipPermanently(id)
        }
    }

    fun emptyTrashSlips() {
        viewModelScope.launch {
            repository.emptyDeletedSlips()
        }
    }

    // Contacts Business Logic
    fun saveContact(name: String, num: String) {
        viewModelScope.launch {
            val newContact = WAContact(
                name = name.trim(),
                num = num.trim()
            )
            repository.insertContact(newContact)
        }
    }

    fun deleteContactToTrash(contact: WAContact) {
        viewModelScope.launch {
            val trashedContact = contact.copy(
                isDeleted = true,
                deletedAt = System.currentTimeMillis()
            )
            repository.updateContact(trashedContact)
        }
    }

    fun restoreContactFromTrash(contact: WAContact) {
        viewModelScope.launch {
            val restoredContact = contact.copy(
                isDeleted = false,
                deletedAt = null
            )
            repository.updateContact(restoredContact)
        }
    }

    fun deleteContactPermanently(id: Long) {
        viewModelScope.launch {
            repository.deleteContactPermanently(id)
        }
    }

    fun emptyTrashContacts() {
        viewModelScope.launch {
            repository.emptyDeletedContacts()
        }
    }

    // Export & Import Backup
    fun getBackupDataJson(): String {
        // Create a JSON string containing active slips and contacts
        val slipsJson = activeSlips.value.map { slip ->
            """{"id":"${slip.id}","name":"${escapeJson(slip.name)}","price":"${slip.price}","phone":"${slip.phone}","note":"${escapeJson(slip.note)}","date":"${slip.date}","time":"${slip.time}","timestamp":${slip.timestamp}}"""
        }.joinToString(",")

        val contactsJson = activeContacts.value.map { contact ->
            """{"name":"${escapeJson(contact.name)}","num":"${contact.num}"}"""
        }.joinToString(",")

        return """{
            "slips": [$slipsJson],
            "contacts": [$contactsJson],
            "settings": {
                "showShopDetails": ${showShopDetails.value},
                "slipDesign": "${slipDesign.value}"
            },
            "customDesign": {
                "shopName": "${escapeJson(customShopName.value)}",
                "subtitle": "${escapeJson(customSubtitle.value)}",
                "footer": "${escapeJson(customFooter.value)}",
                "font": "${customFont.value}",
                "border": "${customBorder.value}",
                "size": "${customSize.value}",
                "bgColor": "${customBgColor.value}"
            }
        }""".trimIndent()
    }

    fun restoreBackupDataJson(jsonStr: String): Boolean {
        return try {
            // Very simple robust custom JSON parsing for pure offline stability without heavy gson/moshi imports issues
            // Since we know the schema, we can extract slips and contacts easily!
            val slips = parseSlipsFromJson(jsonStr)
            val contacts = parseContactsFromJson(jsonStr)

            viewModelScope.launch {
                slips.forEach { repository.insertSlip(it) }
                contacts.forEach { repository.insertContact(it) }
            }

            // Restore settings if present
            if (jsonStr.contains("\"showShopDetails\"")) {
                val showDetails = jsonStr.substringAfter("\"showShopDetails\":").substringBefore(",").substringBefore("}").trim().toBoolean()
                setShowShopDetails(showDetails)
            }
            if (jsonStr.contains("\"slipDesign\"")) {
                val design = jsonStr.substringAfter("\"slipDesign\":\"").substringBefore("\"").trim()
                setSlipDesign(design)
            }
            if (jsonStr.contains("\"customDesign\"")) {
                val shopName = jsonStr.substringAfter("\"shopName\":\"").substringBefore("\"")
                val subtitle = jsonStr.substringAfter("\"subtitle\":\"").substringBefore("\"")
                val footer = jsonStr.substringAfter("\"footer\":\"").substringBefore("\"")
                val font = jsonStr.substringAfter("\"font\":\"").substringBefore("\"")
                val border = jsonStr.substringAfter("\"border\":\"").substringBefore("\"")
                val size = jsonStr.substringAfter("\"size\":\"").substringBefore("\"")
                val bgColor = jsonStr.substringAfter("\"bgColor\":\"").substringBefore("\"")
                saveCustomDesign(shopName, subtitle, footer, font, border, size, bgColor)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun escapeJson(str: String): String {
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t")
    }

    private fun parseSlipsFromJson(jsonStr: String): List<Slip> {
        val list = mutableListOf<Slip>()
        val slipsSection = jsonStr.substringAfter("\"slips\":[").substringBefore("],")
        if (slipsSection.trim().isEmpty() || slipsSection == jsonStr) return emptyList()

        val itemRegex = """\{"id":"([^"]*)","name":"([^"]*)","price":"([^"]*)","phone":"([^"]*)","note":"([^"]*)","date":"([^"]*)","time":"([^"]*)","timestamp":(\d+)\}""".toRegex()
        val matches = itemRegex.findAll(slipsSection)
        for (match in matches) {
            val (id, name, price, phone, note, date, time, timestampStr) = match.destructured
            list.add(
                Slip(
                    id = id,
                    name = name,
                    price = price,
                    phone = phone,
                    note = note,
                    date = date,
                    time = time,
                    timestamp = timestampStr.toLongOrNull() ?: System.currentTimeMillis()
                )
            )
        }
        return list
    }

    private fun parseContactsFromJson(jsonStr: String): List<WAContact> {
        val list = mutableListOf<WAContact>()
        val contactsSection = jsonStr.substringAfter("\"contacts\":[").substringBefore("],")
        if (contactsSection.trim().isEmpty() || contactsSection == jsonStr) return emptyList()

        val itemRegex = """\{"name":"([^"]*)","num":"([^"]*)"\}""".toRegex()
        val matches = itemRegex.findAll(contactsSection)
        for (match in matches) {
            val (name, num) = match.destructured
            list.add(
                WAContact(
                    name = name,
                    num = num
                )
            )
        }
        return list
    }
}
