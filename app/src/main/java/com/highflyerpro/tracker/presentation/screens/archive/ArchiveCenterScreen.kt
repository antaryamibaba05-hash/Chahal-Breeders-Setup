package com.highflyerpro.tracker.presentation.screens.archive

import androidx.compose.foundation.clickable
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
import com.highflyerpro.tracker.presentation.components.PigeonAvatar
import com.highflyerpro.tracker.presentation.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveCenterScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPigeonDetail: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val repo = viewModel.repository

    val archivedPigeons by repo.archivedPigeonsFlow.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Archive Center (${archivedPigeons.size})", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (archivedPigeons.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Archive,
                        contentDescription = "Empty Archive",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No archived pigeons.", style = MaterialTheme.typography.titleMedium)
                    Text("Archived pigeons retain all pedigree, flight, and health history safely.", style = MaterialTheme.typography.bodySmall)
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
                items(archivedPigeons) { pigeon ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToPigeonDetail(pigeon.pigeonId) },
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
                                Text("Status: ${pigeon.status} (Archived)", style = MaterialTheme.typography.labelSmall)
                            }

                            OutlinedButton(onClick = {
                                coroutineScope.launch {
                                    repo.setArchiveStatus(pigeon.pigeonId, false)
                                }
                            }) {
                                Text("Unarchive")
                            }
                        }
                    }
                }
            }
        }
    }
}
