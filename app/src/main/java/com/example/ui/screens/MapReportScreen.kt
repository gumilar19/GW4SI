package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculation.HydroCalculationEngine
import com.example.gis.GisMapScreen
import com.example.model.Project
import com.example.report.ReportEngine
import com.example.ui.viewmodel.MainViewModel

@Composable
fun MapReportScreen(
    viewModel: MainViewModel,
    project: Project,
    results: HydroCalculationEngine.Results,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsState()
    val isIndonesian = language == "Indonesian"

    var selectedSubTab by remember { mutableStateOf(0) } // 0 = GIS Map, 1 = Export Report
    var showPrintDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedSubTab) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = { Text(if (isIndonesian) "Peta GIS Interaktif" else "Interactive GIS Map") },
                icon = { Icon(Icons.Default.Map, null) }
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = { Text(if (isIndonesian) "Ekspor Laporan" else "Export Reports") },
                icon = { Icon(Icons.Default.PictureAsPdf, null) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (selectedSubTab) {
            0 -> {
                GisMapScreen(project = project, modifier = Modifier.weight(1f))
            }
            1 -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = if (isIndonesian) "Hasil Kajian Siap Diekspor" else "Assessment Ready for Export",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isIndonesian) {
                                    "Ekspor semua data masukan, parameter akuifer, dan hasil analisis keseimbangan air tanah ke format spreadsheet Excel/CSV atau dokumen PDF."
                                } else {
                                    "Export all climate inputs, aquifer configurations, and groundwater balance calculation sheets into fully formatted Excel/CSV files or system PDFs."
                                },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Available Export Formats", style = MaterialTheme.typography.labelLarge)
                            
                            // CSV Excel Share
                            Button(
                                onClick = {
                                    val csvContent = ReportEngine.exportProjectToCsv(project, results)
                                    ReportEngine.shareCsvReport(context, project, csvContent)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Share, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Export to MS Excel / CSV")
                            }

                            // PDF print trigger
                            Button(
                                onClick = { showPrintDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Print, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Print / Export to PDF Report")
                            }
                        }
                    }

                    // Disclaimer details
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 8.dp))
                            Text(
                                text = "PDF reports utilize Android's native system print spooler, which automatically handles offline layouts and page counts.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // Print Spooler system WebView integration
    if (showPrintDialog) {
        val htmlReport = remember(project, results) { ReportEngine.generateHtmlReport(project, results) }
        
        AlertDialog(
            onDismissRequest = { showPrintDialog = false },
            title = { Text("PDF Print Spooler") },
            text = {
                Text("Send generated groundwater report to system printer or PDF file creator?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPrintDialog = false
                        // Launch system printing flow directly
                        printHtmlContent(context, htmlReport, project.name)
                    }
                ) {
                    Text("Print PDF")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPrintDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Native Android System Print Service for HTML/PDF Report generation
 */
private fun printHtmlContent(context: Context, html: String, projectName: String) {
    val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
    
    // Create temporary invisible WebView to render HTML
    val webView = WebView(context).apply {
        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                // Print Spooler callback
                val printAdapter = createPrintDocumentAdapter("HydroReport_${projectName.replace(" ", "_")}")
                val jobName = "${context.packageName} Groundwater Assessment - $projectName"
                printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
            }
        }
    }
    webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
}
