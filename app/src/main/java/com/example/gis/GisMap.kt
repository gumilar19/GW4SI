package com.example.gis

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Project
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class GisWell(
    val name: String,
    val type: String, // Domestic, Industrial, Hotel, Ag, Monitoring, Recharge
    val x: Float, // Relative 0 to 1
    val y: Float,
    val capacity: Double, // m3/day
    val isMonitoring: Boolean = false
)

@Composable
fun GisMapScreen(
    project: Project,
    modifier: Modifier = Modifier
) {
    var basemapSatellite by remember { mutableStateOf(false) }
    var showWells by remember { mutableStateOf(true) }
    var showRechargeZones by remember { mutableStateOf(true) }
    var showRiskZones by remember { mutableStateOf(false) }
    var selectedWell by remember { mutableStateOf<GisWell?>(null) }

    // Seed 8 wells dynamically distributed over the island
    val wells = remember(project) {
        listOf(
            GisWell("DW-01 (Domestic)", "Domestic", 0.45f, 0.45f, 150.0),
            GisWell("DW-02 (Domestic)", "Domestic", 0.55f, 0.52f, 120.0),
            GisWell("HW-01 (Resort Well)", "Hotel", 0.32f, 0.65f, 180.0),
            GisWell("HW-02 (Resort Well)", "Hotel", 0.68f, 0.35f, 160.0),
            GisWell("IW-01 (Industrial)", "Industrial", 0.58f, 0.40f, 40.0),
            GisWell("AW-01 (Agriculture)", "Agricultural", 0.40f, 0.32f, 61.0),
            GisWell("MW-01 (Coastal Sentry)", "Monitoring", 0.22f, 0.50f, 0.0, isMonitoring = true),
            GisWell("MW-02 (Inland Sentry)", "Monitoring", 0.50f, 0.20f, 0.0, isMonitoring = true),
            GisWell("RW-01 (Artificial Recharge)", "Recharge", 0.50f, 0.50f, 49.3),
            GisWell("IP-01 (Infiltration Pond)", "Recharge", 0.46f, 0.58f, 25.0)
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Interactive GIS Map - ${project.name}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row {
                        IconButton(onClick = { basemapSatellite = !basemapSatellite }) {
                            Icon(
                                imageVector = if (basemapSatellite) Icons.Default.Satellite else Icons.Default.Map,
                                contentDescription = "Toggle Basemap",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                Text(
                    text = "A spatial representation of aquifer grids, recharge/risk zones, and pumping installations.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // GIS Layers Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = showWells,
                onClick = { showWells = !showWells },
                label = { Text("Wells", fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Default.Place, null, modifier = Modifier.size(14.dp)) }
            )
            FilterChip(
                selected = showRechargeZones,
                onClick = { 
                    showRechargeZones = true
                    showRiskZones = false
                },
                label = { Text("Recharge Zones", fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Default.WaterDrop, null, modifier = Modifier.size(14.dp)) }
            )
            FilterChip(
                selected = showRiskZones,
                onClick = { 
                    showRiskZones = true
                    showRechargeZones = false
                },
                label = { Text("Salinity Risk", fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Default.Warning, null, modifier = Modifier.size(14.dp)) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Map Canvas Box
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                .background(
                    if (basemapSatellite) Color(0xFF152238) else Color(0xFFE0F7FA),
                    RoundedCornerShape(16.dp)
                )
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(wells) {
                        detectTapGestures { offset ->
                            val width = size.width
                            val height = size.height
                            
                            var foundWell: GisWell? = null
                            for (well in wells) {
                                val wx = well.x * width
                                val wy = well.y * height
                                val dist = sqrt((offset.x - wx) * (offset.x - wx) + (offset.y - wy) * (offset.y - wy))
                                if (dist < 32f) { // Tap radius
                                    foundWell = well
                                    break
                                }
                            }
                            selectedWell = foundWell
                        }
                    }
            ) {
                val w = size.width
                val h = size.height
                val center = Offset(w / 2f, h / 2f)
                val rx = w / 2.5f
                val ry = h / 3f

                // Draw Ocean grid lines if topo basemap
                if (!basemapSatellite) {
                    val step = 40f
                    for (x in 0..(w.toInt()) step step.toInt()) {
                        drawLine(Color(0xFFB2EBF2), Offset(x.toFloat(), 0f), Offset(x.toFloat(), h), 1f)
                    }
                    for (y in 0..(h.toInt()) step step.toInt()) {
                        drawLine(Color(0xFFB2EBF2), Offset(0f, y.toFloat()), Offset(w, y.toFloat()), 1f)
                    }
                }

                // Draw the custom vector Island Shape
                val islandBrush = if (basemapSatellite) {
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF2E7D32), Color(0xFF1B5E20), Color(0xFFC2B280)),
                        center = center,
                        radius = rx
                    )
                } else {
                    Brush.radialGradient(
                        colors = listOf(Color(0xFFA5D6A7), Color(0xFF81C784), Color(0xFFE0C097)),
                        center = center,
                        radius = rx
                    )
                }

                drawOval(
                    brush = islandBrush,
                    topLeft = Offset(center.x - rx, center.y - ry),
                    size = Size(rx * 2f, ry * 2f)
                )

                // Draw Recharge Zones (concentric rings inside the island)
                if (showRechargeZones) {
                    drawOval(
                        color = Color(0xFF1976D2).copy(alpha = 0.25f),
                        topLeft = Offset(center.x - rx * 0.7f, center.y - ry * 0.7f),
                        size = Size(rx * 1.4f, ry * 1.4f)
                    )
                    drawOval(
                        color = Color(0xFF1976D2).copy(alpha = 0.15f),
                        topLeft = Offset(center.x - rx * 0.85f, center.y - ry * 0.85f),
                        size = Size(rx * 1.7f, ry * 1.7f)
                    )
                }

                // Draw Coastal Saltwater Risk Zones (red outer coast rim)
                if (showRiskZones) {
                    // High risk at ocean boundaries
                    drawOval(
                        color = Color(0xFFD84315).copy(alpha = 0.35f),
                        topLeft = Offset(center.x - rx * 1.05f, center.y - ry * 1.05f),
                        size = Size(rx * 2.1f, ry * 2.1f),
                        style = Stroke(width = 40f)
                    )
                    // Moderate risk ring slightly inside coast
                    drawOval(
                        color = Color(0xFFFFB300).copy(alpha = 0.25f),
                        topLeft = Offset(center.x - rx * 0.9f, center.y - ry * 0.9f),
                        size = Size(rx * 1.8f, ry * 1.8f),
                        style = Stroke(width = 20f)
                    )
                }

                // Draw wells
                if (showWells) {
                    for (well in wells) {
                        val wx = well.x * w
                        val wy = well.y * h

                        val color = when {
                            well.isMonitoring -> Color(0xFF00ACC1)
                            well.type == "Recharge" -> Color(0xFF4CAF50)
                            well.type == "Hotel" -> Color(0xFFEF6C00)
                            else -> Color(0xFF1565C0)
                        }

                        // Draw outer circle indicator
                        drawCircle(
                            color = Color.White,
                            radius = 12f,
                            center = Offset(wx, wy)
                        )
                        drawCircle(
                            color = color,
                            radius = 8f,
                            center = Offset(wx, wy)
                        )

                        // If selected, draw ring
                        if (selectedWell == well) {
                            drawCircle(
                                color = Color.White,
                                radius = 18f,
                                center = Offset(wx, wy),
                                style = Stroke(width = 3f)
                            )
                        }
                    }
                }
            }

            // Legend Overlay
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Text("Map Legend", fontSize = 10.sp, style = MaterialTheme.typography.labelMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF1565C0), CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pumping Wells", fontSize = 8.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFFEF6C00), CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hotel Wells", fontSize = 8.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF00ACC1), CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Monitoring Wells", fontSize = 8.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF4CAF50), CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Recharge Structures", fontSize = 8.sp)
                }
            }

            // Top Status Alert
            Card(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f))
            ) {
                Text(
                    text = "Map Mode: ${if (basemapSatellite) "Satellite" else "Offline Topo"}",
                    fontSize = 9.sp,
                    modifier = Modifier.padding(6.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Selected Well details panel
        selectedWell?.let { well ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = well.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Pumping/Recharge Capacity: ${well.capacity} m³/day",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    IconButton(onClick = { selectedWell = null }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear Selection",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        } ?: Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Tip: Tap on any well icon on the map above to view hydrogeological sensor data.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
