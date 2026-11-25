package com.unasp.sobra.navigation

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.unasp.sobra.R
import com.unasp.sobra.data.Graph
import com.unasp.sobra.ui.components.BottomTab
import com.unasp.sobra.ui.components.AddContributionDialog
import com.unasp.sobra.ui.components.CategoryAmountDialog
import com.unasp.sobra.ui.components.GoalTermPickerDialog
import com.unasp.sobra.ui.components.LoadingScreen
import com.unasp.sobra.ui.components.CategoryPickerDialog
import com.unasp.sobra.ui.components.ExpenseDatePickerDialog
import com.unasp.sobra.ui.components.MonthPickerDialog
import com.unasp.sobra.ui.model.Category
import com.unasp.sobra.ui.screens.auth.LoginScreen
import com.unasp.sobra.ui.screens.auth.RecoverPasswordScreen
import com.unasp.sobra.ui.screens.auth.RegisterScreen
import com.unasp.sobra.ui.screens.expenses.ExpensesScreen
import com.unasp.sobra.ui.screens.expenses.NewExpenseScreen
import com.unasp.sobra.ui.screens.goals.GoalDetailScreen
import com.unasp.sobra.ui.screens.goals.GoalsScreen
import com.unasp.sobra.ui.screens.goals.NewGoalScreen
import com.unasp.sobra.ui.screens.home.HomeScreen
import com.unasp.sobra.ui.screens.onboarding.OnboardingPage
import com.unasp.sobra.ui.screens.onboarding.OnboardingPages
import com.unasp.sobra.ui.screens.onboarding.SplashScreen
import com.unasp.sobra.ui.screens.profile.ProfileScreen
import com.unasp.sobra.ui.screens.setup.CategoriesScreen
import com.unasp.sobra.ui.screens.setup.IncomeScreen
import com.unasp.sobra.ui.screens.simulate.SavedSimulationsScreen
import com.unasp.sobra.ui.screens.simulate.SimulateScreen
import com.unasp.sobra.ui.screens.simulate.SimulationResultScreen
import com.unasp.sobra.ui.screens.states.ConfirmationDialogProperties
import com.unasp.sobra.ui.screens.states.ConfirmationModal
import com.unasp.sobra.ui.screens.states.ErrorScreen
import com.unasp.sobra.ui.screens.states.HomeSkeletonScreen
import com.unasp.sobra.ui.screens.states.SessionExpiredScreen
import com.unasp.sobra.ui.viewmodel.AuthViewModel
import com.unasp.sobra.ui.viewmodel.ExpensesViewModel
import com.unasp.sobra.ui.viewmodel.GoalDetailViewModel
import com.unasp.sobra.ui.viewmodel.GoalsViewModel
import com.unasp.sobra.ui.viewmodel.NewGoalViewModel
import com.unasp.sobra.ui.viewmodel.SavedSimulationsViewModel
import com.unasp.sobra.ui.viewmodel.SimulationViewModel
import com.unasp.sobra.ui.viewmodel.Load
import com.unasp.sobra.ui.viewmodel.NewExpenseViewModel
import com.unasp.sobra.ui.viewmodel.ProfileViewModel
import com.unasp.sobra.ui.viewmodel.SetupViewModel
import com.unasp.sobra.ui.viewmodel.HomeViewModel
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.delay

/** Uma rota por tela do Figma (24 no total). */
object Routes {
    const val ARG_GOAL_ID = "goalId"

