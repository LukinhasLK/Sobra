package com.unasp.sobra.ui.viewmodel

import com.unasp.sobra.data.remote.ApiException
import org.json.JSONException

/** Estado de uma tela que carrega dados: carregando, erro ou pronta. */
sealed interface Load<out T> {
    data object Loading : Load<Nothing>
    data object Error : Load<Nothing>
    data class Success<T>(val data: T) : Load<T>
}

/** Roda a busca e devolve [Load.Success], ou [Load.Error] se a API ou a resposta falharem. */
internal suspend fun <T> loadResult(block: suspend () -> T): Load<T> = try {
    Load.Success(block())
} catch (e: ApiException) {
    Load.Error // sessão vencida já foi tratada no ApiClient (abre a tela 24)
} catch (e: JSONException) {
    Load.Error // resposta fora do formato esperado
} catch (e: NumberFormatException) {
    Load.Error // valor em dinheiro ou data inválidos
} catch (e: java.time.format.DateTimeParseException) {
    Load.Error
}

/** Roda uma gravação e devolve a mensagem de erro para a tela, ou null se deu certo. */
internal suspend fun runSubmit(block: suspend () -> Unit): String? = try {
    block()
    null
} catch (e: ApiException) {
    e.displayMessage
} catch (e: JSONException) {
    "Resposta inesperada do servidor."
}
