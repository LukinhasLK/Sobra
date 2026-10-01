package com.unasp.sobra.ui.format

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

// Tudo que vira texto para o usuário (dinheiro e datas) é formatado aqui, sempre em pt-BR,
// independente do idioma do aparelho.

private val PT_BR: Locale = Locale.forLanguageTag("pt-BR")
private val SYMBOLS = DecimalFormatSymbols(PT_BR) // ponto no milhar, vírgula no decimal

private const val CURRENCY_PREFIX = "R$ "
private const val CENTS_SCALE = 2

// TODO: limites monetários não definidos no design. Qual o valor máximo aceito nos campos?
private const val MAX_MONEY_DIGITS = 11

private fun decimalFormat(pattern: String) = DecimalFormat(pattern, SYMBOLS).apply {
    // `apply` configura o objeto recém-criado e devolve ele mesmo.
    roundingMode = RoundingMode.HALF_UP
}

/** "1.200,00" (sem o prefixo). Função de extensão: chama-se como `valor.toBrlNumber()`. */
fun BigDecimal.toBrlNumber(): String = decimalFormat("#,##0.00").format(this)

/** "R$ 1.200,00". */
fun BigDecimal.toBrl(): String = CURRENCY_PREFIX + toBrlNumber()

/** "R$ 1.200" — forma curta, sem centavos, usada em chips, categorias e objetivos. */
fun BigDecimal.toBrlShort(): String = CURRENCY_PREFIX + decimalFormat("#,##0").format(this)

/** "+ R$ 500,00" — aportes e rendimento. */
fun BigDecimal.toBrlPositive(): String = "+ " + toBrl()

/**
 * Converte o que foi digitado num campo de dinheiro em valor: só os dígitos contam e os dois
 * últimos são os centavos ("550000" → 5500.00), como em apps de banco.
 */
fun parseMoneyInput(text: String): BigDecimal {
    val digits = text.filter { it.isDigit() }.take(MAX_MONEY_DIGITS)
    if (digits.isEmpty()) return BigDecimal.ZERO.setScale(CENTS_SCALE)
    return BigDecimal(digits).movePointLeft(CENTS_SCALE)
}

private val DAY_MONTH = DateTimeFormatter.ofPattern("dd/MM", PT_BR)
private val FULL_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy", PT_BR)

/** "01/09". */
fun LocalDate.toDayMonth(): String = format(DAY_MONTH)

/** "18/09/2026". */
fun LocalDate.toFullDate(): String = format(FULL_DATE)

private fun String.capitalizeFirst(): String = replaceFirstChar { it.titlecase(PT_BR) }

/** "18 de Out, 2023". */
fun LocalDate.toShortMonthDate(): String {
    val shortMonth = month.getDisplayName(TextStyle.SHORT, PT_BR).removeSuffix(".").capitalizeFirst()
    return "%02d de %s, %d".format(PT_BR, dayOfMonth, shortMonth, year)
}

/** "Março 2028". */
fun YearMonth.toMonthYear(): String =
    month.getDisplayName(TextStyle.FULL, PT_BR).capitalizeFirst() + " " + year
