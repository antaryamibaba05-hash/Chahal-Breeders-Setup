package com.highflyerpro.tracker.presentation.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.highflyerpro.tracker.presentation.viewmodel.MainViewModel
import com.highflyerpro.tracker.util.BackupMetadata
import com.highflyerpro.tracker.util.BackupOperationResult
import com.highflyerpro.tracker.util.BackupRestoreManager
import com.highflyerpro.tracker.util.DailyBackupWorker
import com.highflyerpro.tracker.util.RestoreOperationResult
import com.highflyerpro.tracker.util.RestoreSummary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataAndBackupSettingsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val lastBackupTs by viewModel.preferencesRepository.lastBackupTimestampFlow.collectAsState(initial = 0L)
    val backupFolderUri by viewModel.preferencesRepository.backupFolderUriFlow.collectAsState(initial = null)
    val autoBackupEnabled by viewModel.preferencesRepository.autoBackupEnabledFlow.collectAsState(initial = true)
    val retentionPolicy by viewModel.preferencesRepository.backupRetentionFlow.collectAsState(initial = "KEEP_FOREVER")

    var isBackingUp by remember { mutableStateOf(false) }
    var backupResultMetadata by remember { mutableStateOf<BackupMetadata?>(null) }

    var isRestoring by remember { mutableStateOf(false) }
    var restoreProgressStep by remember { mutableStateOf("Preparing...") }
    var restoreProgressFraction by remember { mutableFloatStateOf(0f) }
    var restoreSummary by remember { mutableStateOf<RestoreSummary?>(null) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var previewMetadata by remember { mutableStateOf<BackupMetadata?>(null) }
    var selectedZipUri by remember { mutableStateOf<Uri?>(null) }

    // Folder Picker
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            } catch (e: Exception) {
                android.util.Log.e("DataAndBackupSettingsScreen", "Failed to take persistable URI permission: ${e.message}", e)
            }

            coroutineScope.launch {
                viewModel.preferencesRepository.setBackupFolderUri(uri.toString())
                DailyBackupWorker.scheduleDailyBackup(context)
            }
        }
    }

    // ZIP File Picker for Restore
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedZipUri = uri
            coroutineScope.launch {
                val result = BackupRestoreManager.validateBackup(context, uri)
                if (result.isValid && result.metadata != null) {
                    previewMetadata = result.metadata
                } else {
                    errorMessage = result.errorMessage ?: "This file is not a valid High Flyer Pro Tracker backup."
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Data & Backup", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Backup Status Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.CloudSync, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                        Column {
                            Text("Automatic Daily Backups", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(
                                if (lastBackupTs > 0) "Last backup: " + SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(lastBackupTs))
                                else "No backups created yet",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // Quick Actions Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Backup & Restore Actions", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

                    Button(
                        onClick = {
                            isBackingUp = true
                            coroutineScope.launch {
                                val uriToUse = backupFolderUri?.let { Uri.parse(it) }
                                val result = BackupRestoreManager.createBackup(
                                    context = context,
                                    database = viewModel.repository.database,
                                    repository = viewModel.repository,
                                    folderUri = uriToUse
                                )
                                isBackingUp = false
                                when (result) {
                                    is BackupOperationResult.Success -> {
                                        viewModel.preferencesRepository.setLastBackupTimestamp(System.currentTimeMillis())
                                        backupResultMetadata = result.metadata
                                    }
                                    is BackupOperationResult.Error -> {
                                        errorMessage = result.message
                                    }
                                }
                            }
                        },
                        enabled = !isBackingUp,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Backup, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("BACKUP NOW", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            filePickerLauncher.launch(arrayOf("application/zip", "application/x-zip-compressed", "*/*"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("RESTORE DATA")
                    }

                    OutlinedButton(
                        onClick = { folderPickerLauncher.launch(null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("CHANGE BACKUP LOCATION")
                    }
                }
            }

            // Configuration Settings Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Configuration", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Automatic Daily Backup", fontWeight = FontWeight.Bold)
                            Text("Runs once every 24 hours via WorkManager", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = autoBackupEnabled,
                            onCheckedChange = { checked ->
                                coroutineScope.launch {
                                    viewModel.preferencesRepository.setAutoBackupEnabled(checked)
                                    if (checked) DailyBackupWorker.scheduleDailyBackup(context)
                                }
                            }
                        )
                    }

                    HorizontalDivider()

                    Column {
                        Text("Backup Retention Policy", fontWeight = FontWeight.Bold)
                        Text("How long to keep past backup ZIP files", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = retentionPolicy == "KEEP_FOREVER",
                                onClick = { coroutineScope.launch { viewModel.preferencesRepository.setBackupRetention("KEEP_FOREVER") } },
                                label = { Text("Keep Forever") }
                            )
                            FilterChip(
                                selected = retentionPolicy == "KEEP_30",
                                onClick = { coroutineScope.launch { viewModel.preferencesRepository.setBackupRetention("KEEP_30") } },
                                label = { Text("Last 30") }
                            )
                            FilterChip(
                                selected = retentionPolicy == "KEEP_60",
                                onClick = { coroutineScope.launch { viewModel.preferencesRepository.setBackupRetention("KEEP_60") } },
                                label = { Text("Last 60") }
                            )
                        }
                    }
                }
            }
        }
    }

    // Backup Progress Dialog
    if (isBackingUp) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Generating Backup") },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    CircularProgressIndicator()
                    Text("Packaging database, records & photos...")
                }
            },
            confirmButton = { }
        )
    }

    // Backup Success Dialog
    backupResultMetadata?.let { meta ->
        AlertDialog(
            onDismissRequest = { backupResultMetadata = null },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(36.dp)) },
            title = { Text("BACKUP COMPLETED SUCCESSFULLY", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("File: ${meta.fileName}", style = MaterialTheme.typography.labelMedium)
                    Text("Pigeons: ${meta.totalPigeons}", style = MaterialTheme.typography.bodySmall)
                    Text("Flights: ${meta.totalFlights}", style = MaterialTheme.typography.bodySmall)
                    Text("Photos: ${meta.totalPhotos}", style = MaterialTheme.typography.bodySmall)
                    Text("Size: ${meta.backupSizeBytes / 1024} KB", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(onClick = { backupResultMetadata = null }) {
                    Text("OK")
                }
            }
        )
    }

    // Backup Preview Dialog
    previewMetadata?.let { meta ->
        AlertDialog(
            onDismissRequest = { previewMetadata = null },
            title = { Text("Backup Preview", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Backup Date: ${meta.backupDate}")
                    Text("Pigeons: ${meta.totalPigeons}")
                    Text("Flights: ${meta.totalFlights}")
                    Text("Breeding: ${meta.totalBreedingPairs}")
                    Text("Health: ${meta.totalHealthRecords}")
                    Text("Photos: ${meta.totalPhotos}")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val uriToRestore = selectedZipUri ?: return@Button
                        previewMetadata = null
                        isRestoring = true
                        coroutineScope.launch {
                            val res = BackupRestoreManager.restoreBackup(
                                context = context,
                                database = viewModel.repository.database,
                                zipUri = uriToRestore,
                                onProgress = { step, pct ->
                                    restoreProgressStep = step
                                    restoreProgressFraction = pct
                                }
                            )
                            isRestoring = false
                            when (res) {
                                is RestoreOperationResult.Success -> {
                                    restoreSummary = res.summary
                                }
                                is RestoreOperationResult.Error -> {
                                    errorMessage = res.message
                                }
                            }
                        }
                    }
                ) {
                    Text("RESTORE BACKUP")
                }
            },
            dismissButton = {
                TextButton(onClick = { previewMetadata = null }) { Text("CANCEL") }
            }
        )
    }

    // Restore Progress Dialog
    if (isRestoring) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Restoring Data") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(restoreProgressStep)
                    LinearProgressIndicator(progress = { restoreProgressFraction }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = { }
        )
    }

    // Restore Summary Dialog
    restoreSummary?.let { summary ->
        AlertDialog(
            onDismissRequest = { restoreSummary = null },
            icon = { Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF16A34A)) },
            title = { Text("RESTORE COMPLETED") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Pigeons Restored: ${summary.pigeonsRestored}")
                    Text("Flights Restored: ${summary.flightsRestored}")
                    Text("Photos Restored: ${summary.photosRestored}")
                    Text("Breeding Records: ${summary.breedingRecordsRestored}")
                    Text("Health Records: ${summary.healthRecordsRestored}")
                }
            },
            confirmButton = {
                Button(onClick = { restoreSummary = null }) {
                    Text("DONE")
                }
            }
        )
    }

    // Error Dialog
    errorMessage?.let { err ->
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text("Backup Error") },
            text = { Text(err) },
            confirmButton = {
                Button(onClick = { errorMessage = null }) { Text("OK") }
            }
        )
    }
}
