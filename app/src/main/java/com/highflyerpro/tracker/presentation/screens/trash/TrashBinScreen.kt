package com.highflyerpro.tracker.presentation.screens.trash

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.highflyerpro.tracker.data.local.entities.PigeonEntity
import com.highflyerpro.tracker.data.repository.PigeonRelatedDataCount
import com.highflyerpro.tracker.presentation.components.PigeonAvatar
import com.highflyerpro.tracker.presentation.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashBinScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPigeonDetail: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val repo = viewModel.repository

    val trashPigeons by repo.trashPigeonsFlow.collectAsState(initial = emptyList())

    var selectedPigeonForDelete by remember { mutableStateOf<PigeonEntity?>(null) }
    var relatedDataCount by remember { mutableStateOf<PigeonRelatedDataCount?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trash Bin / Recycle Bin (${trashPigeons.size})", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (trashPigeons.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.DeleteSweep,
                        contentDescription = "Empty Trash",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Trash Bin is empty.", style = MaterialTheme.typography.titleMedium)
                    Text("Deleted pigeons remain recoverable here before permanent removal.", style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(trashPigeons) { pigeon ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            PigeonAvatar(
                                photoUri = pigeon.photoUri,
                                name = pigeon.name,
                                gender = pigeon.gender,
                                size = 48
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(pigeon.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("Ring: ${pigeon.ringNumber.ifBlank { "No Ring" }} | ${pigeon.breed}", style = MaterialTheme.typography.bodySmall)
                                Text("Status: ${pigeon.status}", style = MaterialTheme.typography.labelSmall)
                            }

                            // Actions
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    repo.restoreFromTrash(pigeon.pigeonId)
                                }
                            }) {
                                Icon(Icons.Default.Restore, contentDescription = "Restore", tint = MaterialTheme.colorScheme.primary)
                            }

                            IconButton(onClick = {
                                coroutineScope.launch {
                                    val count = repo.getPigeonRelatedDataCount(pigeon.pigeonId)
                                    selectedPigeonForDelete = pigeon
                                    relatedDataCount = count
                                    showDeleteConfirmDialog = true
                                }
                            }) {
                                Icon(Icons.Default.DeleteForever, contentDescription = "Permanently Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirmDialog && selectedPigeonForDelete != null) {
        val p = selectedPigeonForDelete!!
        val count = relatedDataCount ?: PigeonRelatedDataCount()

        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = "Warning", tint = MaterialTheme.colorScheme.error) },
            title = { Text("Permanently Delete ${p.name}?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Ring Number: ${p.ringNumber.ifBlank { "N/A" }}", fontWeight = FontWeight.Bold)
                    Text("This action permanently removes this pigeon and all its associated records from the database.", color = MaterialTheme.colorScheme.error)

                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("Associated Records to be Purged:", fontWeight = FontWeight.SemiBold)
                    Text("• Flight Records: ${count.flightRecordsCount}")
                    Text("• Achievements: ${count.achievementsCount}")
                    Text("• Offspring Records: ${count.offspringCount}")
                    Text("• Health & Medical Logs: ${count.healthRecordsCount}")
                    Text("• Photos: ${count.photosCount}")
                    Text("• Breeding History: ${count.breedingRecordsCount}")
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        coroutineScope.launch {
                            repo.permanentlyDeletePigeon(p.pigeonId)
                            showDeleteConfirmDialog = false
                        }
                    }
                ) {
                    Text("Permanently Delete")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = {
                        coroutineScope.launch {
                            repo.setArchiveStatus(p.pigeonId, true)
                            repo.restoreFromTrash(p.pigeonId)
                            showDeleteConfirmDialog = false
                        }
                    }) {
                        Text("Archive Instead")
                    }
                    TextButton(onClick = { showDeleteConfirmDialog = false }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}
