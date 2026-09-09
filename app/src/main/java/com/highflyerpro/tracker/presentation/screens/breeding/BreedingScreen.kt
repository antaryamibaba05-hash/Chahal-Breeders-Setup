package com.highflyerpro.tracker.presentation.screens.breeding

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
import com.highflyerpro.tracker.data.local.entities.BreedingEventEntity
import com.highflyerpro.tracker.data.local.entities.BreedingPairEntity
import com.highflyerpro.tracker.presentation.components.PigeonAvatar
import com.highflyerpro.tracker.presentation.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreedingScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onPigeonClick: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val loftRepo = viewModel.loftMasterRepository
    val repository = viewModel.repository

    val pairs by loftRepo.allBreedingPairsFlow.collectAsState(initial = emptyList())
    val events by loftRepo.allBreedingEventsFlow.collectAsState(initial = emptyList())
    val allPigeonsWithStats by viewModel.filteredPigeonsWithStats.collectAsState()
    val pigeonsMap = remember(allPigeonsWithStats) {
        allPigeonsWithStats.associateBy { it.pigeon.pigeonId }
    }

    var selectedTab by remember { mutableStateOf(0) }
    var showAddPairDialog by remember { mutableStateOf(false) }
    var showAddEventDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Breeding Center", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 0) showAddPairDialog = true else showAddEventDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = if (selectedTab == 0) Icons.Default.Favorite else Icons.Default.Egg,
                    contentDescription = "Add"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Active Pairs (${pairs.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Egg Clutches (${events.size})") }
                )
            }

            when (selectedTab) {
                0 -> PairsTabContent(pairs, pigeonsMap, onPigeonClick) { pairId ->
                    coroutineScope.launch { loftRepo.deleteBreedingPair(pairId) }
                }
                1 -> EventsTabContent(events, pigeonsMap, onPigeonClick) { eventId ->
                    coroutineScope.launch { loftRepo.deleteBreedingEvent(eventId) }
                }
            }
        }
    }

    if (showAddPairDialog) {
        AddPairDialog(
            pigeonsMap = pigeonsMap,
            onDismiss = { showAddPairDialog = false },
            onConfirm = { maleId, femaleId, pairName, notes ->
                coroutineScope.launch {
                    loftRepo.createBreedingPair(maleId, femaleId, pairName, notes)
                    showAddPairDialog = false
                }
            }
        )
    }

    if (showAddEventDialog) {
        AddBreedingEventDialog(
            pairs = pairs,
            onDismiss = { showAddEventDialog = false },
            onConfirm = { event ->
                coroutineScope.launch {
                    loftRepo.createBreedingEvent(event)
                    showAddEventDialog = false
                }
            }
        )
    }
}

