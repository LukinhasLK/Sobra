package com.unasp.sobra.data

import android.content.Context
import com.unasp.sobra.data.remote.ApiClient

/**
 * "Caixa de ferramentas" do app: cria uma vez e entrega onde precisar (injeção de dependência
 * manual, sem biblioteca). Iniciada em MainActivity.onCreate.
 */
object Graph {
    lateinit var session: SessionStore
        private set
    lateinit var api: ApiClient
        private set
    lateinit var auth: AuthRepository
        private set
    lateinit var home: HomeRepository
        private set
    lateinit var setup: SetupRepository
        private set
    lateinit var expenses: ExpensesRepository
        private set
    lateinit var profile: ProfileRepository
        private set
    lateinit var goals: GoalsRepository
        private set
    lateinit var simulation: SimulationRepository
        private set

    fun init(context: Context) {
        if (this::api.isInitialized) return
        session = SessionStore(context.applicationContext)
        api = ApiClient(ApiConfig.BASE_URL, tokenProvider = { session.token }, onSessionExpired = { session.expire() })
        auth = AuthRepository(api, session)
        home = HomeRepository(api)
        setup = SetupRepository(api)
        expenses = ExpensesRepository(api)
        profile = ProfileRepository(api)
        goals = GoalsRepository(api)
        simulation = SimulationRepository(api)
    }
}
