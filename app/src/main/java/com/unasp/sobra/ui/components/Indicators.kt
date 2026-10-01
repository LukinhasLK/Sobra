package com.unasp.sobra.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unasp.sobra.R
import com.unasp.sobra.ui.theme.Dimens
import com.unasp.sobra.ui.theme.SobraColors
import com.unasp.sobra.ui.theme.SobraTheme
import com.unasp.sobra.ui.theme.manrope

// ---------------------------------------------------------------------------------------
// ProgressTrack
// ---------------------------------------------------------------------------------------

/** Alturas de trilha usadas no design. */
object ProgressTrackHeight {
    val Step = 6.dp      // passos de configuração
    val Category = 8.dp  // categorias da Home
    val Goal = 10.dp     // objetivos e prazo da simulação
}

private const val PILL_PERCENT = 50 // raio = metade da altura

/**
 * Barra de progresso: dois Box sobrepostos (trilha Soft + preenchimento).
 * Não usa LinearProgressIndicator, que tem altura, cantos e animação próprios do Material.
 */
@Composable
fun ProgressTrack(
    progress: Float,
    height: Dp,
    modifier: Modifier = Modifier,
    color: Color = SobraColors.Teal,
) {
    val shape = RoundedCornerShape(PILL_PERCENT)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(SobraColors.Soft),
    ) {
        Box(
            modifier = Modifier
                // fillMaxWidth(fração): ocupa só essa fração da largura da trilha.
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(shape)
                .background(color),
        )
    }
}

// ---------------------------------------------------------------------------------------
// Badge
// ---------------------------------------------------------------------------------------

/** Etiqueta: raio 8, fundo TealLight, texto Teal. Padding e estilo variam por tela. */
@Composable
fun Badge(
    text: String,
    textStyle: TextStyle,
    horizontalPadding: Dp,
    verticalPadding: Dp,
    modifier: Modifier = Modifier,
    radius: Dp = Dimens.Radius8,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(radius))
            .background(SobraColors.TealLight)
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, style = textStyle, color = SobraColors.Teal, maxLines = 1)
    }
}

// ---------------------------------------------------------------------------------------
// Fab
// ---------------------------------------------------------------------------------------

private val FabSize = 56.dp
private val FabShadowBlur = 16.dp
private val FabShadowOffsetY = 4.dp

/** Botão flutuante: 56×56, raio 28, Teal, ícone "+" de 24, sombra Teal 20%. */
@Composable
fun Fab(onClick: () -> Unit, contentDescription: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(FabSize)
            .figmaShadow(SobraColors.TealShadow, FabShadowBlur, FabShadowOffsetY, Dimens.Radius28)
            .clip(RoundedCornerShape(Dimens.Radius28))
            .background(SobraColors.Teal)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        SobraIcon(R.drawable.ic_plus_24, tint = SobraColors.OnTeal, contentDescription = contentDescription)
    }
}

// ---------------------------------------------------------------------------------------
// Switch
// ---------------------------------------------------------------------------------------

private val SwitchWidth = 44.dp
private val SwitchHeight = 24.dp
private val SwitchThumb = 20.dp

/**
 * Switch próprio: trilha 44×24 (raio 12, padding 2) e thumb branco de 20.
 * TODO: o Figma só mostra o estado ligado. Qual a cor da trilha desligada? (usando Border)
 * TODO: duração da transição ligado/desligado não definida (hoje troca sem animação).
 */
@Composable
fun SobraSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(width = SwitchWidth, height = SwitchHeight)
            .clip(RoundedCornerShape(Dimens.Radius12))
            .background(if (checked) SobraColors.Teal else SobraColors.Border)
            // toggleable = clickable que informa ligado/desligado ao leitor de tela.
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(Dimens.Space2),
        // Ligado: thumb encostado à direita. Desligado: à esquerda.
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .size(SwitchThumb)
                .clip(CircleShape)
                .background(SobraColors.Surface),
        )
    }
}

// ---------------------------------------------------------------------------------------
// ProgressRing
// ---------------------------------------------------------------------------------------

