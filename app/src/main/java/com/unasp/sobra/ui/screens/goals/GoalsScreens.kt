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
import com.unasp.sobra.ui.components.WhiteSelectorField
import com.unasp.sobra.ui.components.WhiteTextField
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
                items(goals, key = { it.id }) { goal -> GoalCard(goal, onClick = { onGoalClick(goal) }) }
            }
        }
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
    startMonth: YearMonth,
    onDeadlineClick: () -> Unit,
    onBack: () -> Unit,
    onCreate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val suggestion = suggestedMonthlyContribution(target, months)
    FormScreen(
        modifier = modifier,
        footer = {
            FooterActions(gap = Dimens.Space20) {
                PrimaryButton(stringResource(R.string.action_create_goal), onCreate)
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
            WhiteSelectorField(
                label = stringResource(R.string.new_goal_deadline_label),
                // "18 meses (Março 2028)": o mês final é o mês inicial + prazo.
                text = stringResource(
                    R.string.new_goal_deadline_value,
                    // (id, quantidade que decide o plural, valor que entra no %d)
                    pluralStringResource(R.plurals.months_count, months, months),
                    startMonth.plusMonths(months.toLong()).toMonthYear(),
                ),
                onClick = onDeadlineClick,
            )
            // Card calculado: padding 20, gap 6, raio 20, fundo TealLight, sem borda.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .sobraCard(Dimens.Radius20, background = SobraColors.TealLight, borderColor = null)
                    .padding(Dimens.Space20),
                verticalArrangement = Arrangement.spacedBy(Dimens.Space6),
            ) {
                Text(stringResource(R.string.new_goal_suggestion_label), style = SobraType.FormLabel, color = SobraColors.Teal)
                Text(
                    stringResource(R.string.new_goal_suggestion_value, suggestion.toBrl()),
                    style = MoneyHighlight, color = SobraColors.Teal,
                )
                // TODO: a cor do texto de explicação não foi informada no Figma (usando TextSecondary).
                Text(stringResource(R.string.new_goal_suggestion_hint), style = SuggestionHint, color = SobraColors.TextSecondary)
            }
        }
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
    forecast: YearMonth,
    contributions: List<Contribution>,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onAddContribution: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val progress = progressFraction(goal.current, goal.target)
    SobraScreen(modifier = modifier) {
        BackHeader(
            backText = stringResource(R.string.goals_title),
            onBack = onBack,
            trailing = {
                // TODO: a cor do ícone de editar não foi informada no Figma (usando TextSecondary).
                SobraIcon(
                    R.drawable.ic_pencil_20,
                    tint = SobraColors.TextSecondary,
                    contentDescription = stringResource(R.string.goal_detail_edit),
                    modifier = Modifier.clickable(role = Role.Button, onClick = onEdit),
                )
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
                        }
                    }
                }
                item {
                    Column(
                        modifier = Modifier.padding(top = Dimens.Space24, bottom = Dimens.Space20),
                        verticalArrangement = Arrangement.spacedBy(Dimens.Space12),
                    ) {
                        InfoLine(stringResource(R.string.goal_detail_monthly), monthlyContribution.toBrl(), SobraColors.TextPrimary)
                        InfoLine(stringResource(R.string.goal_detail_forecast), forecast.toMonthYear(), SobraColors.Teal)
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
            onDeadlineClick = {}, onBack = {}, onCreate = {},
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
            onBack = {}, onEdit = {}, onAddContribution = {},
        )
    }
}
