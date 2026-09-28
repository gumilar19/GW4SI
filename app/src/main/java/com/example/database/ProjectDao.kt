package com.example.database

import androidx.room.*
import com.example.model.Project
import com.example.model.Scenario
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {

    // Projects Queries
    @Query("SELECT * FROM projects ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE isArchived = 0 AND isBackup = 0 ORDER BY createdAt DESC")
    fun getActiveProjects(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE isArchived = 1 AND isBackup = 0 ORDER BY createdAt DESC")
    fun getArchivedProjects(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE isBackup = 1 ORDER BY createdAt DESC")
    fun getBackupProjects(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE id = :id")
    fun getProjectById(id: Int): Flow<Project?>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectByIdOneShot(id: Int): Project?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: Project): Long

    @Update
    suspend fun updateProject(project: Project)

    @Delete
    suspend fun deleteProject(project: Project)

    // Scenarios Queries
    @Query("SELECT * FROM scenarios WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getScenariosForProject(projectId: Int): Flow<List<Scenario>>

    @Query("SELECT * FROM scenarios WHERE id = :id")
    fun getScenarioById(id: Int): Flow<Scenario?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScenario(scenario: Scenario): Long

    @Update
    suspend fun updateScenario(scenario: Scenario)

    @Delete
    suspend fun deleteScenario(scenario: Scenario)
}
