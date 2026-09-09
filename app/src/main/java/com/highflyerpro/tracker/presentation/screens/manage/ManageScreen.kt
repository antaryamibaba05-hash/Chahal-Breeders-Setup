package com.highflyerpro.tracker.presentation.screens.manage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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

data class ManageTileItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val badge: String = "",
    val color: Color,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageScreen(
    onNavigateToBreeding: () -> Unit,
    onNavigateToHealth: () -> Unit,
    onNavigateToNutrition: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToArchive: () -> Unit,
    onNavigateToTrash: () -> Unit,
    onNavigateToGroups: () -> Unit
) {
    val manageTiles = listOf(
        ManageTileItem(
            title = "Breeding Center",
            description = "Pairings, Nest Boxes, Egg Clutches & Hatch Dates",
            icon = Icons.Default.Favorite,
            color = Color(0xFFE11D48),
            onClick = onNavigateToBreeding
        ),
        ManageTileItem(
            title = "Health & Medical Log",
            description = "Medications, Deworming Automation & Illness Tracking",
            icon = Icons.Default.MedicalServices,
            color = Color(0xFF0284C7),
            onClick = onNavigateToHealth
        ),
        ManageTileItem(
            title = "Nutrition & Grain Stock",
            description = "Feed Recipes, Protein Formulas & Stock Inventory Alerts",
            icon = Icons.Default.Restaurant,
            color = Color(0xFFD97706),
            onClick = onNavigateToNutrition
        ),
        ManageTileItem(
            title = "Task Alerts & Tags",
            description = "Loft Maintenance Schedules & Custom Color Tags",
            icon = Icons.Default.Notifications,
            color = Color(0xFF7C3AED),
            onClick = onNavigateToReminders
        ),
        ManageTileItem(
            title = "Groups & Squads",
            description = "High Flyers, Young Birds, Breeders & Training Teams",
            icon = Icons.Default.Group,
            color = Color(0xFF059669),
            onClick = onNavigateToGroups
        ),
        ManageTileItem(
            title = "Archive Center",
            description = "Preserved Lifetime Historical Records & Retired Birds",
            icon = Icons.Default.Archive,
            color = Color(0xFF475569),
            onClick = onNavigateToArchive
        ),
        ManageTileItem(
            title = "Trash / Recycle Bin",
            description = "Recoverable Soft-Deleted Pigeons & Safety System",
            icon = Icons.Default.DeleteSweep,
            color = Color(0xFFDC2626),
            onClick = onNavigateToTrash
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("CHAHAL BREEDERS SETUP", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Loft Operations Hub", style = MaterialTheme.typography.bodySmall)
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            items(manageTiles) { tile ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { tile.onClick() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = tile.color.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = tile.icon,
                                contentDescription = tile.title,
                                tint = tile.color,
                                modifier = Modifier
                                    .padding(10.dp)
                                    .size(28.dp)
                            )
                        }

                        Text(tile.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(
                            tile.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}
