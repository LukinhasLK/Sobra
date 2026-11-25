package com.unasp.sobra.ui.format

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Test

class FormattersTest {

    // ---- toBrl / toBrlShort / toBrlNumber ----

    @Test
    fun toBrlUsesDotForThousandsAndCommaForCents() {
        assertEquals("R$ 1.200,00", BigDecimal("1200").toBrl())
        assertEquals("R$ 5.079,50", BigDecimal("5079.5").toBrl())
        assertEquals("R$ 0,00", BigDecimal.ZERO.toBrl())
        assertEquals("1.234.567,89", BigDecimal("1234567.89").toBrlNumber())
    }

    @Test
    fun toBrlRoundsHalfUp() {
        assertEquals("R$ 0,01", BigDecimal("0.005").toBrl())
        assertEquals("R$ 14.680,67", BigDecimal("14680.666").toBrl())
    }

    @Test
    fun negativeAmountPutsTheSignBeforeTheSymbol() {
        // "Disponível" pode ficar negativo quando os gastos passam da renda.
        assertEquals("-R$ 80,00", BigDecimal("-80").toBrl())
        assertEquals("-R$ 1.200", BigDecimal("-1200").toBrlShort())
    }

    @Test
    fun tinyNegativeThatRoundsToZeroHasNoSign() {
        assertEquals("R$ 0,00", BigDecimal("-0.001").toBrl())
        assertEquals("R$ 0", BigDecimal("-0.4").toBrlShort())
    }

    @Test
    fun toBrlShortHasNoCents() {
        assertEquals("R$ 1.200", BigDecimal("1200.00").toBrlShort())
        assertEquals("R$ 8.401", BigDecimal("8400.5").toBrlShort())
    }

    @Test
    fun toBrlPositiveAddsPlusSign() {
        assertEquals("+ R$ 500,00", BigDecimal("500").toBrlPositive())
    }

    // ---- parseMoneyInput: o campo de dinheiro funciona como app de banco ----

    @Test
    fun parseMoneyInputTreatsLastTwoDigitsAsCents() {
        assertEquals(BigDecimal("5500.00"), parseMoneyInput("550000"))
        assertEquals(BigDecimal("0.05"), parseMoneyInput("5"))
        assertEquals(BigDecimal("1200.50"), parseMoneyInput("R$ 1.200,50"))
    }

    @Test
    fun parseMoneyInputIgnoresNonDigitsAndEmptyIsZero() {
        assertEquals(BigDecimal("0.00"), parseMoneyInput(""))
        assertEquals(BigDecimal("0.00"), parseMoneyInput("abc"))
    }

    @Test
    fun parseMoneyInputLimitsTo11Digits() {
        // 11 dígitos = no máximo 999.999.999,99; o resto digitado é descartado.
        assertEquals(BigDecimal("123456789.01"), parseMoneyInput("123456789012345"))
    }

    @Test
    fun parseMoneyInputUndoesWhatTheFieldDisplays() {
        // O campo reformata o texto a cada tecla; ler o que ele mostra tem que devolver o mesmo valor.
        val value = BigDecimal("1234.56")
        assertEquals(value, parseMoneyInput(value.toBrlNumber()))
    }
}