private val RingDiameter = 96.dp
private const val RING_INNER_RATIO = 0.85f // raio interno ÷ externo no Figma
private const val RING_START_ANGLE = -90f  // 12 horas (0° no Canvas é 3 horas)
private const val FULL_CIRCLE = 360f
private val RingLabel = manrope(20, FontWeight.W800)

/**
 * Anel de progresso da tela de detalhe do objetivo. Diâmetro 96; a espessura sai da razão
 * interna 0,85: 96 × (1 − 0,85) ÷ 2 = 7,2.
 */
@Composable
fun ProgressRing(progress: Float, label: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(RingDiameter), contentAlignment = Alignment.Center) {
        // Canvas: área de desenho livre. Dentro dele as medidas são em pixels (toPx()).
        Canvas(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
            val thickness = size.minDimension * (1f - RING_INNER_RATIO) / 2f
            // O traço é desenhado centrado na linha do arco; recuamos metade da espessura
            // para ele caber inteiro dentro dos 96.
            val topLeft = Offset(thickness / 2f, thickness / 2f)
            val arcSize = Size(size.width - thickness, size.height - thickness)
            val stroke = Stroke(width = thickness, cap = StrokeCap.Butt) // Butt: ponta reta
            drawArc(SobraColors.Soft, RING_START_ANGLE, FULL_CIRCLE, false, topLeft, arcSize, style = stroke)
            // Decisão de fidelidade: sweep = progresso real × 360° (56% → 201,6°).
            // No mockup o arco tem ~200° (55,56%), levemente diferente do "56%" escrito.
            drawArc(
                SobraColors.Teal, RING_START_ANGLE, progress.coerceIn(0f, 1f) * FULL_CIRCLE,
                false, topLeft, arcSize, style = stroke,
            )
        }
        Text(text = label, style = RingLabel, color = SobraColors.Teal)
    }
}

// ---------------------------------------------------------------------------------------
// Passo de configuração (telas 08 e 09)
// ---------------------------------------------------------------------------------------

// Decisão de fidelidade: a barra mostra passo ÷ total (1 de 2 = 50%). No mockup o passo 1
// tem 56,78% (201 de 354). Para usar o valor do Figma, troque null por 0.5678f.
private val STEP_ONE_OF_TWO_OVERRIDE: Float? = null

private val StepLabel = manrope(13, FontWeight.W600)
private val StepCaption = manrope(13, FontWeight.W400)

@Composable
fun StepProgress(
    stepLabel: String,
    caption: String,
    step: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
) {
    val computed = step.toFloat() / totalSteps
    // `?:` (elvis): usa o valor da esquerda se não for nulo; senão, o da direita.
    val progress = if (step == 1 && totalSteps == 2) STEP_ONE_OF_TWO_OVERRIDE ?: computed else computed
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = Dimens.ScreenMargin, top = Dimens.Space12,
                end = Dimens.ScreenMargin, bottom = Dimens.Space24,
            ),
        verticalArrangement = Arrangement.spacedBy(Dimens.Space8),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = stepLabel, style = StepLabel, color = SobraColors.Teal)
            Text(text = caption, style = StepCaption, color = SobraColors.TextSecondary)
        }
        ProgressTrack(progress = progress, height = ProgressTrackHeight.Step)
    }
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun IndicatorsPreview() {
    SobraTheme {
        Column(
            modifier = Modifier
                .background(SobraColors.Background)
                .padding(Dimens.ScreenMargin),
            verticalArrangement = Arrangement.spacedBy(Dimens.Space20),
        ) {
            ProgressTrack(progress = 0.56f, height = ProgressTrackHeight.Goal)
            Badge("56%", manrope(12, FontWeight.W700), Dimens.Space8, Dimens.Space4)
            SobraSwitch(checked = true, onCheckedChange = {})
            ProgressRing(progress = 0.56f, label = "56%")
            Fab(onClick = {}, contentDescription = "Adicionar")
        }
    }
}
