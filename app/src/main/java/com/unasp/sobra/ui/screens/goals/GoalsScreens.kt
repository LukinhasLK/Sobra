package com.unasp.sobra.ui.screens.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.unasp.sobra.R
import com.unasp.sobra.data.DemoData
import com.unasp.sobra.domain.progressFraction
import com.unasp.sobra.domain.suggestedMonthlyContribution
import com.unasp.sobra.ui.components.BackHeader
import com.unasp.sobra.ui.components.BackLink
import com.unasp.sobra.ui.components.BottomTab
import com.unasp.sobra.ui.components.SelectChip
import com.unasp.sobra.ui.components.ChipGroup
import com.unasp.sobra.ui.components.CircleIconButton
import com.unasp.sobra.ui.components.ContributionRow
import com.unasp.sobra.ui.components.Fab
import com.unasp.sobra.ui.components.FooterActions
import com.unasp.sobra.ui.components.FormScreen
import com.unasp.sobra.ui.components.GoalCard
import com.unasp.sobra.ui.components.PrimaryButton
import com.unasp.sobra.ui.components.ProgressRing
import com.unasp.sobra.ui.components.SobraIcon
import com.unasp.sobra.ui.components.SobraScreen
import com.unasp.sobra.ui.components.StateContent
import com.unasp.sobra.ui.components.TitleHeader
import com.unasp.sobra.ui.components.WhiteMoneyField
import com.unasp.sobra.ui.components.WhiteTextField
import com.unasp.sobra.ui.components.ErrorText
import com.unasp.sobra.ui.components.percentLabel
import com.unasp.sobra.ui.components.sobraCard
import com.unasp.sobra.ui.format.toBrl
import com.unasp.sobra.ui.format.toBrlShort
import com.unasp.sobra.ui.format.toMonthYear
import com.unasp.sobra.ui.model.Contribution
import com.unasp.sobra.ui.model.Goal
import com.unasp.sobra.ui.theme.Dimens
import com.unasp.sobra.ui.theme.SobraColors
import com.unasp.sobra.ui.theme.SobraTheme
import com.unasp.sobra.ui.theme.SobraType
import com.unasp.sobra.ui.theme.manrope
import java.math.BigDecimal
import java.time.YearMonth

private val AddButtonSize = 34.dp

/**
 * 13 Objetivos e 20 Estado vazio. Com a lista vazia, a mesma tela mostra o estado vazio:
 * o header e a navegação são idênticos nas duas.
 */
@Composable
fun GoalsScreen(
    goals: List<Goal>,
    onGoalClick: (Goal) -> Unit,
    onAddGoal: () -> Unit,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isEmpty = goals.isEmpty()
    SobraScreen(
        modifier = modifier,
        bottomTab = BottomTab.Goals,
        onTabSelected = onTabSelected,
        // O estado vazio do Figma não tem FAB: a ação é o botão do próprio estado.
        floatingAction = if (isEmpty) null else {
            { Fab(onAddGoal, stringResource(R.string.goals_add)) }
        },
    ) {
        TitleHeader(
            title = stringResource(R.string.goals_title),
            trailing = {
                CircleIconButton(
                    icon = R.drawable.ic_plus_18,
                    contentDescription = stringResource(R.string.goals_add),
                    onClick = onAddGoal,
                    size = AddButtonSize,
                )
            },
        )
        if (isEmpty) {
            // Fica no TOPO da área de conteúdo (logo abaixo do header), não no centro da tela.
            StateContent(
                icon = R.drawable.ic_target_64,
                circleColor = SobraColors.Soft,
                iconTint = SobraColors.Teal,
                title = stringResource(R.string.goals_empty_title),
                description = stringResource(R.string.goals_empty_description),
                buttonText = stringResource(R.string.goals_empty_button),
                onButtonClick = onAddGoal,
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    start = Dimens.ScreenMargin, top = Dimens.Space24,
                    end = Dimens.ScreenMargin, bottom = Dimens.ListBottomReserve,
                ),
                verticalArrangement = Arrangement.spacedBy(Dimens.Space16),
            ) {
                item(key = "resumo") { GoalsSummary(goals) }
                items(goals, key = { it.id }) { goal -> GoalCard(goal, onClick = { onGoalClick(goal) }) }
            }
        }
    }
}