    const val SPLASH = "splash"                         // 01
    const val ONBOARDING_1 = "onboarding/1"             // 02
    const val ONBOARDING_2 = "onboarding/2"             // 03
    const val ONBOARDING_3 = "onboarding/3"             // 04
    const val LOGIN = "login"                           // 05
    const val REGISTER = "register"                     // 06
    const val RECOVER_PASSWORD = "recover_password"     // 07
    const val INCOME = "setup/income"                   // 08
    const val CATEGORIES = "setup/categories"           // 09
    const val HOME = "home"                             // 10
    const val EXPENSES = "expenses"                     // 11
    const val NEW_EXPENSE = "expenses/new"              // 12
    const val GOALS = "goals"                           // 13
    const val NEW_GOAL = "goals/new"                    // 14
    const val GOAL_DETAIL = "goals/{$ARG_GOAL_ID}"      // 15
    const val SIMULATE = "simulate"                     // 16
    const val SIMULATION_RESULT = "simulate/result"     // 17
    const val SAVED_SIMULATIONS = "simulate/saved"      // 18
    const val PROFILE = "profile"                       // 19
    const val GOALS_EMPTY = "goals/empty"               // 20
    const val HOME_SKELETON = "home/loading"            // 21
    const val ERROR = "error"                           // 22
    const val CONFIRMATION = "confirmation"             // 23
    const val SESSION_EXPIRED = "session_expired"       // 24

    fun goalDetail(goalId: Long) = "goals/$goalId"
}

// Tempo da tela de marca antes de seguir para o onboarding.
// TODO: tempo de exibição e transição da Splash não definidos no Figma.
private const val SPLASH_DURATION_MS = 1200L

private val onboardingRoutes = listOf(Routes.ONBOARDING_1, Routes.ONBOARDING_2, Routes.ONBOARDING_3)

// `when` é o switch do Kotlin; como cobre todos os valores do enum, não precisa de default.
private val BottomTab.route: String
    get() = when (this) {
        BottomTab.Home -> Routes.HOME
        BottomTab.Expenses -> Routes.EXPENSES
        BottomTab.Goals -> Routes.GOALS
        BottomTab.Simulate -> Routes.SIMULATE
        BottomTab.Profile -> Routes.PROFILE
    }

/** Troca de aba mantendo o estado de cada uma (posição de rolagem, campos preenchidos). */
private fun NavHostController.navigateToTab(tab: BottomTab) {
    navigate(tab.route) {
        // A Home é a raiz das abas (o início do grafo é a Splash, que já saiu da pilha):
        // voltamos até ela para não empilhar uma aba em cima da outra.
        popUpTo(Routes.HOME) { saveState = true }
        launchSingleTop = true  // não cria uma segunda cópia se a aba já está no topo
        restoreState = true     // devolve o estado salvo da aba de destino
    }
}

/** Navega e esvazia a pilha: "voltar" não retorna para as telas anteriores (login, logout). */
private fun NavHostController.navigateClearingStack(route: String) {
    navigate(route) {
        popUpTo(graph.id) { inclusive = true }
    }
}

// TODO: duração e tipo das transições entre telas não definidos no design (hoje: padrão
//  do Navigation Compose).

/**
 * Grafo de navegação do app.
 *
 * As telas são "sem estado": cada bloco `composable(...)` abaixo guarda o estado mínimo
 * da tela com `remember` ou ViewModel. Todas as telas com dados já usam a API; o DemoData
 * ficou só para os @Preview.
 */
