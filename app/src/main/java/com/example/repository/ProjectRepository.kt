package com.example.repository

import com.example.database.ProjectDao
import com.example.model.Project
import com.example.model.Scenario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class ProjectRepository(private val projectDao: ProjectDao) {

    val activeProjects: Flow<List<Project>> = projectDao.getActiveProjects()
    val archivedProjects: Flow<List<Project>> = projectDao.getArchivedProjects()
    val backupProjects: Flow<List<Project>> = projectDao.getBackupProjects()
    val allProjects: Flow<List<Project>> = projectDao.getAllProjects()

    fun getProjectById(id: Int): Flow<Project?> = projectDao.getProjectById(id)

    suspend fun insertProject(project: Project): Long = projectDao.insertProject(project)

    suspend fun updateProject(project: Project) = projectDao.updateProject(project)

    suspend fun deleteProject(project: Project) = projectDao.deleteProject(project)

    // Scenarios
    fun getScenariosForProject(projectId: Int): Flow<List<Scenario>> =
        projectDao.getScenariosForProject(projectId)

    fun getScenarioById(id: Int): Flow<Scenario?> = projectDao.getScenarioById(id)

    suspend fun insertScenario(scenario: Scenario): Long = projectDao.insertScenario(scenario)

    suspend fun updateScenario(scenario: Scenario) = projectDao.updateScenario(scenario)

    suspend fun deleteScenario(scenario: Scenario) = projectDao.deleteScenario(scenario)

    /**
     * Seeds the database with the pre-populated "Demo Small Island" if empty.
     */
    suspend fun seedDemoIfEmpty() {
        val projects = projectDao.getActiveProjects().firstOrNull()
        if (projects.isNullOrEmpty()) {
            val demoProject = Project(
                id = 0,
                name = "Demo Small Island",
                description = "Pulau Harapan hydrogeological assessment. Populated with realistic island parameters.",
                isArchived = false,
                isBackup = false,
                
                // Climate Data
                annualRainfall = 1850.0,
                monthlyRainfall = "150,180,200,120,100,80,60,70,110,130,220,230",
                temperature = 28.3,
                potentialET = 1320.0,
                relativeHumidity = 82.0,
                rainyDays = 118,

                // Aquifer Data
                islandArea = 22.5,
                aquiferThickness = 32.0,
                specificYield = 0.18,
                hydraulicConductivity = 14.0,
                storageCoefficient = 0.18,
                porosity = 0.30,
                aquiferType = "Unconfined",

                // Land Characteristics
                landUse = "Forest/Agricultural Mix",
                soilType = "Sandy Clay",
                slope = 3.5,
                rechargeCoefficient = 0.31,
                runoffCoefficient = 0.22,
                vegetationCover = 65.0,

                // Population
                population = 6250,
                annualGrowth = 1.4,
                domesticDemand = 110.0,
                tourismHotels = 12,
                touristsPerDay = 210.0,
                tourismDemand = 150.0,
                industrialDemand = 40.0,
                agriculturalDemand = 120.0,
                livestockDemand = 15.0,

                // Abstraction
                domesticWellsCount = 4,
                industrialWellsCount = 1,
                hotelWellsCount = 2,
                agriculturalWellsCount = 1,
                governmentWellsCount = 1,
                publicSupplyWellsCount = 1,
                totalPumping = 711.0,
                monthlyAbstraction = "680,690,710,720,700,711,730,715,705,710,725,720",

                // Recharge Config
                rechargeMethod = "Coefficient", // CMB is also available, defaults to coefficient
                chlorideRain = 8.2,
                chlorideGroundwater = 42.0,
                userDefinedFormula = "0.31 * Rainfall",

                // Safe Yield
                safetyFactor = 0.50,

                // Seawater Intrusion
                waterTable = 1.85,
                electricalConductivity = 720.0,
                tds = 410.0,
                groundwaterOutflow = 250.0,

                // Artificial Recharge
                artificialRechargeWells = 15,
                artificialInfiltrationPonds = 4,
                artificialRechargeCapacity = 18000.0
            )

            val projectId = projectDao.insertProject(demoProject).toInt()

            // Seed a default management scenario for comparison
            val defaultScenario = Scenario(
                id = 0,
                projectId = projectId,
                name = "Scenario A: Population & Climate Stress",
                description = "Simulates a 15% rainfall reduction (drought) combined with a 20% population and tourism increase.",
                rainfallChangePct = -15.0,
                populationGrowthPct = 20.0,
                tourismGrowthPct = 20.0,
                pumpingChangePct = 15.0,
                artificialRechargeChangePct = 0.0,
                safetyFactorOverride = 0.50
            )
            val rechargeScenario = Scenario(
                id = 0,
                projectId = projectId,
                name = "Scenario B: Artificial Recharge Expansion",
                description = "Simulates expanding infiltration capacity by 100% and reducing abstraction by 10% to secure groundwater.",
                rainfallChangePct = 0.0,
                populationGrowthPct = 10.0,
                tourismGrowthPct = 10.0,
                pumpingChangePct = -10.0,
                artificialRechargeChangePct = 100.0,
                safetyFactorOverride = 0.50
            )
            projectDao.insertScenario(defaultScenario)
            projectDao.insertScenario(rechargeScenario)
        }
    }
}
