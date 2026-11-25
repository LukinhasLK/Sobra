package com.unasp.sobra.data

import com.unasp.sobra.data.remote.ApiClient
import com.unasp.sobra.domain.SimulationResult
import com.unasp.sobra.ui.model.SavedSimulation
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneId
import org.json.JSONObject

/**
 * Simulação: calcular (OP-17), salvar (OP-18) e listar as salvas (OP-19).
 * O cálculo é do servidor: assim a tela e o que fica salvo nunca divergem.
 */
class SimulationRepository(private val api: ApiClient) {

    suspend fun calculate(initial: BigDecimal, monthly: BigDecimal, months: Int): SimulationResult {
        val json = api.post("/simulacoes/calcular", body(initial, monthly, months))
        val series = json.getJSONArray("serie")
        return SimulationResult(
            initial = initial,
            monthlyContribution = monthly,
            months = months,
            totalContributions = BigDecimal(json.getString("total_aportes")),
            totalInvested = BigDecimal(json.getString("total_investido")),
            finalAmount = BigDecimal(json.getString("estimativa_final")),
            estimatedYield = BigDecimal(json.getString("rendimento_estimado")),
            seriesWithYield = (0 until series.length()).map { BigDecimal(series.getJSONObject(it).getString("com_rendimento")) },
            seriesWithoutYield = (0 until series.length()).map { BigDecimal(series.getJSONObject(it).getString("sem_rendimento")) },
        )
    }

    suspend fun save(initial: BigDecimal, monthly: BigDecimal, months: Int) {
        api.post("/simulacoes", body(initial, monthly, months))
    }

    suspend fun listSaved(): List<SavedSimulation> {
        val array = api.get("/simulacoes?limite=50").getJSONArray("simulacoes")
        return (0 until array.length()).map { i ->
            val item = array.getJSONObject(i)
            SavedSimulation(
                id = item.getLong("id"),
                // criado_em vem em UTC ("2026-09-18T15:00:00Z"); a tela mostra o dia no fuso do aparelho.
                date = Instant.parse(item.getString("criado_em")).atZone(ZoneId.systemDefault()).toLocalDate(),
                initial = BigDecimal(item.getString("valor_inicial")),
                monthlyContribution = BigDecimal(item.getString("aporte_mensal")),
                months = item.getInt("prazo_meses"),
                result = BigDecimal(item.getString("resultado")),
                monthlyRate = if (item.isNull("taxa_mensal")) null else BigDecimal(item.getString("taxa_mensal")),
            )
        }
    }

    private fun body(initial: BigDecimal, monthly: BigDecimal, months: Int) = JSONObject()
        .put("valor_inicial", initial.toMoney())
        .put("aporte_mensal", monthly.toMoney())
        .put("prazo_meses", months)
}
