package com.unasp.sobra.ui.screens.expenses

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.unasp.sobra.R
import com.unasp.sobra.data.CATEGORY_ORDER
import com.unasp.sobra.data.DemoData
import com.unasp.sobra.domain.total
import com.unasp.sobra.ui.components.AmountCard
import com.unasp.sobra.ui.components.BottomTab
import com.unasp.sobra.ui.components.CircleIconButton
import com.unasp.sobra.ui.components.ErrorText
import com.unasp.sobra.ui.components.ExpenseRow
import com.unasp.sobra.ui.components.Fab
import com.unasp.sobra.ui.components.FooterActions
import com.unasp.sobra.ui.components.FormScreen
import com.unasp.sobra.ui.components.PrimaryButton
import com.unasp.sobra.ui.components.SecondaryButton
import com.unasp.sobra.ui.components.SobraIcon
import com.unasp.sobra.ui.components.SobraScreen
import com.unasp.sobra.ui.components.TextLink
import com.unasp.sobra.ui.components.TitleHeader
import com.unasp.sobra.ui.components.WhiteTextField
import com.unasp.sobra.ui.components.sobraCard
import com.unasp.sobra.ui.format.toBrl
import com.unasp.sobra.ui.format.toDayMonthLong
import com.unasp.sobra.ui.format.toFullDate
import com.unasp.sobra.ui.format.toMonthYear
import com.unasp.sobra.ui.format.toShortMonth
import com.unasp.sobra.ui.model.Category
import com.unasp.sobra.ui.model.Expense
import com.unasp.sobra.ui.theme.Dimens
import com.unasp.sobra.ui.theme.SobraColors
import com.unasp.sobra.ui.theme.SobraTheme
import com.unasp.sobra.ui.theme.SobraType
import com.unasp.sobra.ui.theme.manrope
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

// Refinamento com referência no app Stoic (Mobbin): fileira de meses no topo (no lugar do
// seletor que abria um diálogo), lista agrupada por dia, e na Nova despesa as escolhas ficam
// à vista em chips (categoria e data), sem diálogos para o caso comum. Cores: só a paleta do Sobra.

private val BannerLabel = manrope(13, FontWeight.W600)
private val BannerValue = manrope(28, FontWeight.W800)
private val BannerCount = manrope(13, FontWeight.W500)
private val DayHeader = manrope(13, FontWeight.W700)
private val DayTotal = manrope(13, FontWeight.W600)
private val EmptyTitle = manrope(15, FontWeight.W700)
private val EmptyText = manrope(14, FontWeight.W400)
private val NewExpenseTitle = manrope(20, FontWeight.W800)
private val AmountPrefix = manrope(24, FontWeight.W700)
private val AmountValue = manrope(36, FontWeight.W800)
private val MonthLabel = manrope(15, FontWeight.W700)
private val YearLabel = manrope(11, FontWeight.W500)
private val ChipText = manrope(14, FontWeight.W600)

// O fechar tem 26 de desenho (Figma), mas a área de toque precisa de 48 (acessibilidade).
private val CloseButtonSize = 26.dp
private val MonthPillWidth = 64.dp
private val MonthPillHeight = 56.dp
private val ChipHeight = 44.dp
private val LoadingTopPadding = 48.dp

/** Meses na fileira: o atual e os 11 anteriores (a API guarda tudo; 12 cobre o uso diário). */
private const val MONTHS_IN_STRIP = 12

/** Os 12 meses da fileira, do mais antigo (esquerda) ao atual (direita), como uma linha do tempo. */
internal fun stripMonths(current: YearMonth): List<YearMonth> =
    (MONTHS_IN_STRIP - 1 downTo 0).map { current.minusMonths(it.toLong()) }

/** Agrupa por dia, do mais recente para o mais antigo, mantendo a ordem dentro do dia. */
internal fun groupByDay(expenses: List<Expense>): List<Pair<LocalDate, List<Expense>>> =
    expenses.sortedByDescending { it.date }.groupBy { it.date }.toList()

// ---------------------------------------------------------------------------------------
// 11 Gastos
// ---------------------------------------------------------------------------------------

/**
 * 11 Gastos. `loading` mostra o indicador no lugar da lista (evita piscar "R$ 0,00" e o
 * estado vazio enquanto os dados chegam).
 */
