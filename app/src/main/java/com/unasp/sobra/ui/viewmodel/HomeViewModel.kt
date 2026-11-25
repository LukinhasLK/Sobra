package com.unasp.sobra.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unasp.sobra.data.Graph
import com.unasp.sobra.data.HomeData
import java.time.YearMonth
import kotlinx.coroutines.launch

/** Home: skeleton (21) = [Load.Loading], erro (22) = [Load.Error], tela pronta (10) = [Load.Success]. */
class HomeViewModel : ViewModel() {
    var state by mutableStateOf<Load<HomeData>>(Load.Loading)
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
            val result = loadResult { Graph.home.load(YearMonth.now()) }
            if (result is Load.Error && keepOnError) return@launch
            state = result
        }
    }
}
