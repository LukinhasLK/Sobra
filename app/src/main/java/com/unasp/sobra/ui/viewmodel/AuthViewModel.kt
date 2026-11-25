package com.unasp.sobra.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unasp.sobra.data.Graph
import com.unasp.sobra.data.remote.ApiException
import kotlinx.coroutines.launch
import org.json.JSONException

/**
 * Estado e ações de Login e Cadastro. O ViewModel sobrevive à rotação da tela;
 * por isso a chamada de rede não é refeita quando o aparelho gira.
 */
class AuthViewModel : ViewModel() {
    var loading by mutableStateOf(false)
        private set

    /** Mensagem de erro para mostrar na tela, ou null. */
    var error by mutableStateOf<String?>(null)
        private set

    /** Aviso de sucesso (ex.: "link enviado"), mostrado em Teal. */
    var info by mutableStateOf<String?>(null)
        private set

    fun clearError() {
        error = null
    }

    /** Tela 07. A API responde igual para e-mail existente ou não, então o texto também é neutro. */
    fun requestPasswordReset(email: String) {
        if (email.isBlank()) {
            error = "Informe seu e-mail."
            return
        }
        if (loading) return
        loading = true
        error = null
        info = null
        viewModelScope.launch {
            val message = runSubmit { Graph.auth.requestPasswordReset(email) }
            loading = false
            if (message == null) info = "Se o e-mail estiver cadastrado, você receberá as instruções em instantes." else error = message
        }
    }

    /** @param onDone recebe `true` se a configuração inicial já foi feita (vai direto à Home). */
    fun login(email: String, password: String, onDone: (setupDone: Boolean) -> Unit) {
        if (email.isBlank() || password.isEmpty()) {
            error = "Informe o e-mail e a senha."
            return
        }
        submit(onDone) { Graph.auth.login(email, password) }
    }

    fun register(name: String, email: String, password: String, confirmation: String, onDone: () -> Unit) {
        if (name.isBlank() || email.isBlank() || password.isEmpty()) {
            error = "Preencha todos os campos."
            return
        }
        if (password != confirmation) {
            error = "As senhas não são iguais."
            return
        }
        submit({ onDone() }) { Graph.auth.register(name, email, password, confirmation) }
    }

    private fun submit(onDone: (Boolean) -> Unit, action: suspend () -> Boolean) {
        if (loading) return // evita enviar duas vezes com toques repetidos
        loading = true
        error = null
        viewModelScope.launch {
            try {
                onDone(action())
            } catch (e: ApiException) {
                error = e.displayMessage
            } catch (e: JSONException) {
                error = "Resposta inesperada do servidor."
            } finally {
                loading = false
            }
        }
    }
}
