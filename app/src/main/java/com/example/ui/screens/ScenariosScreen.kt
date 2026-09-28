package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculation.HydroCalculationEngine
import com.example.model.Project
import com.example.model.Scenario
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun ScenariosScreen(
    viewModel: MainViewModel,
    project: Project,
    scenarios: List<Scenario>,
    activeScenario: Scenario?,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val isIndonesian = language == "Indonesian"

    var showCreateDialog by remember { mutableStateOf(false) }

    // Baseline Results
    val baselineResults = remember(project) { HydroCalculationEngine.calculate(project) }
    
    // Scenario Results if one is active
    val scenarioResults = remember(project, activeScenario) {
        activeScenario?.let { HydroCalculationEngine.calculate(project, it) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Explanatory Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isIndonesian) "Analisis Skenario Manajemen" else "Management Scenario Analysis",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isIndonesian) {
                            "Bandingkan dampak perubahan iklim (curah hujan turun), pertumbuhan populasi, atau perluasan imbuhan buatan terhadap keberlanjutan air secara langsung."
                        } else {
                            "Simulate the immediate impacts of climate changes (reduced rainfall), population spikes, tourism surges, or artificial recharge expansions against baseline parameters."
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Active Scenario Selector
        item {
            Text(
                text = if (isIndonesian) "Pilih Skenario Aktif" else "Select Simulation Scenario",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (scenarios.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "No scenarios created yet. Tap '+' to create one.",
                        fontSize = 12.sp,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Scenario dropdown or scroll chips
                    Column(modifier = Modifier.weight(1f)) {
                        scenarios.forEach { sc ->
                            val isSelected = activeScenario?.id == sc.id
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        if (isSelected) viewModel.selectScenario(null)
                                        else viewModel.selectScenario(sc)
                                    },
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = sc.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = sc.description,
                                            fontSize = 10.sp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Row {
                                        if (isSelected) {
                                            Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                                        }
                                        IconButton(onClick = { viewModel.deleteScenario(sc) }) {
                                            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Comparison Output Layout (Baseline vs Active Scenario)
        scenarioResults?.let { scRes ->
            item {
                Text(
                    text = "Simulation Preview: ${activeScenario?.name}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Headers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("", modifier = Modifier.weight(1.2f))
                        Text("Baseline", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelLarge)
                        Text("Simulation", modifier = Modifier.weight(1.3f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    }

                    HorizontalDivider()

                    // Comparison Rows
                    ComparisonResultRow(
                        label = "Rainfall (mm/yr)",
                        baseVal = String.format("%.0f", baselineResults.annualRainfallMm),
                        scVal = String.format("%.0f (%.0f%%)", scRes.annualRainfallMm, activeScenario?.rainfallChangePct),
                        highlightChange = true,
                        isBetter = scRes.annualRainfallMm >= baselineResults.annualRainfallMm
                    )

                    ComparisonResultRow(
                        label = "Recharge (m³/d)",
                        baseVal = String.format("%.0f", baselineResults.dailyRechargeM3),
                        scVal = String.format("%.0f", scRes.dailyRechargeM3),
                        highlightChange = true,
                        isBetter = scRes.dailyRechargeM3 >= baselineResults.dailyRechargeM3
                    )

                    ComparisonResultRow(
                        label = "Total Demand (m³/d)",
                        baseVal = String.format("%.0f", baselineResults.totalDailyDemandM3),
                        scVal = String.format("%.0f", scRes.totalDailyDemandM3),
                        highlightChange = true,
                        isBetter = scRes.totalDailyDemandM3 <= baselineResults.totalDailyDemandM3
                    )

                    ComparisonResultRow(
                        label = "Pumping Rate (m³/d)",
                        baseVal = String.format("%.0f", baselineResults.totalDailyPumpingM3),
                        scVal = String.format("%.0f", scRes.totalDailyPumpingM3),
                        highlightChange = true,
                        isBetter = scRes.totalDailyPumpingM3 <= baselineResults.totalDailyPumpingM3
                    )

                    ComparisonResultRow(
                        label = "Safe Yield Limit (m³/d)",
                        baseVal = String.format("%.0f", baselineResults.safeYieldM3Day),
                        scVal = String.format("%.0f", scRes.safeYieldM3Day),
                        highlightChange = false,
                        isBetter = true
                    )

                    ComparisonResultRow(
                        label = "Net Balance (m³/d)",
                        baseVal = String.format("%+.1f", baselineResults.netDailyStorageChangeM3),
                        scVal = String.format("%+.1f", scRes.netDailyStorageChangeM3),
                        highlightChange = true,
                        isBetter = scRes.netDailyStorageChangeM3 >= baselineResults.netDailyStorageChangeM3,
                        colorOverride = if (scRes.netDailyStorageChangeM3 >= 0) StatusSafeGreen else StatusDangerRed
                    )

                    ComparisonResultRow(
                        label = "Salinity Risk Level",
                        baseVal = baselineResults.intrusionRiskLevel,
                        scVal = scRes.intrusionRiskLevel,
                        highlightChange = true,
                        isBetter = scRes.intrusionRiskScore <= baselineResults.intrusionRiskScore,
                        colorOverride = when (scRes.intrusionRiskLevel) {
                            "Low" -> StatusSafeGreen
                            "Moderate" -> StatusWarningYellow
                            else -> StatusDangerRed
                        }
                    )

                    ComparisonResultRow(
                        label = "Aquifer Status",
                        baseVal = baselineResults.groundwaterStatus,
                        scVal = scRes.groundwaterStatus,
                        highlightChange = true,
                        isBetter = scRes.groundwaterStatus != "Critical",
                        colorOverride = when (scRes.groundwaterStatus) {
                            "Sustainable" -> StatusSafeGreen
                            "Warning" -> StatusWarningYellow
                            else -> StatusDangerRed
                        }
                    )
                }
            }

            // Scenario Advice
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Simulation Analysis Summary",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        val diff = scRes.netDailyStorageChangeM3 - baselineResults.netDailyStorageChangeM3
                        val text = if (diff < -50) {
                            "⚠️ This management scenario places severe stress on the aquifer! The net balance drops by ${String.format("%.0f", -diff)} m³/day, causing high risks of seawater intrusion and freshwater lens collapse."
                        } else if (diff > 50) {
                            "✅ Highly recommended! By managing demand or introducing artificial recharge installations, the aquifer balance is boosted by ${String.format("%.0f", diff)} m³/day, extending the life of the freshwater lens."
                        } else {
                            "⚖️ This scenario results in minimal changes to baseline conditions. Overall groundwater safety is mostly stable."
                        }

                        Text(
                            text = text,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        } ?: item {
            // Hint to select scenario
            Card(
                modifier = Modifier.fillMaxWidth().height(150.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Select a scenario above to preview instant comparisons.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Button to show Create Dialog
        item {
            Button(
                onClick = { showCreateDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isIndonesian) "Buat Skenario Baru" else "Create Custom Scenario")
            }
        }
    }

    // Create Custom Scenario Dialog
    if (showCreateDialog) {
        var scName by remember { mutableStateOf("") }
        var scDesc by remember { mutableStateOf("") }
        var rainVal by remember { mutableStateOf("0.0") }
        var popVal by remember { mutableStateOf("0.0") }
        var tourVal by remember { mutableStateOf("0.0") }
        var pumpVal by remember { mutableStateOf("0.0") }
        var rechargeVal by remember { mutableStateOf("0.0") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("New Scenario") },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        OutlinedTextField(
                            value = scName,
                            onValueChange = { scName = it },
                            label = { Text("Scenario Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = scDesc,
                            onValueChange = { scDesc = it },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = rainVal,
                            onValueChange = { rainVal = it },
                            label = { Text("Rainfall Change (%) e.g. -15.0") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = popVal,
                            onValueChange = { popVal = it },
                            label = { Text("Population Growth (%) e.g. +10.0") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = tourVal,
                            onValueChange = { tourVal = it },
                            label = { Text("Tourism Growth (%)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = pumpVal,
                            onValueChange = { pumpVal = it },
                            label = { Text("Abstraction Increase (%)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = rechargeVal,
                            onValueChange = { rechargeVal = it },
                            label = { Text("Artif. Recharge expansion (%)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (scName.isNotEmpty()) {
                            viewModel.createScenario(
                                name = scName,
                                desc = scDesc,
                                rainChange = rainVal.toDoubleOrNull() ?: 0.0,
                                popChange = popVal.toDoubleOrNull() ?: 0.0,
                                tourismChange = tourVal.toDoubleOrNull() ?: 0.0,
                                pumpChange = pumpVal.toDoubleOrNull() ?: 0.0,
                                rechargeChange = rechargeVal.toDoubleOrNull() ?: 0.0
                            )
                            showCreateDialog = false
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ComparisonResultRow(
    label: String,
    baseVal: String,
    scVal: String,
    highlightChange: Boolean,
    isBetter: Boolean,
    colorOverride: Color? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1.2f),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = baseVal,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            style = MaterialTheme.typography.bodyMedium
        )
        
        val textColor = if (highlightChange) {
            if (colorOverride != null) colorOverride
            else if (isBetter) StatusSafeGreen else StatusDangerRed
        } else {
            MaterialTheme.colorScheme.primary
        }

        Text(
            text = scVal,
            modifier = Modifier.weight(1.3f),
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            style = MaterialTheme.typography.titleSmall,
            color = textColor
        )
    }
}
