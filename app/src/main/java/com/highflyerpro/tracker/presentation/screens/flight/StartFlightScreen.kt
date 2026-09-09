package com.highflyerpro.tracker.presentation.screens.flight

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.highflyerpro.tracker.data.local.entities.PigeonEntity
import com.highflyerpro.tracker.domain.calculation.PerformanceEngine
import com.highflyerpro.tracker.presentation.components.PigeonAvatar
import com.highflyerpro.tracker.presentation.components.PigeonStatusChip
import com.highflyerpro.tracker.presentation.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartFlightScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onFlightStarted: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val allPigeons by viewModel.allPigeons.collectAsState()
    val allGroups by viewModel.allGroups.collectAsState()

    var sessionName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedGroupFilter by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedPigeonIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var isStarting by remember { mutableStateOf(false) }

    val activeEligiblePigeons = allPigeons.filter { !it.isArchived && (it.status == "Active" || it.status == "Training") }

    val filteredPigeons = activeEligiblePigeons.filter { p ->
        val matchesQuery = searchQuery.isBlank() ||
                p.name.contains(searchQuery, ignoreCase = true) ||
                p.ringNumber.contains(searchQuery, ignoreCase = true) ||
                p.breed.contains(searchQuery, ignoreCase = true)
        matchesQuery
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Release New Flight", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "${selectedPigeonIds.size} Selected",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Release timestamp captured on launch",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    Button(
                        onClick = {
                            if (selectedPigeonIds.isNotEmpty() && !isStarting) {
                                isStarting = true
                                val now = System.currentTimeMillis()
                                val selectedList = allPigeons.filter { selectedPigeonIds.contains(it.pigeonId) }
                                val name = sessionName.ifBlank { "Flight on ${PerformanceEngine.formatDateOnly(now)}" }

                                coroutineScope.launch {
                                    val sessionId = viewModel.repository.startFlightSession(
                                        name = name,
                                        date = now,
                                        releaseTimestamp = now,
                                        selectedPigeons = selectedList,
                                        notes = notes
                                    )
                                    isStarting = false
                                    onFlightStarted(sessionId)
                                }
                            }
                        },
                        enabled = selectedPigeonIds.isNotEmpty() && !isStarting,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("launch_flight_button")
                    ) {
                        if (isStarting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Icon(Icons.Default.FlightTakeoff, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("RELEASE FLIGHT", fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Flight Details Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "FLIGHT SESSION DETAILS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = sessionName,
                        onValueChange = { sessionName = it },
                        label = { Text("Session Name / Tournament Title") },
                        placeholder = { Text("e.g. Morning Conditioning Flight, Cup Trial #1") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("session_name_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Flight Conditions / Notes (Optional)") },
                        placeholder = { Text("e.g. High altitude clear skies, light breeze") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Pigeon Selection Header & Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "SELECT PARTICIPATING BIRDS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Row {
                    Text(
                        text = "Select All",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable {
                                selectedPigeonIds = filteredPigeons.map { it.pigeonId }.toSet()
                            }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Clear",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier
                            .clickable { selectedPigeonIds = emptySet() }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }
            }

            // Quick Search within release roster
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Filter candidate birds...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Participants Checklist
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredPigeons, key = { it.pigeonId }) { pigeon ->
                    val isChecked = selectedPigeonIds.contains(pigeon.pigeonId)
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isChecked) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val set = selectedPigeonIds.toMutableSet()
                                if (isChecked) set.remove(pigeon.pigeonId) else set.add(pigeon.pigeonId)
                                selectedPigeonIds = set
                            }
                            .testTag("select_pigeon_row_${pigeon.pigeonId}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(10.dp)
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = {
                                    val set = selectedPigeonIds.toMutableSet()
                                    if (isChecked) set.remove(pigeon.pigeonId) else set.add(pigeon.pigeonId)
                                    selectedPigeonIds = set
                                }
                            )
                            PigeonAvatar(
                                photoUri = pigeon.photoUri,
                                name = pigeon.name,
                                gender = pigeon.gender,
                                size = 40
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = pigeon.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(
                                    text = "Ring: ${pigeon.ringNumber.ifBlank { "Unbanded" }} • ${pigeon.breed}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            PigeonStatusChip(status = pigeon.status)
                        }
                    }
                }
            }
        }
    }
}