@Composable
fun PairsTabContent(
    pairs: List<BreedingPairEntity>,
    pigeonsMap: Map<String, com.highflyerpro.tracker.domain.model.PigeonWithStats>,
    onPigeonClick: (String) -> Unit,
    onDeletePair: (String) -> Unit
) {
    if (pairs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "No breeding pairs created yet.\nTap + to pair a male and female pigeon.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(pairs) { pair ->
                val male = pigeonsMap[pair.maleId]?.pigeon
                val female = pigeonsMap[pair.femaleId]?.pigeon

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(pair.pairName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            IconButton(onClick = { onDeletePair(pair.pairId) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Pair", tint = MaterialTheme.colorScheme.error)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Male
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { onPigeonClick(pair.maleId) }
                            ) {
                                PigeonAvatar(
                                    photoUri = male?.photoUri ?: "",
                                    name = male?.name ?: "Sire",
                                    gender = "Male",
                                    size = 52
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Sire: ${male?.name ?: pair.maleId}", fontWeight = FontWeight.SemiBold)
                                Text(male?.ringNumber ?: "", style = MaterialTheme.typography.bodySmall)
                            }

                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = "Paired",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )

                            // Female
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { onPigeonClick(pair.femaleId) }
                            ) {
                                PigeonAvatar(
                                    photoUri = female?.photoUri ?: "",
                                    name = female?.name ?: "Dam",
                                    gender = "Female",
                                    size = 52
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Dam: ${female?.name ?: pair.femaleId}", fontWeight = FontWeight.SemiBold)
                                Text(female?.ringNumber ?: "", style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        if (pair.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Notes: ${pair.notes}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EventsTabContent(
    events: List<BreedingEventEntity>,
    pigeonsMap: Map<String, com.highflyerpro.tracker.domain.model.PigeonWithStats>,
    onPigeonClick: (String) -> Unit,
    onDeleteEvent: (String) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }

    if (events.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "No breeding egg clutches logged yet.\nTap + to log an egg clutch date or hatch event.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(events) { evt ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Nest Box: ${evt.nestNumber.ifBlank { "Unassigned" }}", fontWeight = FontWeight.Bold)
                            IconButton(onClick = { onDeleteEvent(evt.eventId) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Egg Status: ${evt.eggStatus}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)

                        if (evt.egg1Date != null) {
                            Text("1st Egg Laid: ${dateFormat.format(Date(evt.egg1Date))}")
                        }
                        if (evt.egg2Date != null) {
                            Text("2nd Egg Laid: ${dateFormat.format(Date(evt.egg2Date))}")
                        }
                        if (evt.expectedHatchDate != null) {
                            Text("Expected Hatch Date: ${dateFormat.format(Date(evt.expectedHatchDate))}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPairDialog(
    pigeonsMap: Map<String, com.highflyerpro.tracker.domain.model.PigeonWithStats>,
    onDismiss: () -> Unit,
    onConfirm: (maleId: String, femaleId: String, pairName: String, notes: String) -> Unit
) {
    val males = pigeonsMap.values.filter { it.pigeon.gender.equals("Male", ignoreCase = true) }
    val females = pigeonsMap.values.filter { it.pigeon.gender.equals("Female", ignoreCase = true) }

    var selectedMaleId by remember { mutableStateOf("") }
    var selectedFemaleId by remember { mutableStateOf("") }
    var maleDropdownExpanded by remember { mutableStateOf(false) }
    var femaleDropdownExpanded by remember { mutableStateOf(false) }

    var pairName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val selectedMale = pigeonsMap[selectedMaleId]?.pigeon
    val selectedFemale = pigeonsMap[selectedFemaleId]?.pigeon

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Breeding Pair") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = pairName,
                    onValueChange = { pairName = it },
                    label = { Text("Pair Name / Label (e.g., Pair #1) *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("Select Male (Sire) *", fontWeight = FontWeight.Bold)
                if (males.isEmpty()) {
                    Text("No male pigeons available.", color = MaterialTheme.colorScheme.error)
                } else {
                    ExposedDropdownMenuBox(
                        expanded = maleDropdownExpanded,
                        onExpandedChange = { maleDropdownExpanded = !maleDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedMale?.let { "${it.name} (${it.ringNumber})" } ?: "Select Male Pigeon",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = maleDropdownExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = maleDropdownExpanded,
                            onDismissRequest = { maleDropdownExpanded = false }
                        ) {
                            males.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text("${m.pigeon.name} (${m.pigeon.ringNumber})") },
                                    onClick = {
                                        selectedMaleId = m.pigeon.pigeonId
                                        maleDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Text("Select Female (Dam) *", fontWeight = FontWeight.Bold)
                if (females.isEmpty()) {
                    Text("No female pigeons available.", color = MaterialTheme.colorScheme.error)
                } else {
                    ExposedDropdownMenuBox(
                        expanded = femaleDropdownExpanded,
                        onExpandedChange = { femaleDropdownExpanded = !femaleDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedFemale?.let { "${it.name} (${it.ringNumber})" } ?: "Select Female Pigeon",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = femaleDropdownExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = femaleDropdownExpanded,
                            onDismissRequest = { femaleDropdownExpanded = false }
                        ) {
                            females.forEach { f ->
                                DropdownMenuItem(
                                    text = { Text("${f.pigeon.name} (${f.pigeon.ringNumber})") },
                                    onClick = {
                                        selectedFemaleId = f.pigeon.pigeonId
                                        femaleDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Breeding Goal") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                enabled = pairName.isNotBlank() && selectedMaleId.isNotBlank() && selectedFemaleId.isNotBlank(),
                onClick = { onConfirm(selectedMaleId, selectedFemaleId, pairName.trim(), notes.trim()) }
            ) {
                Text("Pair Pigeons")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBreedingEventDialog(
    pairs: List<BreedingPairEntity>,
    onDismiss: () -> Unit,
    onConfirm: (BreedingEventEntity) -> Unit
) {
    if (pairs.isEmpty()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("No Active Pairs") },
            text = { Text("Please create a breeding pair before adding egg clutches.") },
            confirmButton = { Button(onClick = onDismiss) { Text("OK") } }
        )
        return
    }

    var selectedPair by remember { mutableStateOf<BreedingPairEntity?>(null) }
    var pairDropdownExpanded by remember { mutableStateOf(false) }
    var nestNumber by remember { mutableStateOf("Nest 1") }
    var eggStatus by remember { mutableStateOf("Fertile") }
    var expectedHatchDays by remember { mutableStateOf("18") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Egg Clutch") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Select Breeding Pair *", fontWeight = FontWeight.Bold)
                ExposedDropdownMenuBox(
                    expanded = pairDropdownExpanded,
                    onExpandedChange = { pairDropdownExpanded = !pairDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedPair?.pairName ?: "Select Pair",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = pairDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = pairDropdownExpanded,
                        onDismissRequest = { pairDropdownExpanded = false }
                    ) {
                        pairs.forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p.pairName) },
                                onClick = {
                                    selectedPair = p
                                    pairDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = nestNumber,
                    onValueChange = { nestNumber = it },
                    label = { Text("Nest Box Number") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = expectedHatchDays,
                    onValueChange = { expectedHatchDays = it },
                    label = { Text("Incubation Days to Hatch") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                enabled = selectedPair != null,
                onClick = {
                    val sp = selectedPair ?: return@Button
                    val now = System.currentTimeMillis()
                    val days = expectedHatchDays.toLongOrNull() ?: 18L
                    val hatchTime = now + (days * 24 * 3600 * 1000L)

                    val evt = BreedingEventEntity(
                        eventId = "BR-EVT-${java.util.UUID.randomUUID().toString().take(6)}",
                        pairId = sp.pairId,
                        maleId = sp.maleId,
                        femaleId = sp.femaleId,
                        date = now,
                        nestNumber = nestNumber,
                        egg1Date = now,
                        expectedHatchDate = hatchTime,
                        eggStatus = eggStatus
                    )
                    onConfirm(evt)
                }
            ) {
                Text("Save Clutch")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