private val SummaryLabel = manrope(13, FontWeight.W600)
private val SummaryValue = manrope(28, FontWeight.W800)
private val SummaryCaption = manrope(13, FontWeight.W500)

/**
 * Visão geral no topo da lista (mesmo banner da tela Gastos): quanto já foi guardado em todos
 * os objetivos e quanto é a soma das metas.
 */
@Composable
private fun GoalsSummary(goals: List<Goal>) {
    val saved = goals.fold(BigDecimal.ZERO) { acc, goal -> acc + goal.current }
    val target = goals.fold(BigDecimal.ZERO) { acc, goal -> acc + goal.target }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .sobraCard(Dimens.Radius20, background = SobraColors.TealDark, borderColor = null)
            .padding(Dimens.Space20),
        verticalArrangement = Arrangement.spacedBy(Dimens.Space4),
    ) {
        Text(stringResource(R.string.goals_total_saved), style = SummaryLabel, color = SobraColors.TealLight)
        Text(saved.toBrl(), style = SummaryValue, color = SobraColors.OnTeal)
        Text(
            pluralStringResource(R.plurals.goals_total_caption, goals.size, target.toBrlShort(), goals.size),
            style = SummaryCaption, color = SobraColors.TealLight,
        )
    }
}

// ---------------------------------------------------------------------------------------
// 14 Novo objetivo
// ---------------------------------------------------------------------------------------

private val NewGoalDescription = manrope(14, FontWeight.W400)
private val MoneyHighlight = manrope(24, FontWeight.W800)
private val SuggestionHint = manrope(12, FontWeight.W400)

/** 14 Novo objetivo. O aporte sugerido é calculado (valor ÷ meses), não é um campo. */
@Composable
fun NewGoalScreen(
    name: String,
    onNameChange: (String) -> Unit,
    target: BigDecimal,
    onTargetChange: (BigDecimal) -> Unit,
    months: Int,
    onMonthsChange: (Int) -> Unit,
    startMonth: YearMonth,
    onBack: () -> Unit,
    onCreate: () -> Unit,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
    saving: Boolean = false,
) {
    val suggestion = suggestedMonthlyContribution(target, months)
    FormScreen(
        modifier = modifier,
        footer = {
            // Rodapé fixo com o resumo (referência: painel inferior do Stoic): o aporte sugerido
            // fica sempre visível enquanto a pessoa muda valor e prazo, mesmo com o teclado aberto.
            FooterActions(gap = Dimens.Space16) {
                SuggestionCard(suggestion)
                PrimaryButton(
                    stringResource(if (saving) R.string.action_saving else R.string.action_create_goal),
                    onCreate,
                )
            }
        },
    ) {
        // Header: padding 12/24/12/24, gap 20 (voltar → textos) e 4 (título → descrição).
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.ScreenMargin, vertical = Dimens.Space12),
            verticalArrangement = Arrangement.spacedBy(Dimens.Space20),
        ) {
            BackLink(stringResource(R.string.action_back), onBack)
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space4)) {
                Text(stringResource(R.string.new_goal_title), style = SobraType.ScreenTitle, color = SobraColors.TextPrimary)
                Text(stringResource(R.string.new_goal_description), style = NewGoalDescription, color = SobraColors.TextSecondary)
            }
        }
        Column(
            modifier = Modifier.padding(Dimens.Space24),
            verticalArrangement = Arrangement.spacedBy(Dimens.Space20),
        ) {
            WhiteTextField(stringResource(R.string.new_goal_name_label), name, onNameChange)
            WhiteMoneyField(stringResource(R.string.new_goal_target_label), target, onTargetChange)
            // Prazo em chips (um toque), no lugar de um campo que abria uma lista.
            ChipGroup(label = stringResource(R.string.new_goal_deadline_label)) {
                GOAL_TERMS.forEach { option ->
                    SelectChip(
                        // (id, quantidade que decide o plural, valor que entra no %d)
                        text = pluralStringResource(R.plurals.months_count, option, option),
                        selected = option == months,
                        onClick = { onMonthsChange(option) },
                    )
                }
            }
            // O mês final é o mês inicial + prazo.
            Text(
                stringResource(R.string.new_goal_end_month, startMonth.plusMonths(months.toLong()).toMonthYear()),
                style = SobraType.FormLabel, color = SobraColors.TextSecondary,
            )
            ErrorText(errorMessage)
        }
    }
}

