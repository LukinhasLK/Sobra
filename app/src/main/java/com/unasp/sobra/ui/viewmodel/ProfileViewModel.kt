package com.unasp.sobra.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unasp.sobra.data.Graph
import com.unasp.sobra.data.ProfileData
import com.unasp.sobra.data.remote.ApiException
import kotlinx.coroutines.launch
import org.json.JSONException

class ProfileViewModel : ViewModel() {
    var state by mutableStateOf<Load<ProfileData>>(Load.Loading)
        private set

    fun load() {
        state = Load.Loading
        viewModelScope.launch { state = loadResult { Graph.profile.load() } }
    }

    fun refresh() {
        if (state is Load.Success) return // o perfil só muda por esta tela
        load()
    }

    /** Muda o interruptor na hora e desfaz se a API recusar. */
    fun setNotifications(enabled: Boolean) {
        val current = (state as? Load.Success)?.data ?: return
        state = Load.Success(current.copy(notificationsEnabled = enabled))
        viewModelScope.launch {
            try {
                Graph.profile.setNotifications(enabled)
            } catch (e: ApiException) {
                state = Load.Success(current)
            } catch (e: JSONException) {
                state = Load.Success(current)
            }
        }
    }
}
