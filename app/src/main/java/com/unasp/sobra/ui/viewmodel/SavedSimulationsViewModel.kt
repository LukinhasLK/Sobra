package com.unasp.sobra.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unasp.sobra.data.Graph
import com.unasp.sobra.ui.model.SavedSimulation
import kotlinx.coroutines.launch

/** Simulações salvas (tela 18). */
class SavedSimulationsViewModel : ViewModel() {
    var state by mutableStateOf<Load<List<SavedSimulation>>>(Load.Loading)
        private set

    init {
        load()
    }

    fun load() {
        state = Load.Loading
        viewModelScope.launch { state = loadResult { Graph.simulation.listSaved() } }
    }
}
