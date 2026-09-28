package com.example.calculation

import com.example.model.Project
import com.example.model.Scenario
import kotlin.math.max
import kotlin.math.min

/**
 * High-precision Hydrogeological Calculation Engine for small islands.
 * Standard equations from Bear (1979), Ghyben-Herzberg, and Chloride Mass Balance.
 */
object HydroCalculationEngine {

    /**
     * Holds the combined result of all hydrological calculations for a project or scenario.
     */
    data class Results(
        val annualRainfallMm: Double,
        val annualRainfallVolM3: Double,
        val rechargeMm: Double,
        val rechargeVolM3: Double,
        val dailyRechargeM3: Double,
        val groundwaterStorageM3: Double,
        val dailyDomesticDemandM3: Double,
        val dailyTourismDemandM3: Double,
        val dailyOtherDemandM3: Double,
        val totalDailyDemandM3: Double,
        val totalDailyPumpingM3: Double,
        val dailyArtificialRechargeM3: Double,
        val dailyOutflowM3: Double,
        val netDailyStorageChangeM3: Double,
        val safeYieldM3Day: Double,
        val pumpingToSafeYieldRatio: Double,
        val theoreticalLensThicknessM: Double,
        val truncatedLensThicknessM: Double,
        val intrusionRiskScore: Int,
        val intrusionRiskLevel: String, // Low, Moderate, High, Very High
        val groundwaterStatus: String, // Sustainable, Warning, Critical
        val recommendations: List<String>,
        val computedRc: Double = 0.31,
        val dailyIndustrialDemandM3: Double = 0.0,
        val dailyAgDemandM3: Double = 0.0,
        val dailyLivestockDemandM3: Double = 0.0
    )

