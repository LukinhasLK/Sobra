package com.unasp.sobra.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import com.unasp.sobra.R
import com.unasp.sobra.data.ContributionType
import com.unasp.sobra.ui.format.toFullDate
import com.unasp.sobra.ui.model.Category
import com.unasp.sobra.ui.theme.Dimens
import com.unasp.sobra.ui.theme.SobraColors
import com.unasp.sobra.ui.theme.SobraType
import com.unasp.sobra.ui.theme.manrope
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

// Seletores abertos pelos campos de Categorias (valor), Novo objetivo (prazo), novo aporte e data. O Figma só desenha os
// campos fechados, então estes diálogos reaproveitam o estilo dos cards (raio, borda, cores).

private val DialogTitle = manrope(18, FontWeight.W700)
private val DialogText = manrope(14, FontWeight.W400)
private val OptionText = manrope(15, FontWeight.W600)

/** Moldura comum dos diálogos: card branco centralizado com título. */
@Composable
private fun PickerDialog(title: String, onDismiss: () -> Unit, content: @Composable () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .sobraCard(Dimens.Radius24, borderColor = null)
                .padding(Dimens.Space24),
            verticalArrangement = Arrangement.spacedBy(Dimens.Space16),
        ) {
            Text(title, style = DialogTitle, color = SobraColors.TextPrimary)
            content()
        }
    }
}

/** Uma opção da lista; a escolhida fica em Teal. */
@Composable
private fun OptionRow(selected: Boolean, onClick: () -> Unit, content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .sobraCard(
                Dimens.Radius16,
                background = if (selected) SobraColors.TealLight else SobraColors.Surface,
            )
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = Dimens.Space16, vertical = Dimens.Space12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.Space12),
    ) { content() }
}

/** Valor estimado de uma categoria (passo 2 da configuração). Valor zero = categoria inativa. */
@Composable
fun CategoryAmountDialog(
    category: Category,
    initial: BigDecimal?,
    onConfirm: (BigDecimal?) -> Unit,
    onDismiss: () -> Unit,
) {
    var amount by remember { mutableStateOf(initial ?: BigDecimal.ZERO.setScale(2)) }
    PickerDialog(stringResource(R.string.picker_category_value_title, stringResource(category.label)), onDismiss) {
        Text(
            stringResource(R.string.picker_category_value_hint),
            style = DialogText,
            color = SobraColors.TextSecondary,
        )
        AmountCard(
            label = stringResource(category.label),
            amount = amount,
            onAmountChange = { amount = it },
            prefixStyle = manrope(20, FontWeight.W700),
            valueStyle = manrope(28, FontWeight.W800),
            padding = Dimens.Space16,
            labelGap = Dimens.Space8,
            prefixGap = Dimens.Space8,
            radius = Dimens.Radius20,
        )
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space12)) {
            PrimaryButton(stringResource(R.string.action_save), {
                onConfirm(if (amount.signum() > 0) amount else null)
                onDismiss()
            })
            SecondaryButton(stringResource(R.string.action_no_value), {
                onConfirm(null)
                onDismiss()
            })
        }
    }
}

/** Calendário da despesa. Bloqueia datas futuras e anteriores a 2000, como a API. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDatePickerDialog(selected: LocalDate, onSelect: (LocalDate) -> Unit, onDismiss: () -> Unit) {
    val todayMillis = remember { LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() }
    val state = rememberDatePickerState(
        initialSelectedDateMillis = selected.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        yearRange = MIN_YEAR..LocalDate.now().year,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= todayMillis
        },
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let {
                    onSelect(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate())
                }
                onDismiss()
            }) { Text(stringResource(R.string.action_ok)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    ) {
        DatePicker(state = state)
    }
}

/** Prazos oferecidos ao criar um objetivo (a API aceita de 1 a 360 meses). */
private val GOAL_TERMS = listOf(3, 6, 9, 12, 18, 24, 30, 36, 48, 60)

/** Escolha do prazo do objetivo, em meses. */
@Composable
fun GoalTermPickerDialog(selected: Int, onSelect: (Int) -> Unit, onDismiss: () -> Unit) {
    PickerDialog(stringResource(R.string.picker_term_title), onDismiss) {
        LazyColumn(
            modifier = Modifier.heightIn(max = ListMaxHeight),
            verticalArrangement = Arrangement.spacedBy(Dimens.Space8),
        ) {
            items(GOAL_TERMS, key = { it }) { months ->
                OptionRow(selected = months == selected, onClick = { onSelect(months); onDismiss() }) {
                    Text(
                        pluralStringResource(R.plurals.months_count, months, months),
                        style = OptionText,
                        color = SobraColors.TextPrimary,
                    )
                }
            }
        }
    }
}

/**
 * Novo aporte de um objetivo (OP-16): valor, tipo (mensal ou extra) e data.
 * O diálogo só fecha quando a API aceita; se recusar, [errorMessage] aparece aqui dentro.
 */
@Composable
fun AddContributionDialog(
    errorMessage: String?,
    saving: Boolean,
    onConfirm: (ContributionType, BigDecimal, LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    var amount by remember { mutableStateOf(BigDecimal.ZERO.setScale(2)) }
    var type by remember { mutableStateOf(ContributionType.Monthly) }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var pickingDate by remember { mutableStateOf(false) }

    PickerDialog(stringResource(R.string.contribution_title), onDismiss) {
        AmountCard(
            label = stringResource(R.string.contribution_amount_label),
            amount = amount,
            onAmountChange = { amount = it },
            prefixStyle = manrope(20, FontWeight.W700),
            valueStyle = manrope(28, FontWeight.W800),
            padding = Dimens.Space16,
            labelGap = Dimens.Space8,
            prefixGap = Dimens.Space8,
            radius = Dimens.Radius20,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Space8)) {
            ContributionType.entries.forEach { option ->
                Box(modifier = Modifier.weight(1f)) {
                    OptionRow(selected = option == type, onClick = { type = option }) {
                        Text(
                            stringResource(
                                if (option == ContributionType.Monthly) R.string.contribution_type_monthly
                                else R.string.contribution_type_extra,
                            ),
                            style = OptionText,
                            color = SobraColors.TextPrimary,
                        )
                    }
                }
            }
        }
        WhiteSelectorField(
            label = stringResource(R.string.field_date),
            text = date.toFullDate(),
            onClick = { pickingDate = true },
            leading = { SobraIcon(R.drawable.ic_calendar_16, tint = SobraColors.Teal) },
        )
        ErrorText(errorMessage)
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space12)) {
            PrimaryButton(stringResource(R.string.action_save), { if (!saving) onConfirm(type, amount, date) })
            SecondaryButton(stringResource(R.string.action_cancel), onDismiss)
        }
    }
    if (pickingDate) {
        ExpenseDatePickerDialog(date, onSelect = { date = it }, onDismiss = { pickingDate = false })
    }
}

/** Mensagem de erro de formulário em vermelho; não ocupa espaço quando é null. */
@Composable
fun ErrorText(message: String?, modifier: Modifier = Modifier) {
    if (message != null) {
        Text(text = message, style = SobraType.FormLabel, color = SobraColors.Red, modifier = modifier)
    }
}

private val ListMaxHeight = Dimens.ListBottomReserve * 3
private const val MIN_YEAR = 2000