/** Prazos oferecidos ao criar um objetivo (a API aceita de 1 a 360 meses). */
private val GOAL_TERMS = listOf(6, 12, 18, 24, 36, 48, 60)

/** Card calculado do rodapé: padding 16/20, gap 4, raio 20, fundo TealLight, sem borda. */
@Composable
private fun SuggestionCard(suggestion: BigDecimal) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .sobraCard(Dimens.Radius20, background = SobraColors.TealLight, borderColor = null)
            .padding(horizontal = Dimens.Space20, vertical = Dimens.Space16),
        verticalArrangement = Arrangement.spacedBy(Dimens.Space4),
    ) {
        Text(stringResource(R.string.new_goal_suggestion_label), style = SobraType.FormLabel, color = SobraColors.TealDark)
        Text(
            stringResource(R.string.new_goal_suggestion_value, suggestion.toBrl()),
            style = MoneyHighlight, color = SobraColors.TealDark,
        )
        // TODO: a cor do texto de explicação não foi informada no Figma (usando TextSecondary).
        Text(stringResource(R.string.new_goal_suggestion_hint), style = SuggestionHint, color = SobraColors.TextSecondary)
    }
}

// ---------------------------------------------------------------------------------------
// 15 Detalhe do objetivo
// ---------------------------------------------------------------------------------------

private val DetailName = manrope(18, FontWeight.W700)
private val DetailValues = manrope(14, FontWeight.W400)
private val InfoLabel = manrope(14, FontWeight.W400)
private val InfoValue = manrope(14, FontWeight.W700)
private val HistoryTitle = manrope(15, FontWeight.W700)

/** 15 Detalhe do objetivo: anel de progresso, informações, histórico e rodapé fixo. */
@Composable
fun GoalDetailScreen(
    goal: Goal,
    monthlyContribution: BigDecimal,
    forecast: YearMonth?,
    contributions: List<Contribution>,
    onBack: () -> Unit,
    onAddContribution: () -> Unit,
    // Editar não existe na API (decisão D-11). Sem ação, o lápis não aparece: um botão que
    // não faz nada confunde mais do que ajuda.
    onEdit: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val progress = progressFraction(goal.current, goal.target)
    SobraScreen(modifier = modifier) {
        BackHeader(
            backText = stringResource(R.string.goals_title),
            onBack = onBack,
            trailing = {
                if (onEdit != null) {
                    // TODO: a cor do ícone de editar não foi informada no Figma (usando TextSecondary).
                    SobraIcon(
                        R.drawable.ic_pencil_20,
                        tint = SobraColors.TextSecondary,
                        contentDescription = stringResource(R.string.goal_detail_edit),
                        modifier = Modifier.clickable(role = Role.Button, onClick = onEdit),
                    )
                }
            },
        )
        // Box empilha os filhos: a lista por baixo e o rodapé fixo por cima, no fim da tela.
        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                // 120 no fim: o último aporte consegue rolar para cima do rodapé fixo.
                contentPadding = PaddingValues(
                    start = Dimens.ScreenMargin, top = Dimens.Space24,
                    end = Dimens.ScreenMargin, bottom = Dimens.ListBottomReserve,
                ),
            ) {
                item {
                    // Card principal: padding 24, gap 20, raio 24.
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .sobraCard(Dimens.Radius24)
                            .padding(Dimens.Space24),
                        verticalArrangement = Arrangement.spacedBy(Dimens.Space20),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        ProgressRing(progress = progress, label = percentLabel(progress))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(Dimens.Space4),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(goal.name, style = DetailName, color = SobraColors.TextPrimary)
                            Text(
                                stringResource(R.string.goal_values, goal.current.toBrlShort(), goal.target.toBrlShort()),
                                style = DetailValues, color = SobraColors.TextSecondary,
                            )
                            GoalRemainingText(goal)
                        }
                    }
                }
                item {
                    Column(
                        modifier = Modifier.padding(top = Dimens.Space24, bottom = Dimens.Space20),
                        verticalArrangement = Arrangement.spacedBy(Dimens.Space12),
                    ) {
                        // Previsão em destaque, com barra lateral (referência: citação do Stoic):
                        // é a resposta para "quando eu chego lá?".
                        ForecastCallout(
                            // Sem previsão (objetivo concluído ou sem aporte planejado) a API devolve nulo.
                            forecast?.toMonthYear() ?: stringResource(R.string.goal_detail_forecast_none),
                        )
                        InfoLine(stringResource(R.string.goal_detail_monthly), monthlyContribution.toBrl(), SobraColors.TextPrimary)
                    }
                }
                item {
                    Text(
                        stringResource(R.string.goal_detail_history),
                        style = HistoryTitle, color = SobraColors.TextPrimary,
                        modifier = Modifier.padding(bottom = Dimens.Space12),
                    )
                }
                items(contributions, key = { it.id }) { contribution ->
                    // Gap 8 entre aportes (padding em cada item, pois a lista mistura blocos diferentes).
                    ContributionRow(contribution, modifier = Modifier.padding(bottom = Dimens.Space8))
                }
            }
            // Rodapé fixo: fundo Background, padding 24.
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(SobraColors.Background)
                    .padding(Dimens.Space24),
            ) {
                PrimaryButton(stringResource(R.string.action_add_contribution), onAddContribution)
            }
        }
    }
}

