package com.highflyerpro.tracker.presentation.screens.health

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
import androidx.compose.ui.unit.sp
import com.highflyerpro.tracker.data.local.entities.DewormingRecordEntity
import com.highflyerpro.tracker.data.local.entities.DiseaseRecordEntity
import com.highflyerpro.tracker.data.local.entities.MedicationRecordEntity
import com.highflyerpro.tracker.data.local.entities.WeightRecordEntity
import com.highflyerpro.tracker.presentation.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val loftRepo = viewModel.loftMasterRepository

    val medications by loftRepo.allMedicationsFlow.collectAsState(initial = emptyList())
    val dewormingList by loftRepo.allDewormingFlow.collectAsState(initial = emptyList())
    val diseases by loftRepo.allDiseasesFlow.collectAsState(initial = emptyList())
    val allPigeonsWithStats by viewModel.filteredPigeonsWithStats.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var showDewormDialog by remember { mutableStateOf(false) }
    var showMedicationDialog by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health & Medical Care", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 0) showDewormDialog = true else showMedicationDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Record")
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
                    text = { Text("Deworming (${dewormingList.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Medications (${medications.size})") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Illness Log (${diseases.size})") }
                )
            }

            when (selectedTab) {
                0 -> {
                    // Deworming Tab
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(dewormingList) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.medicineUsed, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Text("Pigeon: ${item.pigeonId}", style = MaterialTheme.typography.bodyMedium)
                                        Text("Dosage: ${item.dosage}", style = MaterialTheme.typography.bodySmall)
                                        Text("Administered: ${dateFormat.format(Date(item.date))}", style = MaterialTheme.typography.bodySmall)
                                        Text("Next Due: ${dateFormat.format(Date(item.nextDueDate))}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    }
                                    IconButton(onClick = {
                                        coroutineScope.launch { loftRepo.deleteDeworming(item.id) }
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Medications Tab
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(medications) { med ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(med.medicineName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Text("Pigeon: ${med.pigeonId}", style = MaterialTheme.typography.bodyMedium)
                                        Text("Dosage: ${med.dosage} ${med.unit} via ${med.administrationMethod}")
                                        Text("Reason: ${med.reason}")
                                    }
                                    IconButton(onClick = {
                                        coroutineScope.launch { loftRepo.deleteMedication(med.id) }
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Diseases Tab
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(diseases) { dis ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(dis.diseaseName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Text("Pigeon: ${dis.pigeonId}", style = MaterialTheme.typography.bodyMedium)
                                        Text("Severity: ${dis.severity} | Outcome: ${dis.outcome}")
                                        Text("Treatment: ${dis.treatment}")
                                    }
                                    IconButton(onClick = {
                                        coroutineScope.launch { loftRepo.deleteDisease(dis.id) }
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDewormDialog) {
        AddDewormingDialog(
            pigeons = allPigeonsWithStats.map { it.pigeon },
            onDismiss = { showDewormDialog = false },
            onConfirmBulk = { selectedIds, medicine, dosage, nextDays ->
                coroutineScope.launch {
                    loftRepo.recordDewormingBulk(selectedIds, medicine, dosage, nextDays)
                    showDewormDialog = false
                }
            }
        )
    }

    if (showMedicationDialog) {
        AddMedicationDialog(
            pigeons = allPigeonsWithStats.map { it.pigeon },
            onDismiss = { showMedicationDialog = false },
            onConfirm = { med ->
                coroutineScope.launch {
                    loftRepo.addMedication(med)
                    showMedicationDialog = false
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDewormingDialog(
    pigeons: List<com.highflyerpro.tracker.data.local.entities.PigeonEntity>,
    onDismiss: () -> Unit,
    onConfirmBulk: (pigeonIds: List<String>, medicine: String, dosage: String, nextDays: Int) -> Unit
) {
    var medicine by remember { mutableStateOf("Levamisole / Wormer") }
    var dosage by remember { mutableStateOf("1 tablet / 100ml water") }
    var nextDays by remember { mutableStateOf("90") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Loft Deworming Cycle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Applies deworming record & automatically schedules follow-up reminder.")
                OutlinedTextField(
                    value = medicine,
                    onValueChange = { medicine = it },
                    label = { Text("Medicine Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("Dosage / Administration") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = nextDays,
                    onValueChange = { nextDays = it },
                    label = { Text("Repeat Cycle in Days (Default 90)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val pIds = pigeons.map { it.pigeonId }
                val days = nextDays.toIntOrNull() ?: 90
                onConfirmBulk(pIds, medicine, dosage, days)
            }) {
                Text("Record for All Pigeons (${pigeons.size})")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationDialog(
    pigeons: List<com.highflyerpro.tracker.data.local.entities.PigeonEntity>,
    onDismiss: () -> Unit,
    onConfirm: (MedicationRecordEntity) -> Unit
) {
    var selectedPigeonId by remember { mutableStateOf("") }
    var pigeonDropdownExpanded by remember { mutableStateOf(false) }
    var medName by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }

    val selectedPigeon = pigeons.find { it.pigeonId == selectedPigeonId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Pigeon Medication") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Select Target Pigeon *", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                ExposedDropdownMenuBox(
                    expanded = pigeonDropdownExpanded,
                    onExpandedChange = { pigeonDropdownExpanded = !pigeonDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedPigeon?.let { "${it.name} (${it.ringNumber})" } ?: "Select Pigeon (Required)",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = pigeonDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = pigeonDropdownExpanded,
                        onDismissRequest = { pigeonDropdownExpanded = false }
                    ) {
                        pigeons.forEach { p ->
                            DropdownMenuItem(
                                text = { Text("${p.name} (${p.ringNumber})") },
                                onClick = {
                                    selectedPigeonId = p.pigeonId
                                    pigeonDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = medName,
                    onValueChange = { medName = it },
                    label = { Text("Medication Name *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("Dosage (e.g. 5ml)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason / Condition") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                enabled = medName.isNotBlank() && selectedPigeonId.isNotBlank(),
                onClick = {
                    val now = System.currentTimeMillis()
                    val med = MedicationRecordEntity(
                        pigeonId = selectedPigeonId,
                        startDate = now,
                        medicineName = medName,
                        dosage = dosage,
                        reason = reason
                    )
                    onConfirm(med)
                }
            ) {
                Text("Save Record")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
