package com.unasp.sobra.ui.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.unasp.sobra.R
import com.unasp.sobra.ui.theme.SobraColors
import java.math.BigDecimal
import java.time.LocalDate

// Modelos que as telas recebem por parâmetro. O back end deve produzir estes objetos
// (ou ser convertido para eles) antes de entregar às telas.

/**
 * Categorias de gasto. Cada uma tem o ícone em dois tamanhos porque o Figma exporta um
 * vetor por tamanho de uso (16 nos chips e listas, 12 dentro das caixas de 36).
 */
enum class Category(
    @param:StringRes val label: Int,
    @param:DrawableRes val icon16: Int,
    @param:DrawableRes val icon12: Int,
    val color: Color,
) {
    Housing(R.string.category_housing, R.drawable.ic_home_16, R.drawable.ic_home_12, SobraColors.Teal),
    Food(R.string.category_food, R.drawable.ic_utensils_16, R.drawable.ic_utensils_12, SobraColors.Blue),
    Transport(R.string.category_transport, R.drawable.ic_car_16, R.drawable.ic_car_12, SobraColors.Amber),
    Health(R.string.category_health, R.drawable.ic_heart_16, R.drawable.ic_heart_12, SobraColors.Red),

    // TODO: o Figma não define a cor da categoria Educação (ela não aparece na Home). Qual usar?
    Education(R.string.category_education, R.drawable.ic_graduation_16, R.drawable.ic_graduation_12, SobraColors.Teal),
    Leisure(R.string.category_leisure, R.drawable.ic_smile_16, R.drawable.ic_smile_12, SobraColors.Green),
    Other(R.string.category_other, R.drawable.ic_ellipsis_16, R.drawable.ic_ellipsis_12, SobraColors.Violet),
}

/** Chip da tela de categorias: `amount == null` é o estado inativo ("Adicionar"). */
data class CategoryBudget(val category: Category, val amount: BigDecimal?)

/** Linha da Home: quanto foi gasto em uma categoria. */
data class CategorySpending(val category: Category, val amount: BigDecimal)

data class Expense(
    val id: Long,
    val title: String,
    val category: Category,
    val date: LocalDate,
    val amount: BigDecimal,
)

data class Goal(
    val id: Long,
    val name: String,
    val current: BigDecimal,
    val target: BigDecimal,
    @param:DrawableRes val thumbnail: Int,
)

data class Contribution(
    val id: Long,
    val title: String,
    val date: LocalDate,
    val amount: BigDecimal,
)

data class SavedSimulation(
    val id: Long,
    val date: LocalDate,
    val initial: BigDecimal,
    val monthlyContribution: BigDecimal,
    val months: Int,
    val result: BigDecimal,
)
