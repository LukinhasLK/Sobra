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
