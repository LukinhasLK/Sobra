package com.unasp.sobra.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.unasp.sobra.R
import com.unasp.sobra.ui.format.toFullDate
import com.unasp.sobra.ui.theme.Dimens
import com.unasp.sobra.ui.theme.SobraColors
import com.unasp.sobra.ui.theme.SobraType
import com.unasp.sobra.ui.theme.manrope
import java.time.LocalDate

// Chips de escolha única (referência: chips do app Stoic, no Mobbin). Usados em Nova despesa
// (categoria e data), Novo objetivo (prazo) e Novo aporte (data).

private val ChipText = manrope(14, FontWeight.W600)
private val ChipHeight = 44.dp

/** Label do formulário + chips que quebram linha quando não cabem. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChipGroup(label: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Dimens.Space8)) {
        Text(text = label, style = SobraType.FormLabel, color = SobraColors.TextSecondary)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Dimens.Space8),
            verticalArrangement = Arrangement.spacedBy(Dimens.Space8),
        ) { content() }
    }
}

/**
 * Chip de escolha única. Selecionado: Teal escuro cheio e texto branco (contraste 5,47);
 * não selecionado: branco com borda. Altura 44, cantos totalmente arredondados.
 */
@Composable
fun SelectChip(text: String, selected: Boolean, onClick: () -> Unit, @DrawableRes icon: Int? = null) {
    Row(
        modifier = Modifier
            .height(ChipHeight)
            .sobraCard(
                Dimens.RadiusFull,
                background = if (selected) SobraColors.TealDark else SobraColors.Surface,
                borderColor = if (selected) null else SobraColors.Border,
            )
            // selectable informa "selecionado" ao leitor de tela (botão de opção).
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = Dimens.Space16),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.Space8),
    ) {
        if (icon != null) SobraIcon(icon, tint = if (selected) SobraColors.OnTeal else SobraColors.Teal)
        Text(text, style = ChipText, color = if (selected) SobraColors.OnTeal else SobraColors.TextPrimary, maxLines = 1)
    }
}

/**
 * Data em chips: "Hoje" e "Ontem" cobrem quase todos os lançamentos; "Outra data" abre o
 * calendário e, depois de escolhida, mostra a própria data no chip.
 */
@Composable
fun DateChips(date: LocalDate, onDateSelected: (LocalDate) -> Unit, onPickOtherDate: () -> Unit) {
    val today = remember { LocalDate.now() }
    val yesterday = today.minusDays(1)
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
}
