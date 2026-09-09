package com.highflyerpro.tracker.presentation.screens.analytics

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
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.highflyerpro.tracker.domain.calculation.PerformanceEngine
import com.highflyerpro.tracker.presentation.components.OutdoorStatCard
import com.highflyerpro.tracker.presentation.components.PigeonAvatar
import com.highflyerpro.tracker.presentation.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: MainViewModel,
    onNavigateToPigeonDetail: (String) -> Unit
) {
    val pigeonsWithStats by viewModel.filteredPigeonsWithStats.collectAsState()
    val allFlightRecords by viewModel.allFlightRecords.collectAsState()

    var selectedForCompare by remember { mutableStateOf<Set<String>>(emptySet()) }

    val landed = allFlightRecords.filter { it.status == "LANDED" && (it.durationMillis ?: 0L) > 0 }
    val totalLoftMillis = landed.sumOf { it.durationMillis ?: 0L }
    val avgFlightDuration = if (landed.isNotEmpty()) totalLoftMillis / landed.size else 0L
    val bestLoftFlight = landed.maxOfOrNull { it.durationMillis ?: 0L } ?: 0L

    val compareItems = pigeonsWithStats.filter { selectedForCompare.contains(it.pigeon.pigeonId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "LOFT ANALYTICS",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Statistical Metrics & Comparative Analysis",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
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
            // Aggregate Loft Flight Statistics
            item {
                Text(
                    text = "LOFT AGGREGATE PERFORMANCE",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutdoorStatCard(
                        title = "Total Loft Time",
                        value = PerformanceEngine.formatDurationLong(totalLoftMillis),
                        subtitle = "${landed.size} Recorded Flights",
                        icon = Icons.Default.Timer,
                        modifier = Modifier.weight(1f)
                    )
                    OutdoorStatCard(
                        title = "Loft Record",
                        value = PerformanceEngine.formatDurationHMS(bestLoftFlight),
                        subtitle = "All-time Longest",
                        icon = Icons.Default.EmojiEvents,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutdoorStatCard(
                        title = "Average Flight",
                        value = PerformanceEngine.formatDurationHMS(avgFlightDuration),
                        subtitle = "Per Individual Flight",
                        icon = Icons.Default.DateRange,
                        modifier = Modifier.weight(1f)
                    )
                    OutdoorStatCard(
                        title = "Loft Size",
                        value = "${pigeonsWithStats.size}",
                        subtitle = "Registered Birds",
                        icon = Icons.Default.Group,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Pigeon Comparison Tool Section
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Compare, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "HEAD-TO-HEAD COMPARISON TOOL",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Select up to 4 pigeons below to compare flight consistency and endurance stats side by side:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Comparison Selector Chips
            item {
                Text(
                    text = "SELECT PIGEONS TO COMPARE (${selectedForCompare.size}/4)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(pigeonsWithStats) { item ->
                        val isSelected = selectedForCompare.contains(item.pigeon.pigeonId)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                val set = selectedForCompare.toMutableSet()
                                if (isSelected) {
                                    set.remove(item.pigeon.pigeonId)
                                } else {
                                    if (set.size < 4) set.add(item.pigeon.pigeonId)
                                }
                                selectedForCompare = set
                            },
                            label = { Text(item.pigeon.name) }
                        )
                    }
                }
            }

            // Comparison Results Table
            if (compareItems.isNotEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "COMPARISON MATRIX",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            compareItems.forEach { item ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable { onNavigateToPigeonDetail(item.pigeon.pigeonId) }
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                PigeonAvatar(
                                                    photoUri = item.pigeon.photoUri,
                                                    name = item.pigeon.name,
                                                    gender = item.pigeon.gender,
                                                    size = 32
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(item.pigeon.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            }
                                            Text(
                                                text = "${item.performanceScore} pts",
                                                fontWeight = FontWeight.Black,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 14.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Best: ${PerformanceEngine.formatDurationHMS(item.bestFlightMillis)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                            Text("Avg: ${PerformanceEngine.formatDurationHMS(item.averageFlightMillis)}", fontSize = 12.sp)
                                            Text("Consistency: ${item.consistencyScore}%", fontSize = 12.sp)
                                            Text("Form: ${item.currentForm}", fontSize = 12.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
