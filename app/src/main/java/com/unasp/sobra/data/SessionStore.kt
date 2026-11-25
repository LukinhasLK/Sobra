package com.unasp.sobra.data

import android.content.Context
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * Guarda o token da sessão no armazenamento privado do app (SharedPreferences).
 * O backup automático do Android está desligado no manifest para o token não sair do aparelho.
 */
class SessionStore(context: Context) {
    private val prefs = context.getSharedPreferences("sobra_session", Context.MODE_PRIVATE)

    // extraBufferCapacity = 1: o aviso não se perde se ninguém estiver escutando naquele instante.
    private val _expired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /** Emite quando a API diz que a sessão venceu; o NavHost então abre a tela 24. */
    val expired: SharedFlow<Unit> = _expired

    val token: String? get() = prefs.getString(KEY_TOKEN, null)

    fun save(token: String) = prefs.edit().putString(KEY_TOKEN, token).apply()

    fun clear() = prefs.edit().remove(KEY_TOKEN).apply()

    fun expire() {
        clear()
        _expired.tryEmit(Unit)
    }

    private companion object {
        const val KEY_TOKEN = "token"
    }
}
