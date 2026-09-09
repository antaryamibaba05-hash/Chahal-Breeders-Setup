package com.highflyerpro.tracker.presentation.screens.pigeons

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.highflyerpro.tracker.presentation.components.PigeonCardItem
import com.highflyerpro.tracker.presentation.viewmodel.DisplayMode
import com.highflyerpro.tracker.presentation.viewmodel.MainViewModel
import com.highflyerpro.tracker.presentation.viewmodel.PigeonSortOrder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PigeonsScreen(
    viewModel: MainViewModel,
    onNavigateToAddPigeon: () -> Unit,
    onNavigateToPigeonDetail: (String) -> Unit
) {
    val pigeonsWithStats by viewModel.filteredPigeonsWithStats.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val statusFilter by viewModel.selectedStatusFilter.collectAsState()
    val genderFilter by viewModel.selectedGenderFilter.collectAsState()
    val displayMode by viewModel.displayMode.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val selectedIds by viewModel.selectedPigeonIds.collectAsState()

    var showSortMenu by remember { mutableStateOf(false) }
    var showBulkStatusDialog by remember { mutableStateOf(false) }

    val statusOptions = listOf("Active", "Training", "Resting", "Recovering", "Injured", "Retired")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "LOFT ROSTER",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${pigeonsWithStats.size} Pigeons Shown",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                actions = {
                    // Display Mode Switcher
                    IconButton(
                        onClick = {
                            viewModel.setDisplayMode(
                                when (displayMode) {
                                    DisplayMode.LIST -> DisplayMode.GRID
                                    DisplayMode.GRID -> DisplayMode.COMPACT
                                    DisplayMode.COMPACT -> DisplayMode.LIST
                                }
                            )
                        },
                        modifier = Modifier.testTag("toggle_display_mode_button")
                    ) {
                        Icon(
                            imageVector = when (displayMode) {
                                DisplayMode.LIST -> Icons.Default.ViewAgenda
                                DisplayMode.GRID -> Icons.Default.GridView
                                DisplayMode.COMPACT -> Icons.Default.TableRows
                            },
                            contentDescription = "Toggle View"
                        )
                    }

                    // Sort Menu
                    Box {
                        IconButton(
                            onClick = { showSortMenu = true },
                            modifier = Modifier.testTag("sort_pigeons_button")
                        ) {
                            Icon(Icons.Default.Sort, contentDescription = "Sort")
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Name (A to Z)") },
                                onClick = { viewModel.setSortOrder(PigeonSortOrder.NAME_AZ); showSortMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Name (Z to A)") },
                                onClick = { viewModel.setSortOrder(PigeonSortOrder.NAME_ZA); showSortMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Highest Performance Score") },
                                onClick = { viewModel.setSortOrder(PigeonSortOrder.BEST_PERFORMANCE); showSortMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Highest Flight Hours") },
                                onClick = { viewModel.setSortOrder(PigeonSortOrder.HIGHEST_HOURS); showSortMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Most Flight Sessions") },
                                onClick = { viewModel.setSortOrder(PigeonSortOrder.MOST_ACTIVE); showSortMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Ring Number") },
                                onClick = { viewModel.setSortOrder(PigeonSortOrder.RING_NUMBER); showSortMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Newest Registered") },
                                onClick = { viewModel.setSortOrder(PigeonSortOrder.NEWEST); showSortMenu = false }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddPigeon,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_pigeon_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Pigeon")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search by name, ring #, breed, color, ID...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("pigeon_search_input")
            )

            // Status Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                item {
                    FilterChip(
                        selected = statusFilter == null,
                        onClick = { viewModel.setStatusFilter(null) },
                        label = { Text("All Statuses") },
                        colors = FilterChipDefaults.filterChipColors()
                    )
                }
                items(statusOptions) { status ->
                    FilterChip(
                        selected = statusFilter == status,
                        onClick = {
                            viewModel.setStatusFilter(if (statusFilter == status) null else status)
                        },
                        label = { Text(status) }
                    )
                }
            }

            // Bulk Actions Bar (if any pigeons selected)
            if (selectedIds.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${selectedIds.size} Selected",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = { showBulkStatusDialog = true },
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("Status", fontSize = 12.sp)
                            }

                            Button(
                                onClick = { viewModel.bulkArchive() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64748B)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("Archive", fontSize = 12.sp)
                            }

                            IconButton(
                                onClick = { viewModel.clearPigeonSelection() },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            // Pigeons List / Grid / Compact Content
            if (pigeonsWithStats.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No pigeons found",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Try adjusting your search query or status filter.",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontSize = 13.sp
                        )
                    }
                }
            } else if (displayMode == DisplayMode.GRID) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(pigeonsWithStats, key = { it.pigeon.pigeonId }) { item ->
                        PigeonCardItem(
                            item = item,
                            displayMode = DisplayMode.GRID,
                            isSelected = selectedIds.contains(item.pigeon.pigeonId),
                            onToggleSelect = { viewModel.togglePigeonSelection(item.pigeon.pigeonId) },
                            onClick = { onNavigateToPigeonDetail(item.pigeon.pigeonId) }
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(pigeonsWithStats, key = { it.pigeon.pigeonId }) { item ->
                        PigeonCardItem(
                            item = item,
                            displayMode = displayMode,
                            isSelected = selectedIds.contains(item.pigeon.pigeonId),
                            onToggleSelect = { viewModel.togglePigeonSelection(item.pigeon.pigeonId) },
                            onClick = { onNavigateToPigeonDetail(item.pigeon.pigeonId) }
                        )
                    }
                }
            }
        }
    }

    // Bulk Status Dialog
    if (showBulkStatusDialog) {
        AlertDialog(
            onDismissRequest = { showBulkStatusDialog = false },
            title = { Text("Update Status for ${selectedIds.size} Pigeons") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    statusOptions.forEach { s ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.bulkUpdateStatus(s)
                                    showBulkStatusDialog = false
                                }
                        ) {
                            Text(
                                text = s,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showBulkStatusDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
