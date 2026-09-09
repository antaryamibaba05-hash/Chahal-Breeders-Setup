package com.highflyerpro.tracker.presentation.screens.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.highflyerpro.tracker.presentation.viewmodel.MainViewModel
import com.highflyerpro.tracker.util.DailyBackupWorker
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsSetupScreen(
    viewModel: MainViewModel,
    onFinishSetup: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var cameraGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    var notificationsGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val selectedBackupFolderUri by viewModel.preferencesRepository.backupFolderUriFlow.collectAsState(initial = null)

    // Camera Permission Launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        cameraGranted = isGranted
        coroutineScope.launch { viewModel.preferencesRepository.setCameraEnabled(isGranted) }
    }

    // Notification Permission Launcher
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        notificationsGranted = isGranted
        coroutineScope.launch { viewModel.preferencesRepository.setNotificationsEnabled(isGranted) }
    }

    // SAF Folder Picker Launcher
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            } catch (e: Exception) {
                android.util.Log.e("PermissionsSetupScreen", "Failed to take persistable URI permission: ${e.message}", e)
            }

            coroutineScope.launch {
                viewModel.preferencesRepository.setBackupFolderUri(uri.toString())
                DailyBackupWorker.scheduleDailyBackup(context)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SETUP PERMISSIONS", fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Configure permissions and auto-backup location to enable full loft management capabilities.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 1. Photos Section
            PermissionCard(
                icon = Icons.Default.PhotoLibrary,
                title = "PHOTO ACCESS",
                description = "Used to select and attach photos to pigeon profiles and flight achievements. Uses zero-permission Photo Picker.",
                statusText = "Ready (Android Photo Picker)",
                isGranted = true,
                onAllowClick = {
                    coroutineScope.launch { viewModel.preferencesRepository.setPhotosGranted(true) }
                }
            )

            // 2. Camera Section
            PermissionCard(
                icon = Icons.Default.CameraAlt,
                title = "CAMERA",
                description = "Used to capture photos of pigeons directly inside the application. Optional.",
                statusText = if (cameraGranted) "Allowed" else "Not Allowed",
                isGranted = cameraGranted,
                onAllowClick = {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            )

            // 3. Notifications Section
            PermissionCard(
                icon = Icons.Default.Notifications,
                title = "NOTIFICATIONS",
                description = "Used for medication, deworming, health reminders, and daily backup completion status.",
                statusText = if (notificationsGranted) "Allowed" else "Not Allowed",
                isGranted = notificationsGranted,
                onAllowClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        notificationsGranted = true
                    }
                }
            )

            // 4. Backup Location Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Folder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text("BACKUP LOCATION", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(
                                if (selectedBackupFolderUri != null) "Folder Connected" else "No External Folder Selected",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selectedBackupFolderUri != null) Color(0xFF16A34A) else MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Text(
                        "Your pigeon records are automatically backed up daily. Choose a safe folder outside application storage (e.g. Documents/High Flyer Pro Backups).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (selectedBackupFolderUri == null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f))
                        ) {
                            Text(
                                "Warning: Automatic backups are not active until a backup location is selected.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    Button(
                        onClick = { folderPickerLauncher.launch(null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.CreateNewFolder, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (selectedBackupFolderUri != null) "CHANGE BACKUP LOCATION" else "SELECT BACKUP LOCATION")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onFinishSetup,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("FINISH SETUP & OPEN APP", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun PermissionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    statusText: String,
    isGranted: Boolean,
    onAllowClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(
                        statusText,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isGranted) Color(0xFF16A34A) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!isGranted) {
                OutlinedButton(
                    onClick = onAllowClick,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("ALLOW")
                }
            }
        }
    }
}
