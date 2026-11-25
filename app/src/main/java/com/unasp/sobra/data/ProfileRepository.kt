package com.unasp.sobra.data

import com.unasp.sobra.data.remote.ApiClient
import org.json.JSONObject

data class ProfileData(
    val name: String,
    val email: String,
    val initials: String,
    val notificationsEnabled: Boolean,
)

/** Perfil (OP-20) e preferência de notificações (OP-21). */
class ProfileRepository(private val api: ApiClient) {

    suspend fun load(): ProfileData {
        val json = api.get("/perfil")
        return ProfileData(
            name = json.getString("nome_completo"),
            email = json.getString("email"),
            initials = json.getString("iniciais"),
            notificationsEnabled = json.getBoolean("notificacoes_ativas"),
        )
    }

    suspend fun setNotifications(enabled: Boolean) {
        api.put("/perfil/notificacoes", JSONObject().put("notificacoes_ativas", enabled))
    }
}
