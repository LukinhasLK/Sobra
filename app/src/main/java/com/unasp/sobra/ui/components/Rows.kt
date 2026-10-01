package com.unasp.sobra.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.unasp.sobra.R
import com.unasp.sobra.data.DemoData
import com.unasp.sobra.domain.progressFraction
import com.unasp.sobra.ui.format.toBrl
import com.unasp.sobra.ui.format.toBrlPositive
import com.unasp.sobra.ui.format.toBrlShort
import com.unasp.sobra.ui.format.toDayMonth
import com.unasp.sobra.ui.format.toFullDate
import com.unasp.sobra.ui.model.CategoryBudget
import com.unasp.sobra.ui.model.CategorySpending
import com.unasp.sobra.ui.model.Contribution
import com.unasp.sobra.ui.model.Expense
import com.unasp.sobra.ui.model.Goal
import com.unasp.sobra.ui.theme.Dimens
import com.unasp.sobra.ui.theme.SobraColors
import com.unasp.sobra.ui.theme.SobraTheme
import com.unasp.sobra.ui.theme.manrope
import kotlin.math.roundToInt

// TODO: comportamento de textos longos não definido no design. Nas linhas abaixo, nomes
//  longos ficam em uma linha e terminam com reticências.

private val IconBoxSize = 36.dp

/** Caixa de 36×36, raio 12, com um ícone no centro (categorias, despesas). */
@Composable
fun IconBox(@DrawableRes icon: Int, background: Color, tint: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(IconBoxSize)
            .clip(RoundedCornerShape(Dimens.Radius12))
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        SobraIcon(icon, tint = tint)
    }
}

// ---------------------------------------------------------------------------------------
// CategoryRow (Home)
// ---------------------------------------------------------------------------------------

private val CategoryLabel = manrope(14, FontWeight.W600)
private val CategoryValue = manrope(14, FontWeight.W700)

/**
 * Linha de categoria da Home: caixa do ícone na cor da categoria (alpha 21/255), nome,
 * valor e barra de 8 na cor da categoria. `barFraction` já vem calculada pela tela.
 */
@Composable
fun CategoryRow(spending: CategorySpending, barFraction: Float, modifier: Modifier = Modifier) {
    val color = spending.category.color
    Row(
        modifier = modifier
            .fillMaxWidth()
            .sobraCard(Dimens.Radius16)
            .padding(Dimens.Space12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.Space12),
    ) {
        IconBox(spending.category.icon12, SobraColors.categoryIconBackground(color), color)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Dimens.Space4)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(spending.category.label), style = CategoryLabel, color = SobraColors.TextPrimary)
                Text(spending.amount.toBrlShort(), style = CategoryValue, color = SobraColors.TextPrimary)
            }
            ProgressTrack(progress = barFraction, height = ProgressTrackHeight.Category, color = color)
        }
    }
}

// ---------------------------------------------------------------------------------------
// ExpenseRow (Gastos)
// ---------------------------------------------------------------------------------------

private val ExpenseTitle = manrope(14, FontWeight.W700)
private val ExpenseMeta = manrope(12, FontWeight.W500)
private val ExpenseValue = manrope(15, FontWeight.W700)
private const val META_SEPARATOR = "·"

/** Linha de despesa: ícone em caixa TealLight, título, "Categoria · dd/MM" e valor à direita. */
@Composable
fun ExpenseRow(expense: Expense, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .sobraCard(Dimens.Radius16)
            .padding(Dimens.Space12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.Space12),
    ) {
        // TODO: o tamanho do ícone dentro da caixa não foi informado para esta tela (usando 16).
        IconBox(expense.category.icon16, SobraColors.TealLight, SobraColors.Teal)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Dimens.Space2)) {
            Text(
                expense.title, style = ExpenseTitle, color = SobraColors.TextPrimary,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
            // Três textos com gap 6, como no Figma: categoria, ponto e data.
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Space6)) {
                Text(stringResource(expense.category.label), style = ExpenseMeta, color = SobraColors.TextSecondary)
                Text(META_SEPARATOR, style = ExpenseMeta, color = SobraColors.TextSecondary)
                Text(expense.date.toDayMonth(), style = ExpenseMeta, color = SobraColors.TextSecondary)
            }
        }
        Text(expense.amount.toBrl(), style = ExpenseValue, color = SobraColors.TextPrimary)
    }
}

// ---------------------------------------------------------------------------------------
// ContributionRow (Detalhe do objetivo)
// ---------------------------------------------------------------------------------------

private val ContributionTitle = manrope(13, FontWeight.W700)
private val ContributionDate = manrope(11, FontWeight.W400)
private val ContributionValue = manrope(14, FontWeight.W700)

