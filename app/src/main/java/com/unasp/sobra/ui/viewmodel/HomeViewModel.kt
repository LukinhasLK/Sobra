package com.unasp.sobra.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unasp.sobra.data.Graph
import com.unasp.sobra.data.HomeData
import com.unasp.sobra.data.remote.ApiException
import java.time.YearMonth
import kotlinx.coroutines.launch
import org.json.JSONException

/** Os três estados da Home que o design já tem: skeleton (21), erro (22) e a tela pronta (10). */
sealed interface HomeState {
    data object Loading : HomeState
    data object Error : HomeState
    data class Success(val data: HomeData) : HomeState
}

class HomeViewModel : ViewModel() {
    var state by mutableStateOf<HomeState>(HomeState.Loading)
        private set

    init {
        load()
    }

    /** Também é o "Tentar novamente" da tela de erro. */
    fun load() {
        state = HomeState.Loading
        viewModelScope.launch {
            state = try {
                HomeState.Success(Graph.home.load(YearMonth.now()))
            } catch (e: ApiException) {
                // Sessão vencida já foi tratada no ApiClient (abre a tela 24); aqui só mostra o erro.
                HomeState.Error
            } catch (e: JSONException) {
                HomeState.Error // resposta fora do formato esperado
            } catch (e: NumberFormatException) {
                HomeState.Error // valor em dinheiro inválido
            }
        }
    }
}
