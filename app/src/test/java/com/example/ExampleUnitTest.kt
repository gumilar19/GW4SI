package com.example

import com.example.calculation.HydroCalculationEngine
import com.example.model.Project
import org.junit.Assert.*
import org.junit.Test

/**
 * Local scientific hydrogeological unit tests for Groundwater Balance calculations.
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testHydroCalculationEngine_baseline() {
        val testProject = Project(
            name = "Test Island",
            annualRainfall = 1850.0,
            islandArea = 22.5,
            aquiferThickness = 32.0,
            specificYield = 0.18,
            rechargeCoefficient = 0.31,
            rechargeMethod = "Coefficient",
            population = 6250,
            domesticDemand = 110.0,
            totalPumping = 711.0,
            safetyFactor = 0.50,
            waterTable = 1.85,
            rechargeMode = "Manual"
        )

        val results = HydroCalculationEngine.calculate(testProject)

        // 1. Volumetric Rainfall
        // 1850.0 mm * 22.5 km2 * 1000 = 41,625,000 m3
        assertEquals(41625000.0, results.annualRainfallVolM3, 1.0)

        // 2. Recharge mm & volumetric
        // 1850.0 * 0.31 = 573.5 mm
        assertEquals(573.5, results.rechargeMm, 0.01)
        // 573.5 mm * 22.5 * 1000 = 12,903,750 m3/year
        assertEquals(12903750.0, results.rechargeVolM3, 1.0)
        // 12,903,750 / 365 = 35352.7 m3/day
        assertEquals(35352.73, results.dailyRechargeM3, 0.1)

        // 3. Storage capacity
        // 22.5 * 1,000,000 * 32.0 * 0.18 = 129,600,000 m3
        assertEquals(129600000.0, results.groundwaterStorageM3, 1.0)

        // 4. Domestic demand
        // 6250 * 110 / 1000 = 687.5 m3/day
        assertEquals(687.5, results.dailyDomesticDemandM3, 0.01)

        // 5. Safe Yield
        // 35352.73 * 0.50 = 17676.36 m3/day
        assertEquals(17676.36, results.safeYieldM3Day, 0.1)

        // 6. Ghyben-Herzberg lens thickness
        // 1.85 * 41 = 75.85 meters, truncated to aquifer thickness 32.0 meters
        assertEquals(75.85, results.theoreticalLensThicknessM, 0.01)
        assertEquals(32.0, results.truncatedLensThicknessM, 0.01)

        // 7. Status check - sustainable because pumping (711) < safe yield (17676)
        assertEquals("Sustainable", results.groundwaterStatus)
    }
}
