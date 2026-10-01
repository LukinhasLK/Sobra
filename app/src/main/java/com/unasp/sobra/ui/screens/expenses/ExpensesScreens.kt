package com.unasp.sobra.ui.screens.expenses

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.unasp.sobra.R
import com.unasp.sobra.data.DemoData
import com.unasp.sobra.domain.total
import com.unasp.sobra.ui.components.AmountCard
import com.unasp.sobra.ui.components.BottomTab
import com.unasp.sobra.ui.components.CircleIconButton
import com.unasp.sobra.ui.components.ExpenseRow
import com.unasp.sobra.ui.components.Fab
import com.unasp.sobra.ui.components.FooterActions
import com.unasp.sobra.ui.components.FormScreen
import com.unasp.sobra.ui.components.PrimaryButton
import com.unasp.sobra.ui.components.SecondaryButton
import com.unasp.sobra.ui.components.SobraIcon
import com.unasp.sobra.ui.components.SobraScreen
import com.unasp.sobra.ui.components.TitleHeader
import com.unasp.sobra.ui.components.WhiteSelectorField
import com.unasp.sobra.ui.components.WhiteTextField
import com.unasp.sobra.ui.components.sobraCard
import com.unasp.sobra.ui.format.toBrl
import com.unasp.sobra.ui.format.toFullDate
import com.unasp.sobra.ui.format.toMonthYear
import com.unasp.sobra.ui.model.Category
import com.unasp.sobra.ui.model.Expense
import com.unasp.sobra.ui.theme.Dimens
import com.unasp.sobra.ui.theme.SobraColors
import com.unasp.sobra.ui.theme.SobraTheme
import com.unasp.sobra.ui.theme.manrope
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

private val MonthSelectorText = manrope(13, FontWeight.W600)
private val BannerLabel = manrope(13, FontWeight.W600)
private val BannerValue = manrope(28, FontWeight.W800)
private val NewExpenseTitle = manrope(20, FontWeight.W800)
private val AmountPrefix = manrope(24, FontWeight.W700)
private val AmountValue = manrope(36, FontWeight.W800)
private val CloseButtonSize = 26.dp

/** 11 Gastos. */
@Composable
fun ExpensesScreen(
    month: YearMonth,
    expenses: List<Expense>,
    onMonthClick: () -> Unit,
    onAddExpense: () -> Unit,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val total = expenses.map { it.amount }.total()
    SobraScreen(
        modifier = modifier,
        bottomTab = BottomTab.Expenses,
        onTabSelected = onTabSelected,
        floatingAction = { Fab(onAddExpense, stringResource(R.string.expenses_add)) },
    ) {
        TitleHeader(
            title = stringResource(R.string.expenses_title),
            bottomPadding = 0.dp, // header 12/24/0/24: o respiro vem do padding do banner
            trailing = { MonthSelector(month.toMonthYear(), onMonthClick) },
        )
        // Banner do total: área com padding 24; card com padding 20, gap 4, raio 20, Teal.
        Column(
            modifier = Modifier
                .padding(Dimens.Space24)
                .fillMaxWidth()
                .sobraCard(Dimens.Radius20, background = SobraColors.Teal, borderColor = null)
                .padding(Dimens.Space20),
            verticalArrangement = Arrangement.spacedBy(Dimens.Space4),
        ) {
            Text(stringResource(R.string.expenses_total_label), style = BannerLabel, color = SobraColors.TealLight)
            Text(total.toBrl(), style = BannerValue, color = SobraColors.OnTeal)
        }
        // LazyColumn só compõe os itens visíveis (equivale ao RecyclerView).
        // O padding inferior de 120 é interno à rolagem: o último item consegue subir acima
        // do FAB, em vez de ficar coberto por ele como no mockup estático.
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                start = Dimens.ScreenMargin, end = Dimens.ScreenMargin, bottom = Dimens.ListBottomReserve,
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.Space12),
        ) {
            // `key` identifica cada item de forma estável (melhora rolagem e animações).
            items(expenses, key = { it.id }) { expense -> ExpenseRow(expense) }
        }
    }
}

