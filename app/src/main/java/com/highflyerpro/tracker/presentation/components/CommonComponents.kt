package com.highflyerpro.tracker.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.highflyerpro.tracker.R
import com.highflyerpro.tracker.data.local.entities.FlightRecordEntity
import com.highflyerpro.tracker.data.local.entities.FlightSessionEntity
import com.highflyerpro.tracker.domain.calculation.PerformanceEngine
import com.highflyerpro.tracker.ui.theme.GoldTrophy
import com.highflyerpro.tracker.ui.theme.SilverMedal
import com.highflyerpro.tracker.ui.theme.StatusFlying
import com.highflyerpro.tracker.ui.theme.StatusLanded
import com.highflyerpro.tracker.ui.theme.StatusMissing
import com.highflyerpro.tracker.ui.theme.StatusResting
import com.highflyerpro.tracker.ui.theme.StatusTraining

@Composable
fun PigeonStatusChip(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (status.uppercase()) {
        "ACTIVE" -> Pair(Color(0xFFDCFCE7), Color(0xFF15803D))
        "TRAINING" -> Pair(Color(0xFFFEF3C7), Color(0xFFB45309))
        "RESTING" -> Pair(Color(0xFFF1F5F9), Color(0xFF475569))
        "RECOVERING" -> Pair(Color(0xFFEDE9FE), Color(0xFF6D28D9))
        "INJURED" -> Pair(Color(0xFFFFEDD5), Color(0xFFC2410C))
        "MISSING" -> Pair(Color(0xFFFEE2E2), Color(0xFFB91C1C))
        "RETIRED" -> Pair(Color(0xFFE2E8F0), Color(0xFF334155))
        "SOLD" -> Pair(Color(0xFFE0E7FF), Color(0xFF3730A3))
        "DECEASED" -> Pair(Color(0xFFCBD5E1), Color(0xFF1E293B))
        "FLYING" -> Pair(Color(0xFFE0F2FE), Color(0xFF0369A1))
        "LANDED" -> Pair(Color(0xFFD1FAE5), Color(0xFF065F46))
        else -> Pair(Color(0xFFF1F5F9), Color(0xFF475569))
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Text(
            text = status,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun PigeonAvatar(
    photoUri: String,
    name: String,
    gender: String,
    modifier: Modifier = Modifier,
    size: Int = 48
) {
    val isFemale = gender.equals("Female", ignoreCase = true)
    val tintColor = if (isFemale) Color(0xFFDB2777) else Color(0xFF0284C7)
    val bgColor = if (isFemale) Color(0xFFFCE7F3) else Color(0xFFE0F2FE)
    val borderColor = if (isFemale) Color(0xFFF472B6) else Color(0xFF38BDF8)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(bgColor)
            .border(1.5.dp, borderColor, CircleShape)
    ) {
        if (photoUri.isNotBlank()) {
            AsyncImage(
                model = photoUri,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                error = painterResource(R.drawable.ic_pigeon_placeholder),
                modifier = Modifier
                    .size(size.dp)
                    .clip(CircleShape)
            )
        } else {
            Image(
                painter = painterResource(R.drawable.ic_pigeon_placeholder),
                contentDescription = "Pigeon Avatar Placeholder",
                colorFilter = ColorFilter.tint(tintColor),
                modifier = Modifier.size((size * 0.72).dp)
            )
        }
    }
}

@Composable
fun OutdoorStatCard(
    title: String,
    value: String,
    subtitle: String = "",
    icon: ImageVector? = null,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun ActiveFlightBanner(
    session: FlightSessionEntity,
    liveElapsedMillis: Long,
    onViewLiveTracker: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val durationHMS = PerformanceEngine.formatDurationHMS(liveElapsedMillis)

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0284C7) // Bold High Contrast Outdoor Sky Blue
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("active_flight_banner")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF22C55E))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LIVE FLIGHT IN PROGRESS",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = "${session.flyingCount} Flying / ${session.totalParticipants} Total",
                    color = Color(0xFFE0F2FE),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = session.name,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Elapsed: $durationHMS",
                        color = Color(0xFFFDE047), // High visibility amber/yellow outdoor
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Button(
                    onClick = onViewLiveTracker,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF0284C7)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("banner_view_tracker_button")
                ) {
                    Text("Track Live", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun LandingConfirmationDialog(
    record: FlightRecordEntity,
    releaseTimestamp: Long,
    onConfirm: (landingTime: Long, notes: String) -> Unit,
    onDismiss: () -> Unit
) {
    val now = System.currentTimeMillis()
    val calculatedDuration = (now - releaseTimestamp).coerceAtLeast(0L)
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = StatusLanded,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Confirm Landing", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text(
                    text = "Pigeon: ${record.pigeonName}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Ring #: ${record.pigeonRingNumber.ifBlank { "Unbanded" }}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Flight Duration",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = PerformanceEngine.formatDurationHMS(calculatedDuration),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Landing Time: ${PerformanceEngine.formatTimeOnly(now)}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Landing Notes (Optional)") },
                    placeholder = { Text("e.g. touched down on roof, clean trap") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(now, notes) },
                colors = ButtonDefaults.buttonColors(containerColor = StatusLanded),
                modifier = Modifier.testTag("dialog_confirm_landed_button")
            ) {
                Text("Mark Landed", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun LandingCorrectionDialog(
    record: FlightRecordEntity,
    onConfirmCorrection: (newLandingTimestamp: Long?, newStatus: String, notes: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedStatus by remember { mutableStateOf(record.status) }
    var noteText by remember { mutableStateOf(record.notes) }

    val statusOptions = listOf("LANDED", "FLYING", "DID NOT FLY", "MISSING", "INJURED", "DISQUALIFIED")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Correct Flight Record", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text(
                    text = "${record.pigeonName} (${record.pigeonRingNumber})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text("Record Status:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    statusOptions.take(3).forEach { s ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedStatus == s) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedStatus = s }
                        ) {
                            Text(
                                text = s,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedStatus == s) Color.White else MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    statusOptions.drop(3).forEach { s ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedStatus == s) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedStatus = s }
                        ) {
                            Text(
                                text = s,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedStatus == s) Color.White else MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Correction Reason / Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val landingTime = if (selectedStatus == "LANDED") (record.landingTimestamp ?: System.currentTimeMillis()) else null
                    onConfirmCorrection(landingTime, selectedStatus, noteText)
                },
                modifier = Modifier.testTag("dialog_save_correction_button")
            ) {
                Text("Save Correction", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
