package com.highflyerpro.tracker.presentation.screens.pedigree

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.highflyerpro.tracker.data.local.entities.PigeonEntity
import com.highflyerpro.tracker.presentation.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedigreeScreen(
    pigeonId: String,
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPigeonDetail: (String) -> Unit
) {
    val repo = viewModel.repository
    val pigeonState by repo.getPigeonByIdFlow(pigeonId).collectAsState(initial = null)
    val allPigeons by repo.allPigeonsFlow.collectAsState(initial = emptyList())
    val pigeonsMap = remember(allPigeons) { allPigeons.associateBy { it.pigeonId } }

    var genLevel by remember { mutableStateOf(3) } // 2, 3, or 4 generations

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pedigree Tree", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        val target = pigeonState
        if (target == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Header & Controls
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("PEDIGREE: ${target.name} (${target.ringNumber.ifBlank { "No Ring" }})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Breed: ${target.breed} | Gender: ${target.gender} | Status: ${target.status}", style = MaterialTheme.typography.bodySmall)

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Generations:", style = MaterialTheme.typography.labelMedium)
                            FilterChip(selected = genLevel == 2, onClick = { genLevel = 2 }, label = { Text("2-Gen") })
                            FilterChip(selected = genLevel == 3, onClick = { genLevel = 3 }, label = { Text("3-Gen") })
                            FilterChip(selected = genLevel == 4, onClick = { genLevel = 4 }, label = { Text("4-Gen") })
                        }
                    }
                }

                // Scrollable Pedigree Tree Canvas
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(rememberScrollState())
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Gen 1: Target Pigeon
                        PedigreeNodeCard(
                            title = "Subject",
                            pigeon = target,
                            onClick = { onNavigateToPigeonDetail(target.pigeonId) }
                        )

                        // Gen 2: Parents
                        val father = pigeonsMap[target.fatherId]
                        val mother = pigeonsMap[target.motherId]

                        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                            PedigreeNodeCard(
                                title = "Sire (Father)",
                                pigeon = father,
                                fallbackId = target.fatherId,
                                onClick = { father?.pigeonId?.let { onNavigateToPigeonDetail(it) } }
                            )
                            PedigreeNodeCard(
                                title = "Dam (Mother)",
                                pigeon = mother,
                                fallbackId = target.motherId,
                                onClick = { mother?.pigeonId?.let { onNavigateToPigeonDetail(it) } }
                            )
                        }

                        // Gen 3: Grandparents
                        if (genLevel >= 3) {
                            val fFather = pigeonsMap[father?.fatherId]
                            val fMother = pigeonsMap[father?.motherId]
                            val mFather = pigeonsMap[mother?.fatherId]
                            val mMother = pigeonsMap[mother?.motherId]

                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                PedigreeNodeCard("Paternal Grandfather", fFather, father?.fatherId) { fFather?.pigeonId?.let { onNavigateToPigeonDetail(it) } }
                                PedigreeNodeCard("Paternal Grandmother", fMother, father?.motherId) { fMother?.pigeonId?.let { onNavigateToPigeonDetail(it) } }
                                PedigreeNodeCard("Maternal Grandfather", mFather, mother?.fatherId) { mFather?.pigeonId?.let { onNavigateToPigeonDetail(it) } }
                                PedigreeNodeCard("Maternal Grandmother", mMother, mother?.motherId) { mMother?.pigeonId?.let { onNavigateToPigeonDetail(it) } }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PedigreeNodeCard(
    title: String,
    pigeon: PigeonEntity?,
    fallbackId: String? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .clickable(enabled = pigeon != null) { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (pigeon != null) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            if (pigeon != null) {
                Text(pigeon.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(pigeon.ringNumber.ifBlank { "No Ring" }, style = MaterialTheme.typography.bodySmall)
                Text("${pigeon.gender} | ${pigeon.breed}", style = MaterialTheme.typography.labelSmall)
            } else {
                Text(fallbackId?.ifBlank { "Unknown" } ?: "Unspecified", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}
