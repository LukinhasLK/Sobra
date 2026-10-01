package com.unasp.sobra.domain

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

// Regras de cálculo do app, sem nenhuma dependência de Android/Compose: dá para testar
// com JUnit puro (veja app/src/test/.../FinanceTest.kt).

private const val MONEY_SCALE = 2
private val MC = MathContext.DECIMAL64

// TODO: a fórmula da projeção NÃO foi especificada no design (taxa, impostos, momento dos
//  aportes). Qual taxa e qual regra usar? Esta taxa é provisória: com juros compostos
//  mensais e aporte no fim de cada mês ela resulta em ~R$ 14.681 para o exemplo do mockup
//  (R$ 1.000 + 24 × R$ 500), que mostra R$ 14.680,00.
val TAXA_MENSAL_PROVISORIA: BigDecimal = BigDecimal("0.0096")

/** Disponível = renda − gastos (tela Home). */
fun availableAmount(income: BigDecimal, expenses: BigDecimal): BigDecimal = income - expenses

/** Soma uma lista de valores. `fold` acumula a partir de um valor inicial (zero). */
fun List<BigDecimal>.total(): BigDecimal = fold(BigDecimal.ZERO) { acc, value -> acc + value }

/** Fração de progresso entre 0 e 1 (ex.: 8.400 de 15.000 → 0,56). */
fun progressFraction(current: BigDecimal, target: BigDecimal): Float {
    if (target.signum() <= 0) return 0f
    return current.divide(target, MC).toFloat().coerceIn(0f, 1f)
}

/** Aporte mensal sugerido para um objetivo: valor ÷ meses (15.000 ÷ 18 = 833,33). */
fun suggestedMonthlyContribution(target: BigDecimal, months: Int): BigDecimal {
    if (months <= 0) return BigDecimal.ZERO.setScale(MONEY_SCALE)
    return target.divide(BigDecimal(months), MONEY_SCALE, RoundingMode.HALF_UP)
}

/** Resultado de uma simulação. `data class` gera equals/hashCode/toString/copy sozinha. */
data class SimulationResult(
    val initial: BigDecimal,
    val monthlyContribution: BigDecimal,
    val months: Int,
    val totalContributions: BigDecimal,
    val totalInvested: BigDecimal,
    val finalAmount: BigDecimal,
    val estimatedYield: BigDecimal,
    /** Saldo ao fim de cada mês, do mês 0 ao último, com rendimento. */
    val seriesWithYield: List<BigDecimal>,
    /** Mesmo período, só somando os aportes (sem rendimento). */
    val seriesWithoutYield: List<BigDecimal>,
)

/**
 * Projeção com juros compostos mensais e aporte no fim de cada mês.
 * Veja o TODO em [TAXA_MENSAL_PROVISORIA]: a regra real ainda precisa ser definida.
 */
fun simulate(
    initial: BigDecimal,
    monthlyContribution: BigDecimal,
    months: Int,
    monthlyRate: BigDecimal = TAXA_MENSAL_PROVISORIA,
): SimulationResult {
    val growth = BigDecimal.ONE + monthlyRate
    val withYield = mutableListOf(initial)
    val withoutYield = mutableListOf(initial)
    // repeat(n) executa o bloco n vezes (como um for de 0 até n-1).
    repeat(months) {
        withYield += withYield.last().multiply(growth, MC) + monthlyContribution
        withoutYield += withoutYield.last() + monthlyContribution
    }
    val totalContributions = monthlyContribution * BigDecimal(months)
    val totalInvested = initial + totalContributions
    val finalAmount = withYield.last().setScale(MONEY_SCALE, RoundingMode.HALF_UP)
    return SimulationResult(
        initial = initial,
        monthlyContribution = monthlyContribution,
        months = months,
        totalContributions = totalContributions,
        totalInvested = totalInvested,
        finalAmount = finalAmount,
        estimatedYield = finalAmount - totalInvested,
        seriesWithYield = withYield,
        seriesWithoutYield = withoutYield,
    )
}
