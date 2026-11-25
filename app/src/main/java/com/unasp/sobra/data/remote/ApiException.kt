package com.unasp.sobra.data.remote

/**
 * Erro devolvido pela API (formato único `{"erro": {...}}`) ou falha de rede.
 * `status` é 0 quando nem chegou a haver resposta (sem internet, servidor desligado).
 */
class ApiException(
    val status: Int,
    val codigo: String,
    override val message: String,
    /** Mensagens por campo, quando o erro é de validação (HTTP 422). Chave = nome do campo. */
    val campos: Map<String, String> = emptyMap(),
) : Exception(message) {

    /**
     * Sessão vencida (SESSAO_EXPIRADA) ou token que o servidor não conhece (NAO_AUTENTICADO, por
     * exemplo depois de o banco ser recriado). Nos dois casos o caminho é o mesmo: voltar ao login.
     */
    val isSessionExpired: Boolean get() = codigo == CODE_SESSION_EXPIRED || codigo == CODE_NOT_AUTHENTICATED
    val isNetwork: Boolean get() = status == 0

    /** Texto único para mostrar na tela: a mensagem do primeiro campo inválido ou a geral. */
    val displayMessage: String get() = campos.values.firstOrNull() ?: message

    companion object {
        const val CODE_SESSION_EXPIRED = "SESSAO_EXPIRADA"
        const val CODE_NOT_AUTHENTICATED = "NAO_AUTENTICADO"
        const val CODE_NETWORK = "SEM_CONEXAO"
    }
}
