package com.unasp.sobra.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unasp.sobra.data.Graph
import com.unasp.sobra.ui.model.Category
import java.math.BigDecimal
import java.time.LocalDate
import kotlinx.coroutines.launch

/** Formulário da tela 12 (Nova despesa). */
class NewExpenseViewModel : ViewModel() {
    var amount by mutableStateOf(BigDecimal.ZERO.setScale(2))
        private set
    var description by mutableStateOf("")
        private set
    var category by mutableStateOf(Category.Food)
        private set
    var date by mutableStateOf(LocalDate.now())
        private set
    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    fun changeAmount(value: BigDecimal) { amount = value }
    fun changeDescription(value: String) { description = value }
    fun changeCategory(value: Category) { category = value }
    fun changeDate(value: LocalDate) { date = value }

    fun save(onDone: () -> Unit) {
        if (loading) return
        if (amount.signum() <= 0) {
            error = "Informe o valor da despesa."
            return
        }
        if (description.isBlank()) {
            error = "Informe uma descrição."
            return
        }
        loading = true
        error = null
        viewModelScope.launch {
            val message = runSubmit { Graph.expenses.create(description, category, date, amount) }
            loading = false
            if (message == null) onDone() else error = message
        }
    }
}
