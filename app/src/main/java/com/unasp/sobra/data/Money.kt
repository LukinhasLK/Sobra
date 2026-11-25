package com.unasp.sobra.data

import java.math.BigDecimal
import java.math.RoundingMode

/** Dinheiro no formato da API: texto com 2 casas e ponto ("5200.00"), nunca número decimal (Double). */
fun BigDecimal.toMoney(): String = setScale(2, RoundingMode.HALF_UP).toPlainString()
