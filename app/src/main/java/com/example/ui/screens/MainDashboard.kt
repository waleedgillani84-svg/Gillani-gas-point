package com.example.ui.screens

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.example.data.models.Slip
import com.example.data.models.WAContact
import com.example.ui.GillaniViewModel
import com.example.ui.theme.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MainDashboard(viewModel: GillaniViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val previewSlip by viewModel.previewSlip.collectAsState()
    val showTrashDialog by viewModel.showTrashDialog.collectAsState()
    val showShopDetails by viewModel.showShopDetails.collectAsState()

    val customShopName by viewModel.customShopName.collectAsState()
    val customSubtitle by viewModel.customSubtitle.collectAsState()
    val customFooter by viewModel.customFooter.collectAsState()
    val customFont by viewModel.customFont.collectAsState()
    val customBorder by viewModel.customBorder.collectAsState()
    val customSize by viewModel.customSize.collectAsState()
    val customBgColor by viewModel.customBgColor.collectAsState()

    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopHeader() },
        bottomBar = { BottomNavBar(currentTab = currentTab, onTabSelected = { viewModel.setTab(it) }) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg)
                .padding(innerPadding)
        ) {
            when (currentTab) {
                "home" -> HomeScreen(viewModel = viewModel)
                "history" -> HistoryScreen(viewModel = viewModel)
                "whatsapp" -> ContactsScreen(viewModel = viewModel)
                "settings" -> SettingsScreen(viewModel = viewModel)
            }
        }
    }

    // Receipt Preview Modal
    if (previewSlip != null) {
        ReceiptPreviewDialog(
            slip = previewSlip!!,
            showShopDetails = showShopDetails,
            customShopName = customShopName,
            customSubtitle = customSubtitle,
            customFooter = customFooter,
            customFont = customFont,
            customBorder = customBorder,
            customSize = customSize,
            customBgColor = customBgColor,
            onDismiss = { viewModel.showReceiptPreview(null) }
        )
    }

    // Trash / Recycle Bin Modal
    if (showTrashDialog) {
        TrashDialog(viewModel = viewModel)
    }
}

@Composable
fun TopHeader() {
    val dateText = remember {
        val today = Date()
        val format = SimpleDateFormat("EEEE, d MMM yyyy", Locale.getDefault())
        format.format(today)
    }

    Surface(
        color = PrimaryOrange,
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "GILLANI GAS POINT",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = dateText.uppercase(),
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Flame Icon",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun BottomNavBar(currentTab: String, onTabSelected: (String) -> Unit) {
    Surface(
        color = CardBg,
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, BorderSlate)
    ) {
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavBarItem(
                label = "Nayi Slip",
                icon = Icons.Default.Home,
                isActive = currentTab == "home",
                onClick = { onTabSelected("home") },
                testTag = "nav_tab_home"
            )
            NavBarItem(
                label = "History",
                icon = Icons.Default.List,
                isActive = currentTab == "history",
                onClick = { onTabSelected("history") },
                testTag = "nav_tab_history"
            )
            NavBarItem(
                label = "Contacts",
                icon = Icons.Default.Person,
                isActive = currentTab == "whatsapp",
                onClick = { onTabSelected("whatsapp") },
                testTag = "nav_tab_contacts"
            )
            NavBarItem(
                label = "Settings",
                icon = Icons.Default.Settings,
                isActive = currentTab == "settings",
                onClick = { onTabSelected("settings") },
                testTag = "nav_tab_settings"
            )
        }
    }
}

@Composable
fun RowScope.NavBarItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val color = if (isActive) PrimaryOrange else TextMuted
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick)
            .testTag(testTag)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ================= SLIP MAKER / HOME SCREEN =================