private val ForecastValue = manrope(20, FontWeight.W800)
private val ForecastBarWidth = 4.dp

/** Previsão de conclusão: barra Teal à esquerda, label pequeno e o mês grande em Teal escuro. */
@Composable
private fun ForecastCallout(value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(Dimens.Space12),
    ) {
        Box(
            modifier = Modifier
                .width(ForecastBarWidth)
                .fillMaxHeight()
                .clip(RoundedCornerShape(Dimens.RadiusFull))
                .background(SobraColors.Teal),
        )
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space2)) {
            Text(stringResource(R.string.goal_detail_forecast), style = InfoLabel, color = SobraColors.TextSecondary)
            Text(value, style = ForecastValue, color = SobraColors.TealDark)
        }
    }
}

/** "Faltam R$ 6.600" ou, quando a meta foi atingida, "Meta atingida". */
@Composable
private fun GoalRemainingText(goal: Goal) {
    val remaining = goal.target - goal.current
    if (remaining.signum() <= 0) {
        Text(stringResource(R.string.goal_done), style = DetailValues, color = SobraColors.TealDark)
    } else {
        Text(stringResource(R.string.goal_remaining, remaining.toBrlShort()), style = DetailValues, color = SobraColors.TextSecondary)
    }
}

/** Linha de informação: padding 14, raio 16, borda 1; label 14/400 e valor 14/700. */
@Composable
private fun InfoLine(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .sobraCard(Dimens.Radius16)
            .padding(Dimens.Space14),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = InfoLabel, color = SobraColors.TextSecondary)
        Text(value, style = InfoValue, color = valueColor)
    }
}

@Preview(widthDp = 402, heightDp = 874)
@Composable
private fun GoalsScreenPreview() {
    SobraTheme { GoalsScreen(DemoData.goals, onGoalClick = {}, onAddGoal = {}, onTabSelected = {}) }
}

@Preview(name = "20 Estado vazio", widthDp = 402, heightDp = 874)
@Composable
private fun GoalsEmptyPreview() {
    SobraTheme { GoalsScreen(emptyList(), onGoalClick = {}, onAddGoal = {}, onTabSelected = {}) }
}

@Preview(widthDp = 402, heightDp = 874)
@Composable
private fun NewGoalScreenPreview() {
    SobraTheme {
        NewGoalScreen(
            name = DemoData.NEW_GOAL_NAME, onNameChange = {},
            target = DemoData.newGoalTarget, onTargetChange = {},
            months = DemoData.NEW_GOAL_MONTHS, startMonth = DemoData.newGoalStart,
            onMonthsChange = {}, onBack = {}, onCreate = {},
        )
    }
}

@Preview(widthDp = 402, heightDp = 874)
@Composable
private fun GoalDetailScreenPreview() {
    SobraTheme {
        GoalDetailScreen(
            goal = DemoData.goals[0],
            monthlyContribution = DemoData.goalMonthlyContribution,
            forecast = DemoData.goalForecast,
            contributions = DemoData.contributions,
            onBack = {}, onAddContribution = {},
        )
    }
}
