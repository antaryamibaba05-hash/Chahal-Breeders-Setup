package com.highflyerpro.tracker.presentation.screens.pigeons

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.highflyerpro.tracker.data.local.entities.PigeonEntity
import com.highflyerpro.tracker.presentation.components.PhotoSourceSelectionDialog
import com.highflyerpro.tracker.presentation.components.PigeonAvatar
import com.highflyerpro.tracker.presentation.viewmodel.MainViewModel
import com.highflyerpro.tracker.util.PhotoStorageManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditPigeonScreen(
    viewModel: MainViewModel,
    pigeonIdToEdit: String?,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val allPigeons by viewModel.allPigeons.collectAsState()
    val allGroups by viewModel.allGroups.collectAsState()

    var permanentId by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var ringNumber by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("White Cheq") }
    var gender by remember { mutableStateOf("Male") }
    var breed by remember { mutableStateOf("Teddy") }
    var status by remember { mutableStateOf("Active") }
    var fatherId by remember { mutableStateOf<String?>(null) }
    var motherId by remember { mutableStateOf<String?>(null) }
    var notes by remember { mutableStateOf("") }
    var selectedGroupIds by remember { mutableStateOf<Set<String>>(setOf("group-high-flyers")) }

    var profilePhotoPath by remember { mutableStateOf("") }
    var showPhotoSourceDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var isProcessingPhoto by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            isProcessingPhoto = true
            coroutineScope.launch {
                val result = viewModel.saveAndAddPigeonPhoto(
                    pigeonId = permanentId.ifBlank { "pigeon-temp" },
                    sourceUri = uri,
                    category = "Profile",
                    caption = "Profile Photo",
                    isProfile = true
                )
                result.onSuccess { photo ->
                    profilePhotoPath = photo.filePath
                }
                isProcessingPhoto = false
            }
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            val uri = tempCameraUri
            if (uri != null) {
                isProcessingPhoto = true
                coroutineScope.launch {
                    val result = viewModel.saveAndAddPigeonPhoto(
                        pigeonId = permanentId.ifBlank { "pigeon-temp" },
                        sourceUri = uri,
                        category = "Profile",
                        caption = "Profile Photo",
                        isProfile = true
                    )
                    result.onSuccess { photo ->
                        profilePhotoPath = photo.filePath
                    }
                    isProcessingPhoto = false
                }
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val tempUri = PhotoStorageManager.createTempCameraUri(context, permanentId.ifBlank { "pigeon-temp" })
            tempCameraUri = tempUri
            takePictureLauncher.launch(tempUri)
        }
    }

    var ringDuplicateWarning by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    val isEditing = !pigeonIdToEdit.isNullOrBlank()

    // Load pigeon if editing, or generate ID if new
    LaunchedEffect(pigeonIdToEdit) {
        if (isEditing) {
            val p = viewModel.repository.getPigeonById(pigeonIdToEdit!!)
            if (p != null) {
                permanentId = p.pigeonId
                name = p.name
                nickname = p.nickname
                ringNumber = p.ringNumber
                color = p.color
                gender = p.gender
                breed = p.breed
                status = p.status
                profilePhotoPath = p.photoUri
                fatherId = p.fatherId
                motherId = p.motherId
                notes = p.notes
            }
        } else {
            permanentId = viewModel.repository.generatePermanentId()
        }
    }

    // Check ring duplicate
    LaunchedEffect(ringNumber) {
        if (ringNumber.isNotBlank()) {
            ringDuplicateWarning = viewModel.repository.isRingNumberDuplicate(ringNumber, pigeonIdToEdit)
        } else {
            ringDuplicateWarning = false
        }
    }

    val breedSuggestions = listOf("Teddy", "Kamagar", "Kasuri", "Sialkoti", "Rampur", "Ferozpuri", "Golden", "Chapp", "Tipler")
    val genderOptions = listOf("Male", "Female", "Unknown")
    val statusOptions = listOf("Active", "Training", "Resting", "Recovering", "Injured", "Retired", "Sold")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Edit Pigeon Profile" else "Register New Pigeon",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Permanent Digital Lifetime ID Badge
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "PERMANENT DIGITAL LIFETIME ID",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = permanentId.ifBlank { "Generating..." },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        text = "Immutable",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // Profile Photo Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(14.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        PigeonAvatar(
                            photoUri = profilePhotoPath,
                            name = name.ifBlank { "Pigeon" },
                            gender = gender,
                            size = 68
                        )
                        if (isProcessingPhoto) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.5.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "PROFILE PHOTO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (profilePhotoPath.isNotBlank()) "Photo attached" else "No custom photo attached",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { showPhotoSourceDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("add_profile_photo_button")
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (profilePhotoPath.isNotBlank()) "Change" else "Add Photo",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (profilePhotoPath.isNotBlank()) {
                                OutlinedButton(
                                    onClick = { profilePhotoPath = "" },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("remove_profile_photo_button")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Remove", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Name & Nickname
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = it.isBlank()
                },
                label = { Text("Pigeon Name *") },
                isError = nameError,
                supportingText = if (nameError) { { Text("Name is required") } } else null,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pigeon_name_input")
            )

            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text("Nickname / Title (e.g. Champion, Raja Ji)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Ring Number
            OutlinedTextField(
                value = ringNumber,
                onValueChange = { ringNumber = it },
                label = { Text("Ring / Band Number (e.g. PAK-2026-1042)") },
                trailingIcon = {
                    if (ringDuplicateWarning) {
                        Icon(Icons.Default.Warning, contentDescription = "Duplicate", tint = Color(0xFFF59E0B))
                    }
                },
                supportingText = if (ringDuplicateWarning) {
                    { Text("Warning: Ring number already exists in your loft directory", color = Color(0xFFF59E0B)) }
                } else null,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pigeon_ring_input")
            )

            // Gender Selector
            Text("Gender", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                genderOptions.forEach { g ->
                    FilterChip(
                        selected = gender == g,
                        onClick = { gender = g },
                        label = { Text(g) }
                    )
                }
            }

            // Breed Selector & Quick Tags
            Text("Breed Bloodline", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            OutlinedTextField(
                value = breed,
                onValueChange = { breed = it },
                label = { Text("Breed Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                breedSuggestions.forEach { b ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (breed == b) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { breed = b }
                    ) {
                        Text(
                            text = b,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (breed == b) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Color
            OutlinedTextField(
                value = color,
                onValueChange = { color = it },
                label = { Text("Color / Feather Pattern (e.g. White Cheq, Grizzle)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Status Selector
            Text("Status", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                statusOptions.forEach { s ->
                    FilterChip(
                        selected = status == s,
                        onClick = { status = s },
                        label = { Text(s) }
                    )
                }
            }

            // Groups Assignment
            Text("Loft Groups", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                allGroups.forEach { grp ->
                    val isSelected = selectedGroupIds.contains(grp.groupId)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val next = selectedGroupIds.toMutableSet()
                            if (isSelected) next.remove(grp.groupId) else next.add(grp.groupId)
                            selectedGroupIds = next
                        },
                        label = { Text(grp.name) }
                    )
                }
            }

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes & Pedigree Details") },
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Save Button
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                        return@Button
                    }
                    isSaving = true
                    coroutineScope.launch {
                        val pigeon = PigeonEntity(
                            pigeonId = permanentId,
                            name = name.trim(),
                            nickname = nickname.trim(),
                            ringNumber = ringNumber.trim(),
                            color = color.trim(),
                            gender = gender,
                            breed = breed.trim(),
                            status = status,
                            photoUri = profilePhotoPath,
                            fatherId = fatherId,
                            motherId = motherId,
                            notes = notes.trim()
                        )
                        if (isEditing) {
                            viewModel.repository.updatePigeon(pigeon, selectedGroupIds.toList())
                        } else {
                            viewModel.repository.createPigeon(pigeon, selectedGroupIds.toList())
                        }
                        isSaving = false
                        onNavigateBack()
                    }
                },
                enabled = !isSaving,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_pigeon_button")
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = if (isEditing) "Update Pigeon Profile" else "Save Permanent Profile",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }

    if (showPhotoSourceDialog) {
        PhotoSourceSelectionDialog(
            onDismiss = { showPhotoSourceDialog = false },
            onCameraSelect = {
                showPhotoSourceDialog = false
                val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                    val tempUri = PhotoStorageManager.createTempCameraUri(context, permanentId.ifBlank { "pigeon-temp" })
                    tempCameraUri = tempUri
                    takePictureLauncher.launch(tempUri)
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            onGallerySelect = {
                showPhotoSourceDialog = false
                galleryLauncher.launch(
                    androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            title = "Pigeon Profile Photo"
        )
    }
}
