package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String = "",
    val isArchived: Boolean = false,
    val isBackup: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),

    // Climate Data
    val annualRainfall: Double = 1850.0,
    val monthlyRainfall: String = "150,180,200,120,100,80,60,70,110,130,220,230", // 12 months in mm, comma-separated
    val temperature: Double = 28.3,
    val potentialET: Double = 1320.0,
    val relativeHumidity: Double = 82.0,
    val rainyDays: Int = 118,

    // Aquifer Data
    val islandArea: Double = 22.5, // km2
    val aquiferThickness: Double = 32.0, // m
    val specificYield: Double = 0.18,
    val hydraulicConductivity: Double = 14.0, // m/day
    val storageCoefficient: Double = 0.18,
    val porosity: Double = 0.30,
    val aquiferType: String = "Unconfined", // Unconfined, Confined, Semi-confined

    // Land Characteristics
    val landUse: String = "Forest/Agricultural Mix",
    val soilType: String = "Sandy Clay",
    val slope: Double = 3.5, // %
    val rechargeCoefficient: Double = 0.31,
    val runoffCoefficient: Double = 0.22,
    val vegetationCover: Double = 65.0, // %

    // Population & Demand
    val population: Int = 6250,
    val annualGrowth: Double = 1.4, // %
    val domesticDemand: Double = 110.0, // L/person/day
    val tourismHotels: Int = 12,
    val touristsPerDay: Double = 210.0,
    val tourismDemand: Double = 150.0, // L/tourist/day
    val industrialDemand: Double = 40.0, // m3/day
    val agriculturalDemand: Double = 120.0, // m3/day
    val livestockDemand: Double = 15.0, // m3/day

    // Groundwater Abstraction
    val domesticWellsCount: Int = 4,
    val industrialWellsCount: Int = 1,
    val hotelWellsCount: Int = 2,
    val agriculturalWellsCount: Int = 1,
    val governmentWellsCount: Int = 1,
    val publicSupplyWellsCount: Int = 1,
    val totalPumping: Double = 711.0, // m3/day
    val monthlyAbstraction: String = "680,690,710,720,700,711,730,715,705,710,725,720", // 12 months in m3/day, comma-separated

    // Recharge Config
    val rechargeMethod: String = "Coefficient", // Coefficient, WaterBalance, ChlorideMassBalance, UserDefined
    val chlorideRain: Double = 8.2, // mg/L
    val chlorideGroundwater: Double = 42.0, // mg/L
    val userDefinedFormula: String = "0.31 * Rainfall",

    // Safe Yield
    val safetyFactor: Double = 0.50, // 0.4, 0.5, 0.6, 0.7

    // Seawater Intrusion
    val waterTable: Double = 1.85, // m above sea level
    val electricalConductivity: Double = 720.0, // uS/cm
    val tds: Double = 410.0, // mg/L
    val groundwaterOutflow: Double = 250.0, // m3/day

    // Artificial Recharge
    val artificialRechargeWells: Int = 15,
    val artificialInfiltrationPonds: Int = 4,
    val artificialRechargeCapacity: Double = 18000.0, // m3/year

    // Module 3: Land Characteristics & Recharge Coefficient Engine
    val rechargeMode: String = "Automatic", // "Automatic" or "Manual"
    val weightLU: Double = 0.35,
    val weightSOIL: Double = 0.30,
    val weightSLOPE: Double = 0.20,
    val weightVEG: Double = 0.15,
    val landUnitsString: String = "Land Unit 1|5.2|Primary Forest|Sand|2–8 %|Dense Forest|lu_1;Land Unit 2|2.3|Settlement|Clay|0–2 %|Sparse Vegetation|lu_2",
    
    // Editable Factor tables in serialized formats (Format: Key:Value,Key:Value...)
    val landUseFactorsString: String = "Primary Forest:1.0,Secondary Forest:0.9,Mangrove:0.85,Shrub:0.75,Grassland:0.6,Agriculture:0.7,Rice Field:0.65,Plantation:0.7,Settlement:0.35,Commercial Area:0.25,Industrial Area:0.2,Airport:0.1,Bare Land:0.4,Beach Sand:0.8,Rock Outcrop:0.15",
    val soilFactorsString: String = "Sand:0.95,Gravel:1.0,Sandy Loam:0.8,Loam:0.65,Silt:0.45,Clay Loam:0.3,Clay:0.15,Coral Sand:0.92,Volcanic Soil:0.85,Limestone:0.75",
    val slopeFactorsString: String = "0–2 %:1.0,2–8 %:0.9,8–15 %:0.75,15–25 %:0.55,25–40 %:0.35,> 40 %:0.2",
    val vegetationFactorsString: String = "Dense Forest:1.0,Moderate Forest:0.85,Plantation:0.7,Grassland:0.6,Cropland:0.5,Sparse Vegetation:0.35,No Vegetation:0.15",

    // Module 4: Population & Demand Engine
    val cropIrrigatedArea: Double = 120.0, // ha
    val cropWaterRequirement: Double = 5600.0, // m3/ha/year
    val industriesString: String = "Ice Factory|15.0|ind_1;Fish Processing|25.0|ind_2",
    val livestockString: String = "Cattle|430|50.0;Goat|120|10.0;Sheep|0|10.0;Chicken|1200|0.2;Duck|150|0.3;Pig|50|20.0",
    val livestockFactorsString: String = "Cattle:50.0,Goat:10.0,Sheep:10.0,Chicken:0.2,Duck:0.3,Pig:20.0"
)

