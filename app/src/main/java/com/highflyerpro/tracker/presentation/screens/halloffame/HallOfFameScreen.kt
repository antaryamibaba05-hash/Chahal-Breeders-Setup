package com.highflyerpro.tracker.presentation.screens.halloffame

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.highflyerpro.tracker.domain.calculation.PerformanceEngine
import com.highflyerpro.tracker.presentation.components.PigeonAvatar
import com.highflyerpro.tracker.presentation.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HallOfFameScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPigeonDetail: (String) -> Unit
) {
    val pigeonsWithStats by viewModel.filteredPigeonsWithStats.collectAsState()
    val achievements by viewModel.repository.allAchievementsFlow.collectAsState(initial = emptyList())

    val champions = pigeonsWithStats.filter { item ->
        item.pigeon.status.contains("RETIRED", ignoreCase = true) ||
        item.bestFlightMillis > (7 * 3600 * 1000L) ||
        achievements.any { it.pigeonId == item.pigeon.pigeonId }
    }.sortedByDescending { it.bestFlightMillis }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hall of Fame & Legends", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = "Hall of Fame",
                            tint = Color(0xFFEAB308),
                            modifier = Modifier.size(36.dp)
                        )
                        Column {
                            Text("High Flyer Pro Hall of Fame", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("Celebrating top performers, 7+ hour flyers, and retired legends of the loft.", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            if (champions.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text("No champion records yet logged.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                items(champions) { item ->
                    val pigeon = item.pigeon
                    val pigeonAchievements = achievements.filter { it.pigeonId == pigeon.pigeonId }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToPigeonDetail(pigeon.pigeonId) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                PigeonAvatar(
                                    photoUri = pigeon.photoUri,
                                    name = pigeon.name,
                                    gender = pigeon.gender,
                                    size = 52
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(pigeon.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Icon(Icons.Default.Star, contentDescription = "Champion", tint = Color(0xFFEAB308), modifier = Modifier.size(18.dp))
                                    }
                                    Text("Ring: ${pigeon.ringNumber.ifBlank { "No Ring" }} | Breed: ${pigeon.breed}", style = MaterialTheme.typography.bodySmall)
                                    Text("Status: ${pigeon.status}", style = MaterialTheme.typography.labelSmall)
                                }
                            }

                            HorizontalDivider()

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Best Flight", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        if (item.bestFlightMillis > 0) PerformanceEngine.formatDurationHMS(item.bestFlightMillis) else "N/A",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Column {
                                    Text("Total Flights", style = MaterialTheme.typography.labelSmall)
                                    Text("${item.totalFlights}", fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text("Achievements", style = MaterialTheme.typography.labelSmall)
                                    Text("${pigeonAchievements.size}", fontWeight = FontWeight.Bold)
                                }
                            }

                            if (pigeonAchievements.isNotEmpty()) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    pigeonAchievements.take(3).forEach { ach ->
                                        AssistChip(
                                            onClick = { onNavigateToPigeonDetail(item.pigeon.pigeonId) },
                                            label = { Text(ach.title, fontSize = 10.sp) },
                                            leadingIcon = { Icon(Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(12.dp)) }
                                        )
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