    /**
     * Performs calculations for a project, optionally applying a scenario's overrides.
     */
    fun calculate(project: Project, scenario: Scenario? = null): Results {
        // 1. Apply Scenario Overrides (instant update)
        val rainMultiplier = 1.0 + ((scenario?.rainfallChangePct ?: 0.0) / 100.0)
        val popMultiplier = 1.0 + ((scenario?.populationGrowthPct ?: 0.0) / 100.0)
        val tourismMultiplier = 1.0 + ((scenario?.tourismGrowthPct ?: 0.0) / 100.0)
        val pumpingMultiplier = 1.0 + ((scenario?.pumpingChangePct ?: 0.0) / 100.0)
        val artRechargeMultiplier = 1.0 + ((scenario?.artificialRechargeChangePct ?: 0.0) / 100.0)

        val safetyFactor = scenario?.safetyFactorOverride ?: project.safetyFactor

        // Modifying climate
        val rainfallMm = project.annualRainfall * rainMultiplier
        val temperature = project.temperature // temperature doesn't change directly in simple multipliers
        val potentialET = project.potentialET

        // Volumetric Rainfall: Rainfall (mm) * Area (km2) * 1000
        val islandAreaM2 = project.islandArea * 1_000_000.0
        val rainfallVolM3 = (rainfallMm / 1000.0) * islandAreaM2

        // Module 3: Land Characteristics & Weighted Recharge Coefficient (Mode A vs Mode B)
        val luFactors = com.example.model.ProjectParser.parseFactorTable(project.landUseFactorsString)
        val soilFactors = com.example.model.ProjectParser.parseFactorTable(project.soilFactorsString)
        val slopeFactors = com.example.model.ProjectParser.parseFactorTable(project.slopeFactorsString)
        val vegFactors = com.example.model.ProjectParser.parseFactorTable(project.vegetationFactorsString)

        val landUnits = com.example.model.ProjectParser.parseLandUnits(project.landUnitsString)
        
        val computedRc = if (project.rechargeMode == "Automatic" && landUnits.isNotEmpty()) {
            var weightedRcSum = 0.0
            var totalArea = 0.0
            landUnits.forEach { unit ->
                val flu = luFactors[unit.landUse] ?: 0.50
                val fsoil = soilFactors[unit.soilType] ?: 0.50
                val fslope = slopeFactors[unit.slope] ?: 0.50
                val fveg = vegFactors[unit.vegetation] ?: 0.50
                
                val unitRc = project.weightLU * flu + project.weightSOIL * fsoil + project.weightSLOPE * fslope + project.weightVEG * fveg
                weightedRcSum += unitRc * unit.area
                totalArea += unit.area
            }
            if (totalArea > 0.0) weightedRcSum / totalArea else project.rechargeCoefficient
        } else {
            project.rechargeCoefficient
        }

        // 2. Recharge calculation based on active method
        val rechargeMm = when (project.rechargeMethod) {
            "Coefficient" -> {
                rainfallMm * computedRc
            }
            "WaterBalance" -> {
                val runoff = rainfallMm * project.runoffCoefficient
                max(0.0, rainfallMm - potentialET - runoff)
            }
            "ChlorideMassBalance" -> {
                if (project.chlorideGroundwater > 0) {
                    (project.chlorideRain / project.chlorideGroundwater) * rainfallMm
                } else {
                    rainfallMm * computedRc
                }
            }
            "UserDefined" -> {
                evalUserFormula(project.userDefinedFormula, rainfallMm)
            }
            else -> rainfallMm * computedRc
        }

        val rechargeVolM3 = (rechargeMm / 1000.0) * islandAreaM2
        val dailyRechargeM3 = rechargeVolM3 / 365.0

        // 3. Storage calculation: Area (m2) * Thickness (m) * Specific Yield
        val groundwaterStorageM3 = islandAreaM2 * project.aquiferThickness * project.specificYield

        // 4. Demand calculations (m3/day)
        val currentPopulation = (project.population * popMultiplier).toInt()
        val dailyDomesticDemandM3 = (currentPopulation * project.domesticDemand) / 1000.0

        val currentTourists = project.touristsPerDay * tourismMultiplier
        val dailyTourismDemandM3 = (currentTourists * project.tourismDemand) / 1000.0

        // Module 4: New Unlimited Industries demand
        val industriesList = com.example.model.ProjectParser.parseIndustries(project.industriesString)
        val dailyIndustrialDemandM3 = industriesList.sumOf { it.dailyDemand }

        // Module 4: Agricultural demand based on Irrigated Area & Water Requirement
        val annualAgDemandM3 = project.cropIrrigatedArea * project.cropWaterRequirement
        val dailyAgDemandM3 = annualAgDemandM3 / 365.0

        // Module 4: Livestock demand based on animal counts and water consumption
        val livestockList = com.example.model.ProjectParser.parseLivestock(project.livestockString)
        val dailyLivestockDemandM3 = livestockList.sumOf { (it.count * it.demandPerAnimal) / 1000.0 }

        val dailyOtherDemandM3 = dailyIndustrialDemandM3 + dailyAgDemandM3 + dailyLivestockDemandM3
        val totalDailyDemandM3 = dailyDomesticDemandM3 + dailyTourismDemandM3 + dailyOtherDemandM3

        // 5. Abstraction / Pumping (m3/day)
        // Automatically use the calculated Total Groundwater Demand as groundwater abstraction (Qpump)
        val totalDailyPumpingM3 = totalDailyDemandM3 * pumpingMultiplier

        // 6. Artificial Recharge (m3/day)
        val dailyArtificialRechargeM3 = (project.artificialRechargeCapacity * artRechargeMultiplier) / 365.0

        // 7. Natural Outflow (SGD - Submarine Groundwater Discharge)
        val dailyOutflowM3 = project.groundwaterOutflow

        // 8. Storage Change / Balance (m3/day)
        // Recharge + ArtRecharge - Pumping - Outflow
        val netDailyStorageChangeM3 = dailyRechargeM3 + dailyArtificialRechargeM3 - totalDailyPumpingM3 - dailyOutflowM3

        // 9. Safe Yield (m3/day)
        val safeYieldM3Day = dailyRechargeM3 * safetyFactor
        val pumpingToSafeYieldRatio = if (safeYieldM3Day > 0) totalDailyPumpingM3 / safeYieldM3Day else 0.0

        // 10. Freshwater Lens Thickness (Ghyben-Herzberg)
        val theoreticalLensThicknessM = project.waterTable * 41.0
        val truncatedLensThicknessM = min(project.aquiferThickness, theoreticalLensThicknessM)

        // 11. Seawater Intrusion Risk Index
        var riskScore = 0
        
        // Criteria A: Pumping / Recharge ratio (Weight: 40 points)
        val pumpingRechargeRatio = if (dailyRechargeM3 > 0) totalDailyPumpingM3 / dailyRechargeM3 else 0.0
        riskScore += when {
            pumpingRechargeRatio > 0.8 -> 40
            pumpingRechargeRatio > 0.5 -> 28
            pumpingRechargeRatio > 0.2 -> 16
            else -> 6
        }

        // Criteria B: Water Table Height (Weight: 30 points)
        riskScore += when {
            project.waterTable < 0.5 -> 30
            project.waterTable < 1.0 -> 20
            project.waterTable < 1.5 -> 10
            else -> 3
        }

        // Criteria C: Salinity (EC & TDS) (Weight: 30 points)
        riskScore += when {
            project.electricalConductivity > 1500 || project.tds > 1000 -> 30
            project.electricalConductivity > 1000 || project.tds > 700 -> 18
            project.electricalConductivity > 500 || project.tds > 350 -> 8
            else -> 2
        }

        val intrusionRiskLevel = when {
            riskScore >= 70 -> "Very High"
            riskScore >= 45 -> "High"
            riskScore >= 25 -> "Moderate"
            else -> "Low"
        }

        // 12. Overall Sustainability Status
        val groundwaterStatus = when {
            pumpingToSafeYieldRatio > 1.0 || netDailyStorageChangeM3 < -100.0 || intrusionRiskLevel == "Very High" -> "Critical"
            pumpingToSafeYieldRatio > 0.75 || netDailyStorageChangeM3 < 0.0 || intrusionRiskLevel == "High" -> "Warning"
            else -> "Sustainable"
        }

        // 13. Dynamic Recommendations
        val recommendations = mutableListOf<String>()

        if (totalDailyPumpingM3 > safeYieldM3Day) {
            recommendations.add("🚨 CRITICAL: Groundwater extraction exceeds the Safe Yield! Implement daily pumping limits and quotas immediately.")
        }
        if (netDailyStorageChangeM3 < 0) {
            recommendations.add("⚠️ DEFICIT: Groundwater storage is in net decline. Reduce coastal well pumping and scale up artificial recharge methods.")
        }
        if (intrusionRiskLevel == "Very High" || intrusionRiskLevel == "High") {
            recommendations.add("🧂 SALINITY RISK: High risk of seawater intrusion! Restructure pumping networks away from coastlines and establish skimming wells.")
        }
        if (project.electricalConductivity > 1000) {
            recommendations.add("📈 SALINITY ALERT: Elevated Electrical Conductivity (${project.electricalConductivity} µS/cm) detected. Increase testing frequency of monitoring wells.")
        }
        if (dailyTourismDemandM3 > (0.15 * totalDailyDemandM3)) {
            recommendations.add("🏨 TOURISM PRESSURE: Tourism accounts for over 15% of demand. Mandate greywater recycling and low-flow fixtures at hotels.")
        }
        if (computedRc < 0.25) {
            recommendations.add("🌧️ LOW RECHARGE: Low natural recharge coefficient (${String.format("%.2f", computedRc)}). Construct infiltration ponds and run-off harvesting basins.")
        }
        if (project.artificialRechargeCapacity < 5000.0) {
            recommendations.add("💧 ARTIFICIAL RECHARGE: Increase artificial groundwater recharge capacity (current: ${project.artificialRechargeCapacity} m³/year) using recharge wells to bolster the freshwater lens.")
        }
        if (recommendations.isEmpty()) {
            recommendations.add("✅ SUSTAINABLE: The aquifer is within healthy boundaries. Continue routine monthly water table monitoring and salinity tracking.")
        }

        return Results(
            annualRainfallMm = rainfallMm,
            annualRainfallVolM3 = rainfallVolM3,
            rechargeMm = rechargeMm,
            rechargeVolM3 = rechargeVolM3,
            dailyRechargeM3 = dailyRechargeM3,
            groundwaterStorageM3 = groundwaterStorageM3,
            dailyDomesticDemandM3 = dailyDomesticDemandM3,
            dailyTourismDemandM3 = dailyTourismDemandM3,
            dailyOtherDemandM3 = dailyOtherDemandM3,
            totalDailyDemandM3 = totalDailyDemandM3,
            totalDailyPumpingM3 = totalDailyPumpingM3,
            dailyArtificialRechargeM3 = dailyArtificialRechargeM3,
            dailyOutflowM3 = dailyOutflowM3,
            netDailyStorageChangeM3 = netDailyStorageChangeM3,
            safeYieldM3Day = safeYieldM3Day,
            pumpingToSafeYieldRatio = pumpingToSafeYieldRatio,
            theoreticalLensThicknessM = theoreticalLensThicknessM,
            truncatedLensThicknessM = truncatedLensThicknessM,
            intrusionRiskScore = riskScore,
            intrusionRiskLevel = intrusionRiskLevel,
            groundwaterStatus = groundwaterStatus,
            recommendations = recommendations,
            computedRc = computedRc,
            dailyIndustrialDemandM3 = dailyIndustrialDemandM3,
            dailyAgDemandM3 = dailyAgDemandM3,
            dailyLivestockDemandM3 = dailyLivestockDemandM3
        )
    }

    /**
     * Helper to safely evaluate formula like "0.31 * Rainfall"
     */
    private fun evalUserFormula(formula: String, rainfall: Double): Double {
        val sanitized = formula.lowercase().replace("rainfall", "").replace("*", "").trim()
        val factor = sanitized.toDoubleOrNull() ?: 0.31
        return if (factor > 1.0 && factor <= 100.0) {
            (factor / 100.0) * rainfall
        } else if (factor > 0.0 && factor <= 1.0) {
            factor * rainfall
        } else {
            0.31 * rainfall
        }
    }
}
