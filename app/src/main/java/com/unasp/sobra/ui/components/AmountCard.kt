package com.unasp.sobra.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import com.unasp.sobra.ui.theme.SobraColors
import com.unasp.sobra.ui.theme.SobraType
import java.math.BigDecimal

/**
 * Card de valor em destaque (Renda mensal e Nova despesa): label + "R$" + valor grande.
 * As medidas mudam entre as duas telas, por isso chegam por parâmetro.
 */
@Composable
fun AmountCard(
    label: String,
    amount: BigDecimal,
    onAmountChange: (BigDecimal) -> Unit,
    prefixStyle: TextStyle,
    valueStyle: TextStyle,
    padding: Dp,
    labelGap: Dp,
    prefixGap: Dp,
    radius: Dp,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .sobraCard(radius)
            .padding(padding),
        verticalArrangement = Arrangement.spacedBy(labelGap),
    ) {
        Text(text = label, style = SobraType.FormLabel, color = SobraColors.TextSecondary)
        // "R$" (menor) e o valor (maior) ficam apoiados na mesma linha de base do texto,
        // e não alinhados pelo topo ou pelo centro: alignByBaseline() em cada um faz isso.
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(prefixGap),
        ) {
            Text(
                text = CURRENCY_SYMBOL,
                style = prefixStyle,
                color = SobraColors.TextSecondary,
                modifier = Modifier.alignByBaseline(),
            )
            MoneyTextField(
                amount = amount,
                onAmountChange = onAmountChange,
                textStyle = valueStyle.copy(color = SobraColors.Teal),
                modifier = Modifier
                    .alignByBaseline()
                    .weight(1f),
            )
        }
    }
}
