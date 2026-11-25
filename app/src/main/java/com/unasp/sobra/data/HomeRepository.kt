package com.unasp.sobra.data

import com.unasp.sobra.data.remote.ApiClient
import com.unasp.sobra.ui.model.CategorySpending
import java.math.BigDecimal
import java.time.YearMonth

/** O que a tela Home precisa (OP-10 da API). */
data class HomeData(
    val firstName: String,
    val income: BigDecimal,
    val spending: List<CategorySpending>,
)

class HomeRepository(private val api: ApiClient) {

    suspend fun load(month: YearMonth): HomeData {
        val json = api.get("/home?mes=$month") // YearMonth.toString() já dá "2026-11"
        val categories = json.getJSONArray("categorias")
        val spending = (0 until categories.length()).map { i ->
            val item = categories.getJSONObject(i)
            CategorySpending(categoryFromCode(item.getString("codigo")), BigDecimal(item.getString("valor")))
        }
        return HomeData(
            firstName = json.getJSONObject("usuario").getString("primeiro_nome"),
            // "renda" vem nula quando a pessoa ainda não informou a renda daquele mês.
            income = if (json.isNull("renda")) BigDecimal.ZERO else BigDecimal(json.getString("renda")),
            spending = spending,
        )
    }
}
