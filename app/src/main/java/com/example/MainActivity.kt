package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calculation.HydroCalculationEngine
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val app = context.applicationContext as Application
            
            // Injects MainViewModel
            val viewModel: MainViewModel = viewModel(
                factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(app)
            )

            val selectedProject by viewModel.selectedProject.collectAsState()
            val activeProjects by viewModel.activeProjects.collectAsState()
            val archivedProjects by viewModel.archivedProjects.collectAsState()
            val backupProjects by viewModel.backupProjects.collectAsState()
            val activeScenario by viewModel.activeScenario.collectAsState()
            val scenarios by viewModel.scenarios.collectAsState()
            val themeMode by viewModel.themeMode.collectAsState()
            val language by viewModel.language.collectAsState()

            val isIndonesian = language == "Indonesian"

            // Choose theme based on settings
            val isDarkTheme = when (themeMode) {
                "Dark" -> true
                "Light" -> false
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                var currentTab by remember { mutableStateOf(0) } // 0=Dashboard, 1=Modules, 2=Scenarios, 3=Map/Report, 4=Projects, 5=Settings
                var showSettingsDialog by remember { mutableStateOf(false) }

                // Calculate results for the active project (and scenario override if selected)
                val results = remember(selectedProject, activeScenario) {
                    selectedProject?.let { HydroCalculationEngine.calculate(it, activeScenario) }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val isTablet = maxWidth > 600.dp

                        Row(modifier = Modifier.fillMaxSize()) {
                            // SIDE NAVIGATION RAIL (For Tablets / Wide screens)
                            if (isTablet) {
                                NavigationRail(
                                    modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("HYDRO", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(16.dp))

                                    listOf(
                                        Triple(0, if (isIndonesian) "Dasbor" else "Dashboard", Icons.Default.Dashboard),
                                        Triple(1, if (isIndonesian) "Modul" else "Modules", Icons.Default.ViewModule),
                                        Triple(2, if (isIndonesian) "Skenario" else "Scenarios", Icons.Default.Compare),
                                        Triple(3, if (isIndonesian) "Peta" else "GIS Map", Icons.Default.Map),
                                        Triple(4, if (isIndonesian) "Proyek" else "Projects", Icons.Default.Folder)
                                    ).forEach { (idx, label, icon) ->
                                        NavigationRailItem(
                                            selected = currentTab == idx,
                                            onClick = { currentTab = idx },
                                            icon = { Icon(icon, contentDescription = label) },
                                            label = { Text(label, fontSize = 10.sp) }
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.weight(1f))

                                    NavigationRailItem(
                                        selected = currentTab == 5,
                                        onClick = { currentTab = 5 },
                                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                        label = { Text(if (isIndonesian) "Pengaturan" else "Settings", fontSize = 10.sp) }
                                    )
                                }
                            }

                            // MAIN CONTENT AREA
                            Scaffold(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                topBar = {
                                    TopAppBar(
                                        title = {
                                            Column {
                                                Text(
                                                    text = selectedProject?.name ?: "No Project Selected",
                                                    style = MaterialTheme.typography.titleMedium
                                                )
                                                if (activeScenario != null) {
                                                    Text(
                                                        text = "Sim: ${activeScenario?.name}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        },
                                        actions = {
                                            // Status Quick Badge on TopBar
                                            results?.let { res ->
                                                Card(
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = when (res.groundwaterStatus) {
                                                            "Sustainable" -> com.example.ui.theme.StatusSafeGreen.copy(alpha = 0.2f)
                                                            "Warning" -> com.example.ui.theme.StatusWarningYellow.copy(alpha = 0.2f)
                                                            else -> com.example.ui.theme.StatusDangerRed.copy(alpha = 0.2f)
                                                        }
                                                    ),
                                                    modifier = Modifier.padding(end = 8.dp)
                                                ) {
                                                    Text(
                                                        text = res.groundwaterStatus.uppercase(),
                                                        fontSize = 10.sp,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        color = when (res.groundwaterStatus) {
                                                            "Sustainable" -> com.example.ui.theme.StatusSafeGreen
                                                            "Warning" -> com.example.ui.theme.StatusWarningYellow
                                                            else -> com.example.ui.theme.StatusDangerRed
                                                        },
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }

                                            if (!isTablet) {
                                                IconButton(onClick = { currentTab = 5 }) {
                                                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                                                }
                                            }
                                        },
                                        colors = TopAppBarDefaults.topAppBarColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        )
                                    )
                                },
                                bottomBar = {
                                    // BOTTOM NAVIGATION BAR (Only shown on Compact screens/phones)
                                    if (!isTablet) {
                                        NavigationBar {
                                            listOf(
                                                Triple(0, if (isIndonesian) "Dasbor" else "Dashboard", Icons.Default.Dashboard),
                                                Triple(1, if (isIndonesian) "Modul" else "Modules", Icons.Default.ViewModule),
                                                Triple(2, if (isIndonesian) "Skenario" else "Scenarios", Icons.Default.Compare),
                                                Triple(3, if (isIndonesian) "Peta" else "GIS Map", Icons.Default.Map),
                                                Triple(4, if (isIndonesian) "Proyek" else "Projects", Icons.Default.Folder)
                                            ).forEach { (idx, label, icon) ->
                                                NavigationBarItem(
                                                    selected = currentTab == idx,
                                                    onClick = { currentTab = idx },
                                                    icon = { Icon(icon, contentDescription = label) },
                                                    label = { Text(label, fontSize = 10.sp) }
                                                )
                                            }
                                        }
                                    }
                                }
                            ) { innerPadding ->
                                if (selectedProject == null) {
                                    // Empty state when no project selected
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(innerPadding),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                Icons.Default.FolderOpen,
                                                contentDescription = null,
                                                modifier = Modifier.size(64.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text("No project selected.", style = MaterialTheme.typography.titleMedium)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Button(onClick = { currentTab = 4 }) {
                                                Text("Open Project Manager")
                                            }
                                        }
                                    }
                                } else {
                                    // Main active screen content router
                                    results?.let { res ->
                                        when (currentTab) {
                                            0 -> DashboardScreen(
                                                project = selectedProject!!,
                                                results = res,
                                                language = language,
                                                modifier = Modifier.padding(innerPadding)
                                            )
                                            1 -> ModulesScreen(
                                                viewModel = viewModel,
                                                project = selectedProject!!,
                                                results = res,
                                                modifier = Modifier.padding(innerPadding)
                                            )
                                            2 -> ScenariosScreen(
                                                viewModel = viewModel,
                                                project = selectedProject!!,
                                                scenarios = scenarios,
                                                activeScenario = activeScenario,
                                                modifier = Modifier.padding(innerPadding)
                                            )
                                            3 -> MapReportScreen(
                                                viewModel = viewModel,
                                                project = selectedProject!!,
                                                results = res,
                                                modifier = Modifier.padding(innerPadding)
                                            )
                                            4 -> ProjectManagerScreen(
                                                viewModel = viewModel,
                                                selectedProject = selectedProject,
                                                activeProjects = activeProjects,
                                                archivedProjects = archivedProjects,
                                                backupProjects = backupProjects,
                                                modifier = Modifier.padding(innerPadding)
                                            )
                                            5 -> SettingsScreen(
                                                viewModel = viewModel,
                                                modifier = Modifier.padding(innerPadding)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
