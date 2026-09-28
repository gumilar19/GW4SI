package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculation.HydroCalculationEngine
import com.example.model.Project
import com.example.ui.viewmodel.MainViewModel

@Composable
fun ModulesScreen(
    viewModel: MainViewModel,
    project: Project,
    results: HydroCalculationEngine.Results,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val isIndonesian = language == "Indonesian"

    var expandedCardIndex by remember { mutableStateOf(-1) }
    var csvTextToImport by remember { mutableStateOf("") }
    var showCsvDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // MODULE 1: Climate Data
        item {
            ExpandableModuleCard(
                index = 1,
                title = if (isIndonesian) "Modul 1: Data Iklim" else "Module 1: Climate Data",
                icon = Icons.Default.Cloud,
                expandedIndex = expandedCardIndex,
                onCardClicked = { expandedCardIndex = if (expandedCardIndex == 1) -1 else 1 }
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    var annualRain by remember(project) { mutableStateOf(project.annualRainfall.toString()) }
                    var temp by remember(project) { mutableStateOf(project.temperature.toString()) }
                    var pet by remember(project) { mutableStateOf(project.potentialET.toString()) }
                    var humidity by remember(project) { mutableStateOf(project.relativeHumidity.toString()) }
                    var rainyDays by remember(project) { mutableStateOf(project.rainyDays.toString()) }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = annualRain,
                            onValueChange = { annualRain = it },
                            label = { Text("Rainfall (mm/year)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = temp,
                            onValueChange = { temp = it },
                            label = { Text("Temp (°C)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = pet,
                            onValueChange = { pet = it },
                            label = { Text("PET (mm/year)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = humidity,
                            onValueChange = { humidity = it },
                            label = { Text("Humidity (%)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = rainyDays,
                        onValueChange = { rainyDays = it },
                        label = { Text("Rainy Days / Year") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val updated = project.copy(
                                    annualRainfall = annualRain.toDoubleOrNull() ?: project.annualRainfall,
                                    temperature = temp.toDoubleOrNull() ?: project.temperature,
                                    potentialET = pet.toDoubleOrNull() ?: project.potentialET,
                                    relativeHumidity = humidity.toDoubleOrNull() ?: project.relativeHumidity,
                                    rainyDays = rainyDays.toIntOrNull() ?: project.rainyDays
                                )
                                viewModel.updateProject(updated)
                            },
                            modifier = Modifier.weight(1.5f)
                        ) {
                            Text(if (isIndonesian) "Simpan Iklim" else "Save Climate")
                        }

                        FilledTonalButton(
                            onClick = { showCsvDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Upload, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("CSV")
                        }
                    }
                }
            }
        }

        // MODULE 2: Aquifer Data
        item {
            ExpandableModuleCard(
                index = 2,
                title = if (isIndonesian) "Modul 2: Data Akuifer" else "Module 2: Aquifer Data",
                icon = Icons.Default.Layers,
                expandedIndex = expandedCardIndex,
                onCardClicked = { expandedCardIndex = if (expandedCardIndex == 2) -1 else 2 }
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    var area by remember(project) { mutableStateOf(project.islandArea.toString()) }
                    var thick by remember(project) { mutableStateOf(project.aquiferThickness.toString()) }
                    var yield by remember(project) { mutableStateOf(project.specificYield.toString()) }
                    var cond by remember(project) { mutableStateOf(project.hydraulicConductivity.toString()) }
                    var aqType by remember(project) { mutableStateOf(project.aquiferType) }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = area,
                            onValueChange = { area = it },
                            label = { Text("Area (km²)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = thick,
                            onValueChange = { thick = it },
                            label = { Text("Thickness (m)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = yield,
                            onValueChange = { yield = it },
                            label = { Text("Specific Yield (\$S_y\$)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = cond,
                            onValueChange = { cond = it },
                            label = { Text("K Conductivity (m/d)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Aquifer Type Select buttons
                    Text("Aquifer Type", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Unconfined", "Confined", "Semi-confined").forEach { type ->
                            FilterChip(
                                selected = aqType == type,
                                onClick = { aqType = type },
                                label = { Text(type, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val updated = project.copy(
                                islandArea = area.toDoubleOrNull() ?: project.islandArea,
                                aquiferThickness = thick.toDoubleOrNull() ?: project.aquiferThickness,
                                specificYield = yield.toDoubleOrNull() ?: project.specificYield,
                                hydraulicConductivity = cond.toDoubleOrNull() ?: project.hydraulicConductivity,
                                aquiferType = aqType
                            )
                            viewModel.updateProject(updated)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isIndonesian) "Simpan Akuifer" else "Save Aquifer")
                    }
                }
            }
        }

        // MODULE 3: Land Characteristics & Recharge Coefficient Engine
        item {
            ExpandableModuleCard(
                index = 3,
                title = if (isIndonesian) "Modul 3: Karakteristik Lahan & RC Engine" else "Module 3: Land Characteristics & Recharge Engine",
                icon = Icons.Default.Terrain,
                expandedIndex = expandedCardIndex,
                onCardClicked = { expandedCardIndex = if (expandedCardIndex == 3) -1 else 3 }
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Segmented Selector for Mode A vs Mode B
                    Text(
                        text = "Recharge Coefficient Mode",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Automatic", "Manual").forEach { mode ->
                            val isSelected = project.rechargeMode == mode
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    viewModel.updateProject(project.copy(rechargeMode = mode))
                                },
                                label = {
                                    Text(
                                        text = if (mode == "Automatic") "Mode A: Automatic (Recommended)" else "Mode B: Manual Entry",
                                        fontSize = 11.sp
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (project.rechargeMode == "Manual") {
                        // MODE B: Manual Entry
                        Text(
                            text = "Directly specify the aquifer recharge and runoff parameters. Commonly obtained from field measurements or calibration.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        var manualRc by remember(project) { mutableStateOf(project.rechargeCoefficient.toString()) }
                        var runoffCoeff by remember(project) { mutableStateOf(project.runoffCoefficient.toString()) }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = manualRc,
                                onValueChange = { manualRc = it },
                                label = { Text("Recharge Coeff") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = runoffCoeff,
                                onValueChange = { runoffCoeff = it },
                                label = { Text("Runoff Coeff") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Button(
                            onClick = {
                                val updated = project.copy(
                                    rechargeCoefficient = manualRc.toDoubleOrNull() ?: project.rechargeCoefficient,
                                    runoffCoefficient = runoffCoeff.toDoubleOrNull() ?: project.runoffCoefficient
                                )
                                viewModel.updateProject(updated)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save Coefficients")
                        }
                    } else {
                        // MODE A: Automatic Estimation
                        val luFactors = com.example.model.ProjectParser.parseFactorTable(project.landUseFactorsString)
                        val soilFactors = com.example.model.ProjectParser.parseFactorTable(project.soilFactorsString)
                        val slopeFactors = com.example.model.ProjectParser.parseFactorTable(project.slopeFactorsString)
                        val vegFactors = com.example.model.ProjectParser.parseFactorTable(project.vegetationFactorsString)
                        val landUnits = com.example.model.ProjectParser.parseLandUnits(project.landUnitsString)

                        // 1. Prominent calculated results banner
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Estimated Recharge Coefficient (RC_total)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = String.format("%.3f", results.computedRc),
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Annual Recharge Volume: ${String.format("%,.1f", results.rechargeVolM3)} m³/year (${String.format("%.1f", results.rechargeMm)} mm depth)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }

                        // 2. Multiple Land Units Manager
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Land Units / Polygons (${landUnits.size})",
                                style = MaterialTheme.typography.titleSmall
                            )

                            var showAddUnitDialog by remember { mutableStateOf(false) }
                            FilledTonalButton(
                                onClick = { showAddUnitDialog = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                                Text("Add Unit", fontSize = 11.sp)
                            }

                            if (showAddUnitDialog) {
                                var newName by remember { mutableStateOf("Land Unit ${landUnits.size + 1}") }
                                var newArea by remember { mutableStateOf("1.0") }
                                var newLU by remember { mutableStateOf(luFactors.keys.firstOrNull() ?: "") }
                                var newSoil by remember { mutableStateOf(soilFactors.keys.firstOrNull() ?: "") }
                                var newSlope by remember { mutableStateOf(slopeFactors.keys.firstOrNull() ?: "") }
                                var newVeg by remember { mutableStateOf(vegFactors.keys.firstOrNull() ?: "") }

                                AlertDialog(
                                    onDismissRequest = { showAddUnitDialog = false },
                                    title = { Text("Add Land Polygon / Unit") },
                                    text = {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(12.dp),
                                            modifier = Modifier.verticalScroll(rememberScrollState())
                                        ) {
                                            OutlinedTextField(
                                                value = newName,
                                                onValueChange = { newName = it },
                                                label = { Text("Name") },
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            OutlinedTextField(
                                                value = newArea,
                                                onValueChange = { newArea = it },
                                                label = { Text("Area (km²)") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            DropdownSelector(label = "Land Use", selectedValue = newLU, options = luFactors.keys.toList(), onValueChange = { newLU = it })
                                            DropdownSelector(label = "Soil Type", selectedValue = newSoil, options = soilFactors.keys.toList(), onValueChange = { newSoil = it })
                                            DropdownSelector(label = "Slope Class", selectedValue = newSlope, options = slopeFactors.keys.toList(), onValueChange = { newSlope = it })
                                            DropdownSelector(label = "Vegetation Cover", selectedValue = newVeg, options = vegFactors.keys.toList(), onValueChange = { newVeg = it })
                                        }
                                    },
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                val added = com.example.model.LandUnit(
                                                    id = "lu_${System.currentTimeMillis()}",
                                                    name = newName.replace("|", " ").replace(";", " "),
                                                    area = newArea.toDoubleOrNull() ?: 1.0,
                                                    landUse = newLU,
                                                    soilType = newSoil,
                                                    slope = newSlope,
                                                    vegetation = newVeg
                                                )
                                                val newList = landUnits + added
                                                viewModel.updateProject(project.copy(landUnitsString = com.example.model.ProjectParser.serializeLandUnits(newList)))
                                                showAddUnitDialog = false
                                            }
                                        ) {
                                            Text("Add")
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showAddUnitDialog = false }) { Text("Cancel") }
                                    }
                                )
                            }
                        }

                        // Display Land Units list
                        if (landUnits.isEmpty()) {
                            Text(
                                text = "No land units defined. Add one to calculate automatic coefficient.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(8.dp)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                landUnits.forEach { unit ->
                                    val flu = luFactors[unit.landUse] ?: 0.50
                                    val fsoil = soilFactors[unit.soilType] ?: 0.50
                                    val fslope = slopeFactors[unit.slope] ?: 0.50
                                    val fveg = vegFactors[unit.vegetation] ?: 0.50
                                    val unitRc = project.weightLU * flu + project.weightSOIL * fsoil + project.weightSLOPE * fslope + project.weightVEG * fveg

                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(unit.name, style = MaterialTheme.typography.titleSmall)
                                                    Text("Area: ${unit.area} km²", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = "RC = ${String.format("%.3f", unitRc)}",
                                                        style = MaterialTheme.typography.labelLarge,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.padding(end = 8.dp)
                                                    )
                                                    IconButton(
                                                        onClick = {
                                                            val newList = landUnits.filter { it.id != unit.id }
                                                            viewModel.updateProject(project.copy(landUnitsString = com.example.model.ProjectParser.serializeLandUnits(newList)))
                                                        },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(Icons.Default.Delete, "Delete unit", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                            }
                                            
                                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                                            
                                            // Grid details
                                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text("• Land Use: ${unit.landUse} (FLU=${String.format("%.2f", flu)})", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp)
                                                    Text("• Soil: ${unit.soilType} (FSOIL=${String.format("%.2f", fsoil)})", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp)
                                                }
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text("• Slope: ${unit.slope} (FSLOPE=${String.format("%.2f", fslope)})", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp)
                                                    Text("• Veg: ${unit.vegetation} (FVEG=${String.format("%.2f", fveg)})", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 3. Calculation report details step-by-step
                        var showReport by remember { mutableStateOf(false) }
                        OutlinedButton(
                            onClick = { showReport = !showReport },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(if (showReport) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (showReport) "Hide Mathematical Report" else "View Mathematical Report")
                        }

                        if (showReport) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Step-by-Step Calculation Steps", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                                    Text(
                                        text = "Formula: RC_i = (wLU × FLU) + (wSOIL × FSOIL) + (wSLOPE × FSLOPE) + (wVEG × FVEG)\n" +
                                               "Where weights are:\n" +
                                               " • wLU = ${project.weightLU}\n" +
                                               " • wSOIL = ${project.weightSOIL}\n" +
                                               " • wSLOPE = ${project.weightSLOPE}\n" +
                                               " • wVEG = ${project.weightVEG}",
                                        fontSize = 11.sp,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    
                                    Divider()
                                    
                                    landUnits.forEachIndexed { idx, unit ->
                                        val flu = luFactors[unit.landUse] ?: 0.50
                                        val fsoil = soilFactors[unit.soilType] ?: 0.50
                                        val fslope = slopeFactors[unit.slope] ?: 0.50
                                        val fveg = vegFactors[unit.vegetation] ?: 0.50
                                        val unitRc = project.weightLU * flu + project.weightSOIL * fsoil + project.weightSLOPE * fslope + project.weightVEG * fveg
                                        
                                        Text(
                                            text = "Unit ${idx + 1} (${unit.name}):\n" +
                                                   " • RC_${idx + 1} = (${project.weightLU} × ${flu}) + (${project.weightSOIL} × ${fsoil}) + (${project.weightSLOPE} × ${fslope}) + (${project.weightVEG} × ${fveg})\n" +
                                                   " • RC_${idx + 1} = ${String.format("%.4f", unitRc)}",
                                            fontSize = 11.sp,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }

                                    Divider()

                                    val totalArea = landUnits.sumOf { it.area }
                                    Text(
                                        text = "Weighted Average (RC_total):\n" +
                                               " • RC_total = Σ(RC_i × A_i) / ΣA_i\n" +
                                               " • RC_total = [ " + landUnits.mapIndexed { idx, unit ->
                                                   val flu = luFactors[unit.landUse] ?: 0.50
                                                   val fsoil = soilFactors[unit.soilType] ?: 0.50
                                                   val fslope = slopeFactors[unit.slope] ?: 0.50
                                                   val fveg = vegFactors[unit.vegetation] ?: 0.50
                                                   val unitRc = project.weightLU * flu + project.weightSOIL * fsoil + project.weightSLOPE * fslope + project.weightVEG * fveg
                                                   "(${String.format("%.3f", unitRc)} × ${unit.area})"
                                               }.joinToString(" + ") + " ] / $totalArea\n" +
                                               " • RC_total = ${String.format("%.4f", results.computedRc)}",
                                        fontSize = 11.sp,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // 4. Advanced Settings (Custom Weights & Editable Factor Tables)
                        var showAdvancedOptions by remember { mutableStateOf(false) }
                        IconButtonWithLabel(
                            icon = Icons.Default.Settings,
                            label = "Advanced Settings (Weights & Factors)",
                            onClick = { showAdvancedOptions = !showAdvancedOptions }
                        )

                        if (showAdvancedOptions) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    // Custom weights
                                    Text("Custom Weighting Factors (Sum must be 1.0)", style = MaterialTheme.typography.titleSmall)
                                    
                                    var wLU by remember(project) { mutableStateOf(project.weightLU.toString()) }
                                    var wSoil by remember(project) { mutableStateOf(project.weightSOIL.toString()) }
                                    var wSlope by remember(project) { mutableStateOf(project.weightSLOPE.toString()) }
                                    var wVeg by remember(project) { mutableStateOf(project.weightVEG.toString()) }

                                    val sum = (wLU.toDoubleOrNull() ?: 0.0) + (wSoil.toDoubleOrNull() ?: 0.0) + (wSlope.toDoubleOrNull() ?: 0.0) + (wVeg.toDoubleOrNull() ?: 0.0)
                                    val isSumValid = Math.abs(sum - 1.0) < 0.001

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(value = wLU, onValueChange = { wLU = it }, label = { Text("LU") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                                        OutlinedTextField(value = wSoil, onValueChange = { wSoil = it }, label = { Text("Soil") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                                        OutlinedTextField(value = wSlope, onValueChange = { wSlope = it }, label = { Text("Slope") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                                        OutlinedTextField(value = wVeg, onValueChange = { wVeg = it }, label = { Text("Veg") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isSumValid) Icons.Default.CheckCircle else Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = if (isSumValid) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isSumValid) "Sum = 1.00 (Valid)" else "Sum = ${String.format("%.2f", sum)} (Must be equal to 1.00)",
                                            fontSize = 11.sp,
                                            color = if (isSumValid) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            if (isSumValid) {
                                                val updated = project.copy(
                                                    weightLU = wLU.toDoubleOrNull() ?: project.weightLU,
                                                    weightSOIL = wSoil.toDoubleOrNull() ?: project.weightSOIL,
                                                    weightSLOPE = wSlope.toDoubleOrNull() ?: project.weightSLOPE,
                                                    weightVEG = wVeg.toDoubleOrNull() ?: project.weightVEG
                                                )
                                                viewModel.updateProject(updated)
                                            }
                                        },
                                        enabled = isSumValid,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Save Custom Weights")
                                    }

                                    Divider()

                                    // Custom Tables editor
                                    Text("Editable Predefined Factor tables", style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        text = "Review or customize numerical weights for physical variables below. Sourced from Ghyben-Herzberg lens guidelines (Bear 1979).",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    var showLUEditor by remember { mutableStateOf(false) }
                                    OutlinedButton(onClick = { showLUEditor = !showLUEditor }, modifier = Modifier.fillMaxWidth()) {
                                        Text(if (showLUEditor) "Hide Land Use Factors" else "Edit Land Use Factors")
                                    }
                                    if (showLUEditor) {
                                        FactorTableEditor(
                                            title = "Land Use Factor Table (FLU)",
                                            factorsMap = luFactors,
                                            onSave = { updatedMap ->
                                                viewModel.updateProject(project.copy(landUseFactorsString = com.example.model.ProjectParser.serializeFactorTable(updatedMap)))
                                            },
                                            reference = "Literature Reference: FAO Land Guidelines (1995). Predefined coefficients match infiltration rates under standard tropical small-island catchments."
                                        )
                                    }

                                    var showSoilEditor by remember { mutableStateOf(false) }
                                    OutlinedButton(onClick = { showSoilEditor = !showSoilEditor }, modifier = Modifier.fillMaxWidth()) {
                                        Text(if (showSoilEditor) "Hide Soil Factors" else "Edit Soil Factors")
                                    }
                                    if (showSoilEditor) {
                                        FactorTableEditor(
                                            title = "Soil Factor Table (FSOIL)",
                                            factorsMap = soilFactors,
                                            onSave = { updatedMap ->
                                                viewModel.updateProject(project.copy(soilFactorsString = com.example.model.ProjectParser.serializeFactorTable(updatedMap)))
                                            },
                                            reference = "Literature Reference: Todd & Mays Groundwater Hydrology. Higher permeability (Gravel, Sand) increases recharge fraction."
                                        )
                                    }

                                    var showSlopeEditor by remember { mutableStateOf(false) }
                                    OutlinedButton(onClick = { showSlopeEditor = !showSlopeEditor }, modifier = Modifier.fillMaxWidth()) {
                                        Text(if (showSlopeEditor) "Hide Slope Factors" else "Edit Slope Factors")
                                    }
                                    if (showSlopeEditor) {
                                        FactorTableEditor(
                                            title = "Slope Factor Table (FSLOPE)",
                                            factorsMap = slopeFactors,
                                            onSave = { updatedMap ->
                                                viewModel.updateProject(project.copy(slopeFactorsString = com.example.model.ProjectParser.serializeFactorTable(updatedMap)))
                                            },
                                            reference = "Literature Reference: USDA National Engineering Handbook. Steeper slopes promote fast gravity runoff and decrease local storage ponding infiltration."
                                        )
                                    }

                                    var showVegEditor by remember { mutableStateOf(false) }
                                    OutlinedButton(onClick = { showVegEditor = !showVegEditor }, modifier = Modifier.fillMaxWidth()) {
                                        Text(if (showVegEditor) "Hide Vegetation Factors" else "Edit Vegetation Factors")
                                    }
                                    if (showVegEditor) {
                                        FactorTableEditor(
                                            title = "Vegetation Factor Table (FVEG)",
                                            factorsMap = vegFactors,
                                            onSave = { updatedMap ->
                                                viewModel.updateProject(project.copy(vegetationFactorsString = com.example.model.ProjectParser.serializeFactorTable(updatedMap)))
                                            },
                                            reference = "Literature Reference: Hewlett (1982) Forest Hydrology. Root channels promote preferential macropore path infiltration, improving permeability."
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // MODULE 4: Population & Demand Engine
        item {
            ExpandableModuleCard(
                index = 4,
                title = if (isIndonesian) "Modul 4: Kebutuhan Air Populasi" else "Module 4: Population & Demand Engine",
                icon = Icons.Default.People,
                expandedIndex = expandedCardIndex,
                onCardClicked = { expandedCardIndex = if (expandedCardIndex == 4) -1 else 4 }
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Quick stats dashboard for demand
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Total Groundwater Demand (Qpump)",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "${String.format("%,.1f", results.totalDailyDemandM3)} m³/day",
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "Annual Demand: ${String.format("%,.1f", results.totalDailyDemandM3 * 365.0)} m³/year | Monthly: ${String.format("%,.1f", results.totalDailyDemandM3 * 30.4)} m³/month",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    // Section 1: Domestic Water Demand
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Home, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("1. Domestic Water Demand", style = MaterialTheme.typography.titleSmall)
                            }
                            
                            var popInput by remember(project) { mutableStateOf(project.population.toString()) }
                            var domDemandInput by remember(project) { mutableStateOf(project.domesticDemand.toString()) }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = popInput,
                                    onValueChange = { popInput = it },
                                    label = { Text("Population") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1.1f)
                                )
                                OutlinedTextField(
                                    value = domDemandInput,
                                    onValueChange = { domDemandInput = it },
                                    label = { Text("Demand (L/person/day)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1.3f)
                                )
                            }

                            // Math Steps
                            val pop = popInput.toIntOrNull() ?: project.population
                            val demandVal = domDemandInput.toDoubleOrNull() ?: project.domesticDemand
                            val calculatedDailyL = pop * demandVal
                            val calculatedDailyM3 = calculatedDailyL / 1000.0
                            val calculatedAnnualM3 = calculatedDailyM3 * 365.0

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "Equation: Domestic Demand = Population × Per Capita Demand",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Math: $pop × $demandVal = ${String.format("%,.1f", calculatedDailyL)} L/day",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "• Equivalent Daily: ${String.format("%,.1f", calculatedDailyM3)} m³/day\n" +
                                               "• Equivalent Annual: ${String.format("%,.1f", calculatedAnnualM3)} m³/year",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    viewModel.updateProject(project.copy(
                                        population = pop,
                                        domesticDemand = demandVal
                                    ))
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Save Domestic Parameters")
                            }
                        }
                    }

                    // Section 2: Tourism Demand
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Hotel, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("2. Tourism Water Demand", style = MaterialTheme.typography.titleSmall)
                            }

                            var touristsInput by remember(project) { mutableStateOf(project.touristsPerDay.toString()) }
                            var touristsDemandInput by remember(project) { mutableStateOf(project.tourismDemand.toString()) }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = touristsInput,
                                    onValueChange = { touristsInput = it },
                                    label = { Text("Average Tourists/Day") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = touristsDemandInput,
                                    onValueChange = { touristsDemandInput = it },
                                    label = { Text("Demand (L/tourist/day)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1.2f)
                                )
                            }

                            val tourists = touristsInput.toDoubleOrNull() ?: project.touristsPerDay
                            val tourDemand = touristsDemandInput.toDoubleOrNull() ?: project.tourismDemand
                            val calculatedTourM3 = (tourists * tourDemand) / 1000.0

                            Text(
                                text = "Equation: Tourism Demand = Tourists × Demand\n" +
                                       "Math: ${String.format("%.1f", tourists)} × ${String.format("%.1f", tourDemand)} = ${String.format("%.2f", calculatedTourM3)} m³/day",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Button(
                                onClick = {
                                    viewModel.updateProject(project.copy(
                                        touristsPerDay = tourists,
                                        tourismDemand = tourDemand
                                    ))
                                },
                                modifier = Modifier.fillMaxWidth()
                              ) {
                                  Text("Save Tourism Parameters")
                              }
                        }
                    }

                    // Section 3: Industrial Demand (Unlimited list)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Business, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("3. Industrial Water Demand", style = MaterialTheme.typography.titleSmall)
                            }

                            val industries = com.example.model.ProjectParser.parseIndustries(project.industriesString)

                            Text(
                                text = "Add and manage individual industrial facilities. Sum represents total industrial extraction.",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            industries.forEach { ind ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(ind.name, style = MaterialTheme.typography.bodyMedium)
                                        Text("Daily: ${ind.dailyDemand} m³/day", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    IconButton(
                                        onClick = {
                                            val newList = industries.filter { it.id != ind.id }
                                            viewModel.updateProject(project.copy(industriesString = com.example.model.ProjectParser.serializeIndustries(newList)))
                                        }
                                    ) {
                                        Icon(Icons.Default.Delete, "Delete industry", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            Divider()

                            // Form to add industry
                            var newIndName by remember { mutableStateOf("") }
                            var newIndDemand by remember { mutableStateOf("") }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = newIndName,
                                    onValueChange = { newIndName = it },
                                    label = { Text("Facility Name") },
                                    modifier = Modifier.weight(1.3f)
                                )
                                OutlinedTextField(
                                    value = newIndDemand,
                                    onValueChange = { newIndDemand = it },
                                    label = { Text("m³/day") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(0.9f)
                                )
                            }

                            Button(
                                onClick = {
                                    if (newIndName.isNotBlank() && newIndDemand.isNotBlank()) {
                                        val added = com.example.model.IndustryDemandItem(
                                            id = "ind_${System.currentTimeMillis()}",
                                            name = newIndName.replace("|", " ").replace(";", " "),
                                            dailyDemand = newIndDemand.toDoubleOrNull() ?: 0.0
                                        )
                                        val newList = industries + added
                                        viewModel.updateProject(project.copy(industriesString = com.example.model.ProjectParser.serializeIndustries(newList)))
                                        newIndName = ""
                                        newIndDemand = ""
                                    }
                                },
                                enabled = newIndName.isNotBlank() && newIndDemand.isNotBlank(),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Industry")
                            }
                        }
                    }

                    // Section 4: Agricultural Demand
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Agriculture, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("4. Agricultural Demand", style = MaterialTheme.typography.titleSmall)
                            }

                            var areaHa by remember(project) { mutableStateOf(project.cropIrrigatedArea.toString()) }
                            var waterReqInput by remember(project) { mutableStateOf(project.cropWaterRequirement.toString()) }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = areaHa,
                                    onValueChange = { areaHa = it },
                                    label = { Text("Irrigated Area (ha)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = waterReqInput,
                                    onValueChange = { waterReqInput = it },
                                    label = { Text("Crop Water Req (m³/ha/yr)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1.2f)
                                )
                            }

                            val ha = areaHa.toDoubleOrNull() ?: project.cropIrrigatedArea
                            val req = waterReqInput.toDoubleOrNull() ?: project.cropWaterRequirement
                            val calculatedAnnualAg = ha * req
                            val calculatedDailyAg = calculatedAnnualAg / 365.0

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "Equation: Agri Demand = Irrigated Area × Water Requirement",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Math: $ha ha × $req m³/ha/yr = ${String.format("%,.1f", calculatedAnnualAg)} m³/year",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "• Daily Equivalent: ${String.format("%,.1f", calculatedDailyAg)} m³/day\n" +
                                               "• Monthly Equivalent: ${String.format("%,.1f", calculatedAnnualAg / 12.0)} m³/month\n" +
                                               "• Yearly Total: ${String.format("%,.1f", calculatedAnnualAg)} m³/year",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    viewModel.updateProject(project.copy(
                                        cropIrrigatedArea = ha,
                                        cropWaterRequirement = req
                                    ))
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Save Agricultural Parameters")
                            }
                        }
                    }

                    // Section 5: Livestock Demand (Default consumption database)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Water, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("5. Livestock Demand Database", style = MaterialTheme.typography.titleSmall)
                            }

                            val livestockItems = com.example.model.ProjectParser.parseLivestock(project.livestockString)
                            val livestockFactors = com.example.model.ProjectParser.parseFactorTable(project.livestockFactorsString)

                            Text(
                                text = "Equation: Livestock Demand = sum(Animals × consumption)\n" +
                                       "Sourced from standard FAO/WHO tropical livestock guidelines.",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            val draftCounts = remember(project) { mutableStateMapOf<String, String>().apply {
                                livestockItems.forEach { put(it.type, it.count.toString()) }
                            }}

                            livestockItems.forEach { item ->
                                val demand = livestockFactors[item.type] ?: item.demandPerAnimal
                                val inputStr = draftCounts[item.type] ?: item.count.toString()
                                val countInt = inputStr.toIntOrNull() ?: 0
                                val consumptionL = countInt * demand
                                val consumptionM3 = consumptionL / 1000.0

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1.1f)) {
                                        Text(item.type, style = MaterialTheme.typography.bodyMedium)
                                        Text("Std: ${demand} L/animal/day", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    
                                    OutlinedTextField(
                                        value = inputStr,
                                        onValueChange = { draftCounts[item.type] = it },
                                        label = { Text("Count", fontSize = 9.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.width(90.dp).height(50.dp)
                                    )

                                    Column(modifier = Modifier.weight(0.9f), horizontalAlignment = Alignment.End) {
                                        Text("${String.format("%,.1f", consumptionL)} L/d", style = MaterialTheme.typography.bodySmall)
                                        Text("(${String.format("%.2f", consumptionM3)} m³/d)", style = MaterialTheme.typography.bodySmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    val updatedList = livestockItems.map { item ->
                                        val enteredCount = draftCounts[item.type]?.toIntOrNull() ?: item.count
                                        item.copy(count = enteredCount)
                                    }
                                    viewModel.updateProject(project.copy(
                                        livestockString = com.example.model.ProjectParser.serializeLivestock(updatedList)
                                    ))
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Update Livestock Counts")
                            }

                            Divider()

                            // Factor Editor for Livestock
                            var showLsFactorEditor by remember { mutableStateOf(false) }
                            OutlinedButton(
                                onClick = { showLsFactorEditor = !showLsFactorEditor },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (showLsFactorEditor) "Hide Livestock Standards Editor" else "Edit Livestock Standards Editor")
                            }

                            if (showLsFactorEditor) {
                                FactorTableEditor(
                                    title = "Livestock Standards (L/animal/day)",
                                    factorsMap = livestockFactors,
                                    onSave = { updatedFactors ->
                                        val updatedStr = com.example.model.ProjectParser.serializeFactorTable(updatedFactors)
                                        // Also update the livestock items with these new standards
                                        val updatedItems = livestockItems.map { item ->
                                            item.copy(demandPerAnimal = updatedFactors[item.type] ?: item.demandPerAnimal)
                                        }
                                        viewModel.updateProject(project.copy(
                                            livestockFactorsString = updatedStr,
                                            livestockString = com.example.model.ProjectParser.serializeLivestock(updatedItems)
                                        ))
                                    },
                                    reference = "Assumptions based on FAO Animal Water Guidelines. Advanced users can adjust these values based on local heat indices, breeds, or local guidelines."
                                )
                            }
                        }
                    }

                    // Section 6: Sector Breakdown Progress indicators
                    val total = results.totalDailyDemandM3
                    if (total > 0) {
                        val domPct = (results.dailyDomesticDemandM3 / total) * 100
                        val tourPct = (results.dailyTourismDemandM3 / total) * 100
                        val indPct = (results.dailyIndustrialDemandM3 / total) * 100
                        val agPct = (results.dailyAgDemandM3 / total) * 100
                        val lsPct = (results.dailyLivestockDemandM3 / total) * 100

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Sector Demand Contribution Breakdown", style = MaterialTheme.typography.titleSmall)
                                
                                TextWithPercentBar("Domestic (${String.format("%,.1f", results.dailyDomesticDemandM3)} m³/day)", domPct, Color(0xFF1E88E5))
                                TextWithPercentBar("Tourism (${String.format("%,.1f", results.dailyTourismDemandM3)} m³/day)", tourPct, Color(0xFF00ACC1))
                                TextWithPercentBar("Industrial (${String.format("%,.1f", results.dailyIndustrialDemandM3)} m³/day)", indPct, Color(0xFFD81B60))
                                TextWithPercentBar("Agricultural (${String.format("%,.1f", results.dailyAgDemandM3)} m³/day)", agPct, Color(0xFF43A047))
                                TextWithPercentBar("Livestock (${String.format("%,.1f", results.dailyLivestockDemandM3)} m³/day)", lsPct, Color(0xFFF4511E))
                            }
                        }
                    }

                    // Section 7: Integration with Groundwater Balance
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Groundwater Balance Integration", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "Qpump (pumping extraction) in the balance equation is automatically synchronized with the Total Demand computed above.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Divider()

                            Text(
                                text = "Equation: Balance = Recharge − Qpump − Natural Outflow\n" +
                                       "• Recharge (natural) = ${String.format("%,.1f", results.dailyRechargeM3)} m³/day\n" +
                                       "• Qpump (total demand) = ${String.format("%,.1f", results.totalDailyPumpingM3)} m³/day\n" +
                                       "• Outflow (discharge) = ${String.format("%,.1f", results.dailyOutflowM3)} m³/day\n" +
                                       "• NET CHANGE: ${String.format("%,.1f", results.netDailyStorageChangeM3)} m³/day",
                                fontSize = 11.sp,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // MODULE 5: Abstraction / Pumping
        item {
            ExpandableModuleCard(
                index = 5,
                title = if (isIndonesian) "Modul 5: Pengambilan Air Tanah" else "Module 5: Water Abstraction",
                icon = Icons.Default.VerticalShades,
                expandedIndex = expandedCardIndex,
                onCardClicked = { expandedCardIndex = if (expandedCardIndex == 5) -1 else 5 }
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    var pumpingTotal by remember(project) { mutableStateOf(project.totalPumping.toString()) }
                    var domWells by remember(project) { mutableStateOf(project.domesticWellsCount.toString()) }
                    var hotelWells by remember(project) { mutableStateOf(project.hotelWellsCount.toString()) }
                    var indWells by remember(project) { mutableStateOf(project.industrialWellsCount.toString()) }

                    OutlinedTextField(
                        value = pumpingTotal,
                        onValueChange = { pumpingTotal = it },
                        label = { Text("Total Abstraction (m³/day)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = domWells,
                            onValueChange = { domWells = it },
                            label = { Text("Domestic Wells") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = hotelWells,
                            onValueChange = { hotelWells = it },
                            label = { Text("Hotel/Resort Wells") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = indWells,
                        onValueChange = { indWells = it },
                        label = { Text("Industrial Wells") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            val updated = project.copy(
                                totalPumping = pumpingTotal.toDoubleOrNull() ?: project.totalPumping,
                                domesticWellsCount = domWells.toIntOrNull() ?: project.domesticWellsCount,
                                hotelWellsCount = hotelWells.toIntOrNull() ?: project.hotelWellsCount,
                                industrialWellsCount = indWells.toIntOrNull() ?: project.industrialWellsCount
                            )
                            viewModel.updateProject(updated)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isIndonesian) "Simpan Abstraksi" else "Save Abstraction Data")
                    }
                }
            }
        }

        // MODULE 6: Recharge Formulas Method
        item {
            ExpandableModuleCard(
                index = 6,
                title = if (isIndonesian) "Modul 6: Metode Imbuhan" else "Module 6: Recharge Formulas",
                icon = Icons.Default.Calculate,
                expandedIndex = expandedCardIndex,
                onCardClicked = { expandedCardIndex = if (expandedCardIndex == 6) -1 else 6 }
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    var activeMethod by remember(project) { mutableStateOf(project.rechargeMethod) }
                    var clRain by remember(project) { mutableStateOf(project.chlorideRain.toString()) }
                    var clGw by remember(project) { mutableStateOf(project.chlorideGroundwater.toString()) }
                    var customFormula by remember(project) { mutableStateOf(project.userDefinedFormula) }

                    Text("Active Estimation Method", style = MaterialTheme.typography.labelMedium)
                    
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(
                            "Coefficient" to "Recharge Coefficient",
                            "WaterBalance" to "Simple Water Balance",
                            "ChlorideMassBalance" to "Chloride Mass Balance (CMB)",
                            "UserDefined" to "User Defined Formula"
                        ).forEach { (code, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { activeMethod = code }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = activeMethod == code, onClick = { activeMethod = code })
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(label, fontSize = 13.sp)
                            }
                        }
                    }

                    if (activeMethod == "ChlorideMassBalance") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = clRain,
                                onValueChange = { clRain = it },
                                label = { Text("Cl Rainfall (mg/L)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = clGw,
                                onValueChange = { clGw = it },
                                label = { Text("Cl Groundwater (mg/L)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (activeMethod == "UserDefined") {
                        OutlinedTextField(
                            value = customFormula,
                            onValueChange = { customFormula = it },
                            label = { Text("Formula Example: 0.31 * Rainfall") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Button(
                        onClick = {
                            val updated = project.copy(
                                rechargeMethod = activeMethod,
                                chlorideRain = clRain.toDoubleOrNull() ?: project.chlorideRain,
                                chlorideGroundwater = clGw.toDoubleOrNull() ?: project.chlorideGroundwater,
                                userDefinedFormula = customFormula
                            )
                            viewModel.updateProject(updated)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isIndonesian) "Simpan Metode" else "Save Recharge Method")
                    }
                }
            }
        }

        // MODULE 7: Safe Yield Limit Factor
        item {
            ExpandableModuleCard(
                index = 7,
                title = if (isIndonesian) "Modul 7: Safe Yield (Batas Aman)" else "Module 7: Safe Yield Factors",
                icon = Icons.Default.CheckCircleOutline,
                expandedIndex = expandedCardIndex,
                onCardClicked = { expandedCardIndex = if (expandedCardIndex == 7) -1 else 7 }
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    var factor by remember(project) { mutableStateOf(project.safetyFactor) }

                    Text(
                        "Safe Yield is computed as Groundwater Recharge multiplied by an ecological safety factor to prevent lens collapse.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text("Ecological Safety Factor Limit", style = MaterialTheme.typography.labelMedium)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(0.40, 0.50, 0.60, 0.70).forEach { pct ->
                            FilterChip(
                                selected = factor == pct,
                                onClick = { factor = pct },
                                label = { Text("${(pct * 100).toInt()}%", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val updated = project.copy(safetyFactor = factor)
                            viewModel.updateProject(updated)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isIndonesian) "Simpan Batas Aman" else "Save Safe Yield Factor")
                    }
                }
            }
        }

        // MODULE 8: Seawater Intrusion Parameters
        item {
            ExpandableModuleCard(
                index = 8,
                title = if (isIndonesian) "Modul 8: Intrusi Air Laut" else "Module 8: Seawater Intrusion",
                icon = Icons.Default.Waves,
                expandedIndex = expandedCardIndex,
                onCardClicked = { expandedCardIndex = if (expandedCardIndex == 8) -1 else 8 }
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    var wt by remember(project) { mutableStateOf(project.waterTable.toString()) }
                    var ec by remember(project) { mutableStateOf(project.electricalConductivity.toString()) }
                    var tds by remember(project) { mutableStateOf(project.tds.toString()) }
                    var outflow by remember(project) { mutableStateOf(project.groundwaterOutflow.toString()) }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = wt,
                            onValueChange = { wt = it },
                            label = { Text("Water Table (m)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = ec,
                            onValueChange = { ec = it },
                            label = { Text("EC (µS/cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = tds,
                            onValueChange = { tds = it },
                            label = { Text("TDS (mg/L)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = outflow,
                            onValueChange = { outflow = it },
                            label = { Text("SGD Outflow (m³/d)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Button(
                        onClick = {
                            val updated = project.copy(
                                waterTable = wt.toDoubleOrNull() ?: project.waterTable,
                                electricalConductivity = ec.toDoubleOrNull() ?: project.electricalConductivity,
                                tds = tds.toDoubleOrNull() ?: project.tds,
                                groundwaterOutflow = outflow.toDoubleOrNull() ?: project.groundwaterOutflow
                            )
                            viewModel.updateProject(updated)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isIndonesian) "Simpan Sensor" else "Save Seawater Sensors")
                    }
                }
            }
        }
    }

    // CSV Import Dialog (Module 1)
    if (showCsvDialog) {
        AlertDialog(
            onDismissRequest = { showCsvDialog = false },
            title = { Text("CSV Import: Monthly Rainfall") },
            text = {
                Column {
                    Text(
                        "Paste a line of 12 comma-separated numbers representing rainfall for January to December (in mm).",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = csvTextToImport,
                        onValueChange = { csvTextToImport = it },
                        placeholder = { Text("e.g., 150,180,200,120,100,80,60,70,110,130,220,230") },
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val success = viewModel.importRainfallCsv(project, csvTextToImport)
                        if (success) {
                            showCsvDialog = false
                            csvTextToImport = ""
                        }
                    }
                ) {
                    Text("Import")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCsvDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ExpandableModuleCard(
    index: Int,
    title: String,
    icon: ImageVector,
    expandedIndex: Int,
    onCardClicked: () -> Unit,
    content: @Composable () -> Unit
) {
    val isExpanded = index == expandedIndex
    val rotation by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f)

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCardClicked() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = title, style = MaterialTheme.typography.titleMedium)
                }
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    modifier = Modifier.rotate(rotation)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                content()
            }
        }
    }
}

@Composable
fun DropdownSelector(
    label: String,
    selectedValue: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun FactorTableEditor(
    title: String,
    factorsMap: Map<String, Double>,
    onSave: (Map<String, Double>) -> Unit,
    reference: String
) {
    val draftMap = remember(factorsMap) { mutableStateMapOf<String, String>().apply {
        factorsMap.forEach { (k, v) -> put(k, v.toString()) }
    }}

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            
            factorsMap.keys.forEach { key ->
                val draftVal = draftMap[key] ?: "0.0"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(key, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1.2f))
                    OutlinedTextField(
                        value = draftVal,
                        onValueChange = { draftMap[key] = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(0.8f).height(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(reference, style = MaterialTheme.typography.bodySmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Button(
                onClick = {
                    val finalMap = factorsMap.mapValues { (k, _) ->
                        draftMap[k]?.toDoubleOrNull() ?: factorsMap[k] ?: 0.0
                    }
                    onSave(finalMap)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Factor Changes")
            }
        }
    }
}

@Composable
fun IconButtonWithLabel(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun TextWithPercentBar(
    label: String,
    percent: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
            Text("${String.format("%.1f", percent)}%", style = MaterialTheme.typography.labelSmall, fontSize = 11.sp, color = color)
        }
        LinearProgressIndicator(
            progress = (percent / 100.0).toFloat().coerceIn(0f, 1f),
            color = color,
            trackColor = color.copy(alpha = 0.15f),
            modifier = Modifier.fillMaxWidth().height(8.dp)
        )
    }
}