data class LandUnit(
    val id: String,
    val name: String,
    val area: Double,
    val landUse: String,
    val soilType: String,
    val slope: String,
    val vegetation: String
)

data class IndustryDemandItem(
    val id: String,
    val name: String,
    val dailyDemand: Double
)

data class LivestockDemandItem(
    val type: String,
    val count: Int,
    val demandPerAnimal: Double // L/day
)

object ProjectParser {
    fun parseLandUnits(str: String): List<LandUnit> {
        if (str.isBlank()) return emptyList()
        return str.split(";").mapNotNull { unitStr ->
            val parts = unitStr.split("|")
            if (parts.size >= 6) {
                val id = if (parts.size >= 7) parts[6] else parts[0]
                LandUnit(
                    id = id,
                    name = parts[0],
                    area = parts[1].toDoubleOrNull() ?: 0.0,
                    landUse = parts[2],
                    soilType = parts[3],
                    slope = parts[4],
                    vegetation = parts[5]
                )
            } else null
        }
    }

    fun serializeLandUnits(units: List<LandUnit>): String {
        return units.joinToString(";") { "${it.name}|${it.area}|${it.landUse}|${it.soilType}|${it.slope}|${it.vegetation}|${it.id}" }
    }

    fun parseIndustries(str: String): List<IndustryDemandItem> {
        if (str.isBlank()) return emptyList()
        return str.split(";").mapNotNull { indStr ->
            val parts = indStr.split("|")
            if (parts.size >= 2) {
                val id = if (parts.size >= 3) parts[2] else parts[0]
                IndustryDemandItem(
                    id = id,
                    name = parts[0],
                    dailyDemand = parts[1].toDoubleOrNull() ?: 0.0
                )
            } else null
        }
    }

    fun serializeIndustries(industries: List<IndustryDemandItem>): String {
        return industries.joinToString(";") { "${it.name}|${it.dailyDemand}|${it.id}" }
    }

    fun parseLivestock(str: String): List<LivestockDemandItem> {
        if (str.isBlank()) return emptyList()
        return str.split(";").mapNotNull { lsStr ->
            val parts = lsStr.split("|")
            if (parts.size >= 3) {
                LivestockDemandItem(
                    type = parts[0],
                    count = parts[1].toIntOrNull() ?: 0,
                    demandPerAnimal = parts[2].toDoubleOrNull() ?: 0.0
                )
            } else null
        }
    }

    fun serializeLivestock(livestock: List<LivestockDemandItem>): String {
        return livestock.joinToString(";") { "${it.type}|${it.count}|${it.demandPerAnimal}" }
    }

    fun parseFactorTable(str: String): Map<String, Double> {
        if (str.isBlank()) return emptyMap()
        return str.split(",").mapNotNull { entry ->
            val parts = entry.split(":")
            if (parts.size == 2) {
                val key = parts[0].trim()
                val value = parts[1].trim().toDoubleOrNull()
                if (value != null) key to value else null
            } else null
        }.toMap()
    }

    fun serializeFactorTable(table: Map<String, Double>): String {
        return table.entries.joinToString(",") { "${it.key}:${it.value}" }
    }
}
