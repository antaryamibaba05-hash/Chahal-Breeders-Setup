package com.highflyerpro.tracker.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.highflyerpro.tracker.data.local.entities.FlightSessionEntity
import com.highflyerpro.tracker.data.local.entities.PigeonEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditEventDialog(
    existingSession: FlightSessionEntity? = null,
    onConfirm: (name: String, eventType: String, releaseTimestamp: Long, location: String, rules: String, description: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(existingSession?.name ?: "") }
    var eventType by remember { mutableStateOf(existingSession?.eventType ?: "Training") }
    var location by remember { mutableStateOf(existingSession?.location ?: "") }
    var rules by remember { mutableStateOf(existingSession?.rules ?: "") }
    var description by remember { mutableStateOf(existingSession?.description ?: "") }

    val eventTypes = listOf("Training", "Practice", "Competition", "Tournament", "Cup", "Personal Record", "Custom")
    var typeDropdownExpanded by remember { mutableStateOf(false) }

    val initCal = remember {
        Calendar.getInstance().apply {
            timeInMillis = existingSession?.releaseTimestamp ?: System.currentTimeMillis()
        }
    }

    var selectedDateMillis by remember { mutableStateOf(initCal.timeInMillis) }
    var releaseHour by remember { mutableStateOf(initCal.get(Calendar.HOUR_OF_DAY).toString()) }
    var releaseMinute by remember { mutableStateOf(initCal.get(Calendar.MINUTE).toString()) }

    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDateMillis = it }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Event, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (existingSession == null) "Create Custom Event" else "Edit Event Details",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Event Name *") },
                        placeholder = { Text("e.g. Loft Spring Competition") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    ExposedDropdownMenuBox(
                        expanded = typeDropdownExpanded,
                        onExpandedChange = { typeDropdownExpanded = !typeDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = eventType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Event Category / Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = typeDropdownExpanded,
                            onDismissRequest = { typeDropdownExpanded = false }
                        ) {
                            eventTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type) },
                                    onClick = {
                                        eventType = type
                                        typeDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    Text("Event Date & Start Time", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.weight(1.5f)
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(dateFormat.format(Date(selectedDateMillis)), fontSize = 12.sp)
                        }

                        OutlinedTextField(
                            value = releaseHour,
                            onValueChange = { if (it.length <= 2) releaseHour = it },
                            label = { Text("Hour") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = releaseMinute,
                            onValueChange = { if (it.length <= 2) releaseMinute = it },
                            label = { Text("Min") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location / Loft") },
                        placeholder = { Text("e.g. Main Loft") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = rules,
                        onValueChange = { rules = it },
                        label = { Text("Event Rules / Scoring System") },
                        placeholder = { Text("e.g. Minimum 6 hours flight required") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description / Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val finalCal = Calendar.getInstance().apply {
                            timeInMillis = selectedDateMillis
                            set(Calendar.HOUR_OF_DAY, releaseHour.toIntOrNull() ?: 6)
                            set(Calendar.MINUTE, releaseMinute.toIntOrNull() ?: 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        onConfirm(
                            name.trim(),
                            eventType,
                            finalCal.timeInMillis,
                            location.trim(),
                            rules.trim(),
                            description.trim()
                        )
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text(if (existingSession == null) "Create Event" else "Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditManualFlightDialog(
    pigeons: List<PigeonEntity>,
    events: List<FlightSessionEntity> = emptyList(),
    sessionId: String? = null,
    existingRecordId: Long? = null,
    initialPigeonId: String? = null,
    onConfirm: (pigeonId: String, selectedSessionId: String?, releaseTimestamp: Long, landingTimestamp: Long?, directDurationMillis: Long?, status: String, notes: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    // Required explicit pigeon selection. No automatic pigeon selection.
    var selectedPigeonId by remember { mutableStateOf(initialPigeonId ?: "") }
    var pigeonSearchQuery by remember { mutableStateOf("") }
    var pigeonDropdownExpanded by remember { mutableStateOf(false) }

    // Flight Type: Personal Flight (sessionId = null) vs Event Flight
    var isEventFlight by remember { mutableStateOf(sessionId != null) }
    var selectedSessionId by remember { mutableStateOf(sessionId) }
    var eventDropdownExpanded by remember { mutableStateOf(false) }

    var isDirectDurationMode by remember { mutableStateOf(false) }
    var attemptedSave by remember { mutableStateOf(false) }

    // Release Date & Time State
    val releaseCal = remember { Calendar.getInstance() }
    var releaseDateMillis by remember { mutableStateOf(releaseCal.timeInMillis) }
    var releaseHourInt by remember { mutableStateOf(releaseCal.get(Calendar.HOUR_OF_DAY)) }
    var releaseMinuteInt by remember { mutableStateOf(releaseCal.get(Calendar.MINUTE)) }
    var showReleaseTimePicker by remember { mutableStateOf(false) }

    // Landing Date & Time State
    val landingCal = remember { Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 8) } }
    var landingDateMillis by remember { mutableStateOf(landingCal.timeInMillis) }
    var landingHourInt by remember { mutableStateOf(landingCal.get(Calendar.HOUR_OF_DAY)) }
    var landingMinuteInt by remember { mutableStateOf(landingCal.get(Calendar.MINUTE)) }
    var showLandingTimePicker by remember { mutableStateOf(false) }

    // Direct Duration Mode
    var durationHoursInput by remember { mutableStateOf("8") }
    var durationMinutesInput by remember { mutableStateOf("0") }

    var status by remember { mutableStateOf("LANDED") }
    var notes by remember { mutableStateOf("") }

    var showReleaseDatePicker by remember { mutableStateOf(false) }
    var showLandingDatePicker by remember { mutableStateOf(false) }

    val statusList = listOf("LANDED", "FLYING", "DISQUALIFIED", "DID_NOT_FLY", "MISSING")
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    // Filter pigeons
    val filteredPigeons = remember(pigeons, pigeonSearchQuery) {
        if (pigeonSearchQuery.isBlank()) pigeons
        else pigeons.filter {
            it.name.contains(pigeonSearchQuery, ignoreCase = true) ||
            it.ringNumber.contains(pigeonSearchQuery, ignoreCase = true) ||
            it.breed.contains(pigeonSearchQuery, ignoreCase = true)
        }
    }

    val selectedPigeon = pigeons.find { it.pigeonId == selectedPigeonId }
    val selectedEvent = events.find { it.sessionId == selectedSessionId }

    // Compute timestamps for validation cleanly without silent fallbacks
    val computedReleaseTs = remember(releaseDateMillis, releaseHourInt, releaseMinuteInt) {
        Calendar.getInstance().apply {
            timeInMillis = releaseDateMillis
            set(Calendar.HOUR_OF_DAY, releaseHourInt)
            set(Calendar.MINUTE, releaseMinuteInt)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val computedLandingTs = remember(landingDateMillis, landingHourInt, landingMinuteInt) {
        Calendar.getInstance().apply {
            timeInMillis = landingDateMillis
            set(Calendar.HOUR_OF_DAY, landingHourInt)
            set(Calendar.MINUTE, landingMinuteInt)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val directDurationMillis = remember(durationHoursInput, durationMinutesInput) {
        val hrs = durationHoursInput.toLongOrNull() ?: 0L
        val mins = durationMinutesInput.toLongOrNull() ?: 0L
        ((hrs * 60) + mins) * 60 * 1000L
    }

    val liveCalculatedMillis = remember(isDirectDurationMode, status, computedReleaseTs, computedLandingTs, directDurationMillis) {
        if (isDirectDurationMode) {
            directDurationMillis
        } else if (status == "LANDED") {
            (computedLandingTs - computedReleaseTs).coerceAtLeast(0L)
        } else {
            0L
        }
    }

    val formattedDurationString = remember(liveCalculatedMillis) {
        val totalSecs = liveCalculatedMillis / 1000L
        val hrs = totalSecs / 3600
        val mins = (totalSecs % 3600) / 60
        "${hrs} Hours ${mins} Minutes"
    }

    val isTimestampValid = isDirectDurationMode || status != "LANDED" || (computedLandingTs > computedReleaseTs)
    val isPigeonValid = selectedPigeonId.isNotBlank()
    val isEventValid = !isEventFlight || selectedSessionId != null
    val isDurationValid = !isDirectDurationMode || directDurationMillis > 0L
    val isValid = isPigeonValid && isTimestampValid && isEventValid && isDurationValid

    if (showReleaseTimePicker) {
        val tpState = rememberTimePickerState(initialHour = releaseHourInt, initialMinute = releaseMinuteInt, is24Hour = false)
        AlertDialog(
            onDismissRequest = { showReleaseTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    releaseHourInt = tpState.hour
                    releaseMinuteInt = tpState.minute
                    showReleaseTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showReleaseTimePicker = false }) { Text("Cancel") }
            },
            text = { TimePicker(state = tpState) }
        )
    }

    if (showLandingTimePicker) {
        val tpState = rememberTimePickerState(initialHour = landingHourInt, initialMinute = landingMinuteInt, is24Hour = false)
        AlertDialog(
            onDismissRequest = { showLandingTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    landingHourInt = tpState.hour
                    landingMinuteInt = tpState.minute
                    showLandingTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showLandingTimePicker = false }) { Text("Cancel") }
            },
            text = { TimePicker(state = tpState) }
        )
    }

    if (showReleaseDatePicker) {
        val dpState = rememberDatePickerState(initialSelectedDateMillis = releaseDateMillis)
        DatePickerDialog(
            onDismissRequest = { showReleaseDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dpState.selectedDateMillis?.let { releaseDateMillis = it }
                    showReleaseDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showReleaseDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = dpState)
        }
    }

    if (showLandingDatePicker) {
        val dpState = rememberDatePickerState(initialSelectedDateMillis = landingDateMillis)
        DatePickerDialog(
            onDismissRequest = { showLandingDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dpState.selectedDateMillis?.let { landingDateMillis = it }
                    showLandingDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showLandingDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = dpState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.EditCalendar,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (existingRecordId == null) "ADD MANUAL FLIGHT" else "EDIT MANUAL FLIGHT",
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "Record completed, historical or unrecorded flight data",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // 1. Select Pigeon
                item {
                    Column {
                        Text("1. SELECT PIGEON *", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        ExposedDropdownMenuBox(
                            expanded = pigeonDropdownExpanded,
                            onExpandedChange = { pigeonDropdownExpanded = !pigeonDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedPigeon?.let { "${it.name} (${it.ringNumber}) - ${it.breed}" } ?: "Search & Select Pigeon (Required)",
                                onValueChange = {},
                                readOnly = true,
                                isError = attemptedSave && !isPigeonValid,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = pigeonDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = pigeonDropdownExpanded,
                                onDismissRequest = { pigeonDropdownExpanded = false },
                                modifier = Modifier.heightIn(max = 250.dp)
                            ) {
                                OutlinedTextField(
                                    value = pigeonSearchQuery,
                                    onValueChange = { pigeonSearchQuery = it },
                                    placeholder = { Text("Search by name, ring, breed...") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    singleLine = true
                                )
                                if (filteredPigeons.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("No pigeons found") },
                                        onClick = { },
                                        enabled = false
                                    )
                                } else {
                                    filteredPigeons.forEach { pigeon ->
                                        DropdownMenuItem(
                                            text = { Text("${pigeon.name} (${pigeon.ringNumber}) - ${pigeon.breed}") },
                                            onClick = {
                                                selectedPigeonId = pigeon.pigeonId
                                                pigeonDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        if (attemptedSave && !isPigeonValid) {
                            Text("⚠️ Please select a participant pigeon.", color = MaterialTheme.colorScheme.error, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 2. Flight Category Selection
                item {
                    Column {
                        Text("2. FLIGHT CATEGORY", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            FilterChip(
                                selected = !isEventFlight,
                                onClick = {
                                    isEventFlight = false
                                    selectedSessionId = null
                                },
                                label = { Text("Personal Flight", fontWeight = FontWeight.Bold) },
                                leadingIcon = if (!isEventFlight) { { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp)) } } else null,
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = isEventFlight,
                                onClick = {
                                    isEventFlight = true
                                },
                                label = { Text("Event Flight", fontWeight = FontWeight.Bold) },
                                leadingIcon = if (isEventFlight) { { Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(16.dp)) } } else null,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 3. Event Selector (Only if Event Flight)
                if (isEventFlight) {
                    item {
                        Column {
                            Text("SELECT EVENT *", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            ExposedDropdownMenuBox(
                                expanded = eventDropdownExpanded,
                                onExpandedChange = { eventDropdownExpanded = !eventDropdownExpanded }
                            ) {
                                OutlinedTextField(
                                    value = selectedEvent?.name ?: "Select Flight Event",
                                    onValueChange = {},
                                    readOnly = true,
                                    isError = attemptedSave && !isEventValid,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = eventDropdownExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = eventDropdownExpanded,
                                    onDismissRequest = { eventDropdownExpanded = false }
                                ) {
                                    if (events.isEmpty()) {
                                        DropdownMenuItem(
                                            text = { Text("No events created yet") },
                                            onClick = { },
                                            enabled = false
                                        )
                                    } else {
                                        events.forEach { evt ->
                                            DropdownMenuItem(
                                                text = { Text("${evt.name} (${evt.eventType})") },
                                                onClick = {
                                                    selectedSessionId = evt.sessionId
                                                    eventDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            if (attemptedSave && !isEventValid) {
                                Text("⚠️ Please select an event or switch to Personal Flight.", color = MaterialTheme.colorScheme.error, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Mode Switch: Date & Time Mode vs Direct Duration
                item {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(6.dp)
                        ) {
                            FilterChip(
                                selected = !isDirectDurationMode,
                                onClick = { isDirectDurationMode = false },
                                label = { Text("Date & Time Mode", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            FilterChip(
                                selected = isDirectDurationMode,
                                onClick = { isDirectDurationMode = true },
                                label = { Text("Duration Mode", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                if (!isDirectDurationMode) {
                    // Release Date & Time
                    item {
                        Column {
                            Text("3. RELEASE DATE & TIME", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showReleaseDatePicker = true },
                                    modifier = Modifier.weight(1.2f)
                                ) {
                                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(dateFormat.format(Date(releaseDateMillis)), fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = { showReleaseTimePicker = true },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    val relCalFormatted = Calendar.getInstance().apply {
                                        set(Calendar.HOUR_OF_DAY, releaseHourInt)
                                        set(Calendar.MINUTE, releaseMinuteInt)
                                    }
                                    Text(
                                        timeFormat.format(relCalFormatted.time),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    // Landing Date & Time
                    if (status == "LANDED") {
                        item {
                            Column {
                                Text("4. LANDING DATE & TIME", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { showLandingDatePicker = true },
                                        modifier = Modifier.weight(1.2f)
                                    ) {
                                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(dateFormat.format(Date(landingDateMillis)), fontSize = 12.sp)
                                    }
                                    OutlinedButton(
                                        onClick = { showLandingTimePicker = true },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        val landCalFormatted = Calendar.getInstance().apply {
                                            set(Calendar.HOUR_OF_DAY, landingHourInt)
                                            set(Calendar.MINUTE, landingMinuteInt)
                                        }
                                        Text(
                                            timeFormat.format(landCalFormatted.time),
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                                if (computedLandingTs <= computedReleaseTs) {
                                    Text(
                                        text = "⚠️ Landing time must be strictly after release time!",
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Direct Duration Inputs
                    item {
                        Column {
                            Text("3. ENTER FLIGHT DURATION", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                            ) {
                                OutlinedTextField(
                                    value = durationHoursInput,
                                    onValueChange = { durationHoursInput = it },
                                    label = { Text("Hours") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = durationMinutesInput,
                                    onValueChange = { durationMinutesInput = it },
                                    label = { Text("Minutes") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Live Calculated Duration Banner
                item {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "CALCULATED FLIGHT DURATION",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = formattedDurationString,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }

                // Status Selection
                item {
                    Column {
                        Text("FLIGHT STATUS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        ) {
                            statusList.take(3).forEach { st ->
                                FilterChip(
                                    selected = status == st,
                                    onClick = { status = st },
                                    label = { Text(st, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }

                // Notes
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Flight Notes / Remarks") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    attemptedSave = true
                    if (isValid) {
                        val relTs = computedReleaseTs
                        if (!isDirectDurationMode) {
                            val landTs = if (status == "LANDED") computedLandingTs else null
                            val durMillis = if (landTs != null) (landTs - relTs) else null
                            onConfirm(
                                selectedPigeonId,
                                if (isEventFlight) selectedSessionId else null,
                                relTs,
                                landTs,
                                durMillis,
                                status,
                                notes
                            )
                        } else {
                            val landTs = relTs + directDurationMillis
                            onConfirm(
                                selectedPigeonId,
                                if (isEventFlight) selectedSessionId else null,
                                relTs,
                                landTs,
                                directDurationMillis,
                                status,
                                notes
                            )
                        }
                        android.widget.Toast.makeText(context, "Manual flight saved successfully!", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        android.widget.Toast.makeText(context, "Please complete all required fields correctly.", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("SAVE MANUAL FLIGHT", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }
        }
    )
}
