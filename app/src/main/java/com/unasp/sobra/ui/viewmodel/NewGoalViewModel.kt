package com.unasp.sobra.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unasp.sobra.data.Graph
import java.math.BigDecimal
import kotlinx.coroutines.launch

/** Formulário da tela 14 (Novo objetivo). */
class NewGoalViewModel : ViewModel() {
    var name by mutableStateOf("")
        private set
    var target by mutableStateOf(BigDecimal.ZERO.setScale(2))
        private set
    var months by mutableIntStateOf(DEFAULT_MONTHS)
        private set
    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    fun changeName(value: String) { name = value }
    fun changeTarget(value: BigDecimal) { target = value }
    fun changeMonths(value: Int) { months = value }

    fun create(onDone: () -> Unit) {
        if (loading) return
        if (name.isBlank()) {
            error = "Dê um nome ao objetivo."
            return
        }
        if (target.signum() <= 0) {
            error = "Informe o valor desejado."
            return
        }
        loading = true
        error = null
        viewModelScope.launch {
            val message = runSubmit { Graph.goals.create(name, target, months) }
            loading = false
            if (message == null) onDone() else error = message
        }
    }

    private companion object {
        const val DEFAULT_MONTHS = 18 // prazo do mockup
    }
}
