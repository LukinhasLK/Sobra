package com.unasp.sobra.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unasp.sobra.data.ContributionType
import com.unasp.sobra.data.Graph
import com.unasp.sobra.data.GoalDetail
import java.math.BigDecimal
import java.time.LocalDate
import kotlinx.coroutines.launch

/**
 * Detalhe de um objetivo (tela 15). O id vem do argumento da rota (`goals/{goalId}`), que a
 * Navigation já coloca no [SavedStateHandle].
 */
class GoalDetailViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val goalId: Long = savedStateHandle["goalId"] ?: -1L

    var state by mutableStateOf<Load<GoalDetail>>(Load.Loading)
        private set
    var saving by mutableStateOf(false)
        private set

    /** Erro do formulário de novo aporte (mostrado dentro do diálogo). */
    var error by mutableStateOf<String?>(null)
        private set

    init {
        load()
    }

    fun load() {
        state = Load.Loading
        viewModelScope.launch { state = loadResult { Graph.goals.detail(goalId) } }
    }

    fun clearError() {
        error = null
    }

    fun addContribution(type: ContributionType, amount: BigDecimal, date: LocalDate, onDone: () -> Unit) {
        if (saving) return
        if (amount.signum() <= 0) {
            error = "Informe o valor do aporte."
            return
        }
        saving = true
        error = null
        viewModelScope.launch {
            val message = runSubmit { Graph.goals.addContribution(goalId, type, amount, date) }
            saving = false
            if (message == null) {
                // Recarrega em silêncio: o anel de progresso, o acumulado e o histórico se atualizam.
                val fresh = loadResult { Graph.goals.detail(goalId) }
                if (fresh is Load.Success) state = fresh
                onDone()
            } else {
                error = message
            }
        }
    }
}
