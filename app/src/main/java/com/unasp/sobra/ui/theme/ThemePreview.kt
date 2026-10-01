package com.unasp.sobra.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// Arquivo só de conferência visual do tema no Android Studio (painel "Split"/"Design").
// Não é usado pelo app.

private val SwatchSize = 28.dp
private val SwatchGap = 12.dp
private val RowGap = 8.dp
private const val FIGMA_WIDTH_DP = 402

@Preview(name = "Cores", showBackground = true, widthDp = FIGMA_WIDTH_DP)
@Composable
private fun ColorsPreview() {
    // `to` cria um Pair(nome, cor); listOf monta a lista imutável.
    val tokens = listOf(
        "Background  #FAF8F5" to SobraColors.Background,
        "Surface  #FFFFFF" to SobraColors.Surface,
        "TextPrimary  #1C1917" to SobraColors.TextPrimary,
        "TextSecondary  #78716C" to SobraColors.TextSecondary,
        "Teal  #0D9488" to SobraColors.Teal,
        "TealLight  #CCFBF1" to SobraColors.TealLight,
        "Soft  #F2EFEA" to SobraColors.Soft,
        "Border  #E7E5E4" to SobraColors.Border,
        "Blue  #3B82F6" to SobraColors.Blue,
        "Amber  #F59E0B" to SobraColors.Amber,
        "Red  #EF4444" to SobraColors.Red,
        "Green  #10B981" to SobraColors.Green,
        "Violet  #8B5CF6" to SobraColors.Violet,
        "ErrorBg  #FEF2F2" to SobraColors.ErrorBg,
        "Scrim (179/255)" to SobraColors.Scrim,
        "TealShadow (0.20)" to SobraColors.TealShadow,
        "ChartNoYield (0.40)" to SobraColors.ChartNoYield,
        "Ícone de categoria: Teal (21/255)" to SobraColors.categoryIconBackground(SobraColors.Teal),
    )
    SobraTheme {
        Column(
            // Modificadores são aplicados na ordem escrita: primeiro pinta o fundo, depois recua.
            modifier = Modifier
                .background(SobraColors.Background)
                .padding(Dimens.ScreenMargin),
            verticalArrangement = Arrangement.spacedBy(RowGap),
        ) {
            // Desestruturação: (name, color) separa os dois valores de cada Pair.
            tokens.forEach { (name, color) -> SwatchRow(name, color) }
        }
    }
}

@Composable
private fun SwatchRow(name: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SwatchGap),
    ) {
        Box(
            modifier = Modifier
                .size(SwatchSize)
                .background(color, RoundedCornerShape(Dimens.Radius8))
                .border(Dimens.BorderWidth, SobraColors.Border, RoundedCornerShape(Dimens.Radius8)),
        )
        Text(text = name, style = SobraType.FormLabel)
    }
}

@Preview(name = "Tipografia", showBackground = true, widthDp = FIGMA_WIDTH_DP)
@Composable
private fun TypePreview() {
    val styles: List<Pair<String, TextStyle>> = listOf(
        "AuthTitle 24/700 · Boas-vindas ao Sobra" to SobraType.AuthTitle,
        "AuthBody 15/400 · Gerencie seus gastos e cultive sua saúde financeira." to SobraType.AuthBody,
        "ScreenTitle 22/800 · Objetivos" to SobraType.ScreenTitle,
        "FormLabel 13/600 · Valor recebido por mês" to SobraType.FormLabel,
        "Button 16/600 · Continuar" to SobraType.Button,
        "ButtonCompact 15/600 · Tentar novamente" to SobraType.ButtonCompact,
        "NavLabel 11/500 · Início" to SobraType.NavLabel,
        "NavLabelActive 11/700 · Início" to SobraType.NavLabelActive,
    )
    SobraTheme {
        Column(
            modifier = Modifier
                .background(SobraColors.Background)
                .padding(Dimens.ScreenMargin),
            verticalArrangement = Arrangement.spacedBy(SwatchGap),
        ) {
            styles.forEach { (sample, style) -> Text(text = sample, style = style) }
        }
    }
}
