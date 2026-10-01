package com.unasp.sobra.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unasp.sobra.ui.theme.Dimens
import com.unasp.sobra.ui.theme.SobraColors
import com.unasp.sobra.ui.theme.SobraTheme
import com.unasp.sobra.ui.theme.SobraType

private val ButtonHeight = 54.dp
private val CompactButtonHeight = 50.dp

// TODO: o design não define os estados desabilitado, pressionado e de carregamento dos botões.

/** Botão principal: altura 54, raio 27, fundo Teal, texto branco 16/600. */
@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    SobraButton(text, onClick, modifier, SobraColors.Teal, SobraColors.OnTeal)
}

/** Botão secundário: mesma geometria, fundo Soft, texto TextPrimary. */
@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    SobraButton(text, onClick, modifier, SobraColors.Soft, SobraColors.TextPrimary)
}

/**
 * CTA menor das telas de estado (vazio, erro, sessão): 50 de altura, raio 25.
 * O modal de confirmação usa a mesma base com 48 de altura e raio 24.
 */
@Composable
fun CompactButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = SobraType.ButtonCompact,
    height: Dp = CompactButtonHeight,
    radius: Dp = Dimens.Radius25,
) {
    SobraButton(text, onClick, modifier, SobraColors.Teal, SobraColors.OnTeal, textStyle, height, radius)
}

@Composable
private fun SobraButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    background: Color,
    contentColor: Color,
    textStyle: TextStyle = SobraType.Button,
    height: Dp = ButtonHeight,
    radius: Dp = Dimens.Radius27,
) {
    // Box em vez do Button do Material: o Button impõe altura mínima, padding e elevação próprios.
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(radius)) // recorta o fundo e a onda de toque no formato do botão
            .background(background)
            .clickable(role = Role.Button, onClick = onClick) // Role.Button: leitor de tela anuncia "botão"
            .padding(horizontal = Dimens.Space24),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, style = textStyle, color = contentColor, maxLines = 1)
    }
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun ButtonsPreview() {
    SobraTheme {
        Column(
            modifier = Modifier
                .background(SobraColors.Background)
                .padding(Dimens.ScreenMargin),
            verticalArrangement = Arrangement.spacedBy(Dimens.Space12),
        ) {
            PrimaryButton("Entrar", onClick = {})
            SecondaryButton("Criar conta", onClick = {})
            CompactButton("Tentar novamente", onClick = {})
        }
    }
}