@Composable
fun HomeScreen(viewModel: GillaniViewModel) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, BorderSlate),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawBehind {
                                val strokeWidth = 1.dp.toPx()
                                drawLine(
                                    color = BorderSlate,
                                    start = Offset(0f, size.height + 8.dp.toPx()),
                                    end = Offset(size.width, size.height + 8.dp.toPx()),
                                    strokeWidth = strokeWidth
                                )
                            }
                            .padding(bottom = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(PrimaryOrange.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Icon",
                                tint = PrimaryOrange,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "NAYI SLIP BANAYEN",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Customer ki details darj karein",
                                color = TextMuted,
                                fontSize = 9.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Customer Name
                    Column {
                        Text(
                            text = "Customer Ka Naam:",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = { Text("Naam likhen", fontSize = 13.sp, color = TextMuted) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = DarkBg,
                                unfocusedContainerColor = DarkBg,
                                focusedBorderColor = PrimaryOrange,
                                unfocusedBorderColor = BorderSlate
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("cust_name_input"),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    // Price
                    Column {
                        Text(
                            text = "Amount (Rs.):",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            placeholder = { Text("0.00", fontSize = 16.sp, color = TextMuted) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryOrange
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkBg,
                                unfocusedContainerColor = DarkBg,
                                focusedBorderColor = PrimaryOrange,
                                unfocusedBorderColor = BorderSlate
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("cust_price_input"),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Phone Number
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "WhatsApp / Phone:",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                placeholder = { Text("Number likhen", fontSize = 12.sp, color = TextMuted) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = DarkBg,
                                    unfocusedContainerColor = DarkBg,
                                    focusedBorderColor = PrimaryOrange,
                                    unfocusedBorderColor = BorderSlate
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("cust_phone_input"),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }

                        // Extra Notes
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Extra Note (Optional):",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            OutlinedTextField(
                                value = note,
                                onValueChange = { note = it },
                                placeholder = { Text("Cylinder size wagera", fontSize = 12.sp, color = TextMuted) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = DarkBg,
                                    unfocusedContainerColor = DarkBg,
                                    focusedBorderColor = PrimaryOrange,
                                    unfocusedBorderColor = BorderSlate
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("cust_note_input"),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (name.trim().isEmpty() || price.trim().isEmpty()) {
                                Toast.makeText(context, "⚠️ Customer ka Naam aur Amount zaroori hai!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.generateSlip(name, price, phone, note)
                            focusManager.clearFocus()
                            name = ""
                            price = ""
                            phone = ""
                            note = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_slip_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Print Icon",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SLIP PRINT KAREN",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ================= HISTORY SCREEN =================
@Composable
fun HistoryScreen(viewModel: GillaniViewModel) {
    val activeSlips by viewModel.activeSlips.collectAsState()
    val searchQuery by viewModel.historySearchQuery.collectAsState()
    val context = LocalContext.current

    val filteredSlips = remember(activeSlips, searchQuery) {
        val q = searchQuery.lowercase()
        if (q.isEmpty()) activeSlips else {
            activeSlips.filter {
                it.name.lowercase().contains(q) ||
                it.id.contains(q) ||
                it.price.contains(q)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setHistorySearchQuery(it) },
            placeholder = { Text("Naam, ID ya Price se search karein...", fontSize = 12.sp, color = TextMuted) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = TextMuted) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = CardBg,
                unfocusedContainerColor = CardBg,
                focusedBorderColor = PrimaryOrange,
                unfocusedBorderColor = BorderSlate
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("history_search_input"),
            shape = RoundedCornerShape(10.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PICHLI SLIPS (${filteredSlips.size})",
                color = OnBackgroundLight,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            TextButton(
                onClick = {
                    if (activeSlips.isEmpty()) return@TextButton
                    android.app.AlertDialog.Builder(context)
                        .setTitle("Recycle Bin")
                        .setMessage("Sari history Recycle Bin mein move ho jayegi. Kya aap sure hain?")
                        .setPositiveButton("Yes") { dialog, _ ->
                            activeSlips.forEach { viewModel.deleteSlipToTrash(it) }
                            Toast.makeText(context, "Sari slips trash me shift ho gayi hain!", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        }
                        .setNegativeButton("No", null)
                        .show()
                },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Trash",
                    tint = Color.Red,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Clear All", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (filteredSlips.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Empty History",
                        tint = TextMuted.copy(alpha = 0.5f),
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Koi history nahi hai.", color = TextMuted, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredSlips, key = { it.id }) { slip ->
                    HistoryItemCard(
                        slip = slip,
                        onViewClick = { viewModel.showReceiptPreview(slip) },
                        onDeleteClick = {
                            viewModel.deleteSlipToTrash(slip)
                            Toast.makeText(context, "Slip Recycle Bin me bhej di gayi hai!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(
    slip: Slip,
    onViewClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BorderSlate),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onViewClick)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = slip.name.uppercase(),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${slip.date} | ${slip.time} | #${slip.id}",
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (slip.note.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Note: ${slip.note}",
                            color = PrimaryOrange.copy(alpha = 0.8f),
                            fontSize = 9.sp,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }

                Text(
                    text = "Rs. ${slip.price}",
                    color = PrimaryOrange,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = BorderSlate, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(4.dp))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                TextButton(
                    onClick = onDeleteClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red.copy(alpha = 0.8f),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Delete",
                        color = Color.Red.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ================= CONTACTS SCREEN =================
@Composable
fun ContactsScreen(viewModel: GillaniViewModel) {
    val contacts by viewModel.activeContacts.collectAsState()
    val searchQuery by viewModel.contactsSearchQuery.collectAsState()

    var contactName by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    val filteredContacts = remember(contacts, searchQuery) {
        val q = searchQuery.lowercase()
        if (q.isEmpty()) contacts else {
            contacts.filter {
                it.name.lowercase().contains(q) ||
                it.num.contains(q)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Quick input card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderSlate),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "DIRECT MESSAGE / ADD CONTACT",
                        color = Color.Green,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    OutlinedTextField(
                        value = contactName,
                        onValueChange = { contactName = it },
                        placeholder = { Text("Save Karne Ke Liye Naam (Optional)", fontSize = 11.sp, color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = DarkBg,
                            unfocusedContainerColor = DarkBg,
                            focusedBorderColor = PrimaryOrange,
                            unfocusedBorderColor = BorderSlate
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = contactPhone,
                        onValueChange = { contactPhone = it },
                        placeholder = { Text("Number (e.g. 03001234567)", fontSize = 11.sp, color = TextMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = DarkBg,
                            unfocusedContainerColor = DarkBg,
                            focusedBorderColor = PrimaryOrange,
                            unfocusedBorderColor = BorderSlate
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Message Button
                        Button(
                            onClick = {
                                if (contactPhone.length < 10) {
                                    Toast.makeText(context, "Sahi number likhen!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                triggerWhatsApp(context, contactPhone)
                                focusManager.clearFocus()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "WA", tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Message", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        // Call Button
                        Button(
                            onClick = {
                                if (contactPhone.length < 10) {
                                    Toast.makeText(context, "Sahi number likhen!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                triggerCall(context, contactPhone)
                                focusManager.clearFocus()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = "Call", tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Call", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        // Save Button
                        Button(
                            onClick = {
                                if (contactPhone.length < 10) {
                                    Toast.makeText(context, "Sahi number likhen!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val nameStr = if (contactName.trim().isEmpty()) "Unknown Contact" else contactName.trim()
                                viewModel.saveContact(nameStr, contactPhone)
                                contactName = ""
                                contactPhone = ""
                                focusManager.clearFocus()
                                Toast.makeText(context, "✅ Contact Save Ho Gaya!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CardBg),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, BorderSlate),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = "Save", tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Search contacts
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setContactsSearchQuery(it) },
                placeholder = { Text("Contacts search karen...", fontSize = 12.sp, color = TextMuted) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = TextMuted) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = CardBg,
                    unfocusedContainerColor = CardBg,
                    focusedBorderColor = PrimaryOrange,
                    unfocusedBorderColor = BorderSlate
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )
        }

        if (filteredContacts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Koi contact save nahi hai.", color = TextMuted, fontSize = 12.sp)
                }
            }
        } else {
            items(filteredContacts, key = { it.id }) { c ->
                ContactItemRow(
                    contact = c,
                    onMessage = { triggerWhatsApp(context, c.num) },
                    onCall = { triggerCall(context, c.num) },
                    onDelete = {
                        viewModel.deleteContactToTrash(c)
                        Toast.makeText(context, "Contact Trash me shift ho gaya!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
fun ContactItemRow(
    contact: WAContact,
    onMessage: () -> Unit,
    onCall: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BorderSlate),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(BorderSlate, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = contact.name.take(1).uppercase(),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text(
                        text = contact.name.uppercase(),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = contact.num,
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onMessage, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Message",
                        tint = Color(0xFF25D366),
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(onClick = onCall, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call",
                        tint = Color(0xFF3B82F6),
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ================= SETTINGS SCREEN =================
@Composable
fun SettingsScreen(viewModel: GillaniViewModel) {
    val slipDesign by viewModel.slipDesign.collectAsState()
    val showShopDetails by viewModel.showShopDetails.collectAsState()

    val customShopName by viewModel.customShopName.collectAsState()
    val customSubtitle by viewModel.customSubtitle.collectAsState()
    val customFooter by viewModel.customFooter.collectAsState()
    val customFont by viewModel.customFont.collectAsState()
    val customBorder by viewModel.customBorder.collectAsState()
    val customSize by viewModel.customSize.collectAsState()
    val customBgColor by viewModel.customBgColor.collectAsState()

    val context = LocalContext.current

    // Document creation and pickers launcher for Backup/Restore
    val createDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri != null) {
            try {
                val json = viewModel.getBackupDataJson()
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(json.toByteArray())
                }
                Toast.makeText(context, "✅ Backup Download Ho Gaya!", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val openDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bytes = stream.readBytes()
                    val json = String(bytes)
                    val success = viewModel.restoreBackupDataJson(json)
                    if (success) {
                        Toast.makeText(context, "✅ Data Successfully Restore Ho Gaya!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "❌ Kharab File! Restore nahi ho saka.", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Design template selector
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "SLIP DESIGN (PRINTER OPTIMIZED)",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DesignSelectorCard(
                        title = "MEGA",
                        sub = "Big Thermal",
                        isSelected = slipDesign == "thermal-mega",
                        onClick = { viewModel.setSlipDesign("thermal-mega") },
                        modifier = Modifier.weight(1f)
                    )
                    DesignSelectorCard(
                        title = "PRO",
                        sub = "Clean Thermal",
                        isSelected = slipDesign == "thermal-pro",
                        onClick = { viewModel.setSlipDesign("thermal-pro") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DesignSelectorCard(
                        title = "Modern",
                        sub = "Clean Colors",
                        isSelected = slipDesign == "modern",
                        onClick = { viewModel.setSlipDesign("modern") },
                        modifier = Modifier.weight(1f)
                    )
                    DesignSelectorCard(
                        title = "Classic",
                        sub = "Elegant Serif",
                        isSelected = slipDesign == "classic",
                        onClick = { viewModel.setSlipDesign("classic") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Custom Slip Selector
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (slipDesign == "custom") PrimaryOrange.copy(alpha = 0.1f) else CardBg),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (slipDesign == "custom") PrimaryOrange else BorderSlate),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setSlipDesign("custom") }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(PrimaryOrange.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Settings, contentDescription = "Custom Icon", tint = PrimaryOrange, modifier = Modifier.size(16.dp))
                            }
                            Column {
                                Text(
                                    text = "Apna Custom Design",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Dukaan ke mutabiq slip badlen",
                                    color = TextMuted,
                                    fontSize = 8.sp
                                )
                            }
                        }
                        if (slipDesign == "custom") {
                            Icon(imageVector = Icons.Default.Check, contentDescription = "Selected", tint = PrimaryOrange, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Custom design editor form (Only visible when "custom" layout is active)
        if (slipDesign == "custom") {
            item {
                CustomDesignEditorSection(
                    viewModel = viewModel,
                    shopName = customShopName,
                    subtitle = customSubtitle,
                    footer = customFooter,
                    font = customFont,
                    border = customBorder,
                    size = customSize,
                    bgColor = customBgColor
                )
            }
        }

        // Print details toggles
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "PRINT SETTINGS",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BorderSlate),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Dukaan Ke Numbers Dikhayen?",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Slip ke neechay teenon contact print honge",
                                color = TextMuted,
                                fontSize = 8.sp
                            )
                        }

                        Switch(
                            checked = showShopDetails,
                            onCheckedChange = { viewModel.setShowShopDetails(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryOrange,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = BorderSlate
                            )
                        )
                    }
                }
            }
        }

        // Backups & Data Store
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "DATA MANAGEMENT (OFFLINE BACKUP)",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Export Button
                    Button(
                        onClick = {
                            val today = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
                            createDocLauncher.launch("Gillani_Gas_Point_Backup_$today.txt")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A).copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF1E40AF).copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Backup Download (TXT File)", color = Color(0xFF60A5FA), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Backup", tint = Color(0xFF60A5FA), modifier = Modifier.size(16.dp))
                        }
                    }

                    // Import Button
                    Button(
                        onClick = { openDocLauncher.launch("text/plain") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF064E3B).copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF065F46).copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Backup Restore (File Select Karen)", color = Color(0xFF34D399), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Restore", tint = Color(0xFF34D399), modifier = Modifier.size(16.dp))
                        }
                    }

                    // Recycle Bin / Trash Trigger
                    Button(
                        onClick = { viewModel.setShowTrashDialog(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F1D1D).copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF991B1B).copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Recycle Bin (Trash)", color = Color(0xFFF87171), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Trash Bin", tint = Color(0xFFF87171), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // App Footer Version info
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "APP VERSION 5.3 (100% OFFLINE & COMPACT PRO)",
                    color = TextMuted.copy(alpha = 0.6f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Gillani Gas Management",
                    color = TextMuted.copy(alpha = 0.6f),
                    fontSize = 8.sp
                )
            }
        }
    }
}

@Composable
fun DesignSelectorCard(
    title: String,
    sub: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = if (isSelected) PrimaryOrange.copy(alpha = 0.1f) else CardBg),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isSelected) PrimaryOrange else BorderSlate),
        modifier = modifier
            .height(56.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = if (isSelected) PrimaryOrange else Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = sub,
                color = TextMuted,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CustomDesignEditorSection(
    viewModel: GillaniViewModel,
    shopName: String,
    subtitle: String,
    footer: String,
    font: String,
    border: String,
    size: String,
    bgColor: String
) {
    var editedShopName by remember(shopName) { mutableStateOf(shopName) }
    var editedSubtitle by remember(subtitle) { mutableStateOf(subtitle) }
    var editedFooter by remember(footer) { mutableStateOf(footer) }
    var editedFont by remember(font) { mutableStateOf(font) }
    var editedBorder by remember(border) { mutableStateOf(border) }
    var editedSize by remember(size) { mutableStateOf(size) }
    var editedBgColor by remember(bgColor) { mutableStateOf(bgColor) }

    val context = LocalContext.current

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkBg),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, PrimaryOrange.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "CUSTOM DESIGN EDITOR",
                color = PrimaryOrange,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            // Shop Name field
            Column {
                Text("Dukaan Ka Naam:", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = editedShopName,
                    onValueChange = { editedShopName = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = PrimaryOrange, unfocusedBorderColor = BorderSlate
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp)
                )
            }

            // Subtitle field
            Column {
                Text("Chota Header / Pata:", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = editedSubtitle,
                    onValueChange = { editedSubtitle = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = PrimaryOrange, unfocusedBorderColor = BorderSlate
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp)
                )
            }

            // Footer field
            Column {
                Text("Footer Message:", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = editedFooter,
                    onValueChange = { editedFooter = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = PrimaryOrange, unfocusedBorderColor = BorderSlate
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp)
                )
            }

            // Dropdowns
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Font option
                Column(modifier = Modifier.weight(1f)) {
                    Text("Font Style:", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    CustomSelectionMenu(
                        selectedValue = editedFont,
                        options = listOf("monospace" to "Retro Mono", "sans-serif" to "Modern Sans", "serif" to "Elegant Serif"),
                        onSelected = { editedFont = it }
                    )
                }

                // Border option
                Column(modifier = Modifier.weight(1f)) {
                    Text("Border Line:", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    CustomSelectionMenu(
                        selectedValue = editedBorder,
                        options = listOf("dashed" to "Dashed (---)", "solid" to "Solid (___)", "double" to "Double (===)", "none" to "No Border"),
                        onSelected = { editedBorder = it }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Size scheme
                Column(modifier = Modifier.weight(1f)) {
                    Text("Text Size:", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    CustomSelectionMenu(
                        selectedValue = editedSize,
                        options = listOf("normal" to "Normal", "big" to "Big Bold", "giant" to "Mega Giant"),
                        onSelected = { editedSize = it }
                    )
                }

                // Bg color option
                Column(modifier = Modifier.weight(1f)) {
                    Text("Background:", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    CustomSelectionMenu(
                        selectedValue = editedBgColor,
                        options = listOf("#ffffff" to "White Paper", "#f8fafc" to "Creamy Slate", "#fef3c7" to "Vintage Yellow"),
                        onSelected = { editedBgColor = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    viewModel.saveCustomDesign(
                        shopName = editedShopName,
                        subtitle = editedSubtitle,
                        footer = editedFooter,
                        font = editedFont,
                        border = editedBorder,
                        size = editedSize,
                        bgColor = editedBgColor
                    )
                    Toast.makeText(context, "✅ Custom Slip Design Settings Save Ho Gayi Hain!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                Text("Save Design Settings", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CustomSelectionMenu(
    selectedValue: String,
    options: List<Pair<String, String>>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val displayLabel = options.find { it.first == selectedValue }?.second ?: selectedValue

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .background(CardBg, RoundedCornerShape(6.dp))
            .border(1.dp, BorderSlate, RoundedCornerShape(6.dp))
            .clickable { expanded = !expanded }
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(displayLabel, color = Color.White, fontSize = 11.sp)
            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Dropdown", tint = TextMuted, modifier = Modifier.size(12.dp))
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(CardBg)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.second, color = Color.White, fontSize = 11.sp) },
                    onClick = {
                        onSelected(option.first)
                        expanded = false
                    }
                )
            }
        }
    }
}

// ================= RECEIPT PREVIEW DIALOG =================
@Composable
fun ReceiptPreviewDialog(
    slip: Slip,
    showShopDetails: Boolean,
    customShopName: String,
    customSubtitle: String,
    customFooter: String,
    customFont: String,
    customBorder: String,
    customSize: String,
    customBgColor: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var captureViewRef by remember { mutableStateOf<View?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
                .wrapContentHeight(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.width(360.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header row with close button
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            .size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }

                // Slip Container captured inside AndroidView containing ComposeView for precise bitmap measurement
                Card(
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AndroidView(
                        factory = { ctx ->
                            androidx.compose.ui.platform.ComposeView(ctx).apply {
                                setContent {
                                    ReceiptContent(
                                        slip = slip,
                                        showShopDetails = showShopDetails,
                                        customShopName = customShopName,
                                        customSubtitle = customSubtitle,
                                        customFooter = customFooter,
                                        customFont = customFont,
                                        customBorder = customBorder,
                                        customSize = customSize,
                                        customBgColor = customBgColor
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        update = { composeView ->
                            composeView.setContent {
                                ReceiptContent(
                                    slip = slip,
                                    showShopDetails = showShopDetails,
                                    customShopName = customShopName,
                                    customSubtitle = customSubtitle,
                                    customFooter = customFooter,
                                    customFont = customFont,
                                    customBorder = customBorder,
                                    customSize = customSize,
                                    customBgColor = customBgColor
                                )
                            }
                            captureViewRef = composeView
                        }
                    )
                }

                // Quick share buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val viewToCapture = captureViewRef
                            if (viewToCapture != null) {
                                shareReceiptImage(context, viewToCapture, slip.id)
                            } else {
                                Toast.makeText(context, "Slip drawing. Try again in a second!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share Image", tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Image", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            shareReceiptText(context, slip, showShopDetails, customShopName, customSubtitle)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share Text", tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Text", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ================= DYNAMIC RECEIPT CONTENT FOR ALL DESIGNS =================
@Composable
fun ReceiptContent(
    slip: Slip,
    showShopDetails: Boolean,
    customShopName: String,
    customSubtitle: String,
    customFooter: String,
    customFont: String,
    customBorder: String,
    customSize: String,
    customBgColor: String
) {
    val preferences = LocalContext.current.getSharedPreferences("gillani_gas_prefs", Context.MODE_PRIVATE)
    val design = preferences.getString("slip_design", "thermal-mega") ?: "thermal-mega"

    val parsedBg = try {
        Color(android.graphics.Color.parseColor(customBgColor))
    } catch (e: Exception) {
        Color.White
    }

    when (design) {
        "thermal-mega" -> ThermalMegaLayout(slip = slip, showShopDetails = showShopDetails)
        "thermal-pro" -> ThermalProLayout(slip = slip, showShopDetails = showShopDetails)
        "modern" -> ModernLayout(slip = slip, showShopDetails = showShopDetails)
        "classic" -> ClassicLayout(slip = slip, showShopDetails = showShopDetails)
        "custom" -> CustomConfigurableLayout(
            slip = slip,
            showShopDetails = showShopDetails,
            shopName = customShopName,
            subtitle = customSubtitle,
            footer = customFooter,
            font = customFont,
            border = customBorder,
            sizeScheme = customSize,
            bgColor = parsedBg
        )
        else -> ThermalMegaLayout(slip = slip, showShopDetails = showShopDetails)
    }
}

@Composable
fun ThermalMegaLayout(slip: Slip, showShopDetails: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 12.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "GILLANI GAS",
            color = Color.Black,
            fontSize = 32.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
        Text(
            text = "China Scheme Rana Liaqat Chowk Lahore",
            color = Color.Black,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        DashedDivider()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(slip.date, color = Color.Black, fontSize = 15.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Text(slip.time, color = Color.Black, fontSize = 15.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }

        Text(
            text = "BILL NO: #${slip.id}",
            color = Color.Black,
            fontSize = 15.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Black,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            textAlign = TextAlign.Left
        )

        SolidDivider()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text("NAME:", color = Color.Black, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Text(
                text = slip.name.uppercase(),
                color = Color.Black,
                fontSize = 32.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                lineHeight = 34.sp
            )
            if (slip.phone.isNotEmpty()) {
                Text("PH: ${slip.phone}", color = Color.Black, fontSize = 15.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
            }
        }

        if (slip.note.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, Color.Black)
                    .padding(6.dp)
            ) {
                Text("NOTE: ${slip.note.uppercase()}", color = Color.Black, fontSize = 15.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }

        SolidDivider()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("TOTAL AMOUNT", color = Color.Black, fontSize = 16.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Text("RS. ${slip.price}", color = Color.Black, fontSize = 48.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
        }

        if (showShopDetails) {
            DashedDivider()
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("ZOHAIB GILLANI: 0319-1483382", color = Color.Black, fontSize = 13.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
                Text("MUSHTAQ MALIK: 0304-8907521", color = Color.Black, fontSize = 13.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
                Text("JUNAID GILLANI: 0317-6407904", color = Color.Black, fontSize = 13.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
            }
        }

        DashedDivider()
        Text("App Creator Junaid Gillani", color = Color.Black, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ThermalProLayout(slip: Slip, showShopDetails: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 12.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "GILLANI GAS POINT",
            color = Color.Black,
            fontSize = 24.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
        Text(
            text = "China Scheme Rana Liaqat Chowk Lahore",
            color = Color.Black,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        DashedDivider()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Dt: ${slip.date}", color = Color.Black, fontSize = 13.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Text("Tm: ${slip.time}", color = Color.Black, fontSize = 13.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }

        Text(
            text = "Bill #: ${slip.id}",
            color = Color.Black,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            textAlign = TextAlign.Left
        )

        DashedDivider()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text("Customer Name:", color = Color.Black, fontSize = 13.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Text(
                text = slip.name.uppercase(),
                color = Color.Black,
                fontSize = 26.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                lineHeight = 28.sp
            )
            if (slip.phone.isNotEmpty()) {
                Text("Ph: ${slip.phone}", color = Color.Black, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 2.dp))
            }
        }

        if (slip.note.isNotEmpty()) {
            Text(
                text = "Note: ${slip.note}",
                color = Color.Black,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                textAlign = TextAlign.Left
            )
        }

        DashedDivider()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Amount Payable", color = Color.Black, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Text("Rs. ${slip.price}", color = Color.Black, fontSize = 40.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
        }

        if (showShopDetails) {
            DashedDivider()
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("ZOHAIB GILLANI: 0319-1483382", color = Color.Black, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Text("MUSHTAQ MALIK: 0304-8907521", color = Color.Black, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Text("JUNAID GILLANI: 0317-6407904", color = Color.Black, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }

        DashedDivider()
        Text("App Creator Junaid Gillani", color = Color.Black, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ModernLayout(slip: Slip, showShopDetails: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFAFAFA))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .background(Color(0xFFF97316), RoundedCornerShape(8.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = "GILLANI GAS",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )
        }
        Text(
            text = "China Scheme Rana Liaqat Chowk Lahore",
            color = Color(0xFF475569),
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${slip.date}   ${slip.time}",
                        color = Color(0xFF475569),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "#${slip.id}",
                        color = Color(0xFFF97316),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF1F5F9), thickness = 1.5.dp)

                Text(
                    text = "BILLED TO",
                    color = Color(0xFF64748B),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = slip.name.uppercase(),
                    color = Color(0xFF0F172A),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
                if (slip.phone.isNotEmpty()) {
                    Text(
                        text = slip.phone,
                        color = Color(0xFF0F172A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        if (slip.note.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.5.dp, Color(0xFFCBD5E1)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Note: ${slip.note}",
                    color = Color(0xFF334155),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("TOTAL AMOUNT", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Text("Rs. ${slip.price}", color = Color(0xFFF97316), fontSize = 28.sp, fontWeight = FontWeight.Black)
            }
        }

        if (showShopDetails) {
            Spacer(modifier = Modifier.height(10.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text("Zohaib Gillani: 0319-1483382", color = Color(0xFF0F172A), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("Mushtaq Malik: 0304-8907521", color = Color(0xFF0F172A), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("Junaid Gillani: 0317-6407904", color = Color(0xFF0F172A), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Divider(color = Color(0xFFE2E8F0), thickness = 1.dp)
        Spacer(modifier = Modifier.height(6.dp))
        Text("App Creator Junaid Gillani", color = Color(0xFF64748B), fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ClassicLayout(slip: Slip, showShopDetails: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(12.dp)
            .border(3.dp, Color.Black)
            .padding(4.dp)
            .border(1.dp, Color.Black)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "GILLANI GAS",
            color = Color.Black,
            fontSize = 22.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp
        )
        Text(
            text = "China Scheme Rana Liaqat Chowk Lahore",
            color = Color.Black,
            fontSize = 10.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 2.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))
        Divider(color = Color.Black, thickness = 1.5.dp)
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Date: ${slip.date}", color = Color.Black, fontSize = 11.sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
            Text("Time: ${slip.time}", color = Color.Black, fontSize = 11.sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
        }
        Text(
            text = "Receipt No: #${slip.id}",
            color = Color.Black,
            fontSize = 11.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            textAlign = TextAlign.Left
        )

        Spacer(modifier = Modifier.height(6.dp))
        Divider(color = Color.Black, thickness = 1.dp)
        Spacer(modifier = Modifier.height(6.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text("Customer Detail:", color = Color.Black, fontSize = 11.sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
            Text(
                text = slip.name.uppercase(),
                color = Color.Black,
                fontSize = 20.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Black
            )
            if (slip.phone.isNotEmpty()) {
                Text("Phone: ${slip.phone}", color = Color.Black, fontSize = 12.sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 2.dp))
            }
        }

        if (slip.note.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Note: ${slip.note}",
                color = Color.Black,
                fontSize = 11.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Left
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Divider(color = Color.Black, thickness = 1.dp)
        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("AMOUNT PAYABLE", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
                Text("Rs. ${slip.price}", color = Color.White, fontSize = 24.sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
            }
        }

        if (showShopDetails) {
            Spacer(modifier = Modifier.height(10.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.Black)
                    .padding(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text("Zohaib Gillani: 0319-1483382", color = Color.Black, fontSize = 11.sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
                Text("Mushtaq Malik: 0304-8907521", color = Color.Black, fontSize = 11.sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
                Text("Junaid Gillani: 0317-6407904", color = Color.Black, fontSize = 11.sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Divider(color = Color.Black, thickness = 1.dp)
        Spacer(modifier = Modifier.height(6.dp))
        Text("App Creator Junaid Gillani", color = Color.Black, fontSize = 10.sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CustomConfigurableLayout(
    slip: Slip,
    showShopDetails: Boolean,
    shopName: String,
    subtitle: String,
    footer: String,
    font: String,
    border: String,
    sizeScheme: String,
    bgColor: Color
) {
    val selectedFont = when (font) {
        "monospace" -> FontFamily.Monospace
        "sans-serif" -> FontFamily.SansSerif
        "serif" -> FontFamily.Serif
        else -> FontFamily.Monospace
    }

    val headerSize = when (sizeScheme) {
        "normal" -> 20.sp
        "big" -> 26.sp
        "giant" -> 32.sp
        else -> 26.sp
    }

    val nameSize = when (sizeScheme) {
        "normal" -> 22.sp
        "big" -> 28.sp
        "giant" -> 34.sp
        else -> 28.sp
    }

    val priceSize = when (sizeScheme) {
        "normal" -> 32.sp
        "big" -> 40.sp
        "giant" -> 48.sp
        else -> 40.sp
    }

    val bodySize = when (sizeScheme) {
        "normal" -> 11.sp
        "big" -> 13.sp
        "giant" -> 15.sp
        else -> 13.sp
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(vertical = 12.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = shopName.uppercase(),
            color = Color.Black,
            fontSize = headerSize,
            fontFamily = selectedFont,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            lineHeight = (headerSize.value + 2).sp
        )
        if (subtitle.isNotEmpty()) {
            Text(
                text = subtitle,
                color = Color.Black.copy(alpha = 0.8f),
                fontSize = bodySize,
                fontFamily = selectedFont,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        CustomBorderDivider(borderType = border)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("DT: ${slip.date}", color = Color.Black, fontSize = bodySize, fontFamily = selectedFont, fontWeight = FontWeight.Bold)
            Text("TM: ${slip.time}", color = Color.Black, fontSize = bodySize, fontFamily = selectedFont, fontWeight = FontWeight.Bold)
        }

        Text(
            text = "BILL NO: #${slip.id}",
            color = Color.Black,
            fontSize = bodySize,
            fontFamily = selectedFont,
            fontWeight = FontWeight.Black,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            textAlign = TextAlign.Left
        )

        CustomBorderDivider(borderType = border)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text("CUSTOMER NAME:", color = Color.Black.copy(alpha = 0.7f), fontSize = bodySize, fontFamily = selectedFont, fontWeight = FontWeight.Bold)
            Text(
                text = slip.name.uppercase(),
                color = Color.Black,
                fontSize = nameSize,
                fontFamily = selectedFont,
                fontWeight = FontWeight.Black,
                lineHeight = (nameSize.value + 2).sp
            )
            if (slip.phone.isNotEmpty()) {
                Text("PHONE: ${slip.phone}", color = Color.Black, fontSize = bodySize, fontFamily = selectedFont, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 2.dp))
            }
        }

        if (slip.note.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Color.Black)
                    .padding(6.dp)
            ) {
                Text("NOTE: ${slip.note.uppercase()}", color = Color.Black, fontSize = bodySize, fontFamily = selectedFont, fontWeight = FontWeight.Bold)
            }
        }

        CustomBorderDivider(borderType = border)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("TOTAL AMOUNT PAYABLE", color = Color.Black.copy(alpha = 0.8f), fontSize = bodySize, fontFamily = selectedFont, fontWeight = FontWeight.Bold)
            Text("RS. ${slip.price}", color = Color.Black, fontSize = priceSize, fontFamily = selectedFont, fontWeight = FontWeight.Black)
        }

        if (showShopDetails) {
            CustomBorderDivider(borderType = border)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("ZOHAIB GILLANI: 0319-1483382", color = Color.Black, fontSize = bodySize, fontFamily = selectedFont, fontWeight = FontWeight.Bold)
                Text("MUSHTAQ MALIK: 0304-8907521", color = Color.Black, fontSize = bodySize, fontFamily = selectedFont, fontWeight = FontWeight.Bold)
                Text("JUNAID GILLANI: 0317-6407904", color = Color.Black, fontSize = bodySize, fontFamily = selectedFont, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Divider(color = Color.Black.copy(alpha = 0.3f), thickness = 1.dp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(footer, color = Color.Black, fontSize = 10.sp, fontFamily = selectedFont, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DashedDivider() {
    Canvas(
        Modifier
            .fillMaxWidth()
            .height(14.dp)
    ) {
        drawLine(
            color = Color.Black,
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            strokeWidth = 2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )
    }
}

@Composable
fun SolidDivider() {
    Divider(
        color = Color.Black,
        thickness = 2.dp,
        modifier = Modifier.padding(vertical = 6.dp)
    )
}

@Composable
fun CustomBorderDivider(borderType: String) {
    when (borderType) {
        "dashed" -> DashedDivider()
        "solid" -> Divider(color = Color.Black, thickness = 2.dp, modifier = Modifier.padding(vertical = 8.dp))
        "double" -> {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Divider(color = Color.Black, thickness = 1.5.dp)
                Spacer(modifier = Modifier.height(2.dp))
                Divider(color = Color.Black, thickness = 1.5.dp)
            }
        }
        "none" -> Spacer(modifier = Modifier.height(8.dp))
    }
}

// ================= TRASH / RECYCLE BIN MODAL DIALOG =================
@Composable
fun TrashDialog(viewModel: GillaniViewModel) {
    val deletedSlips by viewModel.deletedSlips.collectAsState()
    val deletedContacts by viewModel.deletedContacts.collectAsState()
    val trashTab by viewModel.trashTab.collectAsState()
    val context = LocalContext.current

    Dialog(onDismissRequest = { viewModel.setShowTrashDialog(false) }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            color = DarkBg,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, BorderSlate)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header with close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Trash", tint = Color.Red, modifier = Modifier.size(20.dp))
                        Text("RECYCLE BIN", color = Color.Red, fontSize = 14.sp, fontWeight = FontWeight.Black)
                    }

                    IconButton(onClick = { viewModel.setShowTrashDialog(false) }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BorderSlate),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = "Info", tint = PrimaryOrange, modifier = Modifier.size(16.dp))
                        Text(
                            "Trash items 30 din baad auto-delete ho jayengi.",
                            color = TextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.setTrashTab("slips") },
                        colors = ButtonDefaults.buttonColors(containerColor = if (trashTab == "slips") PrimaryOrange else CardBg),
                        shape = RoundedCornerShape(8.dp),
                        border = if (trashTab != "slips") BorderStroke(1.dp, BorderSlate) else null,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("Deleted Slips", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.setTrashTab("contacts") },
                        colors = ButtonDefaults.buttonColors(containerColor = if (trashTab == "contacts") PrimaryOrange else CardBg),
                        shape = RoundedCornerShape(8.dp),
                        border = if (trashTab != "contacts") BorderStroke(1.dp, BorderSlate) else null,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("Deleted Contacts", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Empty Trash Button
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(
                        onClick = {
                            if (trashTab == "slips") {
                                if (deletedSlips.isEmpty()) return@TextButton
                                android.app.AlertDialog.Builder(context)
                                    .setTitle("Empty Trash")
                                    .setMessage("Deleted Slips ko hamesha ke liye delete kar dein?")
                                    .setPositiveButton("Yes") { dialog, _ ->
                                        viewModel.emptyTrashSlips()
                                        Toast.makeText(context, "Slips Trash khali ho gaya!", Toast.LENGTH_SHORT).show()
                                        dialog.dismiss()
                                    }
                                    .setNegativeButton("No", null)
                                    .show()
                            } else {
                                if (deletedContacts.isEmpty()) return@TextButton
                                android.app.AlertDialog.Builder(context)
                                    .setTitle("Empty Trash")
                                    .setMessage("Deleted Contacts ko hamesha ke liye delete kar dein?")
                                    .setPositiveButton("Yes") { dialog, _ ->
                                        viewModel.emptyTrashContacts()
                                        Toast.makeText(context, "Contacts Trash khali ho gaya!", Toast.LENGTH_SHORT).show()
                                        dialog.dismiss()
                                    }
                                    .setNegativeButton("No", null)
                                    .show()
                            }
                        }
                    ) {
                        Text("Empty Trash", color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // List
                Box(modifier = Modifier.weight(1f)) {
                    if (trashTab == "slips") {
                        if (deletedSlips.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Trash khali hai.", color = TextMuted, fontSize = 11.sp)
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(deletedSlips, key = { it.id }) { s ->
                                    val daysLeft = remember(s.deletedAt) {
                                        if (s.deletedAt == null) 30 else {
                                            val diff = System.currentTimeMillis() - s.deletedAt
                                            val days = 30 - (diff / (1000 * 60 * 60 * 24))
                                            if (days < 0) 0 else days
                                        }
                                    }

                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = CardBg),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, BorderSlate),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(s.name.uppercase(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.LineThrough)
                                                Text("Rs. ${s.price}", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                Text("$daysLeft din baqi hain", color = Color.Red.copy(alpha = 0.8f), fontSize = 8.sp, fontWeight = FontWeight.Black)
                                            }

                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                TextButton(
                                                    onClick = { viewModel.restoreSlipFromTrash(s) },
                                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                                ) {
                                                    Text("Restore", color = Color.Green, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                                TextButton(
                                                    onClick = { viewModel.deleteSlipPermanently(s.id) },
                                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                                ) {
                                                    Text("Delete", color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        if (deletedContacts.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Trash khali hai.", color = TextMuted, fontSize = 11.sp)
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(deletedContacts, key = { it.id }) { c ->
                                    val daysLeft = remember(c.deletedAt) {
                                        if (c.deletedAt == null) 30 else {
                                            val diff = System.currentTimeMillis() - c.deletedAt
                                            val days = 30 - (diff / (1000 * 60 * 60 * 24))
                                            if (days < 0) 0 else days
                                        }
                                    }

                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = CardBg),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, BorderSlate),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(c.name.uppercase(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.LineThrough)
                                                Text(c.num, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                Text("$daysLeft din baqi hain", color = Color.Red.copy(alpha = 0.8f), fontSize = 8.sp, fontWeight = FontWeight.Black)
                                            }

                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                TextButton(
                                                    onClick = { viewModel.restoreContactFromTrash(c) },
                                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                                ) {
                                                    Text("Restore", color = Color.Green, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                                TextButton(
                                                    onClick = { viewModel.deleteContactPermanently(c.id) },
                                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                                ) {
                                                    Text("Delete", color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ================= UTILITIES FOR SHARING & DIALING =================
fun triggerWhatsApp(context: Context, num: String) {
    val cleanNum = num.replace("\\D".toRegex(), "")
    var formattedNum = cleanNum
    if (!formattedNum.startsWith("92")) {
        formattedNum = "92" + (if (formattedNum.startsWith("0")) formattedNum.substring(1) else formattedNum)
    }
    try {
        val uri = Uri.parse("https://wa.me/$formattedNum")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "WhatsApp not installed!", Toast.LENGTH_SHORT).show()
    }
}

fun triggerCall(context: Context, num: String) {
    try {
        val uri = Uri.parse("tel:$num")
        val intent = Intent(Intent.ACTION_DIAL, uri)
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not launch dialer: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

fun shareReceiptImage(context: Context, view: View, slipId: String) {
    try {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)

        val cachePath = File(context.cacheDir, "shared_images")
        cachePath.mkdirs()
        val file = File(cachePath, "Receipt_$slipId.png")
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()

        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, "Receipt #$slipId")
            putExtra(Intent.EXTRA_TEXT, "Apka Gillani Gas Point ka bill munsalik hai.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Receipt Image"))
    } catch (e: Exception) {
        Toast.makeText(context, "Error sharing: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

fun shareReceiptText(
    context: Context,
    slip: Slip,
    showShopDetails: Boolean,
    customShopName: String,
    customSubtitle: String
) {
    val shopName = if (customShopName.isNotEmpty()) customShopName else "GILLANI GAS POINT"
    val subtitle = if (customSubtitle.isNotEmpty()) customSubtitle else "China Scheme Rana Liaqat Chowk Lahore"

    var text = "*$shopName*\n$subtitle\n\n" +
            "*Bill No:* #${slip.id}\n" +
            "*Date:* ${slip.date} | ${slip.time}\n" +
            "*Customer:* ${slip.name}\n" +
            (if (slip.phone.isNotEmpty()) "*Phone:* ${slip.phone}\n" else "") +
            (if (slip.note.isNotEmpty()) "*Note:* ${slip.note}\n" else "") +
            "\n*Total Amount: Rs. ${slip.price}*\n\n"

    if (showShopDetails) {
        text += "Contact details:\n" +
                "Zohaib: 0319-1483382\n" +
                "Mushtaq: 0304-8907521\n" +
                "Junaid: 0317-6407904\n\n"
    }

    text += "Shukriya! Wapis tashreef layen."

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }

    if (slip.phone.isNotEmpty()) {
        val cleanNum = slip.phone.replace("\\D".toRegex(), "")
        var formattedNum = cleanNum
        if (!formattedNum.startsWith("92")) {
            formattedNum = "92" + (if (formattedNum.startsWith("0")) formattedNum.substring(1) else formattedNum)
        }
        try {
            val uri = Uri.parse("https://wa.me/$formattedNum?text=" + Uri.encode(text))
            val waIntent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(waIntent)
            return
        } catch (e: Exception) {
            // Fallback
        }
    }

    context.startActivity(Intent.createChooser(intent, "Share Text via"))
}
