package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.database.AppDatabase
import com.example.model.Project
import com.example.model.Scenario
import com.example.repository.ProjectRepository
import com.example.settings.PreferenceManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProjectRepository
    val preferenceManager = PreferenceManager(application)

    // Projects Flows
    val activeProjects: StateFlow<List<Project>>
    val archivedProjects: StateFlow<List<Project>>
    val backupProjects: StateFlow<List<Project>>

    // Selected Project & Scenario for calculations
    private val _selectedProject = MutableStateFlow<Project?>(null)
    val selectedProject: StateFlow<Project?> = _selectedProject

    private val _scenarios = MutableStateFlow<List<Scenario>>(emptyList())
    val scenarios: StateFlow<List<Scenario>> = _scenarios

    private val _activeScenario = MutableStateFlow<Scenario?>(null)
    val activeScenario: StateFlow<Scenario?> = _activeScenario

    // UI state
    val unitSystem = preferenceManager.unitSystem
    val themeMode = preferenceManager.themeMode
    val language = preferenceManager.language

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ProjectRepository(db.projectDao())

        activeProjects = repository.activeProjects
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        archivedProjects = repository.archivedProjects
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        backupProjects = repository.backupProjects
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Auto seed and load first active project
        viewModelScope.launch {
            repository.seedDemoIfEmpty()
            // Select first available project
            repository.activeProjects.firstOrNull()?.firstOrNull()?.let {
                selectProject(it)
            }
        }
    }

    fun selectProject(project: Project) {
        _selectedProject.value = project
        _activeScenario.value = null // reset active scenario on project switch
        
        // Load scenarios reactively for this project
        viewModelScope.launch {
            repository.getScenariosForProject(project.id).collect {
                _scenarios.value = it
            }
        }
    }

    fun selectScenario(scenario: Scenario?) {
        _activeScenario.value = scenario
    }

    // Project operations
    fun createProject(name: String, desc: String) {
        viewModelScope.launch {
            val newProj = Project(
                name = name,
                description = desc,
                createdAt = System.currentTimeMillis()
            )
            val newId = repository.insertProject(newProj).toInt()
            // Auto select newly created project
            val insertedProj = newProj.copy(id = newId)
            selectProject(insertedProj)
        }
    }

    fun updateProject(project: Project) {
        viewModelScope.launch {
            repository.updateProject(project)
            if (_selectedProject.value?.id == project.id) {
                _selectedProject.value = project
            }
        }
    }

    fun deleteProject(project: Project) {
        viewModelScope.launch {
            repository.deleteProject(project)
            if (_selectedProject.value?.id == project.id) {
                // Find next available
                val remaining = repository.activeProjects.first().firstOrNull()
                _selectedProject.value = remaining
            }
        }
    }

    fun archiveProject(project: Project) {
        viewModelScope.launch {
            val updated = project.copy(isArchived = true)
            repository.updateProject(updated)
            if (_selectedProject.value?.id == project.id) {
                _selectedProject.value = null
            }
        }
    }

    fun restoreProject(project: Project) {
        viewModelScope.launch {
            val updated = project.copy(isArchived = false)
            repository.updateProject(updated)
            selectProject(updated)
        }
    }

    fun duplicateProject(project: Project) {
        viewModelScope.launch {
            val duplicate = project.copy(
                id = 0,
                name = "${project.name} - Duplicate",
                createdAt = System.currentTimeMillis()
            )
            repository.insertProject(duplicate)
        }
    }

    fun backupProject(project: Project) {
        viewModelScope.launch {
            val backup = project.copy(
                id = 0,
                name = "${project.name} (Backup)",
                isBackup = true,
                createdAt = System.currentTimeMillis()
            )
            repository.insertProject(backup)
        }
    }

    fun restoreFromBackup(backupProject: Project) {
        viewModelScope.launch {
            _selectedProject.value?.let { current ->
                val restored = backupProject.copy(
                    id = current.id,
                    isBackup = false,
                    name = backupProject.name.replace(" (Backup)", ""),
                    createdAt = System.currentTimeMillis()
                )
                repository.updateProject(restored)
                selectProject(restored)
            }
        }
    }

    // Scenario operations
    fun createScenario(
        name: String,
        desc: String,
        rainChange: Double,
        popChange: Double,
        tourismChange: Double,
        pumpChange: Double,
        rechargeChange: Double
    ) {
        _selectedProject.value?.let { project ->
            viewModelScope.launch {
                val scenario = Scenario(
                    projectId = project.id,
                    name = name,
                    description = desc,
                    rainfallChangePct = rainChange,
                    populationGrowthPct = popChange,
                    tourismGrowthPct = tourismChange,
                    pumpingChangePct = pumpChange,
                    artificialRechargeChangePct = rechargeChange
                )
                repository.insertScenario(scenario)
            }
        }
    }

    fun deleteScenario(scenario: Scenario) {
        viewModelScope.launch {
            repository.deleteScenario(scenario)
            if (_activeScenario.value?.id == scenario.id) {
                _activeScenario.value = null
            }
        }
    }

    // CSV Import for Rainfall or well parameters
    fun importRainfallCsv(project: Project, csvText: String): Boolean {
        return try {
            // Standard CSV parsing: split commas, newlines
            val tokens = csvText.split(Regex("[,\\s\n\r]"))
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .mapNotNull { it.toDoubleOrNull() }

            if (tokens.size >= 12) {
                // Select first 12 valid numbers as the monthly rainfall values
                val formattedRainfall = tokens.take(12).joinToString(",") { String.format("%.1f", it) }
                val totalRain = tokens.take(12).sum()
                
                val updatedProj = project.copy(
                    monthlyRainfall = formattedRainfall,
                    annualRainfall = totalRain
                )
                updateProject(updatedProj)
                true
            } else if (tokens.size == 1) {
                // If single value, import as annual rainfall
                val updatedProj = project.copy(
                    annualRainfall = tokens[0]
                )
                updateProject(updatedProj)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    // Preference settings
    fun setUnitSystem(system: String) {
        preferenceManager.setUnitSystem(system)
    }

    fun setThemeMode(mode: String) {
        preferenceManager.setThemeMode(mode)
    }

    fun setLanguage(lang: String) {
        preferenceManager.setLanguage(lang)
    }
}
