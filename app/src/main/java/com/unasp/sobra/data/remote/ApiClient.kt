package com.unasp.sobra.data.remote

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject

/**
 * Cliente HTTP mínimo para a API do back end (`backend/`). Usa só o que o Android já tem
 * (HttpURLConnection + org.json), então não precisa de biblioteca extra.
 *
 * Todas as funções são `suspend`: rodam em Dispatchers.IO, fora da thread da tela.
 * Erros viram [ApiException]; o chamador (ViewModel) decide o que mostrar.
 */
class ApiClient(
    private val baseUrl: String,
    private val tokenProvider: () -> String?,
    /** Chamado quando a API responde SESSAO_EXPIRADA (a tela 24 é aberta por quem escuta). */
    private val onSessionExpired: () -> Unit,
) {
    suspend fun get(path: String): JSONObject = call("GET", path, null, null)

    suspend fun post(path: String, body: JSONObject? = null, token: String? = tokenProvider()): JSONObject =
        call("POST", path, body, token)

    suspend fun put(path: String, body: JSONObject): JSONObject = call("PUT", path, body, tokenProvider())

    private suspend fun call(method: String, path: String, body: JSONObject?, tokenOverride: String?): JSONObject =
        withContext(Dispatchers.IO) {
            // GET usa o token da sessão atual; POST/PUT já chegam com o token resolvido.
            val token = tokenOverride ?: tokenProvider()
            val conn = URL(baseUrl + path).openConnection() as HttpURLConnection
            try {
                conn.requestMethod = method
                conn.connectTimeout = TIMEOUT_MS
                conn.readTimeout = TIMEOUT_MS
                conn.setRequestProperty("Accept", "application/json")
                if (token != null) conn.setRequestProperty("Authorization", "Bearer $token")
                if (body != null) {
                    conn.doOutput = true
                    conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
                }

                val status = conn.responseCode
                val stream = if (status in 200..299) conn.inputStream else conn.errorStream
                val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()

                if (status in 200..299) {
                    // 204 (logout) vem sem corpo.
                    return@withContext if (text.isBlank()) JSONObject() else JSONObject(text)
                }
                val error = parseError(status, text)
                if (error.isSessionExpired) onSessionExpired()
                throw error
            } catch (e: IOException) {
                throw ApiException(0, ApiException.CODE_NETWORK, "Não foi possível conectar ao servidor. Verifique a conexão.")
            } catch (e: JSONException) {
                throw ApiException(0, "RESPOSTA_INVALIDA", "Resposta inesperada do servidor.")
            } finally {
                conn.disconnect()
            }
        }

    /** Lê `{"erro": {"codigo", "mensagem", "campos": [{"campo", "mensagem"}]}}`. */
    private fun parseError(status: Int, text: String): ApiException {
        return try {
            val erro = JSONObject(text).getJSONObject("erro")
            val campos = linkedMapOf<String, String>()
            val lista = erro.optJSONArray("campos")
            if (lista != null) {
                for (i in 0 until lista.length()) {
                    val item = lista.getJSONObject(i)
                    campos[item.optString("campo")] = item.optString("mensagem")
                }
            }
            ApiException(status, erro.optString("codigo"), erro.optString("mensagem"), campos)
        } catch (e: JSONException) {
            ApiException(status, "ERRO_DESCONHECIDO", "Ocorreu um erro inesperado. Tente novamente.")
        }
    }

    private companion object {
        const val TIMEOUT_MS = 15_000
    }
}
