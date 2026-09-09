package com.highflyerpro.tracker.presentation.screens.nutrition

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
import com.highflyerpro.tracker.data.local.entities.FoodEntity
import com.highflyerpro.tracker.data.local.entities.FoodInventoryEntity
import com.highflyerpro.tracker.presentation.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val loftRepo = viewModel.loftMasterRepository

    val foods by loftRepo.allFoodsFlow.collectAsState(initial = emptyList())
    val inventory by loftRepo.inventoryFlow.collectAsState(initial = emptyList())

    var selectedTab by remember { mutableStateOf(0) }
    var showAddFoodDialog by remember { mutableStateOf(false) }
    var showAddInventoryDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nutrition & Grain Inventory", fontWeight = FontWeight.Bold) },
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
                    if (selectedTab == 0) showAddFoodDialog = true else showAddInventoryDialog = true
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
                    text = { Text("Feed Recipes (${foods.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Grain Stock (${inventory.size})") }
                )
            }

            when (selectedTab) {
                0 -> {
                    if (foods.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No grain mixtures logged.\nTap + to create a feed mix formulation.")
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(foods) { food ->
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
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(food.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                            Text("Purpose: ${food.purpose}", style = MaterialTheme.typography.bodyMedium)
                                            Text("Protein: ${food.proteinPercentage ?: 15.0}% | Energy: ${food.energyKcal ?: 3000.0} kcal/kg")
                                            if (food.ingredients.isNotBlank()) {
                                                Text("Grains: ${food.ingredients}", style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                        IconButton(onClick = {
                                            coroutineScope.launch { loftRepo.deleteFood(food.foodId) }
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
                    if (inventory.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No inventory stock recorded.\nTap + to add grain stock (e.g. Maize, Peas, Millet).")
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(inventory) { item ->
                                val isLow = item.currentQuantity <= item.minimumStock

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isLow) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(item.foodName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                            Text("Stock Remaining: ${item.currentQuantity} ${item.unit}", fontWeight = FontWeight.SemiBold)
                                            if (isLow) {
                                                Text("⚠️ LOW STOCK ALERT (Min: ${item.minimumStock}${item.unit})", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        IconButton(onClick = {
                                            coroutineScope.launch { loftRepo.deleteInventory(item.inventoryId) }
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

    if (showAddFoodDialog) {
        AddFoodDialog(
            onDismiss = { showAddFoodDialog = false },
            onConfirm = { food ->
                coroutineScope.launch {
                    loftRepo.addFood(food)
                    showAddFoodDialog = false
                }
            }
        )
    }

    if (showAddInventoryDialog) {
        AddInventoryDialog(
            onDismiss = { showAddInventoryDialog = false },
            onConfirm = { inv ->
                coroutineScope.launch {
                    loftRepo.addInventory(inv)
                    showAddInventoryDialog = false
                }
            }
        )
    }
}

@Composable
fun AddFoodDialog(
    onDismiss: () -> Unit,
    onConfirm: (FoodEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf("High Flyer Racing Mix") }
    var protein by remember { mutableStateOf("16.5") }
    var grains by remember { mutableStateOf("Maize, Peas, Wheat, Safflower, Hemp") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Feed Recipe") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Recipe Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = purpose,
                    onValueChange = { purpose = it },
                    label = { Text("Purpose (e.g. Flying, Breeding, Rest)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = protein,
                    onValueChange = { protein = it },
                    label = { Text("Protein %") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = grains,
                    onValueChange = { grains = it },
                    label = { Text("Grain Ingredients") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank(),
                onClick = {
                    val food = FoodEntity(
                        foodId = "FOOD-${UUID.randomUUID().toString().take(6)}",
                        name = name,
                        purpose = purpose,
                        proteinPercentage = protein.toDoubleOrNull() ?: 15.0,
                        ingredients = grains
                    )
                    onConfirm(food)
                }
            ) {
                Text("Save Recipe")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AddInventoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (FoodInventoryEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("50.0") }
    var threshold by remember { mutableStateOf("10.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Grain Stock") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Grain Name (e.g. Yellow Corn / Maize)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = qty,
                    onValueChange = { qty = it },
                    label = { Text("Current Stock (kg)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = threshold,
                    onValueChange = { threshold = it },
                    label = { Text("Low Stock Alert Threshold (kg)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank(),
                onClick = {
                    val inv = FoodInventoryEntity(
                        inventoryId = "INV-${UUID.randomUUID().toString().take(6)}",
                        foodName = name,
                        currentQuantity = qty.toDoubleOrNull() ?: 50.0,
                        minimumStock = threshold.toDoubleOrNull() ?: 10.0
                    )
                    onConfirm(inv)
                }
            ) {
                Text("Add Stock")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
