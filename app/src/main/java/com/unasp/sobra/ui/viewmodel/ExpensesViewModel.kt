package com.unasp.sobra.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unasp.sobra.data.ExpensesData
import com.unasp.sobra.data.Graph
import java.time.YearMonth
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** Tela Gastos: lista do mês escolhido. */
class ExpensesViewModel : ViewModel() {
    var month by mutableStateOf(YearMonth.now())
        private set
    var state by mutableStateOf<Load<ExpensesData>>(Load.Loading)
        private set

    private var job: Job? = null

    fun selectMonth(value: YearMonth) {
        month = value
        load()
    }

    /** Busca mostrando o carregando (primeira vez, troca de mês, "Tentar novamente"). */
    fun load() {
        state = Load.Loading
        fetch(keepOnError = false)
    }

    /** Ao voltar para a tela: atualiza sem piscar; se já havia lista e a rede falhar, mantém a lista. */
    fun refresh() {
        if (state is Load.Success) fetch(keepOnError = true) else load()
    }

    private fun fetch(keepOnError: Boolean) {
        job?.cancel() // evita uma resposta antiga sobrescrever a do mês novo
        val requested = month
        job = viewModelScope.launch {
            val result = loadResult { Graph.expenses.list(requested) }
            if (result is Load.Error && keepOnError) return@launch
            state = result
        }
    }
}
