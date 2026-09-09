package com.highflyerpro.tracker.presentation.screens.flight

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.FlightLand
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.highflyerpro.tracker.data.local.entities.FlightRecordEntity
import com.highflyerpro.tracker.domain.calculation.PerformanceEngine
import com.highflyerpro.tracker.presentation.components.LandingConfirmationDialog
import com.highflyerpro.tracker.presentation.components.LandingCorrectionDialog
import com.highflyerpro.tracker.presentation.components.LiveParticipantTrackerCard
import com.highflyerpro.tracker.presentation.viewmodel.MainViewModel
import com.highflyerpro.tracker.ui.theme.StatusFlying
import com.highflyerpro.tracker.ui.theme.StatusLanded
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveFlightTrackerScreen(
    viewModel: MainViewModel,
    sessionId: String,
    onNavigateBack: () -> Unit,
    onFlightCompleted: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val sessionFlow = viewModel.repository.getSessionByIdFlow(sessionId).collectAsState(initial = null)
    val session = sessionFlow.value

    val records by viewModel.repository.getRecordsBySessionFlow(sessionId).collectAsState(initial = emptyList())
    val liveCurrentTime by viewModel.currentLiveTimeMillis.collectAsState()

    val pendingLandingRecord by viewModel.pendingLandingRecord.collectAsState()
    val recordToCorrect by viewModel.recordToCorrect.collectAsState()
    val allPigeons by viewModel.allPigeons.collectAsState()
    val pigeonMap = remember(allPigeons) { allPigeons.associateBy { it.pigeonId } }

    var showEndFlightDialog by remember { mutableStateOf(false) }

    val flyingRecords = records.filter { it.status == "FLYING" }
    val landedRecords = records.filter { it.status == "LANDED" }

    // REAL TIME ELAPSED CALCULATION: System.currentTimeMillis() - releaseTimestamp
    val releaseTime = session?.releaseTimestamp ?: System.currentTimeMillis()
    val liveElapsedMillis = (liveCurrentTime - releaseTime).coerceAtLeast(0L)
    val liveElapsedHMS = PerformanceEngine.formatDurationHMS(liveElapsedMillis)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = session?.name ?: "Live Flight Tracker",
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "Release: ${PerformanceEngine.formatTimeOnly(releaseTime)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = { showEndFlightDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("end_session_action_button")
                    ) {
                        Icon(Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Finish", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // High-Contrast Outdoor Live Stopwatch Panel
            Card(
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF0F172A) // Rich Dark Night / Outdoor High Contrast Canvas
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 20.dp, horizontal = 16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF22C55E))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "OFFICIAL FLIGHT DURATION",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Huge Outdoor Stopwatch Timer
                    Text(
                        text = liveElapsedHMS,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF38BDF8), // Glowing High-Visibility Cyan
                        letterSpacing = 2.sp,
                        modifier = Modifier.testTag("live_stopwatch_text")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Live participant counts
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("FLYING", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${flyingRecords.size}",
                                color = StatusFlying,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("LANDED", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${landedRecords.size}",
                                color = StatusLanded,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("TOTAL", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${records.size}",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            // Participants List (Flying First, then Landed in Chronological Order)
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (flyingRecords.isNotEmpty()) {
                    item {
                        Text(
                            text = "FLYING AT ALTITUDE (${flyingRecords.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = StatusFlying
                        )
                    }
                    items(flyingRecords, key = { it.id }) { record ->
                        val pigeon = pigeonMap[record.pigeonId]
                        LiveParticipantTrackerCard(
                            record = record,
                            liveElapsedMillis = liveElapsedMillis,
                            photoUri = pigeon?.photoUri ?: "",
                            gender = pigeon?.gender ?: "Male",
                            onMarkLanded = { viewModel.onMarkLandedClicked(record) },
                            onEditRecord = { viewModel.openCorrectionDialog(record) }
                        )
                    }
                }

                if (landedRecords.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "CONFIRMED LANDED CHRONOLOGICAL RANKING (${landedRecords.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = StatusLanded
                        )
                    }
                    items(landedRecords.sortedBy { it.landingPosition ?: 999 }, key = { it.id }) { record ->
                        val pigeon = pigeonMap[record.pigeonId]
                        LiveParticipantTrackerCard(
                            record = record,
                            liveElapsedMillis = liveElapsedMillis,
                            photoUri = pigeon?.photoUri ?: "",
                            gender = pigeon?.gender ?: "Male",
                            onMarkLanded = { /* already landed */ },
                            onEditRecord = { viewModel.openCorrectionDialog(record) }
                        )
                    }
                }
            }
        }
    }

    // Landing Confirmation Dialog
    if (pendingLandingRecord != null) {
        LandingConfirmationDialog(
            record = pendingLandingRecord!!,
            releaseTimestamp = releaseTime,
            onConfirm = { landingTime, notes ->
                viewModel.confirmLanding(landingTime, notes)
            },
            onDismiss = { viewModel.cancelLanding() }
        )
    }

    // Landing Correction Dialog
    if (recordToCorrect != null) {
        LandingCorrectionDialog(
            record = recordToCorrect!!,
            onConfirmCorrection = { newTime, newStatus, notes ->
                viewModel.submitCorrection(newTime, newStatus, notes)
            },
            onDismiss = { viewModel.closeCorrectionDialog() }
        )
    }

    // End Session Dialog
    if (showEndFlightDialog) {
        AlertDialog(
            onDismissRequest = { showEndFlightDialog = false },
            title = { Text("Complete Flight Session") },
            text = {
                Column {
                    Text("Are you sure you want to finalize this flight session?")
                    if (flyingRecords.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Note: ${flyingRecords.size} birds are still marked as flying. They will be finalized at the current timestamp.",
                            color = Color(0xFFEF4444),
                            fontSize = 13.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.repository.endSessionManually(sessionId)
                            showEndFlightDialog = false
                            onFlightCompleted(sessionId)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    modifier = Modifier.testTag("confirm_finish_flight_button")
                ) {
                    Text("Finalize Session")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndFlightDialog = false }) {
                    Text("Continue Flying")
                }
            }
        )
    }
}
