package com.highflyerpro.tracker.presentation.screens.flight

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.highflyerpro.tracker.data.local.entities.FlightSessionEntity
import com.highflyerpro.tracker.domain.calculation.PerformanceEngine
import com.highflyerpro.tracker.presentation.components.ActiveFlightBanner
import com.highflyerpro.tracker.presentation.components.CreateEditEventDialog
import com.highflyerpro.tracker.presentation.components.AddEditManualFlightDialog
import com.highflyerpro.tracker.presentation.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightListScreen(
    viewModel: MainViewModel,
    onNavigateToStartFlight: () -> Unit,
    onNavigateToLiveTracker: (String) -> Unit,
    onNavigateToFlightDetail: (String) -> Unit
) {
    val activeSession by viewModel.activeSession.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()
    val allPigeons by viewModel.allPigeons.collectAsState()
    val liveTime by viewModel.currentLiveTimeMillis.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var showCreateEventDialog by remember { mutableStateOf(false) }
    var sessionToEdit by remember { mutableStateOf<FlightSessionEntity?>(null) }
    var showManualFlightDialog by remember { mutableStateOf(false) }
    var targetSessionForManualRecord by remember { mutableStateOf<String?>(null) }
    var showFabMenu by remember { mutableStateOf(false) }

    val completedSessions = allSessions.filter { it.status == "COMPLETED" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "FLIGHTS",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Track live and historical pigeon flights.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateEventDialog = true }) {
                        Icon(Icons.Default.Event, contentDescription = "Create Custom Event", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            Box {
                ExtendedFloatingActionButton(
                    onClick = { showFabMenu = !showFabMenu },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("RECORD FLIGHT", fontWeight = FontWeight.Bold) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("flight_fab_menu_button")
                )

                DropdownMenu(
                    expanded = showFabMenu,
                    onDismissRequest = { showFabMenu = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FlightTakeoff, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("START LIVE FLIGHT", fontWeight = FontWeight.Bold)
                            }
                        },
                        onClick = {
                            showFabMenu = false
                            onNavigateToStartFlight()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.EditCalendar, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ADD MANUAL FLIGHT", fontWeight = FontWeight.Bold)
                            }
                        },
                        onClick = {
                            showFabMenu = false
                            targetSessionForManualRecord = null
                            showManualFlightDialog = true
                        }
                    )
                    Divider()
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Event, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Create Custom Event", fontSize = 13.sp)
                            }
                        },
                        onClick = {
                            showFabMenu = false
                            showCreateEventDialog = true
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Live Active Flight Banner
            if (activeSession != null) {
                item {
                    val session = activeSession!!
                    val elapsed = (liveTime - session.releaseTimestamp).coerceAtLeast(0L)
                    ActiveFlightBanner(
                        session = session,
                        liveElapsedMillis = elapsed,
                        onViewLiveTracker = { onNavigateToLiveTracker(session.sessionId) },
                        onDismiss = { /* do not dismiss on flight screen */ }
                    )
                }
            }

            // Prominent Flight Recording Choice Section
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "HOW WOULD YOU LIKE TO RECORD A FLIGHT?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 0.8.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Option 1: Live Flight Card
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToStartFlight() }
                            .testTag("start_live_flight_card")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.FlightTakeoff,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "START LIVE FLIGHT",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Release pigeons now and track their landing times live.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                            )
                        }
                    }

                    // Option 2: Add Manual Flight Card
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                targetSessionForManualRecord = null
                                showManualFlightDialog = true
                            }
                            .testTag("add_manual_flight_card")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.EditCalendar,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ADD MANUAL FLIGHT",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Manually enter a completed, historical or unrecorded flight.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            // Custom Event Management Banner
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("CUSTOM EVENT MANAGEMENT", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Create & schedule customized training or tournament events", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }
                        OutlinedButton(
                            onClick = { showCreateEventDialog = true },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Event", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "HISTORICAL FLIGHT SESSIONS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    TextButton(
                        onClick = {
                            targetSessionForManualRecord = null
                            showManualFlightDialog = true
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Manual Record", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (completedSessions.isEmpty() && activeSession == null) {
                item {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.FlightTakeoff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Flight Sessions Recorded",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap 'New Flight' or '+ Manual Record' to log flights.",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            } else {
                items(completedSessions) { s ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToFlightDetail(s.sessionId) }
                            .testTag("flight_session_${s.sessionId}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = s.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    SuggestionChip(
                                        onClick = { onNavigateToFlightDetail(s.sessionId) },
                                        label = { Text(s.eventType, fontSize = 10.sp) },
                                        modifier = Modifier.height(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Released: ${PerformanceEngine.formatTimestamp(s.releaseTimestamp)}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${s.totalParticipants} Participants",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "• ${s.landedCount} Landed",
                                        fontSize = 12.sp,
                                        color = Color(0xFF16A34A),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            IconButton(
                                onClick = { sessionToEdit = s }
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Event", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showCreateEventDialog || sessionToEdit != null) {
        CreateEditEventDialog(
            existingSession = sessionToEdit,
            onConfirm = { name, type, releaseTs, loc, rules, desc ->
                coroutineScope.launch {
                    if (sessionToEdit == null) {
                        viewModel.repository.createCustomEventSession(name, type, releaseTs, loc, rules, desc)
                    } else {
                        viewModel.repository.updateFlightSession(
                            sessionToEdit!!.copy(
                                name = name,
                                eventType = type,
                                releaseTimestamp = releaseTs,
                                location = loc,
                                rules = rules,
                                description = desc
                            )
                        )
                    }
                    showCreateEventDialog = false
                    sessionToEdit = null
                }
            },
            onDismiss = {
                showCreateEventDialog = false
                sessionToEdit = null
            }
        )
    }

    if (showManualFlightDialog) {
        AddEditManualFlightDialog(
            pigeons = allPigeons,
            events = completedSessions,
            sessionId = targetSessionForManualRecord,
            onConfirm = { pigeonId, selSessionId, releaseTs, landingTs, directDur, status, notes ->
                coroutineScope.launch {
                    viewModel.repository.addManualFlightRecord(
                        sessionId = selSessionId,
                        pigeonId = pigeonId,
                        releaseTimestamp = releaseTs,
                        landingTimestamp = landingTs,
                        directDurationMillis = directDur,
                        status = status,
                        notes = notes
                    )
                    showManualFlightDialog = false
                    targetSessionForManualRecord = null
                }
            },
            onDismiss = {
                showManualFlightDialog = false
                targetSessionForManualRecord = null
            }
        )
    }
}
