package com.unasp.sobra.navigation

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.unasp.sobra.data.DemoData
import com.unasp.sobra.domain.SimulationResult
import com.unasp.sobra.domain.simulate
import com.unasp.sobra.ui.components.BottomTab
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
 * da tela com `remember` e usa DemoData no lugar dos dados reais.
 * TODO(back end): trocar cada `remember`/DemoData por um ViewModel ligado ao repositório.
 */
@Composable
fun SobraNavHost(modifier: Modifier = Modifier) {
    val nav = rememberNavController()
    val onTab: (BottomTab) -> Unit = { nav.navigateToTab(it) }

    // Último resultado calculado em "Simular", lido pela tela de resultado.
    var simulationResult by remember { mutableStateOf<SimulationResult?>(null) }

    NavHost(navController = nav, startDestination = Routes.SPLASH, modifier = modifier) {

        // ---- 01 Splash ----
        composable(Routes.SPLASH) {
            LightStatusBarIcons()
            // LaunchedEffect(Unit) roda uma única vez quando a tela entra; delay não trava a UI.
            LaunchedEffect(Unit) {
                delay(SPLASH_DURATION_MS)
                nav.navigate(Routes.ONBOARDING_1) { popUpTo(Routes.SPLASH) { inclusive = true } }
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
            LoginScreen(
                email = email, onEmailChange = { email = it },
                password = password, onPasswordChange = { password = it },
                passwordVisible = passwordVisible,
                onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                onBack = { nav.popBackStack() },
                onForgotPassword = { nav.navigate(Routes.RECOVER_PASSWORD) },
                // TODO(back end): autenticar antes de navegar; tratar erro de credenciais.
                onLogin = { nav.navigateClearingStack(Routes.HOME) },
                onCreateAccount = { nav.navigate(Routes.REGISTER) },
            )
        }

        // ---- 06 Cadastro ----
        composable(Routes.REGISTER) {
            var name by rememberSaveable { mutableStateOf("") }
            var email by rememberSaveable { mutableStateOf("") }
            var password by remember { mutableStateOf("") }
            var confirmPassword by remember { mutableStateOf("") }
            RegisterScreen(
                name = name, onNameChange = { name = it },
                email = email, onEmailChange = { email = it },
                password = password, onPasswordChange = { password = it },
                confirmPassword = confirmPassword, onConfirmPasswordChange = { confirmPassword = it },
                onBack = { nav.popBackStack() },
                // TODO(back end): criar a conta antes de seguir para a configuração.
                onCreateAccount = { nav.navigate(Routes.INCOME) },
                onGoToLogin = { nav.popBackStack(Routes.LOGIN, inclusive = false) },
            )
        }

        // ---- 07 Recuperar senha ----
        composable(Routes.RECOVER_PASSWORD) {
            var email by rememberSaveable { mutableStateOf("") }
            RecoverPasswordScreen(
                email = email, onEmailChange = { email = it },
                onBack = { nav.popBackStack() },
                // TODO(back end): enviar o e-mail de recuperação. O estado pós-envio não existe no Figma.
                onSend = { nav.popBackStack() },
                onBackToLogin = { nav.popBackStack() },
            )
        }

        // ---- 08 Renda mensal ----
        composable(Routes.INCOME) {
            var income by rememberSaveable { mutableStateOf(DemoData.setupIncome) }
            IncomeScreen(
                income = income, onIncomeChange = { income = it },
                onBack = { nav.popBackStack() },
                onContinue = { nav.navigate(Routes.CATEGORIES) }, // TODO(back end): salvar a renda
            )
        }

        // ---- 09 Categorias ----
        composable(Routes.CATEGORIES) {
            CategoriesScreen(
                budgets = DemoData.categoryBudgets,
                onCategoryClick = { /* TODO: edição do valor da categoria não definida no design */ },
                onBack = { nav.popBackStack() },
                onFinish = { nav.navigateClearingStack(Routes.HOME) }, // TODO(back end): salvar categorias
            )
        }

        // ---- 10 Home ----
        composable(Routes.HOME) {
            HomeScreen(
                userName = DemoData.USER_FIRST_NAME,
                avatar = R.drawable.img_avatar,
                income = DemoData.homeIncome,
                spending = DemoData.homeSpending,
                onNotificationsClick = { /* TODO: tela de notificações não existe no Figma */ },
                onTabSelected = onTab,
            )
        }

        // ---- 11 Gastos ----
        composable(Routes.EXPENSES) {
            ExpensesScreen(
                month = DemoData.expensesMonth,
                expenses = DemoData.expenses,
                onMonthClick = { /* TODO: seletor de mês aberto não existe no Figma */ },
                onAddExpense = { nav.navigate(Routes.NEW_EXPENSE) },
                onTabSelected = onTab,
            )
        }

        // ---- 12 Nova despesa ----
        composable(Routes.NEW_EXPENSE) {
            var amount by rememberSaveable { mutableStateOf(DemoData.newExpenseAmount) }
            var description by rememberSaveable { mutableStateOf(DemoData.NEW_EXPENSE_DESCRIPTION) }
            val today = remember { LocalDate.now() }
            NewExpenseScreen(
                amount = amount, onAmountChange = { amount = it },
                description = description, onDescriptionChange = { description = it },
                category = Category.Food,
                onCategoryClick = { /* TODO: seletor de categoria aberto não existe no Figma */ },
                date = today,
                isToday = true,
                onDateClick = { /* TODO: calendário aberto não existe no Figma */ },
                onClose = { nav.popBackStack() },
                onSave = {
                    // TODO(back end): salvar a despesa antes de confirmar.
                    // O modal de confirmação do Figma aparece sobre a Home: voltamos até
                    // ela e abrimos o modal por cima.
                    nav.popBackStack(Routes.HOME, inclusive = false)
                    nav.navigate(Routes.CONFIRMATION)
                },
                onCancel = { nav.popBackStack() },
            )
        }

        // ---- 13 Objetivos ----
        composable(Routes.GOALS) {
            // Com a lista vazia, a própria GoalsScreen mostra o estado vazio (tela 20).
            GoalsScreen(
                goals = DemoData.goals,
                onGoalClick = { nav.navigate(Routes.goalDetail(it.id)) },
                onAddGoal = { nav.navigate(Routes.NEW_GOAL) },
                onTabSelected = onTab,
            )
        }

        // ---- 14 Novo objetivo ----
        composable(Routes.NEW_GOAL) {
            var name by rememberSaveable { mutableStateOf(DemoData.NEW_GOAL_NAME) }
            var target by rememberSaveable { mutableStateOf(DemoData.newGoalTarget) }
            val startMonth = remember { YearMonth.now() }
            NewGoalScreen(
                name = name, onNameChange = { name = it },
                target = target, onTargetChange = { target = it },
                months = DemoData.NEW_GOAL_MONTHS,
                startMonth = startMonth,
                onDeadlineClick = { /* TODO: seletor de prazo aberto não existe no Figma */ },
                onBack = { nav.popBackStack() },
                onCreate = { nav.popBackStack() }, // TODO(back end): salvar o objetivo
            )
        }

        // ---- 15 Detalhe do objetivo ----
        composable(
            route = Routes.GOAL_DETAIL,
            arguments = listOf(navArgument(Routes.ARG_GOAL_ID) { type = NavType.LongType }),
        ) { entry ->
            val goalId = entry.arguments?.getLong(Routes.ARG_GOAL_ID)
            // TODO(back end): buscar o objetivo e os aportes pelo id.
            val goal = DemoData.goals.firstOrNull { it.id == goalId } ?: DemoData.goals.first()
            GoalDetailScreen(
                goal = goal,
                monthlyContribution = DemoData.goalMonthlyContribution,
                forecast = DemoData.goalForecast,
                contributions = DemoData.contributions,
                onBack = { nav.popBackStack() },
                onEdit = { /* TODO: tela de edição do objetivo não existe no Figma */ },
                onAddContribution = { /* TODO: fluxo de adicionar aporte não existe no Figma */ },
            )
        }

        // ---- 16 Simular ----
        composable(Routes.SIMULATE) {
            var initial by rememberSaveable { mutableStateOf(DemoData.simulationInitial) }
            var monthly by rememberSaveable { mutableStateOf(DemoData.simulationMonthly) }
            // mutableIntStateOf evita "boxing" (Integer) para estados de int.
            var months by rememberSaveable { mutableIntStateOf(DemoData.SIMULATION_MONTHS) }
            SimulateScreen(
                initial = initial, onInitialChange = { initial = it },
                monthly = monthly, onMonthlyChange = { monthly = it },
                months = months, onMonthsChange = { months = it },
                onSimulate = {
                    simulationResult = simulate(initial, monthly, months)
                    nav.navigate(Routes.SIMULATION_RESULT)
                },
                onTabSelected = onTab,
            )
        }

        // ---- 17 Resultado da simulação ----
        composable(Routes.SIMULATION_RESULT) {
            SimulationResultScreen(
                // Se o processo foi recriado e o resultado se perdeu, mostra o exemplo do mockup.
                result = simulationResult ?: DemoData.simulationResult,
                onBack = { nav.popBackStack() },
                onSave = { nav.navigate(Routes.SAVED_SIMULATIONS) }, // TODO(back end): salvar a simulação
                onNewSimulation = { nav.popBackStack() },
            )
        }

        // ---- 18 Simulações salvas ----
        composable(Routes.SAVED_SIMULATIONS) {
            SavedSimulationsScreen(
                simulations = DemoData.savedSimulations,
                onBack = { nav.popBackStack() },
                onTabSelected = onTab,
            )
        }

        // ---- 19 Perfil ----
        composable(Routes.PROFILE) {
            var notifications by rememberSaveable { mutableStateOf(true) }
            ProfileScreen(
                initials = DemoData.USER_INITIALS,
                name = DemoData.USER_FULL_NAME,
                email = DemoData.USER_EMAIL,
                notificationsEnabled = notifications,
                onNotificationsChange = { notifications = it },
                onPersonalDataClick = { /* TODO: tela não existe no Figma */ },
                onSecurityClick = { /* TODO: tela não existe no Figma */ },
                onAboutClick = { /* TODO: tela não existe no Figma */ },
                onTermsClick = { /* TODO: tela não existe no Figma */ },
                onLogout = { nav.navigateClearingStack(Routes.LOGIN) }, // TODO(back end): encerrar a sessão
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
