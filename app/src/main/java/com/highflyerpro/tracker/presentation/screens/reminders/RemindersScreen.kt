package com.highflyerpro.tracker.presentation.screens.reminders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.highflyerpro.tracker.data.local.entities.ReminderEntity
import com.highflyerpro.tracker.data.local.entities.TagEntity
import com.highflyerpro.tracker.presentation.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val loftRepo = viewModel.loftMasterRepository

    val reminders by loftRepo.allRemindersFlow.collectAsState(initial = emptyList())
    val tags by loftRepo.allTagsFlow.collectAsState(initial = emptyList())

    var selectedTab by remember { mutableStateOf(0) }
    var showAddReminderDialog by remember { mutableStateOf(false) }
    var showAddTagDialog by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Alerts & Custom Tags", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 0) showAddReminderDialog = true else showAddTagDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Active Reminders (${reminders.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Custom Tags (${tags.size})") }
                )
            }

            when (selectedTab) {
                0 -> {
                    if (reminders.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No pending loft tasks or reminders.\nTap + to set a task alert.")
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(reminders) { rem ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (rem.isCompleted) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = rem.isCompleted,
                                            onCheckedChange = { checked ->
                                                coroutineScope.launch {
                                                    loftRepo.setReminderCompleted(rem.reminderId, checked)
                                                }
                                            }
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(rem.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                            Text("Category: ${rem.category}", style = MaterialTheme.typography.bodyMedium)
                                            Text("Due Date: ${dateFormat.format(Date(rem.dueDate))}", fontWeight = FontWeight.SemiBold)
                                            if (rem.description.isNotBlank()) {
                                                Text(rem.description, style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                        IconButton(onClick = {
                                            coroutineScope.launch { loftRepo.deleteReminder(rem.reminderId) }
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    if (tags.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No tags created yet.\nTap + to create custom tags (e.g. #Champion, #Derby2026).")
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(tags) { tag ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Tag, contentDescription = "Tag", tint = MaterialTheme.colorScheme.primary)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(tag.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        }
                                        IconButton(onClick = {
                                            coroutineScope.launch { loftRepo.deleteTag(tag.name) }
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
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

    if (showAddReminderDialog) {
        AddReminderDialog(
            onDismiss = { showAddReminderDialog = false },
            onConfirm = { reminder ->
                coroutineScope.launch {
                    loftRepo.createReminder(reminder)
                    showAddReminderDialog = false
                }
            }
        )
    }

    if (showAddTagDialog) {
        AddTagDialog(
            onDismiss = { showAddTagDialog = false },
            onConfirm = { name, color ->
                coroutineScope.launch {
                    loftRepo.createTag(name, color)
                    showAddTagDialog = false
                }
            }
        )
    }
}

@Composable
fun AddReminderDialog(
    onDismiss: () -> Unit,
    onConfirm: (ReminderEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Loft Maintenance") }
    var dueDays by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Task Reminder") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (e.g. Vaccination, Cleaning)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dueDays,
                    onValueChange = { dueDays = it },
                    label = { Text("Due in Days from Today") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Details / Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                enabled = title.isNotBlank(),
                onClick = {
                    val days = dueDays.toLongOrNull() ?: 1L
                    val dueTime = System.currentTimeMillis() + (days * 24 * 3600 * 1000L)
                    val reminder = ReminderEntity(
                        reminderId = "REM-${UUID.randomUUID().toString().take(6)}",
                        title = title,
                        description = desc,
                        category = category,
                        dueDate = dueTime
                    )
                    onConfirm(reminder)
                }
            ) {
                Text("Schedule Alert")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AddTagDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, colorHex: String) -> Unit
) {
    var tagName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Custom Tag") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = tagName,
                    onValueChange = { tagName = it },
                    label = { Text("Tag Name (e.g., #TopBreeder)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                enabled = tagName.isNotBlank(),
                onClick = {
                    onConfirm(tagName.trim(), "#0284C7")
                }
            ) {
                Text("Create Tag")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
