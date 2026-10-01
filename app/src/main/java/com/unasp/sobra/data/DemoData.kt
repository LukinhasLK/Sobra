package com.unasp.sobra.data

import com.unasp.sobra.R
import com.unasp.sobra.domain.SimulationResult
import com.unasp.sobra.ui.model.Category
import com.unasp.sobra.ui.model.CategoryBudget
import com.unasp.sobra.ui.model.CategorySpending
import com.unasp.sobra.ui.model.Contribution
import com.unasp.sobra.ui.model.Expense
import com.unasp.sobra.ui.model.Goal
import com.unasp.sobra.ui.model.SavedSimulation
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

/**
 * Dados do mockup do Figma, usados nos @Preview e, por enquanto, na navegação.
 * São demonstrativos e NÃO formam um único usuário consistente (rendas e datas variam
 * entre telas). TODO(back end): substituir cada uso em SobraNavHost por dados reais.
 */
object DemoData {

    // Atalho para escrever valores: "1200".brl em vez de BigDecimal("1200").
    private val String.brl: BigDecimal get() = BigDecimal(this)

    const val USER_FIRST_NAME = "Lucas"
    const val USER_FULL_NAME = "Lucas Costa" // TODO: nome completo do Perfil não informado no Figma
    const val USER_EMAIL = "lucas@email.com" // TODO: e-mail do Perfil não informado no Figma
    const val USER_INITIALS = "LC"

    // ---- 08 Renda / 09 Categorias ----
    val setupIncome = "5500.00".brl
    val categoryBudgets = listOf(
        CategoryBudget(Category.Housing, "1800".brl),
        CategoryBudget(Category.Food, "900".brl),
        CategoryBudget(Category.Transport, "450".brl),
        CategoryBudget(Category.Health, null),
        CategoryBudget(Category.Education, "350".brl),
        CategoryBudget(Category.Leisure, "300".brl),
        CategoryBudget(Category.Other, "200".brl),
    )

    // ---- 10 Home ----
    val homeIncome = "5200".brl
    val homeSpending = listOf(
        CategorySpending(Category.Housing, "1200".brl),
        CategorySpending(Category.Food, "850".brl),
        CategorySpending(Category.Transport, "430".brl),
        CategorySpending(Category.Health, "350".brl),
        CategorySpending(Category.Leisure, "350".brl),
        CategorySpending(Category.Other, "300".brl),
    )

    // ---- 11 Gastos / 12 Nova despesa ----
    val expensesMonth: YearMonth = YearMonth.of(2026, 9)
    val expenses = listOf(
        Expense(1, "Aluguel", Category.Housing, LocalDate.of(2026, 9, 1), "1200.00".brl),
        Expense(2, "Supermercado", Category.Food, LocalDate.of(2026, 9, 3), "420.00".brl),
        Expense(3, "Uber", Category.Transport, LocalDate.of(2026, 9, 5), "85.00".brl),
        Expense(4, "Farmácia", Category.Health, LocalDate.of(2026, 9, 7), "120.00".brl),
        Expense(5, "Academia", Category.Education, LocalDate.of(2026, 9, 10), "89.00".brl),
        Expense(6, "Restaurante", Category.Food, LocalDate.of(2026, 9, 12), "156.00".brl),
        Expense(7, "Netflix", Category.Leisure, LocalDate.of(2026, 9, 15), "39.90".brl),
    )
    val newExpenseAmount = "120.00".brl
    const val NEW_EXPENSE_DESCRIPTION = "Supermercado Mensal"
    val newExpenseDate: LocalDate = LocalDate.of(2026, 9, 18)

    // ---- 13 a 15 Objetivos ----
    val goals = listOf(
        Goal(1, "Reserva de emergência", "8400".brl, "15000".brl, R.drawable.img_goal_emergency),
        Goal(2, "Viagem Europa", "3200".brl, "12000".brl, R.drawable.img_goal_travel),
        Goal(3, "Notebook novo", "1800".brl, "4500".brl, R.drawable.img_goal_notebook),
    )
    const val NEW_GOAL_NAME = "Reserva de Emergência"
    val newGoalTarget = "15000.00".brl
    const val NEW_GOAL_MONTHS = 18
    val newGoalStart: YearMonth = YearMonth.of(2026, 9)

    // TODO: os valores de "Aporte mensal" e "Previsão de conclusão" não foram informados.
    val goalMonthlyContribution = "500.00".brl
    val goalForecast: YearMonth = YearMonth.of(2027, 11)
    val contributions = listOf(
        Contribution(1, "Aporte Mensal", LocalDate.of(2026, 9, 15), "500.00".brl),
        Contribution(2, "Aporte Mensal", LocalDate.of(2026, 8, 10), "500.00".brl),
        Contribution(3, "Aporte Extra", LocalDate.of(2026, 7, 15), "1200.00".brl),
    )

    // ---- 16 a 18 Simular ----
    val simulationInitial = "1000.00".brl
    val simulationMonthly = "500.00".brl
    const val SIMULATION_MONTHS = 24

    // Pontos do gráfico do mockup, em unidades do Figma (y cresce para baixo, área de 120).
    // O último ponto (x ≈ 310) não tem y informado: foi extrapolado pela tendência
    // (queda de ~14 por passo na série com rendimento e de 8 na sem rendimento).
    private val chartWithYieldY = listOf(120, 114, 106, 97, 87, 75, 63, 51, 38, 24, 10)
    private val chartWithoutYieldY = listOf(120, 112, 104, 96, 88, 80, 72, 64, 56, 48, 40)
    private const val CHART_AREA_HEIGHT = 120

    /** Resultado exatamente como no mockup (valores fixos, não calculados). */
    val simulationResult = SimulationResult(
        initial = simulationInitial,
        monthlyContribution = simulationMonthly,
        months = SIMULATION_MONTHS,
        totalContributions = "12000.00".brl,
        totalInvested = "13000.00".brl,
        finalAmount = "14680.00".brl,
        estimatedYield = "1680.00".brl,
        // O gráfico só usa a proporção entre os valores, então "altura no mockup" serve de valor.
        seriesWithYield = chartWithYieldY.map { BigDecimal(CHART_AREA_HEIGHT - it) },
        seriesWithoutYield = chartWithoutYieldY.map { BigDecimal(CHART_AREA_HEIGHT - it) },
    )

    val savedSimulations = listOf(
        SavedSimulation(1, LocalDate.of(2023, 10, 18), "1500".brl, "300".brl, 12, "5340.12".brl),
        SavedSimulation(2, LocalDate.of(2023, 10, 4), "10000".brl, "500".brl, 24, "24120.44".brl),
        SavedSimulation(3, LocalDate.of(2023, 9, 15), "500".brl, "100".brl, 6, "1135.80".brl),
    )
}
