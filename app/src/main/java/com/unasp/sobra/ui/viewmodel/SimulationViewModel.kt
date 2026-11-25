package com.unasp.sobra.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unasp.sobra.data.Graph
import com.unasp.sobra.domain.SimulationResult
import java.math.BigDecimal
import kotlinx.coroutines.launch

/**
 * Simular (telas 16 e 17). Fica no escopo do app (criado no NavHost) porque as duas telas
 * compartilham o resultado; como o ViewModel sobrevive à rotação, o resultado não se perde.
 */
class SimulationViewModel : ViewModel() {
    // Valores iniciais do mockup, como sugestão para a pessoa ver o resultado rápido.
    var initial by mutableStateOf(BigDecimal("1000.00"))
        private set
    var monthly by mutableStateOf(BigDecimal("500.00"))
        private set
    var months by mutableIntStateOf(24)
        private set

    var result by mutableStateOf<SimulationResult?>(null)
        private set
    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    fun changeInitial(value: BigDecimal) { initial = value }
    fun changeMonthly(value: BigDecimal) { monthly = value }
    fun changeMonths(value: Int) { months = value }
    fun clearError() { error = null }

    fun calculate(onDone: () -> Unit) {
        if (loading) return
        loading = true
        error = null
        viewModelScope.launch {
            val loaded = loadResult { Graph.simulation.calculate(initial, monthly, months) }
            loading = false
            when (loaded) {
                is Load.Success -> { result = loaded.data; onDone() }
                else -> error = "Não foi possível calcular a simulação. Verifique os valores e a conexão."
            }
        }
    }

    fun save(onDone: () -> Unit) {
        val current = result ?: return
        if (loading) return
        loading = true
        error = null
        viewModelScope.launch {
            val message = runSubmit { Graph.simulation.save(current.initial, current.monthlyContribution, current.months) }
            loading = false
            if (message == null) onDone() else error = message
        }
    }

    fun reset() {
        result = null
        error = null
    }
}
