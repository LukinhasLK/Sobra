package com.unasp.sobra.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unasp.sobra.data.Graph
import com.unasp.sobra.data.HomeData
import com.unasp.sobra.ui.model.Goal
import java.time.YearMonth
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/** Tudo que a Home mostra: resumo do mês e o carrossel de objetivos. */
data class HomeUi(val summary: HomeData, val goals: List<Goal>)

/** Home: skeleton (21) = [Load.Loading], erro (22) = [Load.Error], tela pronta (10) = [Load.Success]. */
class HomeViewModel : ViewModel() {
    var state by mutableStateOf<Load<HomeUi>>(Load.Loading)
        private set

    /** Também é o "Tentar novamente" da tela de erro. */
    fun load() {
        state = Load.Loading
        fetch(keepOnError = false)
    }

    /**
     * Ao voltar para a Home (por exemplo depois de salvar uma despesa): atualiza sem mostrar o
     * skeleton. Se já havia dados e a rede falhar, mantém o que estava na tela.
     */
    fun refresh() {
        if (state is Load.Success) fetch(keepOnError = true) else load()
    }

    private fun fetch(keepOnError: Boolean) {
        viewModelScope.launch {
            val result = loadResult {
                // As duas buscas saem ao mesmo tempo (async); se uma falhar, a Home mostra o erro.
                coroutineScope {
                    val summary = async { Graph.home.load(YearMonth.now()) }
                    val goals = async { Graph.goals.list() }
                    HomeUi(summary.await(), goals.await())
                }
            }
            if (result is Load.Error && keepOnError) return@launch
            state = result
        }
    }
}