@Composable
fun SobraNavHost(modifier: Modifier = Modifier) {
    val nav = rememberNavController()
    val onTab: (BottomTab) -> Unit = { nav.navigateToTab(it) }

    // Simular (telas 16 e 17): entradas e resultado compartilhados; sobrevive à rotação da tela.
    val simulation: SimulationViewModel = viewModel()

    // Renda e categorias da configuração inicial: um só estado para os dois passos.
    val setup: SetupViewModel = viewModel()

    // Quando a API diz que a sessão venceu (qualquer tela), abre a tela 24 e limpa a pilha.
    LaunchedEffect(Unit) {
        Graph.session.expired.collect { nav.navigateClearingStack(Routes.SESSION_EXPIRED) }
    }

    NavHost(navController = nav, startDestination = Routes.SPLASH, modifier = modifier) {

        // ---- 01 Splash ----
        composable(Routes.SPLASH) {
            LightStatusBarIcons()
            // LaunchedEffect(Unit) roda uma única vez quando a tela entra; delay não trava a UI.
            LaunchedEffect(Unit) {
                delay(SPLASH_DURATION_MS)
                // Com token salvo, pula onboarding e login (se o token estiver vencido, a Home
                // recebe SESSAO_EXPIRADA e abre a tela 24).
                val next = if (Graph.session.token != null) Routes.HOME else Routes.ONBOARDING_1
                nav.navigate(next) { popUpTo(Routes.SPLASH) { inclusive = true } }
            }
            SplashScreen()
        }

        // ---- 02, 03, 04 Onboarding ----
        onboardingRoutes.forEachIndexed { index, route ->
            composable(route) {
                OnboardingPage(
                    content = OnboardingPages[index],
                    pageIndex = index,
                    pageCount = OnboardingPages.size,
                    onSkip = { nav.navigate(Routes.LOGIN) },
                    // getOrNull devolve null depois da última página: aí seguimos para o login.
                    onNext = { nav.navigate(onboardingRoutes.getOrNull(index + 1) ?: Routes.LOGIN) },
                )
            }
        }

        // ---- 05 Login ----
        composable(Routes.LOGIN) {
            // rememberSaveable sobrevive à rotação da tela; remember sozinho, não.
            var email by rememberSaveable { mutableStateOf("") }
            // A senha fica só em memória de propósito (não é salva no estado da Activity).
            var password by remember { mutableStateOf("") }
            var passwordVisible by rememberSaveable { mutableStateOf(false) }
            val auth: AuthViewModel = viewModel()
            LoginScreen(
                errorMessage = auth.error,
                email = email, onEmailChange = { email = it },
                password = password, onPasswordChange = { password = it },
                passwordVisible = passwordVisible,
                onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                onBack = { nav.popBackStack() },
                onForgotPassword = { nav.navigate(Routes.RECOVER_PASSWORD) },
                onLogin = {
                    auth.login(email, password) { setupDone ->
                        setup.reset() // outra pessoa pode ter usado o app antes
                        // Conta nova sem configuração segue para renda e categorias.
                        nav.navigateClearingStack(if (setupDone) Routes.HOME else Routes.INCOME)
                    }
                },
                onCreateAccount = { nav.navigate(Routes.REGISTER) },
            )
        }

        // ---- 06 Cadastro ----
        composable(Routes.REGISTER) {
            var name by rememberSaveable { mutableStateOf("") }
            var email by rememberSaveable { mutableStateOf("") }
            var password by remember { mutableStateOf("") }
            var confirmPassword by remember { mutableStateOf("") }
            val auth: AuthViewModel = viewModel()
            RegisterScreen(
                errorMessage = auth.error,
                name = name, onNameChange = { name = it },
                email = email, onEmailChange = { email = it },
                password = password, onPasswordChange = { password = it },
                confirmPassword = confirmPassword, onConfirmPasswordChange = { confirmPassword = it },
                onBack = { nav.popBackStack() },
                onCreateAccount = {
                    auth.register(name, email, password, confirmPassword) {
                        setup.reset()
                        nav.navigate(Routes.INCOME)
                    }
                },
                onGoToLogin = { nav.popBackStack(Routes.LOGIN, inclusive = false) },
            )
        }

        // ---- 07 Recuperar senha ----
        composable(Routes.RECOVER_PASSWORD) {
            var email by rememberSaveable { mutableStateOf("") }
            val auth: AuthViewModel = viewModel()
            RecoverPasswordScreen(
                email = email, onEmailChange = { email = it },
                errorMessage = auth.error,
                infoMessage = auth.info,
                onBack = { nav.popBackStack() },
                // O estado pós-envio não existe no Figma: a confirmação aparece abaixo do campo.
                onSend = { auth.requestPasswordReset(email) },
                onBackToLogin = { nav.popBackStack() },
            )
        }

        // ---- 08 Renda mensal ----
        composable(Routes.INCOME) {
            IncomeScreen(
                income = setup.income, onIncomeChange = setup::changeIncome,
                errorMessage = setup.error,
                onBack = { nav.popBackStack() },
                onContinue = { setup.saveIncome { nav.navigate(Routes.CATEGORIES) } },
            )
        }

        // ---- 09 Categorias ----
        composable(Routes.CATEGORIES) {
            // Tocar num chip abre um pequeno diálogo para informar o valor estimado da categoria.
            var editing by remember { mutableStateOf<Category?>(null) }
            CategoriesScreen(
                budgets = setup.budgets,
                errorMessage = setup.error,
                onCategoryClick = { editing = it },
                onBack = { nav.popBackStack() },
                onFinish = { setup.saveCategories { nav.navigateClearingStack(Routes.HOME) } },
            )
            editing?.let { category ->
                CategoryAmountDialog(
                    category = category,
                    initial = setup.budgets.firstOrNull { it.category == category }?.amount,
                    onConfirm = { setup.setBudget(category, it) },
                    onDismiss = { editing = null },
                )
            }
        }

        // ---- 10 Home ----
        composable(Routes.HOME) {
            val vm: HomeViewModel = viewModel()
            // Roda toda vez que a Home volta a aparecer (ex.: depois de salvar uma despesa).
            LaunchedEffect(Unit) { vm.refresh() }
            // As três telas que o design já tinha: skeleton (21), erro (22) e a Home (10).
            when (val state = vm.state) {
                Load.Loading -> HomeSkeletonScreen(onTabSelected = onTab)
                Load.Error -> ErrorScreen(
                    onBack = vm::load, // a Home é a raiz: não há tela anterior, então "voltar" recarrega
                    onRetry = vm::load,
                    onTabSelected = onTab,
                )
                is Load.Success -> HomeScreen(
                    userName = state.data.summary.firstName,
                    avatar = R.drawable.img_avatar, // TODO: a API devolve avatar_url (catálogo de imagens)
                    income = state.data.summary.income,
                    spending = state.data.summary.spending,
                    goals = state.data.goals,
                    onGoalClick = { nav.navigate(Routes.goalDetail(it.id)) },
                    onSeeAllGoals = { onTab(BottomTab.Goals) },
                    onAddGoal = { nav.navigate(Routes.NEW_GOAL) },
                    onNotificationsClick = { /* TODO: tela de notificações não existe no Figma */ },
                    onTabSelected = onTab,
                )
            }
        }

        // ---- 11 Gastos ----
        composable(Routes.EXPENSES) {
            val vm: ExpensesViewModel = viewModel()
            var pickingMonth by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { vm.refresh() }
            when (val state = vm.state) {
                Load.Error -> ErrorScreen(onBack = vm::load, onRetry = vm::load, onTabSelected = onTab)
                else -> ExpensesScreen(
                    month = vm.month,
                    // Enquanto carrega, mostra a tela sem itens (não há skeleton de Gastos no design).
                    expenses = (state as? Load.Success)?.data?.expenses.orEmpty(),
                    onMonthClick = { pickingMonth = true },
                    onAddExpense = { nav.navigate(Routes.NEW_EXPENSE) },
                    onTabSelected = onTab,
                )
            }
            if (pickingMonth) {
                MonthPickerDialog(
                    selected = vm.month,
                    onSelect = vm::selectMonth,
                    onDismiss = { pickingMonth = false },
                )
            }
        }

        // ---- 12 Nova despesa ----
        composable(Routes.NEW_EXPENSE) {
            val vm: NewExpenseViewModel = viewModel()
            var pickingCategory by remember { mutableStateOf(false) }
            var pickingDate by remember { mutableStateOf(false) }
            NewExpenseScreen(
                amount = vm.amount, onAmountChange = vm::changeAmount,
                description = vm.description, onDescriptionChange = vm::changeDescription,
                category = vm.category,
                onCategoryClick = { pickingCategory = true },
                date = vm.date,
                isToday = vm.date == LocalDate.now(),
                onDateClick = { pickingDate = true },
                errorMessage = vm.error,
                onClose = { nav.popBackStack() },
                onSave = {
                    vm.save {
                        // O modal de confirmação do Figma aparece sobre a Home: voltamos até
                        // ela e abrimos o modal por cima. A Home e os Gastos se atualizam sozinhos.
                        nav.popBackStack(Routes.HOME, inclusive = false)
                        nav.navigate(Routes.CONFIRMATION)
                    }
                },
                onCancel = { nav.popBackStack() },
            )
            if (pickingCategory) {
                CategoryPickerDialog(vm.category, vm::changeCategory) { pickingCategory = false }
            }
            if (pickingDate) {
                ExpenseDatePickerDialog(vm.date, vm::changeDate) { pickingDate = false }
            }
        }

        // ---- 13 Objetivos (e 20 Estado vazio, quando a lista vem vazia) ----
        composable(Routes.GOALS) {
            val vm: GoalsViewModel = viewModel()
            LaunchedEffect(Unit) { vm.refresh() }
            when (val state = vm.state) {
                Load.Loading -> LoadingScreen(BottomTab.Goals, onTab)
                Load.Error -> ErrorScreen(onBack = vm::load, onRetry = vm::load, onTabSelected = onTab)
                is Load.Success -> GoalsScreen(
                    goals = state.data,
                    onGoalClick = { nav.navigate(Routes.goalDetail(it.id)) },
                    onAddGoal = { nav.navigate(Routes.NEW_GOAL) },
                    onTabSelected = onTab,
                )
            }
        }

        // ---- 14 Novo objetivo ----
        composable(Routes.NEW_GOAL) {
            val vm: NewGoalViewModel = viewModel()
            var pickingTerm by remember { mutableStateOf(false) }
            val startMonth = remember { YearMonth.now() }
            NewGoalScreen(
                name = vm.name, onNameChange = vm::changeName,
                target = vm.target, onTargetChange = vm::changeTarget,
                months = vm.months,
                startMonth = startMonth,
                onDeadlineClick = { pickingTerm = true },
                errorMessage = vm.error,
                onBack = { nav.popBackStack() },
                onCreate = { vm.create { nav.popBackStack() } }, // a lista se atualiza ao reaparecer
            )
            if (pickingTerm) {
                GoalTermPickerDialog(vm.months, vm::changeMonths) { pickingTerm = false }
            }
        }

        // ---- 15 Detalhe do objetivo ----
        composable(
            route = Routes.GOAL_DETAIL,
            arguments = listOf(navArgument(Routes.ARG_GOAL_ID) { type = NavType.LongType }),
        ) {
            // O id da rota chega ao ViewModel pelo SavedStateHandle.
            val vm: GoalDetailViewModel = viewModel()
            var adding by remember { mutableStateOf(false) }
            when (val state = vm.state) {
                Load.Loading -> LoadingScreen()
                Load.Error -> ErrorScreen(onBack = { nav.popBackStack() }, onRetry = vm::load, onTabSelected = onTab)
                is Load.Success -> GoalDetailScreen(
                    goal = state.data.goal,
                    monthlyContribution = state.data.monthlyContribution,
                    forecast = state.data.forecast,
                    contributions = state.data.contributions,
                    onBack = { nav.popBackStack() },
                    onEdit = { /* TODO: editar objetivo não existe no Figma nem na API (só criar e listar) */ },
                    onAddContribution = { vm.clearError(); adding = true },
                )
            }
            if (adding) {
                AddContributionDialog(
                    errorMessage = vm.error,
                    saving = vm.saving,
                    onConfirm = { type, amount, date -> vm.addContribution(type, amount, date) { adding = false } },
                    onDismiss = { adding = false },
                )
            }
        }

        // ---- 16 Simular ----
        composable(Routes.SIMULATE) {
            SimulateScreen(
                initial = simulation.initial, onInitialChange = simulation::changeInitial,
                monthly = simulation.monthly, onMonthlyChange = simulation::changeMonthly,
                months = simulation.months, onMonthsChange = simulation::changeMonths,
                errorMessage = simulation.error,
                // O cálculo é feito pela API (a mesma regra da simulação salva).
                onSimulate = { simulation.calculate { nav.navigate(Routes.SIMULATION_RESULT) } },
                onTabSelected = onTab,
            )
        }

        // ---- 17 Resultado da simulação ----
        composable(Routes.SIMULATION_RESULT) {
            val result = simulation.result
            if (result == null) {
                // Não deveria acontecer (o ViewModel guarda o resultado); volta para o formulário.
                LaunchedEffect(Unit) { nav.popBackStack() }
            } else {
                SimulationResultScreen(
                    result = result,
                    errorMessage = simulation.error,
                    onBack = { nav.popBackStack() },
                    onSave = { simulation.save { nav.navigate(Routes.SAVED_SIMULATIONS) } },
                    onNewSimulation = { simulation.clearError(); nav.popBackStack() },
                )
            }
        }

        // ---- 18 Simulações salvas ----
        composable(Routes.SAVED_SIMULATIONS) {
            val vm: SavedSimulationsViewModel = viewModel()
            when (val state = vm.state) {
                Load.Loading -> LoadingScreen(BottomTab.Simulate, onTab)
                Load.Error -> ErrorScreen(onBack = { nav.popBackStack() }, onRetry = vm::load, onTabSelected = onTab)
                is Load.Success -> SavedSimulationsScreen(
                    simulations = state.data,
                    onBack = { nav.popBackStack() },
                    onTabSelected = onTab,
                )
            }
        }

        // ---- 19 Perfil ----
        composable(Routes.PROFILE) {
            val vm: ProfileViewModel = viewModel()
            LaunchedEffect(Unit) { vm.refresh() }
            val profile = (vm.state as? Load.Success)?.data
            if (vm.state == Load.Error) {
                ErrorScreen(onBack = vm::load, onRetry = vm::load, onTabSelected = onTab)
                return@composable
            }
            ProfileScreen(
                // Enquanto carrega, os campos ficam vazios (não há skeleton do Perfil no design).
                initials = profile?.initials.orEmpty(),
                name = profile?.name.orEmpty(),
                email = profile?.email.orEmpty(),
                notificationsEnabled = profile?.notificationsEnabled ?: true,
                onNotificationsChange = vm::setNotifications,
                onPersonalDataClick = { /* TODO: tela não existe no Figma */ },
                onSecurityClick = { /* TODO: tela não existe no Figma */ },
                onAboutClick = { /* TODO: tela não existe no Figma */ },
                onTermsClick = { /* TODO: tela não existe no Figma */ },
                onLogout = {
                    setup.reset()
                    simulation.reset()
                    Graph.auth.logout()
                    nav.navigateClearingStack(Routes.LOGIN)
                },
                onTabSelected = onTab,
            )
        }

        // ---- 20 Estado vazio de Objetivos ----
        composable(Routes.GOALS_EMPTY) {
            GoalsScreen(
                goals = emptyList(),
                onGoalClick = {},
                onAddGoal = { nav.navigate(Routes.NEW_GOAL) },
                onTabSelected = onTab,
            )
        }

        // ---- 21 Skeleton da Home ----
        // TODO(back end): mostrar enquanto os dados da Home carregam.
        composable(Routes.HOME_SKELETON) { HomeSkeletonScreen(onTabSelected = onTab) }

        // ---- 22 Estado de erro ----
        // TODO(back end): navegar para cá quando o carregamento falhar.
        composable(Routes.ERROR) {
            ErrorScreen(
                onBack = { nav.popBackStack() },
                onRetry = { nav.popBackStack() }, // TODO: política de retry não definida
                onTabSelected = onTab,
            )
        }

        // ---- 23 Confirmação ----
        // `dialog` abre o destino como janela por cima da tela atual, que continua visível.
        dialog(Routes.CONFIRMATION, dialogProperties = ConfirmationDialogProperties) {
            ConfirmationModal(onDismiss = { nav.popBackStack() })
        }

        // ---- 24 Sessão expirada ----
        // TODO(back end): navegar para cá (limpando a pilha) quando o token expirar.
        composable(Routes.SESSION_EXPIRED) {
            SessionExpiredScreen(onLogin = { nav.navigateClearingStack(Routes.LOGIN) })
        }
    }
}

/**
 * Deixa os ícones da status bar brancos enquanto a tela de fundo Teal (Splash) está visível
 * e devolve os ícones escuros ao sair.
 */
@Composable
private fun LightStatusBarIcons() {
    val activity = LocalActivity.current as? ComponentActivity ?: return
    // DisposableEffect: o bloco roda ao entrar na tela e `onDispose` roda ao sair.
    DisposableEffect(activity) {
        val controller = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
        controller.isAppearanceLightStatusBars = false // false = ícones claros
        onDispose { controller.isAppearanceLightStatusBars = true }
    }
}
