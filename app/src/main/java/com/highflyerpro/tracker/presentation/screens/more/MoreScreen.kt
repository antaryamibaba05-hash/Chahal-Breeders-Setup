package com.highflyerpro.tracker.presentation.screens.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class MoreOptionItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    onNavigateToAnalytics: () -> Unit,
    onNavigateToHallOfFame: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val options = listOf(
        MoreOptionItem(
            title = "Hall of Fame & Champions",
            subtitle = "Spotlight for Record Holders, Cup Winners & Legends",
            icon = Icons.Default.EmojiEvents,
            color = Color(0xFFEAB308),
            onClick = onNavigateToHallOfFame
        ),
        MoreOptionItem(
            title = "Flight Leaderboard & Standings",
            subtitle = "Overall Flight Duration Rankings & Performance Scores",
            icon = Icons.Default.Leaderboard,
            color = Color(0xFF2563EB),
            onClick = onNavigateToLeaderboard
        ),
        MoreOptionItem(
            title = "Performance Intelligence Analytics",
            subtitle = "Flight Duration Trends, Graphs & Loft Statistics",
            icon = Icons.Default.BarChart,
            color = Color(0xFF059669),
            onClick = onNavigateToAnalytics
        ),
        MoreOptionItem(
            title = "PDF & CSV Export Center",
            subtitle = "Generate Printable PDF Certificates, Event Results & CSV Data",
            icon = Icons.Default.PictureAsPdf,
            color = Color(0xFFDC2626),
            onClick = onNavigateToReports
        ),
        MoreOptionItem(
            title = "Application Settings",
            subtitle = "Theme, Text Scaling, Default Flight Rules & Data Backup",
            icon = Icons.Default.Settings,
            color = Color(0xFF4B5563),
            onClick = onNavigateToSettings
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("CHAHAL BREEDERS SETUP", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("System Menu & Intelligence", style = MaterialTheme.typography.bodySmall)
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
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("CHAHAL BREEDERS SETUP", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("Professional Pigeon Loft Management System", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("Offline Native Android • Room Database • High Flyer Tracking", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                    }
                }
            }

            items(options) { option ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { option.onClick() },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            color = option.color.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = option.icon,
                                contentDescription = option.title,
                                tint = option.color,
                                modifier = Modifier
                                    .padding(10.dp)
                                    .size(26.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(option.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(option.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Icon(Icons.Default.ChevronRight, contentDescription = "Open", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
