package com.unasp.sobra.data

import com.unasp.sobra.data.remote.ApiClient
import com.unasp.sobra.ui.model.CategoryBudget
import java.math.BigDecimal
import org.json.JSONArray
import org.json.JSONObject

/** Configuração inicial: renda (OP-07) e estimativa por categoria (OP-08). */
class SetupRepository(private val api: ApiClient) {

    suspend fun saveIncome(income: BigDecimal) {
        api.put("/configuracao/renda", JSONObject().put("valor", income.toMoney()))
    }

    /** A API exige as 7 categorias; as sem valor vão como `null` (categoria inativa). */
    suspend fun saveCategories(budgets: List<CategoryBudget>) {
        val items = JSONArray()
        budgets.forEach { budget ->
            items.put(
                JSONObject()
                    .put("codigo", categoryToCode(budget.category))
                    .put("valor_estimado", budget.amount?.toMoney() ?: JSONObject.NULL),
            )
        }
        api.put("/configuracao/categorias", JSONObject().put("categorias", items))
    }
}
