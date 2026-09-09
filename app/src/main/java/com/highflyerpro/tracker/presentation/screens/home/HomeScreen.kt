package com.highflyerpro.tracker.presentation.screens.home

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.highflyerpro.tracker.domain.calculation.PerformanceEngine
import com.highflyerpro.tracker.presentation.components.ActiveFlightBanner
import com.highflyerpro.tracker.presentation.components.OutdoorStatCard
import com.highflyerpro.tracker.presentation.components.PigeonAvatar
import com.highflyerpro.tracker.presentation.components.PigeonStatusChip
import com.highflyerpro.tracker.presentation.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToPigeons: () -> Unit,
    onNavigateToStartFlight: () -> Unit,
    onNavigateToLiveTracker: (String) -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToGroups: () -> Unit,
    onNavigateToPigeonDetail: (String) -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToBreeding: () -> Unit = {},
    onNavigateToHealth: () -> Unit = {},
    onNavigateToNutrition: () -> Unit = {},
    onNavigateToReminders: () -> Unit = {}
) {
    val activeSession by viewModel.activeSession.collectAsState()
    val liveTime by viewModel.currentLiveTimeMillis.collectAsState()
    val dismissedRecoveryId by viewModel.dismissedRecoverySessionId.collectAsState()
    val pigeonsWithStats by viewModel.filteredPigeonsWithStats.collectAsState()
    val flightRecords by viewModel.allFlightRecords.collectAsState()
    val recentEvents by viewModel.recentEvents.collectAsState()

    val totalActivePigeons = pigeonsWithStats.count { !it.pigeon.isArchived && it.pigeon.status == "Active" }
    val totalInTraining = pigeonsWithStats.count { !it.pigeon.isArchived && it.pigeon.status == "Training" }
    val totalLandedFlights = flightRecords.filter { it.status == "LANDED" }
    val totalLoftMillis = totalLandedFlights.sumOf { it.durationMillis ?: 0L }
    val topFlyers = pigeonsWithStats.sortedByDescending { it.bestFlightMillis }.take(5)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "High Flyer Pro Tracker",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Professional High Flyer Pigeon Loft Management System",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("home_settings_action")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings & Information")
                    }
                    Button(
                        onClick = onNavigateToStartFlight,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .testTag("home_launch_flight_action")
                    ) {
                        Icon(Icons.Default.FlightTakeoff, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Flight", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Active Flight Banner (Highest priority item if active)
            if (activeSession != null && activeSession?.sessionId != dismissedRecoveryId) {
                item {
                    val session = activeSession!!
                    val elapsed = (liveTime - session.releaseTimestamp).coerceAtLeast(0L)
                    ActiveFlightBanner(
                        session = session,
                        liveElapsedMillis = elapsed,
                        onViewLiveTracker = { onNavigateToLiveTracker(session.sessionId) },
                        onDismiss = { viewModel.dismissRecoverySession(session.sessionId) }
                    )
                }
            }

            // High-Contrast Loft Statistics Cards
            item {
                Column {
                    Text(
                        text = "LOFT OVERVIEW",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutdoorStatCard(
                            title = "Active Birds",
                            value = "$totalActivePigeons",
                            subtitle = "$totalInTraining Training",
                            icon = Icons.Default.Group,
                            modifier = Modifier.weight(1f)
                        )
                        OutdoorStatCard(
                            title = "Loft Flight Time",
                            value = PerformanceEngine.formatDurationLong(totalLoftMillis),
                            subtitle = "${totalLandedFlights.size} Completed",
                            icon = Icons.Default.Timer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Quick Operations Grid
            item {
                Column {
                    Text(
                        text = "QUICK OPERATIONS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToStartFlight() }
                                .testTag("quick_start_flight_card")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Icon(Icons.Default.FlightTakeoff, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Release Flight", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Start live flight timer", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        }

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToLeaderboard() }
                                .testTag("quick_leaderboard_card")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFF59E0B))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Event Standings", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Rankings & Points", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "LOFT OPERATIONS HUB",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToBreeding() }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Breeding", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Pairs & Clutches", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        }
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToHealth() }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Health Care", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Medication & Deworming", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        }
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToNutrition() }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = Color(0xFF10B981))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Feed & Stock", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Grains & Mixes", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        }
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToReminders() }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Icon(Icons.Default.History, contentDescription = null, tint = Color(0xFFF59E0B))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Alerts", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Tasks & Tags", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            }

            // Top Performing Champions Roster
            if (topFlyers.isNotEmpty()) {
                item {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "ENDURANCE CHAMPIONS",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "View All",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { onNavigateToPigeons() }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(topFlyers) { item ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    modifier = Modifier
                                        .width(180.dp)
                                        .clickable { onNavigateToPigeonDetail(item.pigeon.pigeonId) }
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            PigeonAvatar(
                                                photoUri = item.pigeon.photoUri,
                                                name = item.pigeon.name,
                                                gender = item.pigeon.gender,
                                                size = 36
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = item.pigeon.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    maxLines = 1
                                                )
                                                Text(
                                                    text = item.pigeon.ringNumber.ifBlank { "Unbanded" },
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = "Best: ${PerformanceEngine.formatDurationLong(item.bestFlightMillis)}",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            PigeonStatusChip(status = item.pigeon.status)
                                            Text(
                                                text = "${item.performanceScore} pts",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = Color(0xFF16A34A)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Real-Time Activity Log Feed
            item {
                Column {
                    Text(
                        text = "RECENT TIMELINE ACTIVITY",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            if (recentEvents.isEmpty()) {
                item {
                    Text(
                        text = "No recent events recorded yet.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else {
                items(recentEvents.take(10)) { event ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToPigeonDetail(event.pigeonId) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Icon(
                                imageVector = when (event.eventType) {
                                    "FLIGHT_STARTED" -> Icons.Default.FlightTakeoff
                                    "FLIGHT_COMPLETED" -> Icons.Default.MilitaryTech
                                    "ACHIEVEMENT_UNLOCKED" -> Icons.Default.EmojiEvents
                                    else -> Icons.Default.History
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = event.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = event.description,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            Text(
                                text = PerformanceEngine.formatTimeOnly(event.timestamp),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}