@Composable
fun ExpensesScreen(
    month: YearMonth,
    expenses: List<Expense>,
    onMonthSelected: (YearMonth) -> Unit,
    onAddExpense: () -> Unit,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
) {
    val total = expenses.map { it.amount }.total()
    SobraScreen(
        modifier = modifier,
        bottomTab = BottomTab.Expenses,
        onTabSelected = onTabSelected,
        floatingAction = { Fab(onAddExpense, stringResource(R.string.expenses_add)) },
    ) {
        TitleHeader(title = stringResource(R.string.expenses_title), bottomPadding = 0.dp)
        MonthStrip(selected = month, onSelect = onMonthSelected)

        // Banner do total: padding 20, gap 4, raio 20, Teal cheio.
        Column(
            modifier = Modifier
                .padding(start = Dimens.ScreenMargin, end = Dimens.ScreenMargin, top = Dimens.Space16, bottom = Dimens.Space8)
                .fillMaxWidth()
                .sobraCard(Dimens.Radius20, background = SobraColors.Teal, borderColor = null)
                .padding(Dimens.Space20),
            verticalArrangement = Arrangement.spacedBy(Dimens.Space4),
        ) {
            Text(
                stringResource(R.string.expenses_total_month, month.toMonthYear()),
                style = BannerLabel, color = SobraColors.TealLight,
            )
            Text(if (loading) "—" else total.toBrl(), style = BannerValue, color = SobraColors.OnTeal)
            if (!loading && expenses.isNotEmpty()) {
                Text(
                    pluralStringResource(R.plurals.expenses_count, expenses.size, expenses.size),
                    style = BannerCount, color = SobraColors.TealLight,
                )
            }
        }

        when {
            loading -> Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(top = LoadingTopPadding),
                contentAlignment = Alignment.TopCenter,
            ) { CircularProgressIndicator(color = SobraColors.Teal) }

            expenses.isEmpty() -> EmptyMonth(month, onAddExpense, Modifier.weight(1f))

            else -> ExpenseList(expenses, Modifier.weight(1f))
        }
    }
}

/**
 * Fileira de meses (referência: a fileira de dias do Stoic). Começa rolada até o mês escolhido;
 * o escolhido fica em Teal cheio. Cada pílula tem 64×56 (área de toque acima de 48).
 */
@Composable
private fun MonthStrip(selected: YearMonth, onSelect: (YearMonth) -> Unit) {
    val months = remember { stripMonths(YearMonth.now()) }
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = months.indexOf(selected).coerceAtLeast(0),
    )
    LazyRow(
        state = listState,
        contentPadding = PaddingValues(start = Dimens.ScreenMargin, end = Dimens.ScreenMargin, top = Dimens.Space16),
        horizontalArrangement = Arrangement.spacedBy(Dimens.Space8),
    ) {
        items(months, key = { it.toString() }) { month ->
            val isSelected = month == selected
            Column(
                modifier = Modifier
                    .width(MonthPillWidth)
                    .height(MonthPillHeight)
                    .sobraCard(
                        Dimens.Radius20,
                        background = if (isSelected) SobraColors.Teal else SobraColors.Surface,
                        borderColor = if (isSelected) null else SobraColors.Border,
                    )
                    // selectable informa "selecionado" ao leitor de tela.
                    .selectable(selected = isSelected, role = Role.Tab, onClick = { onSelect(month) }),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    month.toShortMonth(), style = MonthLabel,
                    color = if (isSelected) SobraColors.OnTeal else SobraColors.TextPrimary,
                )
                Text(
                    month.year.toString(), style = YearLabel,
                    color = if (isSelected) SobraColors.TealLight else SobraColors.TextSecondary,
                )
            }
        }
    }
}

/** Lista agrupada por dia: cabeçalho "Hoje", "Ontem" ou "18 de setembro" com o total do dia. */
@Composable
private fun ExpenseList(expenses: List<Expense>, modifier: Modifier = Modifier) {
    val groups = remember(expenses) { groupByDay(expenses) }
    val today = remember { LocalDate.now() }
    // LazyColumn só compõe os itens visíveis (equivale ao RecyclerView).
    // O padding inferior de 120 deixa o último item subir acima do FAB.
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = Dimens.ScreenMargin, end = Dimens.ScreenMargin, bottom = Dimens.ListBottomReserve,
        ),
    ) {
        groups.forEach { (day, items) ->
            item(key = "dia-$day") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Dimens.Space16, bottom = Dimens.Space8),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(dayLabel(day, today), style = DayHeader, color = SobraColors.TextSecondary)
                    Text(items.map { it.amount }.total().toBrl(), style = DayTotal, color = SobraColors.TextSecondary)
                }
            }
            // `key` identifica cada item de forma estável (melhora rolagem e animações).
            items(items, key = { it.id }) { expense ->
                ExpenseRow(expense, modifier = Modifier.padding(bottom = Dimens.Space8))
            }
        }
    }
}

@Composable
private fun dayLabel(day: LocalDate, today: LocalDate): String = when (day) {
    today -> stringResource(R.string.day_today)
    today.minusDays(1) -> stringResource(R.string.day_yesterday)
    else -> day.toDayMonthLong()
}

/** Mês sem despesas: explica e oferece a ação, em vez de uma área em branco. */
@Composable
private fun EmptyMonth(month: YearMonth, onAddExpense: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.ScreenMargin, vertical = Dimens.Space32),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.Space8),
    ) {
        Text(stringResource(R.string.expenses_empty_title, month.toMonthYear()), style = EmptyTitle, color = SobraColors.TextPrimary)
        Text(stringResource(R.string.expenses_empty_description), style = EmptyText, color = SobraColors.TextSecondary)
        TextLink(stringResource(R.string.expenses_add), onAddExpense, modifier = Modifier.padding(top = Dimens.Space8))
    }
}

