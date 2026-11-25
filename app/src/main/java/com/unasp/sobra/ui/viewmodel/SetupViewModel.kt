package com.unasp.sobra.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unasp.sobra.data.CATEGORY_ORDER
import com.unasp.sobra.data.Graph
import com.unasp.sobra.ui.model.Category
import com.unasp.sobra.ui.model.CategoryBudget
import java.math.BigDecimal
import kotlinx.coroutines.launch

/**
 * Configuração inicial (telas 08 e 09). Fica no escopo do app (criado no NavHost) para os dois
 * passos compartilharem o mesmo estado; [reset] limpa quando muda a pessoa logada.
 */
class SetupViewModel : ViewModel() {
    var income by mutableStateOf(BigDecimal.ZERO.setScale(2))
        private set
    var budgets by mutableStateOf(emptyBudgets())
        private set
    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    fun changeIncome(value: BigDecimal) {
        income = value
        error = null
    }

    /** `amount == null` deixa a categoria inativa ("Adicionar"). */
    fun setBudget(category: Category, amount: BigDecimal?) {
        budgets = budgets.map { if (it.category == category) it.copy(amount = amount) else it }
    }

    fun saveIncome(onDone: () -> Unit) {
        if (income.signum() <= 0) {
            error = "Informe sua renda mensal."
            return
        }
        submit(onDone) { Graph.setup.saveIncome(income) }
    }

    fun saveCategories(onDone: () -> Unit) = submit(onDone) { Graph.setup.saveCategories(budgets) }

    fun reset() {
        income = BigDecimal.ZERO.setScale(2)
        budgets = emptyBudgets()
        error = null
    }

    private fun submit(onDone: () -> Unit, block: suspend () -> Unit) {
        if (loading) return
        loading = true
        error = null
        viewModelScope.launch {
            val message = runSubmit(block)
            loading = false
            if (message == null) onDone() else error = message
        }
    }

    private fun emptyBudgets() = CATEGORY_ORDER.map { CategoryBudget(it, null) }
}
