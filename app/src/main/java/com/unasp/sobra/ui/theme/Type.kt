package com.unasp.sobra.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.unasp.sobra.R

// Um arquivo .ttf por peso em res/font. O Compose escolhe o arquivo pelo FontWeight pedido.
val Manrope = FontFamily(
    Font(R.font.manrope_regular, FontWeight.W400),
    Font(R.font.manrope_medium, FontWeight.W500),
    Font(R.font.manrope_semibold, FontWeight.W600),
    Font(R.font.manrope_bold, FontWeight.W700),
    Font(R.font.manrope_extrabold, FontWeight.W800),
)

// No Figma, o espaço extra da altura de linha é dividido igualmente acima e abaixo do texto,
// inclusive na primeira e na última linha. O padrão do Compose corta esse espaço nas pontas.
private val FigmaLineHeight = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

/**
 * Cria um estilo Manrope com as medidas do Figma (números do Figma = sp).
 *
 * Use para declarar os estilos específicos de cada tela como constantes nomeadas no
 * arquivo da tela/componente (ex.: `private val AmountStyle = manrope(48, FontWeight.W800)`).
 *
 * @param lineHeight altura de linha em sp. Deixe `Unspecified` quando o Figma diz "AUTO":
 *   aí valem as métricas da própria fonte, como no Figma.
 * @param tracking espaçamento entre letras em sp (pode ser negativo).
 */
fun manrope(
    size: Int,
    weight: FontWeight,
    lineHeight: TextUnit = TextUnit.Unspecified,
    tracking: TextUnit = 0.sp,
): TextStyle = TextStyle(
    fontFamily = Manrope,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight,
    // Sempre explícito (mesmo 0) para não herdar o tracking de outro estilo ao mesclar.
    letterSpacing = tracking,
    lineHeightStyle = FigmaLineHeight,
)

/** Estilos de texto compartilhados entre telas (seção 2 da especificação). */
object SobraType {

    /** Título de onboarding e autenticação: 24 / 700, linha 31,2, tracking −0,5. */
    // TODO: confirmar no Figma se o tracking −0,5 está em px (assumido aqui, vira sp) ou em %.
    val AuthTitle = manrope(24, FontWeight.W700, lineHeight = 31.2.sp, tracking = (-0.5).sp)

    /** Descrição de onboarding e autenticação: 15 / 400, linha 22,5. */
    val AuthBody = manrope(15, FontWeight.W400, lineHeight = 22.5.sp)

    /** Título de seção/tela principal: 22 / 800, linha auto. */
    val ScreenTitle = manrope(22, FontWeight.W800)

    /** Label de formulário: 13 / 600, linha auto. */
    val FormLabel = manrope(13, FontWeight.W600)

    /** Texto de botão principal (altura 54): 16 / 600. */
    val Button = manrope(16, FontWeight.W600)

    /** Texto do CTA de 50 de altura (estados vazio/erro): 15 / 600. */
    val ButtonCompact = manrope(15, FontWeight.W600)

    /** Item inativo da navegação: 11 / 500. */
    val NavLabel = manrope(11, FontWeight.W500)

    /** Item ativo da navegação: 11 / 700. */
    val NavLabelActive = manrope(11, FontWeight.W700)

    // TODO: o design não define o comportamento com fonte ampliada por acessibilidade
    //  (sp cresce com a escala do sistema). Limitar a escala ou deixar o layout crescer?
}

// Estilo-base de todo Text que não receber `style`: só a família, sem altura de linha nem
// tracking, para que os padrões do Material (24sp / 0.5sp) não vazem para as telas.
internal val BaseTextStyle = TextStyle(fontFamily = Manrope, letterSpacing = 0.sp)

// As telas usam SobraType/manrope(). Esta tipografia do Material só garante que qualquer
// componente Material que apareça por engano use Manrope em vez de Roboto.
internal val SobraMaterialTypography = Typography().let { m ->
    // `let` executa o bloco passando o objeto como parâmetro (aqui chamado de `m`).
    Typography(
        displayLarge = m.displayLarge.copy(fontFamily = Manrope),
        displayMedium = m.displayMedium.copy(fontFamily = Manrope),
        displaySmall = m.displaySmall.copy(fontFamily = Manrope),
        headlineLarge = m.headlineLarge.copy(fontFamily = Manrope),
        headlineMedium = m.headlineMedium.copy(fontFamily = Manrope),
        headlineSmall = m.headlineSmall.copy(fontFamily = Manrope),
        titleLarge = m.titleLarge.copy(fontFamily = Manrope),
        titleMedium = m.titleMedium.copy(fontFamily = Manrope),
        titleSmall = m.titleSmall.copy(fontFamily = Manrope),
        bodyLarge = m.bodyLarge.copy(fontFamily = Manrope),
        bodyMedium = m.bodyMedium.copy(fontFamily = Manrope),
        bodySmall = m.bodySmall.copy(fontFamily = Manrope),
        labelLarge = m.labelLarge.copy(fontFamily = Manrope),
        labelMedium = m.labelMedium.copy(fontFamily = Manrope),
        labelSmall = m.labelSmall.copy(fontFamily = Manrope),
    )
}
