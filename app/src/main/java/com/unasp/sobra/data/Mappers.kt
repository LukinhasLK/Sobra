package com.unasp.sobra.data

import com.unasp.sobra.ui.model.Category

/** Código da categoria na API (coluna `categoria.codigo` do banco) → categoria do app. */
fun categoryFromCode(code: String): Category = when (code) {
    "MORADIA" -> Category.Housing
    "ALIMENTACAO" -> Category.Food
    "TRANSPORTE" -> Category.Transport
    "SAUDE" -> Category.Health
    "EDUCACAO" -> Category.Education
    "LAZER" -> Category.Leisure
    else -> Category.Other // "OUTROS" e qualquer código novo
}

/** As 7 categorias na ordem do banco (a API exige as 7 na configuração inicial). */
val CATEGORY_ORDER: List<Category> = listOf(
    Category.Housing, Category.Food, Category.Transport, Category.Health,
    Category.Education, Category.Leisure, Category.Other,
)

/** Categoria do app → código da API (`categoria.codigo`). */
fun categoryToCode(category: Category): String = when (category) {
    Category.Housing -> "MORADIA"
    Category.Food -> "ALIMENTACAO"
    Category.Transport -> "TRANSPORTE"
    Category.Health -> "SAUDE"
    Category.Education -> "EDUCACAO"
    Category.Leisure -> "LAZER"
    Category.Other -> "OUTROS"
}
