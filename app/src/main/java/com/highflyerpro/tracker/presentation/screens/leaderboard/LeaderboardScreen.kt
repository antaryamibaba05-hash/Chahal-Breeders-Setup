package com.highflyerpro.tracker.presentation.screens.leaderboard

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.highflyerpro.tracker.data.local.entities.PointRuleEntity
import com.highflyerpro.tracker.domain.calculation.PerformanceEngine
import com.highflyerpro.tracker.domain.model.LeaderboardEntry
import com.highflyerpro.tracker.presentation.components.PigeonAvatar
import com.highflyerpro.tracker.presentation.viewmodel.LeaderboardRankingMode
import com.highflyerpro.tracker.presentation.viewmodel.LeaderboardTimeframe
import com.highflyerpro.tracker.presentation.viewmodel.MainViewModel
import com.highflyerpro.tracker.ui.theme.BronzeMedal
import com.highflyerpro.tracker.ui.theme.GoldTrophy
import com.highflyerpro.tracker.ui.theme.SilverMedal
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    viewModel: MainViewModel,
    onNavigateToPigeonDetail: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val entries by viewModel.leaderboardEntries.collectAsState()
    val timeframe by viewModel.leaderboardTimeframe.collectAsState()
    val rankingMode by viewModel.leaderboardRankingMode.collectAsState()
    val pointRules by viewModel.pointRules.collectAsState()

    var showPointRulesDialog by remember { mutableStateOf(false) }

    val timeframes = listOf(
        Pair(LeaderboardTimeframe.LIFETIME, "Lifetime"),
        Pair(LeaderboardTimeframe.YEAR, "This Year"),
        Pair(LeaderboardTimeframe.MONTH, "This Month"),
        Pair(LeaderboardTimeframe.WEEK, "This Week"),
        Pair(LeaderboardTimeframe.TODAY, "Today")
    )

    val rankingModes = listOf(
        Pair(LeaderboardRankingMode.LONGEST_FLIGHT, "Longest Flight"),
        Pair(LeaderboardRankingMode.TOTAL_FLIGHT_TIME, "Total Hours"),
        Pair(LeaderboardRankingMode.AVERAGE_FLIGHT, "Average Flight"),
        Pair(LeaderboardRankingMode.PERFORMANCE_SCORE, "Score"),
        Pair(LeaderboardRankingMode.CONSISTENCY, "Consistency"),
        Pair(LeaderboardRankingMode.CUSTOM_POINTS, "Event Points")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "EVENT & LOFT LEADERBOARD",
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Official Loft Standings & Endurance Ranks",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showPointRulesDialog = true },
                        modifier = Modifier.testTag("point_rules_button")
                    ) {
                        Icon(Icons.Default.Rule, contentDescription = "Point Rules")
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
            // Timeframe Selector Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(timeframes) { tf ->
                    FilterChip(
                        selected = timeframe == tf.first,
                        onClick = { viewModel.setLeaderboardTimeframe(tf.first) },
                        label = { Text(tf.second, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            // Ranking Mode Selector Tabs
            ScrollableTabRow(
                selectedTabIndex = rankingModes.indexOfFirst { it.first == rankingMode }.coerceAtLeast(0),
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                rankingModes.forEach { mode ->
                    Tab(
                        selected = rankingMode == mode.first,
                        onClick = { viewModel.setLeaderboardRankingMode(mode.first) },
                        text = {
                            Text(
                                text = mode.second,
                                fontWeight = if (rankingMode == mode.first) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }

            // Top 3 Podium Highlights (if available)
            if (entries.size >= 3) {
                val rank1 = entries[0]
                val rank2 = entries[1]
                val rank3 = entries[2]

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Rank 2 (Silver)
                    PodiumCard(
                        entry = rank2,
                        rank = 2,
                        badgeColor = SilverMedal,
                        rankingMode = rankingMode,
                        onClick = { onNavigateToPigeonDetail(rank2.pigeonId) },
                        modifier = Modifier.weight(1f)
                    )

                    // Rank 1 (Gold) - Elevated Center
                    PodiumCard(
                        entry = rank1,
                        rank = 1,
                        badgeColor = GoldTrophy,
                        rankingMode = rankingMode,
                        onClick = { onNavigateToPigeonDetail(rank1.pigeonId) },
                        modifier = Modifier.weight(1.15f)
                    )

                    // Rank 3 (Bronze)
                    PodiumCard(
                        entry = rank3,
                        rank = 3,
                        badgeColor = BronzeMedal,
                        rankingMode = rankingMode,
                        onClick = { onNavigateToPigeonDetail(rank3.pigeonId) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Full Leaderboard Table List
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(entries) { item ->
                    LeaderboardRowItem(
                        entry = item,
                        rankingMode = rankingMode,
                        onClick = { onNavigateToPigeonDetail(item.pigeonId) }
                    )
                }
            }
        }
    }

    // Configurable Point Rules Dialog
    if (showPointRulesDialog) {
        var newMinHours by remember { mutableStateOf("") }
        var newPoints by remember { mutableStateOf("") }
        var newLabel by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showPointRulesDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = GoldTrophy)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Event Point Rules", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        Text(
                            text = "Points awarded per flight based on duration:",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }

                    items(pointRules) { rule ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Column {
                                    Text(text = rule.label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        text = "${rule.minHours}+ Hours",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${rule.points} pts",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                viewModel.repository.deletePointRule(rule.id)
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Add Custom Rule", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = newLabel,
                            onValueChange = { newLabel = it },
                            label = { Text("Label (e.g. 11+ Hours)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = newMinHours,
                                onValueChange = { newMinHours = it },
                                label = { Text("Min Hours") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = newPoints,
                                onValueChange = { newPoints = it },
                                label = { Text("Points") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = {
                                val minH = newMinHours.toDoubleOrNull() ?: 0.0
                                val pts = newPoints.toIntOrNull() ?: 0
                                if (newLabel.isNotBlank() && pts > 0) {
                                    coroutineScope.launch {
                                        viewModel.repository.addPointRule(
                                            PointRuleEntity(minHours = minH, maxHours = 999.0, points = pts, label = newLabel.trim())
                                        )
                                        newLabel = ""
                                        newMinHours = ""
                                        newPoints = ""
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Add Point Rule")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPointRulesDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun PodiumCard(
    entry: LeaderboardEntry,
    rank: Int,
    badgeColor: Color,
    rankingMode: LeaderboardRankingMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = if (rank == 1) 4.dp else 2.dp),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = badgeColor,
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "#$rank",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            PigeonAvatar(
                photoUri = entry.photoUri,
                name = entry.name,
                gender = "Male",
                size = if (rank == 1) 48 else 40
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = entry.name,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1
            )
            Text(
                text = when (rankingMode) {
                    LeaderboardRankingMode.LONGEST_FLIGHT -> PerformanceEngine.formatDurationHMS(entry.bestDurationMillis)
                    LeaderboardRankingMode.TOTAL_FLIGHT_TIME -> PerformanceEngine.formatDurationLong(entry.totalDurationMillis)
                    LeaderboardRankingMode.AVERAGE_FLIGHT -> PerformanceEngine.formatDurationHMS(entry.averageDurationMillis)
                    LeaderboardRankingMode.CUSTOM_POINTS -> "${entry.points} pts"
                    LeaderboardRankingMode.CONSISTENCY -> "${entry.consistencyScore}%"
                    LeaderboardRankingMode.PERFORMANCE_SCORE -> "${entry.performanceScore} pts"
                    else -> PerformanceEngine.formatDurationHMS(entry.bestDurationMillis)
                },
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun LeaderboardRowItem(
    entry: LeaderboardEntry,
    rankingMode: LeaderboardRankingMode,
    onClick: () -> Unit
) {
    val rankBadgeColor = when (entry.rank) {
        1 -> GoldTrophy
        2 -> SilverMedal
        3 -> BronzeMedal
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = rankBadgeColor,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "${entry.rank}",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = if (entry.rank in 1..3) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            PigeonAvatar(
                photoUri = entry.photoUri,
                name = entry.name,
                gender = "Male",
                size = 40
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = entry.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(
                    text = "Ring: ${entry.ringNumber.ifBlank { "Unbanded" }} • ${entry.flightsCount} Flights",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                val primaryValue = when (rankingMode) {
                    LeaderboardRankingMode.LONGEST_FLIGHT -> PerformanceEngine.formatDurationHMS(entry.bestDurationMillis)
                    LeaderboardRankingMode.TOTAL_FLIGHT_TIME -> PerformanceEngine.formatDurationLong(entry.totalDurationMillis)
                    LeaderboardRankingMode.AVERAGE_FLIGHT -> PerformanceEngine.formatDurationHMS(entry.averageDurationMillis)
                    LeaderboardRankingMode.CUSTOM_POINTS -> "${entry.points} pts"
                    LeaderboardRankingMode.CONSISTENCY -> "${entry.consistencyScore}%"
                    LeaderboardRankingMode.PERFORMANCE_SCORE -> "${entry.performanceScore} pts"
                    else -> PerformanceEngine.formatDurationHMS(entry.bestDurationMillis)
                }

                Text(
                    text = primaryValue,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Best: ${PerformanceEngine.formatDurationHMS(entry.bestDurationMillis)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}
