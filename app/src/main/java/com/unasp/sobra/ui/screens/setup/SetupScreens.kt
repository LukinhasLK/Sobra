package com.unasp.sobra.ui.screens.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.unasp.sobra.R
import com.unasp.sobra.data.DemoData
import com.unasp.sobra.ui.components.AmountCard
import com.unasp.sobra.ui.components.AuthHeader
import com.unasp.sobra.ui.components.CategoryChip
import com.unasp.sobra.ui.components.FooterActions
import com.unasp.sobra.ui.components.FormScreen
import com.unasp.sobra.ui.components.PrimaryButton
import com.unasp.sobra.ui.components.StepProgress
import com.unasp.sobra.ui.model.Category
import com.unasp.sobra.ui.model.CategoryBudget
import com.unasp.sobra.ui.theme.Dimens
import com.unasp.sobra.ui.theme.SobraTheme
import com.unasp.sobra.ui.theme.manrope
import java.math.BigDecimal

private const val TOTAL_STEPS = 2
private const val STEP_INCOME = 1
private const val STEP_CATEGORIES = 2

private val IncomePrefix = manrope(28, FontWeight.W700)
private val IncomeValue = manrope(48, FontWeight.W800)

/** 08 Renda mensal (passo 1 de 2). */
@Composable
fun IncomeScreen(
    income: BigDecimal,
    onIncomeChange: (BigDecimal) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FormScreen(
        modifier = modifier,
        footer = {
            FooterActions(gap = Dimens.Space20) {
                PrimaryButton(stringResource(R.string.action_continue), onContinue)
            }
        },
    ) {
        StepProgress(
            stepLabel = stringResource(R.string.setup_step, STEP_INCOME, TOTAL_STEPS),
            caption = stringResource(R.string.income_caption),
            step = STEP_INCOME,
            totalSteps = TOTAL_STEPS,
        )
        AuthHeader(
            backText = stringResource(R.string.action_back),
            onBack = onBack,
            title = stringResource(R.string.income_title),
            description = stringResource(R.string.income_description),
        )
        // TODO: comportamento de valores longos no card (ex.: R$ 1.000.000,00 em 48sp não
        //  cabe em uma linha) não definido no design.
        AmountCard(
            label = stringResource(R.string.income_card_label),
            amount = income,
            onAmountChange = onIncomeChange,
            prefixStyle = IncomePrefix,
            valueStyle = IncomeValue,
            padding = Dimens.Space24,
            labelGap = Dimens.Space12,
            prefixGap = Dimens.Space12,
            radius = Dimens.Radius20,
            modifier = Modifier.padding(horizontal = Dimens.ScreenMargin),
        )
    }
}

/** 09 Categorias (passo 2 de 2). Tocar num chip avisa a tela-mãe via `onCategoryClick`. */
// FlowRow ainda é marcado como API experimental em algumas versões do Compose; o OptIn
// declara que sabemos disso.
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoriesScreen(
    budgets: List<CategoryBudget>,
    onCategoryClick: (Category) -> Unit,
    onBack: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FormScreen(
        modifier = modifier,
        footer = {
            FooterActions(gap = Dimens.Space20) {
                PrimaryButton(stringResource(R.string.action_finish), onFinish)
            }
        },
    ) {
        StepProgress(
            stepLabel = stringResource(R.string.setup_step, STEP_CATEGORIES, TOTAL_STEPS),
            caption = stringResource(R.string.categories_caption),
            step = STEP_CATEGORIES,
            totalSteps = TOTAL_STEPS,
        )
        AuthHeader(
            backText = stringResource(R.string.categories_back),
            onBack = onBack,
            title = stringResource(R.string.categories_title),
            description = stringResource(R.string.categories_description),
        )
        // FlowRow: coloca os chips lado a lado e quebra para a linha de baixo quando não
        // cabe mais. Cada chip tem a largura do próprio conteúdo (não é uma grade).
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.ScreenMargin),
            horizontalArrangement = Arrangement.spacedBy(Dimens.Space10),
            verticalArrangement = Arrangement.spacedBy(Dimens.Space10),
        ) {
            budgets.forEach { budget ->
                // TODO: o design não mostra como se informa/edita o valor de uma categoria
                //  ao tocar no chip (teclado? outra tela?).
                CategoryChip(budget, onClick = { onCategoryClick(budget.category) })
            }
        }
    }
}

@Preview(widthDp = 402, heightDp = 874)
@Composable
private fun IncomeScreenPreview() {
    SobraTheme {
        IncomeScreen(income = DemoData.setupIncome, onIncomeChange = {}, onBack = {}, onContinue = {})
    }
}

@Preview(widthDp = 402, heightDp = 874)
@Composable
private fun CategoriesScreenPreview() {
    SobraTheme {
        CategoriesScreen(budgets = DemoData.categoryBudgets, onCategoryClick = {}, onBack = {}, onFinish = {})
    }
}
