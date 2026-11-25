package com.unasp.sobra.data

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Test

/** Dinheiro vai para a API como texto com 2 casas e ponto, nunca como número decimal. */
class MoneyTest {

    @Test
    fun alwaysHasTwoDecimalPlaces() {
        assertEquals("5200.00", BigDecimal("5200").toMoney())
        assertEquals("120.50", BigDecimal("120.5").toMoney())
        assertEquals("0.00", BigDecimal.ZERO.toMoney())
    }

    @Test
    fun roundsHalfUp() {
        assertEquals("0.01", BigDecimal("0.005").toMoney())
        assertEquals("833.33", BigDecimal("833.333333").toMoney())
    }

    @Test
    fun neverUsesScientificNotation() {
        // BigDecimal.toString() daria "1E+3"; o JSON da API precisa de "1000.00".
        assertEquals("1000.00", BigDecimal("1E+3").toMoney())
    }
}
