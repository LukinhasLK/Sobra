package com.unasp.sobra.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiExceptionTest {

    private fun error(codigo: String, status: Int = 401, campos: Map<String, String> = emptyMap()) =
        ApiException(status, codigo, "mensagem geral", campos)

    @Test
    fun expiredOrUnknownTokenSendsThePersonBackToLogin() {
        assertTrue(error("SESSAO_EXPIRADA").isSessionExpired)
        assertTrue(error("NAO_AUTENTICADO").isSessionExpired)
    }

    @Test
    fun wrongPasswordIsNotAnExpiredSession() {
        // Credenciais erradas no login mostram a mensagem no formulário; não abrem a tela 24.
        assertFalse(error("CREDENCIAIS_INVALIDAS").isSessionExpired)
        assertFalse(error("VALIDACAO", 422).isSessionExpired)
    }

    @Test
    fun statusZeroMeansNoConnection() {
        assertTrue(ApiException(0, ApiException.CODE_NETWORK, "sem rede").isNetwork)
        assertFalse(error("ERRO_INTERNO", 500).isNetwork)
    }

    @Test
    fun displayMessagePrefersTheFirstFieldMessage() {
        val withFields = error("VALIDACAO", 422, linkedMapOf("senha" to "Senha muito curta.", "email" to "E-mail inválido."))
        assertEquals("Senha muito curta.", withFields.displayMessage)
    }

    @Test
    fun displayMessageFallsBackToTheGeneralMessage() {
        assertEquals("mensagem geral", error("CREDENCIAIS_INVALIDAS").displayMessage)
    }
}