// ---------------------------------------------------------------------------------------
// 12 Nova despesa
// ---------------------------------------------------------------------------------------

/** 12 Nova despesa — tela cheia (não é sheet nem tem scrim). */
@Composable
fun NewExpenseScreen(
    amount: BigDecimal,
    onAmountChange: (BigDecimal) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    category: Category,
    onCategorySelected: (Category) -> Unit,
    date: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onPickOtherDate: () -> Unit,
    onClose: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
    saving: Boolean = false,
) {
    val today = remember { LocalDate.now() }
    val yesterday = today.minusDays(1)
    FormScreen(
        modifier = modifier,
        footer = {
            FooterActions(gap = Dimens.Space12) {
                // Enquanto salva, o texto muda: mostra que o toque foi recebido (o ViewModel
                // também ignora toques repetidos).
                PrimaryButton(stringResource(if (saving) R.string.action_saving else R.string.action_save), onSave)
                SecondaryButton(stringResource(R.string.action_cancel), onCancel)
            }
        },
    ) {
        // Header: padding 12/24/12/24, título 20/800 e fechar (fundo Soft, sem borda).
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.ScreenMargin, vertical = Dimens.Space12),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.new_expense_title), style = NewExpenseTitle, color = SobraColors.TextPrimary)
            // Caixa de 48 em volta do botão de 26: área de toque confortável sem mudar o desenho.
            Box(modifier = Modifier.size(Dimens.MinTouchTarget), contentAlignment = Alignment.Center) {
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
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done,
                ),
            )

            // Categoria: as 7 opções à vista (1 toque), no lugar de um campo que abria uma lista.
            ChipGroup(label = stringResource(R.string.field_category)) {
                CATEGORY_ORDER.forEach { option ->
                    SelectChip(
                        text = stringResource(option.label),
                        selected = option == category,
                        onClick = { onCategorySelected(option) },
                        icon = option.icon16,
                    )
                }
            }

            // Data: "Hoje" e "Ontem" cobrem quase todos os lançamentos; "Outra data" abre o calendário
            // e, depois de escolhida, mostra a própria data no chip.
            val isOther = date != today && date != yesterday
            ChipGroup(label = stringResource(R.string.field_date)) {
                SelectChip(stringResource(R.string.day_today), date == today, { onDateSelected(today) })
                SelectChip(stringResource(R.string.day_yesterday), date == yesterday, { onDateSelected(yesterday) })
                SelectChip(
                    text = if (isOther) date.toFullDate() else stringResource(R.string.day_other),
                    selected = isOther,
                    onClick = onPickOtherDate,
                    icon = R.drawable.ic_calendar_16,
                )
            }
            ErrorText(errorMessage)
        }
    }
}

/** Label do formulário + chips que quebram linha quando não cabem. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipGroup(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space8)) {
        Text(text = label, style = SobraType.FormLabel, color = SobraColors.TextSecondary)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Dimens.Space8),
            verticalArrangement = Arrangement.spacedBy(Dimens.Space8),
        ) { content() }
    }
}

/**
 * Chip de escolha única (referência: chips do Stoic). Selecionado: Teal cheio e texto branco;
 * não selecionado: branco com borda. Altura 44 e borda arredondada por inteiro.
 */
@Composable
private fun SelectChip(text: String, selected: Boolean, onClick: () -> Unit, @DrawableRes icon: Int? = null) {
    val content = if (selected) SobraColors.OnTeal else SobraColors.TextPrimary
    Row(
        modifier = Modifier
            .height(ChipHeight)
            .sobraCard(
                Dimens.RadiusFull,
                background = if (selected) SobraColors.Teal else SobraColors.Surface,
                borderColor = if (selected) null else SobraColors.Border,
            )
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = Dimens.Space16),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.Space8),
    ) {
        if (icon != null) SobraIcon(icon, tint = if (selected) SobraColors.OnTeal else SobraColors.Teal)
        Text(text, style = ChipText, color = content, maxLines = 1)
    }
}

@Preview(widthDp = 402, heightDp = 874)
@Composable
private fun ExpensesScreenPreview() {
    SobraTheme {
        ExpensesScreen(
            month = DemoData.expensesMonth, expenses = DemoData.expenses,
            onMonthSelected = {}, onAddExpense = {}, onTabSelected = {},
        )
    }
}

@Preview(name = "Mês vazio", widthDp = 402, heightDp = 874)
@Composable
private fun ExpensesEmptyPreview() {
    SobraTheme {
        ExpensesScreen(
            month = YearMonth.now(), expenses = emptyList(),
            onMonthSelected = {}, onAddExpense = {}, onTabSelected = {},
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
            category = Category.Food, onCategorySelected = {},
            date = LocalDate.now(), onDateSelected = {}, onPickOtherDate = {},
            onClose = {}, onSave = {}, onCancel = {},
        )
    }
}
