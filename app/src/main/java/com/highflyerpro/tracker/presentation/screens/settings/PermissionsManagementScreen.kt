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
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.highflyerpro.tracker.presentation.viewmodel.MainViewModel
import com.highflyerpro.tracker.util.DailyBackupWorker
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsManagementScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
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

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        cameraGranted = isGranted
        coroutineScope.launch { viewModel.preferencesRepository.setCameraEnabled(isGranted) }
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        notificationsGranted = isGranted
        coroutineScope.launch { viewModel.preferencesRepository.setNotificationsEnabled(isGranted) }
    }

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            } catch (e: Exception) {
                android.util.Log.e("PermissionsManagementScreen", "Failed to take persistable URI permission: ${e.message}", e)
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
                title = { Text("Permissions & Access", fontWeight = FontWeight.Bold) },
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
            Text("Manage hardware, storage, and system notification access.", style = MaterialTheme.typography.bodySmall)

            // Photo Picker
            PermissionRowCard(
                icon = Icons.Default.PhotoLibrary,
                title = "Photo Access",
                subtitle = "Android Photo Picker (Zero broad storage permission required)",
                status = "Granted",
                isOk = true,
                buttonText = "Active",
                onClick = {
                    android.widget.Toast.makeText(context, "Android Photo Picker is active by default", android.widget.Toast.LENGTH_SHORT).show()
                }
            )

            // Camera
            PermissionRowCard(
                icon = Icons.Default.CameraAlt,
                title = "Camera Access",
                subtitle = "Used to take photos of pigeons directly inside the application",
                status = if (cameraGranted) "Granted" else "Not Granted",
                isOk = cameraGranted,
                buttonText = if (cameraGranted) "Active" else "Grant",
                onClick = { cameraLauncher.launch(Manifest.permission.CAMERA) }
            )

            // Notifications
            PermissionRowCard(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                subtitle = "Reminders for medication, health, deworming, and daily backups",
                status = if (notificationsGranted) "Granted" else "Not Granted",
                isOk = notificationsGranted,
                buttonText = if (notificationsGranted) "Active" else "Grant",
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            )

            // Backup Folder Access
            PermissionRowCard(
                icon = Icons.Default.Folder,
                title = "Backup Location Access",
                subtitle = selectedBackupFolderUri ?: "No external backup folder set",
                status = if (selectedBackupFolderUri != null) "Connected" else "Disconnected",
                isOk = selectedBackupFolderUri != null,
                buttonText = "Change Folder",
                onClick = { folderPickerLauncher.launch(null) }
            )
        }
    }
}

@Composable
private fun PermissionRowCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    status: String,
    isOk: Boolean,
    buttonText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(status, style = MaterialTheme.typography.labelSmall, color = if (isOk) Color(0xFF16A34A) else MaterialTheme.colorScheme.error)
                }
                TextButton(onClick = onClick) {
                    Text(buttonText)
                }
            }
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
