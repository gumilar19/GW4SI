package com.example.report

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.calculation.HydroCalculationEngine
import com.example.model.Project
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportEngine {

    /**
     * Generates a fully-formatted, responsive HTML report.
     * Can be rendered in a WebView or printed directly as a high-fidelity PDF.
     */
    fun generateHtmlReport(project: Project, results: HydroCalculationEngine.Results): String {
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <title>Groundwater Balance Report: ${project.name}</title>
                <style>
                    body {
                        font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
                        color: #333333;
                        line-height: 1.5;
                        padding: 20px;
                        margin: 0;
                        background-color: #ffffff;
                    }
                    .header {
                        border-bottom: 3px solid #0061A4;
                        padding-bottom: 12px;
                        margin-bottom: 24px;
                    }
                    .header h1 {
                        color: #0061A4;
                        margin: 0 0 6px 0;
                        font-size: 26px;
                    }
                    .header p {
                        margin: 0;
                        color: #666666;
                        font-size: 13px;
                    }
                    .status-badge {
                        display: inline-block;
                        padding: 6px 12px;
                        border-radius: 4px;
                        font-weight: bold;
                        font-size: 14px;
                        margin-top: 8px;
                    }
                    .status-sustainable { background-color: #E8F5E9; color: #2E7D32; }
                    .status-warning { background-color: #FFF3E0; color: #EF6C00; }
                    .status-critical { background-color: #FFEBEE; color: #C62828; }
                    
                    .section-title {
                        color: #00497D;
                        border-bottom: 1px solid #DFE2EB;
                        padding-bottom: 6px;
                        margin-top: 30px;
                        margin-bottom: 12px;
                        font-size: 18px;
                    }
                    .grid {
                        display: grid;
                        grid-template-columns: 1fr 1fr;
                        gap: 16px;
                        margin-bottom: 20px;
                    }
                    .card {
                        background-color: #F8F9FA;
                        border: 1px solid #E9ECEF;
                        border-radius: 6px;
                        padding: 12px;
                    }
                    .card-title {
                        font-size: 11px;
                        color: #777777;
                        text-transform: uppercase;
                        margin-bottom: 4px;
                    }
                    .card-value {
                        font-size: 20px;
                        font-weight: bold;
                        color: #001D36;
                    }
                    table {
                        width: 100%;
                        border-collapse: collapse;
                        margin-bottom: 20px;
                        font-size: 13px;
                    }
                    th, td {
                        padding: 8px 12px;
                        text-align: left;
                        border-bottom: 1px solid #E0E0E0;
                    }
                    th {
                        background-color: #F0F4F8;
                        color: #333333;
                    }
                    tr:hover {
                        background-color: #F9FAFB;
                    }
                    .recommendation-list {
                        background-color: #FFFDE7;
                        border-left: 4px solid #FBC02D;
                        padding: 12px 12px 12px 24px;
                        border-radius: 4px;
                    }
                    .recommendation-list li {
                        margin-bottom: 8px;
                        font-size: 13px;
                    }
                    .footer {
                        text-align: center;
                        margin-top: 50px;
                        font-size: 11px;
                        color: #999999;
                        border-top: 1px solid #E0E0E0;
                        padding-top: 12px;
                    }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>${project.name} Groundwater Assessment</h1>
                    <p>Report Generated on: $dateStr | Location: Small Island Aquifer System</p>
                    <div class="status-badge ${
                        when (results.groundwaterStatus) {
                            "Sustainable" -> "status-sustainable"
                            "Warning" -> "status-warning"
                            else -> "status-critical"
                        }
                    }">
                        Groundwater Status: ${results.groundwaterStatus.uppercase()}
                    </div>
                </div>

                <div class="section-title">Key Hydrological Benchmarks</div>
                <div class="grid">
                    <div class="card">
                        <div class="card-title">Annual Rainfall</div>
                        <div class="card-value">${String.format("%.1f", results.annualRainfallMm)} mm/yr</div>
                    </div>
                    <div class="card">
                        <div class="card-title">Annual Recharge</div>
                        <div class="card-value">${String.format("%.1f", results.rechargeMm)} mm/yr</div>
                    </div>
                    <div class="card">
                        <div class="card-title">Freshwater Lens Thickness</div>
                        <div class="card-value">${String.format("%.2f", results.truncatedLensThicknessM)} meters</div>
                    </div>
                    <div class="card">
                        <div class="card-title">Groundwater Abstraction (Pumping)</div>
                        <div class="card-value">${String.format("%.1f", results.totalDailyPumpingM3)} m³/day</div>
                    </div>
                    <div class="card">
                        <div class="card-title">Safe Yield Limit</div>
                        <div class="card-value">${String.format("%.1f", results.safeYieldM3Day)} m³/day</div>
                    </div>
                    <div class="card">
                        <div class="card-title">Daily Net Balance Change</div>
                        <div class="card-value" style="color: ${if (results.netDailyStorageChangeM3 >= 0) "#2E7D32" else "#D84315"}">
                            ${String.format("%.2f", results.netDailyStorageChangeM3)} m³/day
                        </div>
                    </div>
                </div>

                <div class="section-title">AQUIFER & LAND PARAMETERS</div>
                <table>
                    <tr><th>Parameter</th><th>Value</th><th>Description</th></tr>
                    <tr><td>Island Area</td><td>${project.islandArea} km²</td><td>Total landmass surface area</td></tr>
                    <tr><td>Aquifer Thickness</td><td>${project.aquiferThickness} m</td><td>Saturated freshwater aquifer depth</td></tr>
                    <tr><td>Specific Yield (${'$'}S_y${'$'})</td><td>${project.specificYield}</td><td>Drainable porosity ratio</td></tr>
                    <tr><td>Hydraulic Conductivity (${'$'}K${'$'})</td><td>${project.hydraulicConductivity} m/day</td><td>Transmission velocity constant</td></tr>
                    <tr><td>Recharge Estimation Method</td><td>${project.rechargeMethod}</td><td>Method applied to calculate infiltration</td></tr>
                    <tr><td>Water Table Height</td><td>${project.waterTable} m</td><td>Hydraulic head above sea level</td></tr>
                    <tr><td>Electrical Conductivity</td><td>${project.electricalConductivity} µS/cm</td><td>Soluble mineral salinity indicator</td></tr>
                </table>

                <div class="section-title">POPULATION & WATER DEMAND BREAKDOWN</div>
                <table>
                    <tr><th>Sector</th><th>Quantity / Factor</th><th>Calculated Demand (m³/day)</th></tr>
                    <tr><td>Domestic Demand</td><td>${project.population} people @ ${project.domesticDemand} L/day</td><td>${String.format("%.2f", results.dailyDomesticDemandM3)}</td></tr>
                    <tr><td>Tourism Demand</td><td>${project.tourismHotels} hotels @ ${project.touristsPerDay} tourists/day</td><td>${String.format("%.2f", results.dailyTourismDemandM3)}</td></tr>
                    <tr><td>Industrial & Agricultural</td><td>Fixed sectors</td><td>${String.format("%.2f", results.dailyOtherDemandM3)}</td></tr>
                    <tr style="font-weight: bold; background-color: #F8F9FA;"><td>Total Demand</td><td>-</td><td>${String.format("%.2f", results.totalDailyDemandM3)}</td></tr>
                </table>

                <div class="section-title">SEAWATER INTRUSION RISK INDEX</div>
                <div class="card" style="margin-bottom: 20px;">
                    <div class="card-title">Calculated Ghyben-Herzberg Risk Rating</div>
                    <div class="card-value" style="color: ${
                        when (results.intrusionRiskLevel) {
                            "Low" -> "#2E7D32"
                            "Moderate" -> "#EF6C00"
                            else -> "#C62828"
                        }
                    }">${results.intrusionRiskLevel.uppercase()} (Score: ${results.intrusionRiskScore}/100)</div>
                </div>

                <div class="section-title">DECISION SUPPORT & MANAGEMENT ADVICE</div>
                <div class="recommendation-list">
                    <ul style="margin: 0; padding-left: 12px;">
                        ${results.recommendations.joinToString("") { "<li>$it</li>" }}
                    </ul>
                </div>

                <div class="footer">
                    <p>Groundwater Balance For Small Islands Tool | Developed by Scientific Hydrology Engineers</p>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    /**
     * Exports all project settings and results into a perfectly parsed CSV spreadsheet.
     */
    fun exportProjectToCsv(project: Project, results: HydroCalculationEngine.Results): String {
        val sb = StringBuilder()
        
        sb.append("GROUNDWATER BALANCE FOR SMALL ISLANDS - HYDROLOGICAL EXPORT\n")
        sb.append("Project Name,${project.name}\n")
        sb.append("Description,${project.description}\n")
        sb.append("Status,${results.groundwaterStatus}\n")
        sb.append("Generated Date,${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}\n\n")

        sb.append("CLIMATE DATA\n")
        sb.append("Annual Rainfall (mm/year),${project.annualRainfall}\n")
        sb.append("Temperature (C),${project.temperature}\n")
        sb.append("Potential ET (mm/year),${project.potentialET}\n")
        sb.append("Rainy Days,${project.rainyDays}\n\n")

        sb.append("AQUIFER PARAMETERS\n")
        sb.append("Island Area (km2),${project.islandArea}\n")
        sb.append("Aquifer Thickness (m),${project.aquiferThickness}\n")
        sb.append("Specific Yield,${project.specificYield}\n")
        sb.append("Hydraulic Conductivity (m/day),${project.hydraulicConductivity}\n")
        sb.append("Aquifer Type,${project.aquiferType}\n\n")

        sb.append("CALCULATED RESULTS\n")
        sb.append("Annual Rainfall Volumetric (m3/year),${results.annualRainfallVolM3}\n")
        sb.append("Calculated Recharge (mm/year),${results.rechargeMm}\n")
        sb.append("Volumetric Recharge (m3/year),${results.rechargeVolM3}\n")
        sb.append("Daily Recharge (m3/day),${results.dailyRechargeM3}\n")
        sb.append("Aquifer Storage capacity (m3),${results.groundwaterStorageM3}\n")
        sb.append("Safe Yield Limit (m3/day),${results.safeYieldM3Day}\n")
        sb.append("Daily Groundwater Abstraction (m3/day),${results.totalDailyPumpingM3}\n")
        sb.append("Daily Outflow Outwards (m3/day),${results.dailyOutflowM3}\n")
        sb.append("Net Daily Storage Change (m3/day),${results.netDailyStorageChangeM3}\n")
        sb.append("Seawater Intrusion Risk level,${results.intrusionRiskLevel}\n")
        sb.append("Seawater Intrusion Risk score (100),${results.intrusionRiskScore}\n")

        return sb.toString()
    }

    /**
     * Saves the CSV string to a shareable file in the external files directory.
     */
    fun shareCsvReport(context: Context, project: Project, csvData: String) {
        try {
            val fileName = "HydroReport_${project.name.replace(" ", "_")}.csv"
            val file = File(context.cacheDir, fileName)
            file.writeText(csvData)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "Groundwater Report: ${project.name}")
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Export Excel/CSV via"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
