package com.unasp.sobra.domain

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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

    // ---- Casos que o back end também testa: o app e a API precisam concordar ----

    @Test
    fun simulationFinalAmountMatchesTheBackend() {
        // 1.000 inicial + 24 aportes de 500 a 0,96% ao mês = 14.680,67 (o mockup mostra 14.680,00).
        val result = simulate(BigDecimal("1000.00"), BigDecimal("500.00"), 24)
        assertEquals(BigDecimal("14680.67"), result.finalAmount)
        assertEquals(BigDecimal("1680.67"), result.estimatedYield)
    }

    @Test
    fun simulationSeriesStartsAtInitialAndEndsAtFinalAmount() {
        val result = simulate(BigDecimal("1000.00"), BigDecimal("500.00"), 24)
        assertEquals(0, BigDecimal("1000").compareTo(result.seriesWithYield.first()))
        assertEquals(0, result.finalAmount.compareTo(result.seriesWithYield.last().setScale(2, java.math.RoundingMode.HALF_UP)))
        // Com rendimento o saldo nunca fica abaixo do saldo sem rendimento.
        result.seriesWithYield.zip(result.seriesWithoutYield).forEach { (with, without) ->
            assertTrue(with >= without)
        }
    }

    @Test
    fun simulationWithZeroMonthsIsJustTheInitialAmount() {
        val result = simulate(BigDecimal("1000.00"), BigDecimal("500.00"), 0)
        assertEquals(1, result.seriesWithYield.size)
        assertEquals(BigDecimal("1000.00"), result.finalAmount)
        assertEquals(0, result.estimatedYield.signum())
    }

    @Test
    fun availableCanBeNegative() {
        assertEquals(BigDecimal("-50"), availableAmount(BigDecimal("100"), BigDecimal("150")))
    }

    @Test
    fun suggestedContributionRoundsHalfUpToTwoPlaces() {
        assertEquals(BigDecimal("3333.33"), suggestedMonthlyContribution(BigDecimal("10000"), 3))
        assertEquals(BigDecimal("12.50"), suggestedMonthlyContribution(BigDecimal("100"), 8))
        assertEquals(BigDecimal("0.01"), suggestedMonthlyContribution(BigDecimal("0.01"), 2)) // 0,005 sobe
    }

    @Test
    fun progressIsClampedBetweenZeroAndOne() {
        // Objetivo acima da meta não passa de 100%.
        assertEquals(1f, progressFraction(BigDecimal("20000"), BigDecimal("15000")), 0f)
        assertEquals(0f, progressFraction(BigDecimal("-10"), BigDecimal("15000")), 0f)
    }
}
