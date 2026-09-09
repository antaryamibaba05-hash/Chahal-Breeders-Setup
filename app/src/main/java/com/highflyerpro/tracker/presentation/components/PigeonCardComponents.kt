package com.highflyerpro.tracker.presentation.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlightLand
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.highflyerpro.tracker.data.local.entities.FlightRecordEntity
import com.highflyerpro.tracker.domain.calculation.PerformanceEngine
import com.highflyerpro.tracker.domain.model.PigeonWithStats
import com.highflyerpro.tracker.presentation.viewmodel.DisplayMode
import com.highflyerpro.tracker.ui.theme.GoldTrophy
import com.highflyerpro.tracker.ui.theme.SilverMedal
import com.highflyerpro.tracker.ui.theme.StatusFlying
import com.highflyerpro.tracker.ui.theme.StatusLanded
import com.highflyerpro.tracker.ui.theme.StatusMissing

@Composable
fun PigeonCardItem(
    item: PigeonWithStats,
    displayMode: DisplayMode,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pigeon = item.pigeon

    when (displayMode) {
        DisplayMode.LIST -> {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                modifier = modifier
                    .fillMaxWidth()
                    .clickable { onClick() }
                    .testTag("pigeon_item_${pigeon.pigeonId}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onToggleSelect() },
                        modifier = Modifier.testTag("checkbox_${pigeon.pigeonId}")
                    )

                    PigeonAvatar(
                        photoUri = pigeon.photoUri,
                        name = pigeon.name,
                        gender = pigeon.gender,
                        size = 48
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = pigeon.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (pigeon.nickname.isNotBlank()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(${pigeon.nickname})",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    maxLines = 1
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "${pigeon.ringNumber.ifBlank { "No Ring" }} • ${pigeon.breed} • ${pigeon.color}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PigeonStatusChip(status = pigeon.status)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Flights: ${item.totalFlights}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Best: ${PerformanceEngine.formatDurationHMS(item.bestFlightMillis)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "${item.performanceScore} pts",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.currentForm,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (item.currentForm == "Excellent") Color(0xFF16A34A) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        DisplayMode.COMPACT -> {
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = modifier
                    .fillMaxWidth()
                    .clickable { onClick() }
                    .testTag("pigeon_item_${pigeon.pigeonId}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onToggleSelect() },
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = pigeon.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = pigeon.ringNumber.ifBlank { "--" },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    PigeonStatusChip(status = pigeon.status)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${item.performanceScore}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        DisplayMode.GRID -> {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = modifier
                    .fillMaxWidth()
                    .clickable { onClick() }
                    .testTag("pigeon_item_${pigeon.pigeonId}")
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { onToggleSelect() },
                            modifier = Modifier.align(Alignment.TopStart)
                        )
                        PigeonAvatar(
                            photoUri = pigeon.photoUri,
                            name = pigeon.name,
                            gender = pigeon.gender,
                            size = 54,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = pigeon.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = pigeon.ringNumber.ifBlank { "Unbanded" },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    PigeonStatusChip(status = pigeon.status)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Score: ${item.performanceScore}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun LiveParticipantTrackerCard(
    record: FlightRecordEntity,
    liveElapsedMillis: Long,
    onMarkLanded: () -> Unit,
    onEditRecord: () -> Unit,
    modifier: Modifier = Modifier,
    photoUri: String = "",
    gender: String = "Male"
) {
    val isLanded = record.status == "LANDED"
    val durationText = if (isLanded) {
        PerformanceEngine.formatDurationHMS(record.durationMillis ?: 0L)
    } else {
        PerformanceEngine.formatDurationHMS(liveElapsedMillis)
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLanded) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isLanded) 1.dp else 3.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("live_participant_${record.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
        ) {
            // Profile photo avatar with flight status badge overlay
            Box(contentAlignment = Alignment.BottomEnd) {
                PigeonAvatar(
                    photoUri = photoUri,
                    name = record.pigeonName,
                    gender = gender,
                    size = 48
                )

                if (isLanded) {
                    Surface(
                        shape = CircleShape,
                        color = when (record.landingPosition) {
                            1 -> GoldTrophy
                            2 -> SilverMedal
                            3 -> Color(0xFFB45309)
                            else -> MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${record.landingPosition ?: "-"}",
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = CircleShape,
                        color = StatusFlying,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Flying",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.pigeonName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Ring: ${record.pigeonRingNumber.ifBlank { "Unbanded" }}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isLanded) "Landed: ${PerformanceEngine.formatTimeOnly(record.landingTimestamp ?: 0L)}" else "Status: Flying at altitude",
                    fontSize = 11.sp,
                    color = if (isLanded) StatusLanded else StatusFlying,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = durationText,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = if (isLanded) StatusLanded else MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isLanded) {
                        Button(
                            onClick = onMarkLanded,
                            colors = ButtonDefaults.buttonColors(containerColor = StatusLanded),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("mark_landed_button_${record.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlightLand,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("LANDED", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    } else {
                        PigeonStatusChip(status = record.status)
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onEditRecord,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Correct",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
