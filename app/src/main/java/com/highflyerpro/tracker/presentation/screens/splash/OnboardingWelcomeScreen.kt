package com.highflyerpro.tracker.presentation.screens.splash

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.highflyerpro.tracker.util.BackupMetadata
import com.highflyerpro.tracker.util.BackupOperationResult
import com.highflyerpro.tracker.util.BackupRestoreManager
import com.highflyerpro.tracker.util.RestoreOperationResult
import com.highflyerpro.tracker.util.RestoreSummary
import com.highflyerpro.tracker.presentation.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingWelcomeScreen(
    viewModel: MainViewModel,
    onStartFreshSelected: () -> Unit,
    onRestoreCompleted: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isValidating by remember { mutableStateOf(false) }
    var isRestoring by remember { mutableStateOf(false) }
    var restoreProgressStep by remember { mutableStateOf("Preparing Restore...") }
    var restoreProgressFraction by remember { mutableFloatStateOf(0f) }

    var previewMetadata by remember { mutableStateOf<BackupMetadata?>(null) }
    var selectedZipUri by remember { mutableStateOf<Uri?>(null) }

    var restoreSummary by remember { mutableStateOf<RestoreSummary?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // File Picker for ZIP
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedZipUri = uri
            coroutineScope.launch {
                isValidating = true
                val result = BackupRestoreManager.validateBackup(context, uri)
                isValidating = false
                if (result.isValid && result.metadata != null) {
                    previewMetadata = result.metadata
                } else {
                    errorMessage = result.errorMessage ?: "This file is not a valid High Flyer Pro Tracker backup."
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier
                    .size(96.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.FlightTakeoff,
                        contentDescription = "Logo",
                        modifier = Modifier.size(54.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "High Flyer Pro Tracker",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Professional Pigeon Loft Management System",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Welcome to your offline-first pigeon loft operating system. Choose how you would like to begin.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                viewModel.preferencesRepository.setInitialized(true)
                                onStartFreshSelected()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AddCircleOutline, contentDescription = null)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("START FRESH", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            filePickerLauncher.launch(arrayOf("application/zip", "application/x-zip-compressed", "*/*"))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("RESTORE DATA", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }

    // Loading / Validation Dialog
    if (isValidating) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Validating Backup") },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text("Reading backup file structure...")
                }
            },
            confirmButton = { }
        )
    }

    // Backup Preview Dialog
    previewMetadata?.let { meta ->
        AlertDialog(
            onDismissRequest = { previewMetadata = null },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Backup Preview", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Verify backup contents before restoring:", style = MaterialTheme.typography.bodySmall)
                    HorizontalDivider()

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Backup Date:", style = MaterialTheme.typography.labelMedium)
                        Text(meta.backupDate, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("App Version:", style = MaterialTheme.typography.labelMedium)
                        Text(meta.appVersion, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Pigeons:", style = MaterialTheme.typography.labelMedium)
                        Text("${meta.totalPigeons}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Flight Records:", style = MaterialTheme.typography.labelMedium)
                        Text("${meta.totalFlights}", fontWeight = FontWeight.Bold)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Breeding Records:", style = MaterialTheme.typography.labelMedium)
                        Text("${meta.totalBreedingPairs}", fontWeight = FontWeight.Bold)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Health Records:", style = MaterialTheme.typography.labelMedium)
                        Text("${meta.totalHealthRecords}", fontWeight = FontWeight.Bold)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Achievements:", style = MaterialTheme.typography.labelMedium)
                        Text("${meta.totalAchievements}", fontWeight = FontWeight.Bold)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Photos:", style = MaterialTheme.typography.labelMedium)
                        Text("${meta.totalPhotos}", fontWeight = FontWeight.Bold)
                    }
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
                                    viewModel.preferencesRepository.setInitialized(true)
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
                TextButton(onClick = { previewMetadata = null }) {
                    Text("CANCEL")
                }
            }
        )
    }

    // Restore Progress Dialog
    if (isRestoring) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Restoring Data") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(restoreProgressStep, style = MaterialTheme.typography.bodyMedium)
                    LinearProgressIndicator(
                        progress = { restoreProgressFraction },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("${(restoreProgressFraction * 100).toInt()}% Complete", style = MaterialTheme.typography.labelSmall)
                }
            },
            confirmButton = { }
        )
    }

    // Restore Summary Screen Dialog
    restoreSummary?.let { summary ->
        AlertDialog(
            onDismissRequest = { },
            icon = { Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(36.dp)) },
            title = { Text("RESTORE COMPLETED SUCCESSFULLY", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("All pigeon records and photos have been restored.", style = MaterialTheme.typography.bodySmall)
                    HorizontalDivider()

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Pigeons Restored:", style = MaterialTheme.typography.labelMedium)
                        Text("${summary.pigeonsRestored}", fontWeight = FontWeight.Bold)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Flights Restored:", style = MaterialTheme.typography.labelMedium)
                        Text("${summary.flightsRestored}", fontWeight = FontWeight.Bold)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Photos Restored:", style = MaterialTheme.typography.labelMedium)
                        Text("${summary.photosRestored}", fontWeight = FontWeight.Bold)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Breeding Records:", style = MaterialTheme.typography.labelMedium)
                        Text("${summary.breedingRecordsRestored}", fontWeight = FontWeight.Bold)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Health Records:", style = MaterialTheme.typography.labelMedium)
                        Text("${summary.healthRecordsRestored}", fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        restoreSummary = null
                        onRestoreCompleted()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("CONTINUE TO APP")
                }
            }
        )
    }

    // Error / Invalid Backup Dialog
    errorMessage?.let { err ->
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Invalid Backup File", fontWeight = FontWeight.Bold) },
            text = { Text(err, style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                Button(onClick = {
                    errorMessage = null
                    filePickerLauncher.launch(arrayOf("application/zip", "application/x-zip-compressed", "*/*"))
                }) {
                    Text("TRY AGAIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { errorMessage = null }) {
                    Text("CANCEL")
                }
            }
        )
    }
}
