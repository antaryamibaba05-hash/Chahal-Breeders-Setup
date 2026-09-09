package com.highflyerpro.tracker.presentation.screens.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FlightLand
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.highflyerpro.tracker.data.repository.LandingMode
import com.highflyerpro.tracker.data.repository.ThemeMode
import com.highflyerpro.tracker.presentation.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateBack: (() -> Unit)? = null,
    onNavigateToDataAndBackup: (() -> Unit)? = null,
    onNavigateToPermissions: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val themeMode by viewModel.themeMode.collectAsState()
    val landingMode by viewModel.landingMode.collectAsState()

    var showBackupDialog by remember { mutableStateOf(false) }
    var backupJsonText by remember { mutableStateOf("") }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var restoreJsonInput by remember { mutableStateOf("") }
    var isProcessingBackup by remember { mutableStateOf(false) }

    var showCsvExportDialog by remember { mutableStateOf(false) }
    var csvExportContent by remember { mutableStateOf("") }
    var csvExportTitle by remember { mutableStateOf("") }

    var showCsvImportDialog by remember { mutableStateOf(false) }
    var csvImportInput by remember { mutableStateOf("") }
    var showAboutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("settings_back_button")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                title = {
                    Column {
                        Text(text = "SETTINGS & ABOUT", fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Text(text = "High Flyer Pro Tracker • Professional Loft OS", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Data & Backup System Navigation Card
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToDataAndBackup?.invoke() }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Backup, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Data & Automatic Backup", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("ZIP export, daily WorkManager backups, SAF location setup & restore", style = MaterialTheme.typography.bodySmall)
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                    }
                }
            }

            // Permissions & Access Navigation Card
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToPermissions?.invoke() }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Permissions & Access", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("Photo Picker, Camera, Notifications & Backup Folder management", style = MaterialTheme.typography.bodySmall)
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                    }
                }
            }
            // Theme Mode Configuration
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("APPEARANCE THEME", fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 1.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = themeMode == ThemeMode.SYSTEM,
                                onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) },
                                label = { Text("System") }
                            )
                            FilterChip(
                                selected = themeMode == ThemeMode.LIGHT,
                                onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) },
                                label = { Text("Light") }
                            )
                            FilterChip(
                                selected = themeMode == ThemeMode.DARK,
                                onClick = { viewModel.setThemeMode(ThemeMode.DARK) },
                                label = { Text("Dark") }
                            )
                        }
                    }
                }
            }

            // Landing Detection Mode Configuration
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FlightLand, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("FLIGHT LANDING MODE", fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 1.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setLandingMode(LandingMode.CONFIRMATION) }
                        ) {
                            RadioButton(
                                selected = landingMode == LandingMode.CONFIRMATION,
                                onClick = { viewModel.setLandingMode(LandingMode.CONFIRMATION) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Confirmation Mode (Recommended)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Shows prompt dialog to verify landing timestamp & note before saving", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setLandingMode(LandingMode.QUICK_ONE_TAP) }
                        ) {
                            RadioButton(
                                selected = landingMode == LandingMode.QUICK_ONE_TAP,
                                onClick = { viewModel.setLandingMode(LandingMode.QUICK_ONE_TAP) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Quick One-Tap Landing Mode", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Instantly captures landing on touch without confirmation", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            }

            // Offline JSON Full Database Backup & Restore
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Backup, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("OFFLINE JSON BACKUP & RESTORE", fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 1.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Export your entire loft database (300+ pigeons, flight sessions, landing records, health logs, breeding trees) into a single offline JSON backup file.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = {
                                    isProcessingBackup = true
                                    coroutineScope.launch {
                                        backupJsonText = viewModel.repository.exportFullDatabaseJson()
                                        isProcessingBackup = false
                                        showBackupDialog = true
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("export_backup_button")
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Export JSON")
                            }

                            OutlinedButton(
                                onClick = { showRestoreDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("restore_backup_button")
                            ) {
                                Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Restore JSON")
                            }
                        }
                    }
                }
            }

            // CSV Data Management
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Upload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("CSV SPREADSHEET TOOLS", fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 1.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        csvExportTitle = "Pigeons CSV Export"
                                        csvExportContent = viewModel.repository.exportPigeonsCsv()
                                        showCsvExportDialog = true
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Export Pigeons CSV", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        csvExportTitle = "Flight Records CSV Export"
                                        csvExportContent = viewModel.repository.exportFlightRecordsCsv()
                                        showCsvExportDialog = true
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Export Flights CSV", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { showCsvImportDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Import Pigeons from CSV")
                        }
                    }
                }
            }

            // Application Information & About Section
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_about_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("APPLICATION INFORMATION", fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 1.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "MALIK BREEDERS SETUP",
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            letterSpacing = 0.5.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Professional High Flyer Pigeon Management System",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Version 1.0.0 (Offline-First Native Edition)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(12.dp))

                        AboutInfoRow(label = "Application", value = "High Flyer Pro Tracker")
                        Spacer(modifier = Modifier.height(6.dp))
                        AboutInfoRow(label = "System Platform", value = "Native Android (Kotlin)")
                        Spacer(modifier = Modifier.height(6.dp))
                        AboutInfoRow(label = "Database Engine", value = "Room SQLite (Offline-First)")
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Application Purpose:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "A professional offline-first system for managing pigeon records, High Flyer flights, breeding, health, performance and lifetime historical data.",
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedButton(
                            onClick = { showAboutDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("view_full_about_button")
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("View Official System Specifications & Credits", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // Export JSON Dialog
    if (showBackupDialog) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = { Text("Offline Database Export") },
            text = {
                Column {
                    Text("Your complete loft database backup has been generated with official High Flyer Pro Tracker metadata:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = backupJsonText.take(600) + if (backupJsonText.length > 600) "\n... [Total size: ${backupJsonText.length} bytes]" else "",
                        onValueChange = {},
                        readOnly = true,
                        maxLines = 6,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("HighFlyerProTrackerBackup", backupJsonText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Backup copied to clipboard!", Toast.LENGTH_SHORT).show()
                        showBackupDialog = false
                    }
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Backup JSON")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackupDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // Restore JSON Dialog
    if (showRestoreDialog) {
        var restoreMessage by remember { mutableStateOf<String?>(null) }
        var isRestoring by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("Restore Offline JSON Backup") },
            text = {
                Column {
                    Text("Paste your exported JSON database backup below:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = restoreJsonInput,
                        onValueChange = { restoreJsonInput = it },
                        placeholder = { Text("Paste JSON here...") },
                        maxLines = 6,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (restoreMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(restoreMessage!!, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (restoreJsonInput.isNotBlank() && !isRestoring) {
                            isRestoring = true
                            coroutineScope.launch {
                                val result = viewModel.repository.restoreFullDatabaseJson(restoreJsonInput, replaceExisting = true)
                                isRestoring = false
                                restoreMessage = result.message
                            }
                        }
                    },
                    enabled = restoreJsonInput.isNotBlank() && !isRestoring
                ) {
                    Text("Restore Database")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // CSV Export Dialog
    if (showCsvExportDialog) {
        AlertDialog(
            onDismissRequest = { showCsvExportDialog = false },
            title = { Text(csvExportTitle) },
            text = {
                Column {
                    OutlinedTextField(
                        value = csvExportContent.take(1000) + if (csvExportContent.length > 1000) "\n..." else "",
                        onValueChange = {},
                        readOnly = true,
                        maxLines = 8,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("CSVExport", csvExportContent)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "CSV copied to clipboard!", Toast.LENGTH_SHORT).show()
                        showCsvExportDialog = false
                    }
                ) {
                    Text("Copy CSV")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCsvExportDialog = false }) { Text("Close") }
            }
        )
    }

    // CSV Import Dialog
    if (showCsvImportDialog) {
        var importReport by remember { mutableStateOf<String?>(null) }
        var isImporting by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showCsvImportDialog = false },
            title = { Text("Import Pigeons from CSV") },
            text = {
                Column {
                    Text("Format: Name, Ring Number, Breed, Color, Gender, Status")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = csvImportInput,
                        onValueChange = { csvImportInput = it },
                        placeholder = { Text("Kamagar White, PAK-2026-901, Kamagar, White, Male, Active") },
                        maxLines = 6,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (importReport != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(importReport!!, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (csvImportInput.isNotBlank() && !isImporting) {
                            isImporting = true
                            coroutineScope.launch {
                                val res = viewModel.repository.importPigeonsCsv(csvImportInput)
                                isImporting = false
                                importReport = res.message
                            }
                        }
                    },
                    enabled = csvImportInput.isNotBlank() && !isImporting
                ) {
                    Text("Import Birds")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCsvImportDialog = false }) { Text("Close") }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Column {
                    Text(
                        text = "MALIK BREEDERS SETUP",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        letterSpacing = 0.5.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Professional High Flyer Pigeon Management System",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                    item {
                        AboutInfoRow("Application", "High Flyer Pro Tracker")
                    }
                    item {
                        AboutInfoRow("System Platform", "Native Android (Kotlin)")
                    }
                    item {
                        AboutInfoRow("Database Engine", "Room SQLite (Offline-First)")
                    }
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Application Purpose",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "A professional offline-first system for managing pigeon records, High Flyer flights, breeding, health, performance and lifetime historical data.",
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Core Architecture & Capabilities",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "• Scalable Room SQLite Persistence (300+ to 1,000+ birds)\n" +
                                   "• Real Timestamp Flight Calculation (Current Time - Release Time)\n" +
                                   "• Background Recovery & Persistence Protection\n" +
                                   "• Instant One-Tap & Confirmation Landing Modes\n" +
                                   "• Custom Event Scoring & Point Rules\n" +
                                   "• 12-Tab Digital Lifetime Pigeon Profile\n" +
                                   "• 100% Offline-First (No Internet, Firebase, or Server required)",
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showAboutDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun AboutInfoRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
