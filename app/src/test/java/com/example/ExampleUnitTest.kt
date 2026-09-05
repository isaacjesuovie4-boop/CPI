package com.example

import com.example.data.repository.DashboardOverview
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testDefaultDashboardOverviewForNewUser() {
        val overview = DashboardOverview()
        assertEquals(0.0, overview.availableBalance, 0.001)
        assertEquals(0.0, overview.totalInvested, 0.001)
        assertEquals(0, overview.activeInvestmentsCount)
        assertEquals(0.0, overview.totalWithdrawn, 0.001)
        assertEquals(0.0, overview.overallPerformancePercentage, 0.001)
    }

    @Test
    fun testCurrencyRulesConstants() {
        val usdtMin = 50.0
        val usdtMax = 5000.0
        val phpMin = 3000.0
        val phpMax = 100000.0

        assertTrue(usdtMin < usdtMax)
        assertTrue(phpMin < phpMax)
        assertEquals(50.0, usdtMin, 0.0)
        assertEquals(5000.0, usdtMax, 0.0)
        assertEquals(3000.0, phpMin, 0.0)
        assertEquals(100000.0, phpMax, 0.0)
    }

    @Test
    fun testPerformanceCalculation() {
        val totalInvested = 1000.0
        val currentValue = 1150.0
        val performance = ((currentValue - totalInvested) / totalInvested) * 100.0
        assertEquals(15.0, performance, 0.001)
    }
}

