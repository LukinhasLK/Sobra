package com.unasp.sobra.data

import com.unasp.sobra.R
import com.unasp.sobra.data.remote.ApiClient
import com.unasp.sobra.ui.model.Contribution
import com.unasp.sobra.ui.model.Goal
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import org.json.JSONObject

/** Tipo do aporte aceito pela API; o servidor monta o título ("Aporte Mensal"/"Aporte Extra"). */
enum class ContributionType(val api: String) { Monthly("MENSAL"), Extra("EXTRA") }

/** Tudo da tela de detalhe (OP-15). `forecast` é nulo quando não há previsão (ex.: já concluído). */
data class GoalDetail(
    val goal: Goal,
    val monthlyContribution: BigDecimal,
    val forecast: YearMonth?,
    val completed: Boolean,
    val contributions: List<Contribution>,
)

/** Objetivos: listar (OP-13), criar (OP-14), detalhe (OP-15) e registrar aporte (OP-16). */
class GoalsRepository(private val api: ApiClient) {

    suspend fun list(): List<Goal> {
        val array = api.get("/objetivos").getJSONArray("objetivos")
        return (0 until array.length()).map { goal(array.getJSONObject(it)) }
    }

    suspend fun create(name: String, target: BigDecimal, months: Int) {
        val body = JSONObject()
            .put("nome", name.trim())
            .put("valor_meta", target.toMoney())
            .put("prazo_meses", months)
        api.post("/objetivos", body)
    }

    suspend fun detail(id: Long): GoalDetail {
        val json = api.get("/objetivos/$id")
        val history = json.getJSONArray("aportes")
        return GoalDetail(
            goal = goal(json),
            monthlyContribution = BigDecimal(json.getString("aporte_mensal")),
            forecast = if (json.isNull("previsao_conclusao")) null else YearMonth.parse(json.getString("previsao_conclusao")),
            completed = json.getBoolean("concluido"),
            contributions = (0 until history.length()).map { i ->
                val item = history.getJSONObject(i)
                Contribution(
                    id = item.getLong("id"),
                    title = item.getString("titulo"),
                    date = LocalDate.parse(item.getString("data")),
                    amount = BigDecimal(item.getString("valor")),
                )
            },
        )
    }

    suspend fun addContribution(goalId: Long, type: ContributionType, amount: BigDecimal, date: LocalDate) {
        val body = JSONObject()
            .put("tipo", type.api)
            .put("valor", amount.toMoney())
            .put("data", date.toString())
        api.post("/objetivos/$goalId/aportes", body)
    }

    /** Lista e detalhe trazem os mesmos campos básicos. */
    private fun goal(json: JSONObject) = Goal(
        id = json.getLong("id"),
        name = json.getString("nome"),
        current = BigDecimal(json.getString("valor_acumulado")),
        target = BigDecimal(json.getString("valor_meta")),
        thumbnail = goalThumbnail(json.getLong("id"), json.getString("nome")),
    )
}

/**
 * A API devolve `imagem_url`, mas o catálogo de imagens ainda não foi decidido (D-10) e o app não
 * tem biblioteca de imagens. Por isso a miniatura é uma das 3 do app: pelo assunto do nome
 * quando dá para reconhecer, senão alternando por id.
 */
private fun goalThumbnail(id: Long, name: String): Int {
    val lower = name.lowercase()
    return when {
        "reserva" in lower || "emerg" in lower -> R.drawable.img_goal_emergency
        "viagem" in lower || "europa" in lower || "férias" in lower -> R.drawable.img_goal_travel
        "notebook" in lower || "computador" in lower -> R.drawable.img_goal_notebook
        else -> listOf(R.drawable.img_goal_emergency, R.drawable.img_goal_travel, R.drawable.img_goal_notebook)[(id % 3).toInt()]
    }
}
