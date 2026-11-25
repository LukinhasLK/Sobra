package com.unasp.sobra.data

import com.unasp.sobra.data.remote.ApiClient
import com.unasp.sobra.ui.model.Category
import com.unasp.sobra.ui.model.Expense
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import org.json.JSONObject

data class ExpensesData(val total: BigDecimal, val expenses: List<Expense>)

/** Lista de despesas do mês (OP-11) e criação de despesa (OP-12). */
class ExpensesRepository(private val api: ApiClient) {

    suspend fun list(month: YearMonth): ExpensesData {
        val json = api.get("/despesas?mes=$month")
        val array = json.getJSONArray("despesas")
        val expenses = (0 until array.length()).map { i ->
            val item = array.getJSONObject(i)
            Expense(
                id = item.getLong("id"),
                title = item.getString("descricao"),
                category = categoryFromCode(item.getJSONObject("categoria").getString("codigo")),
                date = LocalDate.parse(item.getString("data")),
                amount = BigDecimal(item.getString("valor")),
            )
        }
        return ExpensesData(BigDecimal(json.getString("total")), expenses)
    }

    suspend fun create(description: String, category: Category, date: LocalDate, amount: BigDecimal) {
        val body = JSONObject()
            .put("descricao", description.trim())
            .put("categoria_codigo", categoryToCode(category))
            .put("data", date.toString()) // LocalDate.toString() já é "AAAA-MM-DD"
            .put("valor", amount.toMoney())
        api.post("/despesas", body)
    }
}