/** Linha de aporte: ícone 16, título 13/700, data 11/400 e valor "+ R$" em Teal. */
@Composable
fun ContributionRow(contribution: Contribution, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .sobraCard(Dimens.Radius16)
            .padding(Dimens.Space14),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.Space10),
    ) {
        SobraIcon(R.drawable.ic_arrow_up_right_16, tint = SobraColors.Teal)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Dimens.Space2)) {
            Text(contribution.title, style = ContributionTitle, color = SobraColors.TextPrimary)
            Text(contribution.date.toFullDate(), style = ContributionDate, color = SobraColors.TextSecondary)
        }
        Text(contribution.amount.toBrlPositive(), style = ContributionValue, color = SobraColors.Teal)
    }
}

// ---------------------------------------------------------------------------------------
// GoalCard (Objetivos)
// ---------------------------------------------------------------------------------------

private val GoalThumbnailSize = 48.dp
private val GoalName = manrope(15, FontWeight.W700)
private val GoalValues = manrope(13, FontWeight.W400)
private val GoalBadge = manrope(12, FontWeight.W700)
private const val PERCENT = 100

/** "56%" a partir da fração 0,56. */
fun percentLabel(fraction: Float): String = "${(fraction * PERCENT).roundToInt()}%"

/** Card de objetivo: thumbnail 48, nome, "R$ atual de R$ meta", badge de % e trilha de 10. */
@Composable
fun GoalCard(goal: Goal, onClick: () -> Unit, modifier: Modifier = Modifier) {
    // Decisão de fidelidade: barra e badge usam o mesmo valor real (atual ÷ meta).
    // No mockup as barras são levemente menores que os badges (54,46% vs 56%).
    val progress = progressFraction(goal.current, goal.target)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .sobraCard(Dimens.Radius20)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(Dimens.Space20),
        verticalArrangement = Arrangement.spacedBy(Dimens.Space16),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.Space12),
        ) {
            Image(
                painter = painterResource(goal.thumbnail),
                contentDescription = null,
                // Crop: preenche o quadrado cortando o que sobrar (equivale ao FILL do Figma).
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(GoalThumbnailSize)
                    .clip(RoundedCornerShape(Dimens.Radius12)),
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Dimens.Space2)) {
                Text(
                    goal.name, style = GoalName, color = SobraColors.TextPrimary,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
                Text(
                    stringResource(R.string.goal_values, goal.current.toBrlShort(), goal.target.toBrlShort()),
                    style = GoalValues, color = SobraColors.TextSecondary,
                )
            }
            Badge(percentLabel(progress), GoalBadge, horizontalPadding = Dimens.Space8, verticalPadding = Dimens.Space4)
        }
        ProgressTrack(progress = progress, height = ProgressTrackHeight.Goal)
    }
}

// ---------------------------------------------------------------------------------------
// CategoryChip (Categorias, passo 2)
// ---------------------------------------------------------------------------------------

private val ChipHeight = 59.dp
private val ChipTitle = manrope(13, FontWeight.W600)
private val ChipSubtitle = manrope(11, FontWeight.W400)

/**
 * Chip de categoria: largura pelo conteúdo, altura 59, raio 24, borda 1.
 * Ativo (com valor): fundo TealLight e tudo em Teal. Inativo: fundo branco e "Adicionar".
 */
@Composable
fun CategoryChip(budget: CategoryBudget, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val active = budget.amount != null
    val accent = if (active) SobraColors.Teal else SobraColors.TextSecondary
    Row(
        modifier = modifier
            .height(ChipHeight)
            .sobraCard(
                radius = Dimens.Radius24,
                background = if (active) SobraColors.TealLight else SobraColors.Surface,
                borderColor = if (active) SobraColors.Teal else SobraColors.Border,
            )
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = Dimens.Space16, vertical = Dimens.Space12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.Space10),
    ) {
        SobraIcon(budget.category.icon16, tint = accent)
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space2)) {
            Text(
                stringResource(budget.category.label),
                style = ChipTitle,
                color = if (active) SobraColors.Teal else SobraColors.TextPrimary,
            )
            Text(
                // `?.` só chama toBrlShort() se amount não for nulo; `?:` dá o texto alternativo.
                budget.amount?.toBrlShort() ?: stringResource(R.string.category_add),
                style = ChipSubtitle,
                color = accent,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun RowsPreview() {
    SobraTheme {
        Column(
            modifier = Modifier
                .background(SobraColors.Background)
                .padding(Dimens.ScreenMargin),
            verticalArrangement = Arrangement.spacedBy(Dimens.Space12),
        ) {
            CategoryRow(DemoData.homeSpending[1], barFraction = 0.58f)
            ExpenseRow(DemoData.expenses[1])
            ContributionRow(DemoData.contributions[0])
            GoalCard(DemoData.goals[0], onClick = {})
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Space10)) {
                CategoryChip(DemoData.categoryBudgets[0], onClick = {})
                CategoryChip(DemoData.categoryBudgets[3], onClick = {})
            }
        }
    }
}
