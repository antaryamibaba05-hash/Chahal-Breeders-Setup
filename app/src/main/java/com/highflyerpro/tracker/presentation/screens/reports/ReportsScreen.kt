package com.highflyerpro.tracker.presentation.screens.reports

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.highflyerpro.tracker.data.local.entities.FlightSessionEntity
import com.highflyerpro.tracker.data.local.entities.PigeonEntity
import com.highflyerpro.tracker.presentation.viewmodel.MainViewModel
import com.highflyerpro.tracker.util.CsvReportGenerator
import com.highflyerpro.tracker.util.PdfReportGenerator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ReportOption(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isPdf: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repo = viewModel.repository
    val loftRepo = viewModel.loftMasterRepository

    var isGenerating by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }

    // Dialog state for selecting pigeon/session when needed
    var activeReportForPigeonPick by remember { mutableStateOf<ReportOption?>(null) }
    var activeReportForSessionPick by remember { mutableStateOf<ReportOption?>(null) }

    var availablePigeons by remember { mutableStateOf<List<PigeonEntity>>(emptyList()) }
    var availableSessions by remember { mutableStateOf<List<FlightSessionEntity>>(emptyList()) }

    var selectedPigeonIdForReport by remember { mutableStateOf("") }
    var selectedSessionIdForReport by remember { mutableStateOf("") }

    val reportList = listOf(
        ReportOption("1", "Complete Loft Master Roster PDF", "All active pigeons, ring numbers, breeds & statuses", Icons.Default.ListAlt),
        ReportOption("2", "Flight Session Performance PDF", "Detailed release, landing times & standings for a selected session", Icons.Default.Timer),
        ReportOption("3", "Leaderboard & Cumulative Standings PDF", "Overall loft endurance & flight duration rankings", Icons.Default.EmojiEvents),
        ReportOption("4", "Individual Pigeon Profile PDF", "Detailed lifetime profile of a selected pigeon", Icons.Default.Badge),
        ReportOption("5", "Pedigree Bloodline & Lineage PDF", "Sire & dam pedigree lineage certificate for a selected pigeon", Icons.Default.AccountTree),
        ReportOption("6", "Breeding & Pairing Register PDF", "Active pair registers, sire, dam & clutch histories", Icons.Default.Favorite),
        ReportOption("7", "Health & Veterinary Treatment Logs PDF", "Medical records, treatments, costs & recovery status", Icons.Default.LocalHospital),
        ReportOption("8", "Vaccination & Deworming Schedule PDF", "Upcoming and historical vaccine administration logs", Icons.Default.Vaccines),
        ReportOption("9", "Loft Group & Squad Roster PDF", "Group memberships (High Flyers, Breeding Squad, etc.)", Icons.Default.Group),
        ReportOption("10", "Loft Achievements & Badges PDF", "Unlocked achievements, milestones & honor roll certificates", Icons.Default.Stars),
        ReportOption("11", "Full Loft Directory CSV Spreadsheet", "Raw CSV export compatible with Excel & Google Sheets", Icons.Default.TableChart, isPdf = false)
    )

    // Pigeon Picker Modal Dialog
    if (activeReportForPigeonPick != null) {
        val rep = activeReportForPigeonPick!!
        AlertDialog(
            onDismissRequest = { activeReportForPigeonPick = null },
            title = { Text("Select Pigeon for ${rep.title}") },
            text = {
                if (availablePigeons.isEmpty()) {
                    Text("No pigeons available in loft.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Choose a pigeon from your roster:", fontSize = 12.sp)
                        var expanded by remember { mutableStateOf(false) }
                        val selP = availablePigeons.find { it.pigeonId == selectedPigeonIdForReport }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = selP?.let { "${it.name} (${it.ringNumber})" } ?: "Select Pigeon",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                availablePigeons.forEach { p ->
                                    DropdownMenuItem(
                                        text = { Text("${p.name} (${p.ringNumber})") },
                                        onClick = {
                                            selectedPigeonIdForReport = p.pigeonId
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = selectedPigeonIdForReport.isNotBlank(),
                    onClick = {
                        val pId = selectedPigeonIdForReport
                        activeReportForPigeonPick = null
                        coroutineScope.launch {
                            isGenerating = true
                            statusMessage = "Generating ${rep.title}..."
                            val pigeon = availablePigeons.find { it.pigeonId == pId }
                            if (pigeon != null) {
                                val history = repo.getRecordsByPigeonFlow(pId).first()
                                val father = pigeon.fatherId?.let { repo.allPigeonsFlow.first().find { p -> p.pigeonId == it } }
                                val mother = pigeon.motherId?.let { repo.allPigeonsFlow.first().find { p -> p.pigeonId == it } }

                                val file = if (rep.id == "4") {
                                    PdfReportGenerator.generatePigeonProfilePdf(context, pigeon, father, mother, history)
                                } else {
                                    PdfReportGenerator.generateGenericReportPdf(
                                        context = context,
                                        title = "PEDIGREE CERTIFICATE - ${pigeon.name}",
                                        subtitle = "Ring #: ${pigeon.ringNumber} | Gender: ${pigeon.gender} | Breed: ${pigeon.breed}",
                                        headers = listOf("Relation", "Pigeon Name", "Ring Number", "Breed", "Color"),
                                        rows = listOf(
                                            listOf("Subject", pigeon.name, pigeon.ringNumber.ifBlank { "Unbanded" }, pigeon.breed, pigeon.color),
                                            listOf("Sire (Father)", father?.name ?: "Unknown", father?.ringNumber ?: "N/A", father?.breed ?: "-", father?.color ?: "-"),
                                            listOf("Dam (Mother)", mother?.name ?: "Unknown", mother?.ringNumber ?: "N/A", mother?.breed ?: "-", mother?.color ?: "-")
                                        )
                                    )
                                }

                                if (file != null) {
                                    statusMessage = "Generated: ${file.name}"
                                    PdfReportGenerator.sharePdf(context, file)
                                } else {
                                    statusMessage = "Failed to export report for ${pigeon.name}."
                                }
                            } else {
                                statusMessage = "No records available for export."
                            }
                            isGenerating = false
                        }
                    }
                ) {
                    Text("Generate Report")
                }
            },
            dismissButton = {
                TextButton(onClick = { activeReportForPigeonPick = null }) { Text("Cancel") }
            }
        )
    }

    // Session Picker Modal Dialog
    if (activeReportForSessionPick != null) {
        val rep = activeReportForSessionPick!!
        AlertDialog(
            onDismissRequest = { activeReportForSessionPick = null },
            title = { Text("Select Flight Session") },
            text = {
                if (availableSessions.isEmpty()) {
                    Text("No historical flight sessions found.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Choose a flight session:", fontSize = 12.sp)
                        var expanded by remember { mutableStateOf(false) }
                        val selS = availableSessions.find { it.sessionId == selectedSessionIdForReport }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = selS?.let { "${it.name} (${it.eventType})" } ?: "Select Session",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                availableSessions.forEach { s ->
                                    DropdownMenuItem(
                                        text = { Text("${s.name} (${s.eventType})") },
                                        onClick = {
                                            selectedSessionIdForReport = s.sessionId
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = selectedSessionIdForReport.isNotBlank(),
                    onClick = {
                        val sId = selectedSessionIdForReport
                        activeReportForSessionPick = null
                        coroutineScope.launch {
                            isGenerating = true
                            statusMessage = "Generating Session Report..."
                            val session = availableSessions.find { it.sessionId == sId }
                            if (session != null) {
                                val records = repo.getRecordsBySessionFlow(sId).first()
                                if (records.isEmpty()) {
                                    statusMessage = "No flight records found for session '${session.name}'."
                                } else {
                                    val pigeons = repo.allPigeonsFlow.first()
                                    val pigeonsMap = pigeons.associateBy { it.pigeonId }
                                    val file = PdfReportGenerator.generateFlightSessionPdf(context, session.name, session.releaseTimestamp, records, pigeonsMap)
                                    if (file != null) {
                                        statusMessage = "Generated: ${file.name}"
                                        PdfReportGenerator.sharePdf(context, file)
                                    } else {
                                        statusMessage = "Failed to generate session PDF."
                                    }
                                }
                            }
                            isGenerating = false
                        }
                    }
                ) {
                    Text("Generate Report")
                }
            },
            dismissButton = {
                TextButton(onClick = { activeReportForSessionPick = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PDF & CSV Export Center", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("HIGH FLYER PRO TRACKER", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("Export official printable PDF documents and raw CSV data spreadsheets for your loft.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }

            if (statusMessage.isNotBlank()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                        Text(statusMessage, modifier = Modifier.padding(12.dp), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }

            items(reportList) { report ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(
                            imageVector = report.icon,
                            contentDescription = null,
                            tint = if (report.isPdf) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(30.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(report.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(report.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Button(
                            enabled = !isGenerating,
                            onClick = {
                                coroutineScope.launch {
                                    val pigeons = repo.allPigeonsFlow.first()
                                    val sessions = repo.allSessionsFlow.first()

                                    when (report.id) {
                                        "1" -> { // Master Roster
                                            if (pigeons.isEmpty()) {
                                                statusMessage = "No records available for export."
                                            } else {
                                                isGenerating = true
                                                statusMessage = "Generating Master Roster PDF..."
                                                val file = PdfReportGenerator.generateMasterRosterPdf(context, pigeons)
                                                if (file != null) {
                                                    statusMessage = "Generated: ${file.name}"
                                                    PdfReportGenerator.sharePdf(context, file)
                                                } else {
                                                    statusMessage = "Failed to export Master Roster."
                                                }
                                                isGenerating = false
                                            }
                                        }
                                        "2" -> { // Flight Session Performance
                                            if (sessions.isEmpty()) {
                                                statusMessage = "No flight sessions available for export."
                                            } else {
                                                availableSessions = sessions
                                                selectedSessionIdForReport = ""
                                                activeReportForSessionPick = report
                                            }
                                        }
                                        "3" -> { // Leaderboard
                                            val entries = viewModel.leaderboardEntries.value
                                            if (entries.isEmpty()) {
                                                statusMessage = "No flight records available for leaderboard export."
                                            } else {
                                                isGenerating = true
                                                statusMessage = "Generating Leaderboard PDF..."
                                                val file = PdfReportGenerator.generateLeaderboardPdf(context, entries)
                                                if (file != null) {
                                                    statusMessage = "Generated: ${file.name}"
                                                    PdfReportGenerator.sharePdf(context, file)
                                                } else {
                                                    statusMessage = "Failed to export Leaderboard."
                                                }
                                                isGenerating = false
                                            }
                                        }
                                        "4", "5" -> { // Individual Profile / Pedigree
                                            if (pigeons.isEmpty()) {
                                                statusMessage = "No pigeons available for export."
                                            } else {
                                                availablePigeons = pigeons
                                                selectedPigeonIdForReport = ""
                                                activeReportForPigeonPick = report
                                            }
                                        }
                                        "6" -> { // Breeding Register
                                            val pairs = loftRepo.allBreedingPairsFlow.first()
                                            if (pairs.isEmpty()) {
                                                statusMessage = "No breeding records available for export."
                                            } else {
                                                isGenerating = true
                                                val file = PdfReportGenerator.generateGenericReportPdf(
                                                    context = context,
                                                    title = "BREEDING & PAIRING REGISTER",
                                                    subtitle = "Active Pairs: ${pairs.size}",
                                                    headers = listOf("Pair ID", "Pair Name", "Sire ID", "Dam ID", "Status"),
                                                    rows = pairs.map { listOf(it.pairId, it.pairName, it.maleId, it.femaleId, it.status) }
                                                )
                                                if (file != null) {
                                                    statusMessage = "Generated: ${file.name}"
                                                    PdfReportGenerator.sharePdf(context, file)
                                                }
                                                isGenerating = false
                                            }
                                        }
                                        "7" -> { // Health Logs
                                            val meds = loftRepo.allMedicationsFlow.first()
                                            val deworms = loftRepo.allDewormingFlow.first()
                                            if (meds.isEmpty() && deworms.isEmpty()) {
                                                statusMessage = "No health records available for export."
                                            } else {
                                                isGenerating = true
                                                val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                                val rows = meds.map { listOf("Medication", it.pigeonId, it.medicineName, "${it.dosage} ${it.unit}", dateFormat.format(Date(it.startDate))) } +
                                                        deworms.map { listOf("Deworming", it.pigeonId, it.medicineUsed, it.dosage, dateFormat.format(Date(it.date))) }
                                                val file = PdfReportGenerator.generateGenericReportPdf(
                                                    context = context,
                                                    title = "HEALTH & VETERINARY TREATMENT LOGS",
                                                    subtitle = "Total Medical Logs: ${rows.size}",
                                                    headers = listOf("Type", "Pigeon ID", "Medicine", "Dosage", "Date"),
                                                    rows = rows
                                                )
                                                if (file != null) {
                                                    statusMessage = "Generated: ${file.name}"
                                                    PdfReportGenerator.sharePdf(context, file)
                                                }
                                                isGenerating = false
                                            }
                                        }
                                        "8" -> { // Vaccination & Deworming Schedule
                                            val deworms = loftRepo.allDewormingFlow.first()
                                            val reminders = loftRepo.allRemindersFlow.first().filter { it.category == "Vaccination" || it.category == "Deworming" }
                                            if (deworms.isEmpty() && reminders.isEmpty()) {
                                                statusMessage = "No vaccination or deworming records available for export."
                                            } else {
                                                isGenerating = true
                                                val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                                val rows = deworms.map {
                                                    listOf("Deworming", it.pigeonId, it.medicineUsed, it.dosage, dateFormat.format(Date(it.date)), dateFormat.format(Date(it.nextDueDate)))
                                                } + reminders.map {
                                                    listOf("Vaccine Schedule", it.pigeonId ?: "Loft-Wide", it.title, it.description, "Due: ${dateFormat.format(Date(it.dueDate))}", if (it.isCompleted) "Completed" else "Pending")
                                                }
                                                val file = PdfReportGenerator.generateGenericReportPdf(
                                                    context = context,
                                                    title = "VACCINATION & DEWORMING SCHEDULE",
                                                    subtitle = "Total Preventive Health Records: ${rows.size}",
                                                    headers = listOf("Type", "Target Pigeon", "Treatment / Title", "Dosage / Info", "Date Administered", "Status / Next Due"),
                                                    rows = rows
                                                )
                                                if (file != null) {
                                                    statusMessage = "Generated: ${file.name}"
                                                    PdfReportGenerator.sharePdf(context, file)
                                                }
                                                isGenerating = false
                                            }
                                        }
                                        "9" -> { // Loft Group & Squad Roster
                                            val groups = repo.allGroupsFlow.first()
                                            if (groups.isEmpty()) {
                                                statusMessage = "No loft groups or squads available for export."
                                            } else {
                                                isGenerating = true
                                                val rows = mutableListOf<List<String>>()
                                                for (g in groups) {
                                                    val members = repo.getPigeonsInGroup(g.groupId)
                                                    if (members.isEmpty()) {
                                                        rows.add(listOf(g.name, g.description, "0 Members", "None"))
                                                    } else {
                                                        members.forEach { m ->
                                                            rows.add(listOf(g.name, g.description, "${members.size} Pigeons", "${m.name} (${m.ringNumber}) - ${m.status}"))
                                                        }
                                                    }
                                                }
                                                val file = PdfReportGenerator.generateGenericReportPdf(
                                                    context = context,
                                                    title = "LOFT GROUPS & SQUAD ROSTERS",
                                                    subtitle = "Total Groups Registered: ${groups.size}",
                                                    headers = listOf("Squad / Group", "Description", "Squad Size", "Member Pigeon Details"),
                                                    rows = rows
                                                )
                                                if (file != null) {
                                                    statusMessage = "Generated: ${file.name}"
                                                    PdfReportGenerator.sharePdf(context, file)
                                                }
                                                isGenerating = false
                                            }
                                        }
                                        "10" -> { // Achievements
                                            val achs = repo.allAchievementsFlow.first()
                                            if (achs.isEmpty()) {
                                                statusMessage = "No unlocked achievements available for export."
                                            } else {
                                                isGenerating = true
                                                val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                                val file = PdfReportGenerator.generateGenericReportPdf(
                                                    context = context,
                                                    title = "LOFT ACHIEVEMENTS & HONOR ROLL",
                                                    subtitle = "Total Badges Unlocked: ${achs.size}",
                                                    headers = listOf("Pigeon ID", "Achievement Title", "Description", "Date Unlocked"),
                                                    rows = achs.map { listOf(it.pigeonId, it.title, it.description, dateFormat.format(Date(it.unlockedAt))) }
                                                )
                                                if (file != null) {
                                                    statusMessage = "Generated: ${file.name}"
                                                    PdfReportGenerator.sharePdf(context, file)
                                                }
                                                isGenerating = false
                                            }
                                        }
                                        "11" -> { // CSV Export
                                            if (pigeons.isEmpty()) {
                                                statusMessage = "No pigeons available to export CSV."
                                            } else {
                                                isGenerating = true
                                                val file = CsvReportGenerator.exportPigeonsCsv(context, pigeons)
                                                if (file != null) {
                                                    statusMessage = "Exported: ${file.name}"
                                                    CsvReportGenerator.shareCsv(context, file)
                                                } else {
                                                    statusMessage = "Failed to export CSV."
                                                }
                                                isGenerating = false
                                            }
                                        }
                                        else -> {
                                            if (pigeons.isEmpty()) {
                                                statusMessage = "No records available for export."
                                            } else {
                                                isGenerating = true
                                                val file = PdfReportGenerator.generateMasterRosterPdf(context, pigeons)
                                                if (file != null) {
                                                    statusMessage = "Generated: ${file.name}"
                                                    PdfReportGenerator.sharePdf(context, file)
                                                }
                                                isGenerating = false
                                            }
                                        }
                                    }
                                }
                            }
                        ) {
                            Text("Export", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
