package com.unasp.sobra.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unasp.sobra.data.Graph
import com.unasp.sobra.ui.model.Goal
import kotlinx.coroutines.launch

/** Lista de Objetivos (tela 13; lista vazia = estado vazio da tela 20). */
class GoalsViewModel : ViewModel() {
    var state by mutableStateOf<Load<List<Goal>>>(Load.Loading)
        private set

    fun load() {
        state = Load.Loading
        fetch(keepOnError = false)
    }

    /** Ao voltar para a lista (depois de criar objetivo ou aporte): atualiza sem piscar. */
    fun refresh() {
        if (state is Load.Success) fetch(keepOnError = true) else load()
    }

    private fun fetch(keepOnError: Boolean) {
        viewModelScope.launch {
            val result = loadResult { Graph.goals.list() }
            if (result is Load.Error && keepOnError) return@launch
            state = result
        }
    }
}
