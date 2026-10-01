package com.unasp.sobra.domain

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Test

class FinanceTest {

    @Test
    fun availableIsIncomeMinusExpenses() {
        // Home do mockup: renda 5.200, gastos 3.480 → disponível 1.720.
        val expenses = listOf("1200", "850", "430", "350", "350", "300").map(::BigDecimal).total()
        assertEquals(BigDecimal("3480"), expenses)
        assertEquals(BigDecimal("1720"), availableAmount(BigDecimal("5200"), expenses))
    }

    @Test
    fun suggestedContributionIsTargetDividedByMonths() {
        // Novo objetivo do mockup: 15.000 ÷ 18 = 833,33.
        assertEquals(BigDecimal("833.33"), suggestedMonthlyContribution(BigDecimal("15000.00"), 18))
    }

    @Test
    fun suggestedContributionWithZeroMonthsIsZero() {
        assertEquals(0, suggestedMonthlyContribution(BigDecimal("15000.00"), 0).signum())
    }

    @Test
    fun progressFractionMatchesGoalBadges() {
        assertEquals(0.56f, progressFraction(BigDecimal("8400"), BigDecimal("15000")), 0.0001f)
        assertEquals(0.40f, progressFraction(BigDecimal("1800"), BigDecimal("4500")), 0.0001f)
        assertEquals(0f, progressFraction(BigDecimal("100"), BigDecimal.ZERO), 0f)
    }

    @Test
    fun simulationTotalsMatchMockupInputs() {
        val result = simulate(BigDecimal("1000.00"), BigDecimal("500.00"), 24)
        assertEquals(0, BigDecimal("12000").compareTo(result.totalContributions))
        assertEquals(0, BigDecimal("13000").compareTo(result.totalInvested))
        assertEquals(result.finalAmount - result.totalInvested, result.estimatedYield)
        // Uma entrada por mês, mais o ponto inicial.
        assertEquals(25, result.seriesWithYield.size)
        assertEquals(0, BigDecimal("13000").compareTo(result.seriesWithoutYield.last()))
    }

    @Test
    fun simulationWithZeroRateHasNoYield() {
        val result = simulate(BigDecimal("1000.00"), BigDecimal("500.00"), 24, monthlyRate = BigDecimal.ZERO)
        assertEquals(0, result.estimatedYield.signum())
    }
}
