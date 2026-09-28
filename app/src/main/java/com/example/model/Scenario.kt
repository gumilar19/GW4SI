package com.example.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "scenarios",
    foreignKeys = [
        ForeignKey(
            entity = Project::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Scenario(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectId: Int,
    val name: String,
    val description: String = "",
    val rainfallChangePct: Double = 0.0,      // e.g., -10.0 for 10% decrease
    val populationGrowthPct: Double = 0.0,    // e.g., +5.0 for 5% growth
    val tourismGrowthPct: Double = 0.0,       // e.g., +20.0%
    val pumpingChangePct: Double = 0.0,       // e.g., +15.0%
    val artificialRechargeChangePct: Double = 0.0, // e.g., +50.0%
    val safetyFactorOverride: Double? = null,  // can override safety factor (e.g. 0.40)
    val createdAt: Long = System.currentTimeMillis()
)
