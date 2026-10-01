package com.unasp.sobra.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unasp.sobra.R
import com.unasp.sobra.ui.format.parseMoneyInput
import com.unasp.sobra.ui.format.toBrlNumber
import com.unasp.sobra.ui.theme.Dimens
import com.unasp.sobra.ui.theme.SobraColors
import com.unasp.sobra.ui.theme.SobraTheme
import com.unasp.sobra.ui.theme.SobraType
import com.unasp.sobra.ui.theme.manrope
import java.math.BigDecimal

private val SoftFieldHeight = 50.dp
private val WhiteFieldHeight = 48.dp

private val FieldText = manrope(15, FontWeight.W400, lineHeight = 22.5.sp) // 15/400, linha 150%
private val WhiteFieldText = manrope(15, FontWeight.W400)
private val MoneyPrefixText = manrope(15, FontWeight.W700)
private val MoneyValueText = manrope(15, FontWeight.W600)

const val CURRENCY_SYMBOL = "R$"

// TODO: o design não define os estados de foco, erro de validação e desabilitado dos campos.
// TODO: comportamento de textos longos nos campos não definido (hoje: uma linha, rola na horizontal).

/**
 * Campo suave das telas de autenticação: label em cima + caixa de 50, raio 25, fundo Soft.
 * `trailing` é um espaço opcional à direita, dentro da caixa (ex.: "Mostrar" da senha).
 */
@Composable
fun SoftField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailing: (@Composable () -> Unit)? = null,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Dimens.Space8)) {
        Text(text = label, style = SobraType.FormLabel, color = SobraColors.TextSecondary)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(SoftFieldHeight)
                .clip(RoundedCornerShape(Dimens.Radius25))
                .background(SobraColors.Soft)
                .padding(horizontal = Dimens.Space20),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.Space12),
        ) {
            // BasicTextField é o campo "cru": sem caixa, sem label, sem padding do Material.
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f), // ocupa o que sobrar da linha
                singleLine = true,
                // TODO: a cor do texto digitado não aparece no Figma (só o placeholder). TextPrimary?
                textStyle = FieldText.copy(color = SobraColors.TextPrimary),
                cursorBrush = SolidColor(SobraColors.Teal),
                keyboardOptions = keyboardOptions,
                visualTransformation = visualTransformation,
                // decorationBox envolve o texto digitado (innerTextField): aqui só para o placeholder.
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) {
                            Text(text = placeholder, style = FieldText, color = SobraColors.TextSecondary, maxLines = 1)
                        }
                        innerTextField()
                    }
                },
            )
            trailing?.invoke() // `?.invoke()` chama a função só se ela não for nula
        }
    }
}

/**
 * Base do campo branco dos formulários: label em cima + caixa de 48, raio 25, borda 1.
 * O conteúdo da caixa é livre (`content`), disposto em linha.
 */
@Composable
fun WhiteField(
    label: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Dimens.Space8)) {
        Text(text = label, style = SobraType.FormLabel, color = SobraColors.TextSecondary)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(WhiteFieldHeight)
                .sobraCard(Dimens.Radius25)
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = Dimens.Space20, vertical = Dimens.Space14),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

/** Campo branco de texto livre. */
@Composable
fun WhiteTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    WhiteField(label = label, modifier = modifier) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = WhiteFieldText.copy(color = SobraColors.TextPrimary),
            cursorBrush = SolidColor(SobraColors.Teal),
        )
    }
}

/** Campo branco de dinheiro: prefixo "R$" 15/700, gap 4, valor 15/600. */
@Composable
fun WhiteMoneyField(
    label: String,
    amount: BigDecimal,
    onAmountChange: (BigDecimal) -> Unit,
    modifier: Modifier = Modifier,
) {
    WhiteField(label = label, modifier = modifier) {
        Text(text = CURRENCY_SYMBOL, style = MoneyPrefixText, color = SobraColors.TextSecondary)
        MoneyTextField(
            amount = amount,
            onAmountChange = onAmountChange,
            textStyle = MoneyValueText.copy(color = SobraColors.TextPrimary),
            modifier = Modifier
                .padding(start = Dimens.Space4)
                .weight(1f),
        )
    }
}

/**
 * Campo branco que abre um seletor (categoria, data, prazo): texto + chevron de 14.
 * TODO: os seletores abertos (lista de categorias, calendário, prazos) não existem no Figma.
 */
@Composable
fun WhiteSelectorField(
    label: String,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
) {
    WhiteField(label = label, modifier = modifier, onClick = onClick) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.Space10),
        ) {
            leading?.invoke()
            Text(text = text, style = WhiteFieldText, color = SobraColors.TextPrimary, maxLines = 1)
        }
        SobraIcon(R.drawable.ic_chevron_down_14, tint = SobraColors.TextSecondary)
    }
}

/**
 * Entrada de dinheiro sem caixa: mostra sempre o valor formatado ("5.500,00") e trata cada
 * dígito digitado como centavos. Usado dentro dos cards de valor e do WhiteMoneyField.
 */
@Composable
fun MoneyTextField(
    amount: BigDecimal,
    onAmountChange: (BigDecimal) -> Unit,
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
) {
    val text = amount.toBrlNumber()
    BasicTextField(
        // TextFieldValue = texto + posição do cursor. Forçamos o cursor no fim, porque o
        // texto é reformatado a cada tecla.
        value = TextFieldValue(text = text, selection = TextRange(text.length)),
        onValueChange = { onAmountChange(parseMoneyInput(it.text)) },
        modifier = modifier,
        singleLine = true,
        textStyle = textStyle,
        cursorBrush = SolidColor(SobraColors.Teal),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun FieldsPreview() {
    SobraTheme {
        Column(
            modifier = Modifier
                .background(SobraColors.Background)
                .padding(Dimens.ScreenMargin),
            verticalArrangement = Arrangement.spacedBy(Dimens.Space16),
        ) {
            SoftField("E-mail", value = "", onValueChange = {}, placeholder = "seu@email.com")
            WhiteTextField("Descrição", value = "Supermercado Mensal", onValueChange = {})
            WhiteMoneyField("Valor inicial", amount = BigDecimal("1000.00"), onAmountChange = {})
            WhiteSelectorField("Prazo", text = "18 meses (Março 2028)", onClick = {})
        }
    }
}
