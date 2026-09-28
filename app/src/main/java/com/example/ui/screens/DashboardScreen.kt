package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculation.HydroCalculationEngine
import com.example.model.Project
import com.example.ui.components.BarChart
import com.example.ui.components.PieChart
import com.example.ui.components.RadarChart
import com.example.ui.theme.*
import kotlin.math.max

@Composable
fun DashboardScreen(
    project: Project,
    results: HydroCalculationEngine.Results,
    language: String,
    modifier: Modifier = Modifier
) {
    val isIndonesian = language == "Indonesian"

    // Labels translating
    val lblStatus = if (isIndonesian) "Status Airtanah" else "Groundwater Status"
    val lblRisk = if (isIndonesian) "Risiko Intrusi" else "Intrusion Risk"
    val lblRain = if (isIndonesian) "Curah Hujan Tahunan" else "Annual Rainfall"
    val lblRecharge = if (isIndonesian) "Imbuhan Airtanah" else "Groundwater Recharge"
    val lblStorage = if (isIndonesian) "Simpanan Airtanah" else "Groundwater Storage"
    val lblDemand = if (isIndonesian) "Total Kebutuhan" else "Total Demand"
    val lblSafeYield = if (isIndonesian) "Aman Diambil (Safe Yield)" else "Safe Yield Limit"
    val lblBalance = if (isIndonesian) "Neraca Airtanah" else "Groundwater Balance"

    // Map month names
    val months = if (isIndonesian) {
        listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agt", "Sep", "Okt", "Nov", "Des")
    } else {
        listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Flat Geometric Header (no Welcome Card)
        item {
            val initials = remember(project.name) {
                project.name.split(" ")
                    .filter { it.isNotEmpty() }
                    .take(2)
                    .map { it.first().uppercase() }
                    .joinToString("")
                    .ifEmpty { "PR" }
            }
            
            val statusColor = when (results.groundwaterStatus) {
                "Sustainable" -> StatusSafeGreen
                "Warning" -> StatusWarningYellow
                else -> StatusDangerRed
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isIndonesian) "TINJAUAN PROYEK" else "PROJECT OVERVIEW",
                        fontSize = 11.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.5.sp
                    )
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            fontSize = 12.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = project.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(statusColor, CircleShape)
                    )
                    Text(
                        text = "Status: ",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = results.groundwaterStatus,
                        fontSize = 13.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                        color = statusColor
                    )
                }
                
                if (project.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = project.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Critical Status Row (Groundwater status, Intrusion risk)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Status Card
                StatusCard(
                    title = lblStatus,
                    value = results.groundwaterStatus,
                    icon = Icons.Default.CheckCircle,
                    color = when (results.groundwaterStatus) {
                        "Sustainable" -> StatusSafeGreen
                        "Warning" -> StatusWarningYellow
                        else -> StatusDangerRed
                    },
                    modifier = Modifier.weight(1f)
                )

                // Intrusion Risk Card
                StatusCard(
                    title = lblRisk,
                    value = results.intrusionRiskLevel,
                    icon = Icons.Default.Warning,
                    color = when (results.intrusionRiskLevel) {
                        "Low" -> StatusSafeGreen
                        "Moderate" -> StatusWarningYellow
                        else -> StatusDangerRed
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Grid of Key numerical metrics
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = lblRain,
                        value = String.format("%.0f mm/yr", results.annualRainfallMm),
                        icon = Icons.Default.Cloud,
                        subtitle = "Total Vol: ${String.format("%.1f M m³", results.annualRainfallVolM3 / 1_000_000.0)}",
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = lblRecharge,
                        value = String.format("%.0f mm/yr", results.rechargeMm),
                        icon = Icons.Default.Water,
                        subtitle = "Daily: ${String.format("%.0f m³", results.dailyRechargeM3)}",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = lblStorage,
                        value = String.format("%.1f M m³", results.groundwaterStorageM3 / 1_000_000.0),
                        icon = Icons.Default.Layers,
                        subtitle = "Thick: ${project.aquiferThickness} m",
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = lblDemand,
                        value = String.format("%.0f m³/day", results.totalDailyDemandM3),
                        icon = Icons.Default.People,
                        subtitle = "Dom: ${String.format("%.0f L/day", project.domesticDemand)}",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Groundwater Balance Analysis Card (Prominent green/red card like HTML)
        item {
            val descriptionText = if (results.netDailyStorageChangeM3 >= 0) {
                if (isIndonesian) {
                    "Simpanan akuifer meningkat. Tingkat pengambilan saat ini berada dalam batas aman yield lensa air tawar."
                } else {
                    "Aquifer storage is increasing. Current abstraction rates are well within the safe yield limits of the freshwater lens."
                }
            } else {
                if (isIndonesian) {
                    "Simpanan akuifer menurun! Tingkat pengambilan melebihi batas aman yield, berisiko tinggi intrusi air laut."
                } else {
                    "Aquifer storage is decreasing! Abstraction rates exceed the safe yield limits, posing a high risk of saltwater intrusion."
                }
            }

            BalanceAnalysisCard(
                title = lblBalance,
                value = results.netDailyStorageChangeM3,
                isSustainable = results.netDailyStorageChangeM3 >= 0,
                description = descriptionText
            )
        }

        // Safe Yield Limit Card (Prominent white card)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = lblSafeYield.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = String.format("%.0f m³/day", results.safeYieldM3Day),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        Text(
                            text = if (isIndonesian) "Faktor Keamanan: ${String.format("%.0f%%", project.safetyFactor * 100)}" else "Safety Factor: ${String.format("%.0f%%", project.safetyFactor * 100)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Section: Visual Charts
        item {
            Text(
                text = if (isIndonesian) "Visualisasi & Grafik" else "Visualization & Charts",
                fontSize = 11.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        // Demand Pie Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isIndonesian) "Distribusi Kebutuhan Air Harian" else "Daily Water Demand Sector Breakdown",
                        fontSize = 11.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val demandValues = listOf(
                        results.dailyDomesticDemandM3,
                        results.dailyTourismDemandM3,
                        project.agriculturalDemand,
                        project.industrialDemand,
                        project.livestockDemand
                    )
                    val demandLabels = listOf(
                        if (isIndonesian) "Domestik" else "Domestic",
                        if (isIndonesian) "Wisata" else "Tourism",
                        if (isIndonesian) "Pertanian" else "Agriculture",
                        if (isIndonesian) "Industri" else "Industry",
                        if (isIndonesian) "Ternak" else "Livestock"
                    )
                    val demandColors = listOf(
                        Color(0xFF1E88E5), Color(0xFFE53935), Color(0xFF43A047),
                        Color(0xFFFFB300), Color(0xFF8E24AA)
                    )
                    
                    PieChart(
                        data = demandValues,
                        labels = demandLabels,
                        colors = demandColors
                    )
                }
            }
        }

        // Rainfall Bar Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isIndonesian) "Curah Hujan Bulanan (mm)" else "Monthly Rainfall Distribution (mm)",
                        fontSize = 11.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val rainValues = project.monthlyRainfall.split(",").mapNotNull { it.trim().toDoubleOrNull() }
                    if (rainValues.size >= 12) {
                        BarChart(
                            data = rainValues,
                            labels = months
                        )
                    } else {
                        Text(
                            "Invalid monthly rainfall data in project settings.",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Radar Security Spider Web Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isIndonesian) "Sarang Laba-Laba Kamanan Airtanah" else "Hydrogeological Security Risk Web",
                        fontSize = 11.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val f1 = (results.totalDailyPumpingM3 / max(1.0, results.dailyRechargeM3)).coerceIn(0.0, 1.0)
                    val f2 = (results.intrusionRiskScore / 100.0).coerceIn(0.0, 1.0)
                    val f3 = project.runoffCoefficient.coerceIn(0.0, 1.0)
                    val f4 = (results.totalDailyPumpingM3 / max(1.0, results.safeYieldM3Day)).coerceIn(0.0, 1.0)
                    val f5 = (project.artificialRechargeCapacity / 30000.0).coerceIn(0.0, 1.0)

                    val radarValues = listOf(f1, f2, f3, f4, f5)
                    val radarLabels = listOf(
                        if (isIndonesian) "Tekanan Pompa" else "Pumping Ratio",
                        if (isIndonesian) "Risiko Intrusi" else "Intrusion Risk",
                        if (isIndonesian) "Koef. Runoff" else "Runoff Coeff",
                        if (isIndonesian) "Pencapaian SY" else "Safe Yield Ratio",
                        if (isIndonesian) "Imbuhan Buatan" else "Art. Recharge"
                    )

                    RadarChart(values = radarValues, labels = radarLabels)
                }
            }
        }

        // Section: Decision Support System (DSS) Recommendations
        item {
            Text(
                text = if (isIndonesian) "Dukungan Keputusan & Rekomendasi" else "Decision Support & Recommendations",
                fontSize = 11.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        items(results.recommendations) { rec ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val icon = if (rec.contains("CRITICAL") || rec.contains("🚨")) {
                        Icons.Default.Dangerous
                    } else if (rec.contains("⚠️") || rec.contains("DEFICIT") || rec.contains("RISK")) {
                        Icons.Default.Warning
                    } else {
                        Icons.Default.CheckCircle
                    }
                    
                    val tint = if (rec.contains("CRITICAL") || rec.contains("🚨")) {
                        StatusDangerRed
                    } else if (rec.contains("⚠️") || rec.contains("DEFICIT") || rec.contains("RISK")) {
                        StatusWarningYellow
                    } else {
                        StatusSafeGreen
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(tint.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = "Rec Status",
                            tint = tint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = rec,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun BalanceAnalysisCard(
    title: String,
    value: Double,
    isSustainable: Boolean,
    description: String,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSustainable) Color(0xFFE8F5E9) else Color(0xFFFDEEE9)
    val borderColor = if (isSustainable) Color(0xFFC8E6C9) else Color(0xFFFCD3C1)
    val textColor = if (isSustainable) Color(0xFF1B5E20) else Color(0xFFB71C1C)
    val badgeBg = Color.White
    val badgeTextColor = if (isSustainable) Color(0xFF2E7D32) else Color(0xFFD32F2F)
    val badgeText = if (isSustainable) "LOW RISK" else "HIGH RISK"

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = textColor,
                    letterSpacing = 1.sp
                )
                Card(
                    colors = CardDefaults.cardColors(containerColor = badgeBg),
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, borderColor.copy(alpha = 0.8f))
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 10.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = badgeTextColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = String.format("%+.1f", value),
                    fontSize = 36.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Light,
                    color = textColor
                )
                Text(
                    text = "m³/day",
                    fontSize = 14.sp,
                    color = textColor.copy(alpha = 0.8f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = description,
                fontSize = 12.sp,
                color = textColor,
                lineHeight = 16.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
            )
        }
    }
}

@Composable
fun StatusCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    val containerColor = color.copy(alpha = 0.08f)
    val borderColor = color.copy(alpha = 0.3f)
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(color.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
                }
                Text(text = title, fontSize = 11.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    subtitle: String,
    modifier: Modifier = Modifier,
    colorOverride: Color? = null
) {
    val borderColor = MaterialTheme.colorScheme.outline
    val containerColor = MaterialTheme.colorScheme.surfaceVariant
    
    Card(
        modifier = modifier
            .height(110.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title.uppercase(),
                fontSize = 10.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.8.sp
            )
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val valueParts = value.split(" ")
                    val mainVal = valueParts.firstOrNull() ?: ""
                    val unitVal = valueParts.drop(1).joinToString(" ")
                    
                    Text(
                        text = mainVal,
                        fontSize = 18.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = colorOverride ?: MaterialTheme.colorScheme.onSurface
                    )
                    if (unitVal.isNotEmpty()) {
                        Text(
                            text = unitVal,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
