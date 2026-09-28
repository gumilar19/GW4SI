package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Project
import com.example.ui.viewmodel.MainViewModel

@Composable
fun ProjectManagerScreen(
    viewModel: MainViewModel,
    selectedProject: Project?,
    activeProjects: List<Project>,
    archivedProjects: List<Project>,
    backupProjects: List<Project>,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val isIndonesian = language == "Indonesian"

    var selectedSubTab by remember { mutableStateOf(0) } // 0 = Active, 1 = Archived, 2 = Backups
    var showCreateDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedSubTab) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = { Text(if (isIndonesian) "Aktif" else "Active") },
                icon = { Icon(Icons.Default.Folder, null) }
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = { Text(if (isIndonesian) "Arsip" else "Archived") },
                icon = { Icon(Icons.Default.Archive, null) }
            )
            Tab(
                selected = selectedSubTab == 2,
                onClick = { selectedSubTab = 2 },
                text = { Text(if (isIndonesian) "Cadangan" else "Backups") },
                icon = { Icon(Icons.Default.SettingsBackupRestore, null) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
            when (selectedSubTab) {
                0 -> { // Active Projects
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(activeProjects) { proj ->
                            val isSelected = selectedProject?.id == proj.id
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectProject(proj) },
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
                                            text = proj.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = proj.description.ifEmpty { "No description provided." },
                                            fontSize = 11.sp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(onClick = { viewModel.duplicateProject(proj) }) {
                                            Icon(Icons.Default.ContentCopy, "Duplicate", modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(onClick = { viewModel.backupProject(proj) }) {
                                            Icon(Icons.Default.CloudUpload, "Backup", modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(onClick = { viewModel.archiveProject(proj) }) {
                                            Icon(Icons.Default.Archive, "Archive", modifier = Modifier.size(18.dp))
                                        }
                                        if (proj.name != "Demo Small Island") {
                                            IconButton(onClick = { viewModel.deleteProject(proj) }) {
                                                Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> { // Archived Projects
                    if (archivedProjects.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "No archived projects found.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(archivedProjects) { proj ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(proj.name, style = MaterialTheme.typography.titleSmall)
                                            Text(proj.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Row {
                                            IconButton(onClick = { viewModel.restoreProject(proj) }) {
                                                Icon(Icons.Default.Unarchive, "Restore", tint = MaterialTheme.colorScheme.primary)
                                            }
                                            IconButton(onClick = { viewModel.deleteProject(proj) }) {
                                                Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> { // Backups (Recovery)
                    if (backupProjects.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "No backup points found.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(backupProjects) { proj ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(proj.name, style = MaterialTheme.typography.titleSmall)
                                            Text("Backup Created Offline", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Row {
                                            IconButton(onClick = { viewModel.restoreFromBackup(proj) }) {
                                                Icon(Icons.Default.SettingsBackupRestore, "Restore / Recover", tint = MaterialTheme.colorScheme.primary)
                                            }
                                            IconButton(onClick = { viewModel.deleteProject(proj) }) {
                                                Icon(Icons.Default.Delete, "Delete Backup", tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Floating Add Action Button on bottom of Active Tab
            if (selectedSubTab == 0) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, "Create Project")
                }
            }
        }
    }

    // New Project Dialog
    if (showCreateDialog) {
        var pName by remember { mutableStateOf("") }
        var pDesc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create New Project") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = pName,
                        onValueChange = { pName = it },
                        label = { Text("Island Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pDesc,
                        onValueChange = { pDesc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pName.isNotEmpty()) {
                            viewModel.createProject(pName, pDesc)
                            showCreateDialog = false
                        }
                    }
                ) {
                    Text("Create")
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
