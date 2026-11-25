package com.unasp.sobra.data

import com.unasp.sobra.data.remote.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONObject

/** Cadastro, login e logout (OP-01, OP-02, OP-03 da API). */
class AuthRepository(private val api: ApiClient, private val session: SessionStore) {

    // Escopo do app inteiro: o logout continua mesmo se a tela que o pediu já saiu.
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** @return `true` se a configuração inicial (renda e categorias) já foi feita. */
    suspend fun login(email: String, password: String): Boolean {
        val body = JSONObject().put("email", email.trim()).put("senha", password)
        return saveSession(api.post("/auth/login", body))
    }

    suspend fun register(name: String, email: String, password: String, confirmation: String): Boolean {
        val body = JSONObject()
            .put("nome_completo", name.trim())
            .put("email", email.trim())
            .put("senha", password)
            .put("confirmacao_senha", confirmation)
        return saveSession(api.post("/auth/cadastro", body))
    }

    /** OP-04: a API responde igual exista ou não o e-mail (não revela quem tem conta). */
    suspend fun requestPasswordReset(email: String) {
        api.post("/auth/recuperar-senha", JSONObject().put("email", email.trim()))
    }

    /** Sai na hora (apaga o token local) e avisa o servidor em segundo plano; falha de rede é ignorada. */
    fun logout() {
        val token = session.token
        session.clear()
        if (token != null) {
            appScope.launch { runCatching { api.post("/auth/logout", null, token) } }
        }
    }

    private fun saveSession(json: JSONObject): Boolean {
        session.save(json.getJSONObject("sessao").getString("token"))
        return json.optBoolean("configuracao_concluida", false)
    }
}