/** Seletor de mês: padding 6/14, raio 16, borda 1, branco, texto 13/600 + chevron 12. */
@Composable
private fun MonthSelector(label: String, onClick: () -> Unit) {
    // TODO: o seletor de mês aberto (lista/calendário) não existe no Figma.
    Row(
        modifier = Modifier
            .sobraCard(Dimens.Radius16)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = Dimens.Space14, vertical = Dimens.Space6),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.Space8),
    ) {
        Text(label, style = MonthSelectorText, color = SobraColors.TextPrimary)
        SobraIcon(R.drawable.ic_chevron_down_12, tint = SobraColors.TextSecondary)
    }
}

/** 12 Nova despesa — tela cheia (não é sheet nem tem scrim). */
@Composable
fun NewExpenseScreen(
    amount: BigDecimal,
    onAmountChange: (BigDecimal) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    category: Category,
    onCategoryClick: () -> Unit,
    date: LocalDate,
    isToday: Boolean,
    onDateClick: () -> Unit,
    onClose: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FormScreen(
        modifier = modifier,
        footer = {
            FooterActions(gap = Dimens.Space12) {
                PrimaryButton(stringResource(R.string.action_save), onSave)
                SecondaryButton(stringResource(R.string.action_cancel), onCancel)
            }
        },
    ) {
        // Header: padding 12/24/12/24, título 20/800 e fechar 26×26 (fundo Soft, sem borda).
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.ScreenMargin, vertical = Dimens.Space12),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.new_expense_title), style = NewExpenseTitle, color = SobraColors.TextPrimary)
            CircleIconButton(
                icon = R.drawable.ic_close_14,
                contentDescription = stringResource(R.string.action_close),
                onClick = onClose,
                size = CloseButtonSize,
                background = SobraColors.Soft,
                borderColor = null,
                tint = SobraColors.TextSecondary,
            )
        }
        // Corpo: padding 24, gap 20.
        Column(
            modifier = Modifier.padding(Dimens.Space24),
            verticalArrangement = Arrangement.spacedBy(Dimens.Space20),
        ) {
            AmountCard(
                label = stringResource(R.string.new_expense_amount_label),
                amount = amount,
                onAmountChange = onAmountChange,
                prefixStyle = AmountPrefix,
                valueStyle = AmountValue,
                padding = Dimens.Space20,
                labelGap = Dimens.Space8,
                prefixGap = Dimens.Space8,
                radius = Dimens.Radius20,
            )
            WhiteTextField(
                label = stringResource(R.string.field_description),
                value = description,
                onValueChange = onDescriptionChange,
            )
            WhiteSelectorField(
                label = stringResource(R.string.field_category),
                text = stringResource(category.label),
                onClick = onCategoryClick,
                // Ícone efetivo de 12 dentro de um wrapper de 16 (2 de folga em cada lado).
                leading = { SobraIcon(category.icon12, tint = SobraColors.Teal, modifier = Modifier.padding(Dimens.Space2)) },
            )
            val dateText = date.toFullDate()
            WhiteSelectorField(
                label = stringResource(R.string.field_date),
                text = if (isToday) stringResource(R.string.date_today, dateText) else dateText,
                onClick = onDateClick,
                leading = { SobraIcon(R.drawable.ic_calendar_16, tint = SobraColors.Teal) },
            )
        }
    }
}

@Preview(widthDp = 402, heightDp = 874)
@Composable
private fun ExpensesScreenPreview() {
    SobraTheme {
        ExpensesScreen(
            month = DemoData.expensesMonth, expenses = DemoData.expenses,
            onMonthClick = {}, onAddExpense = {}, onTabSelected = {},
        )
    }
}

@Preview(widthDp = 402, heightDp = 874)
@Composable
private fun NewExpenseScreenPreview() {
    SobraTheme {
        NewExpenseScreen(
            amount = DemoData.newExpenseAmount, onAmountChange = {},
            description = DemoData.NEW_EXPENSE_DESCRIPTION, onDescriptionChange = {},
            category = Category.Food, onCategoryClick = {},
            date = DemoData.newExpenseDate, isToday = true, onDateClick = {},
            onClose = {}, onSave = {}, onCancel = {},
        )
    }
}
