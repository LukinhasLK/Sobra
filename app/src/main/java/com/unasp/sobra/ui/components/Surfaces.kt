package com.unasp.sobra.ui.components

import android.graphics.BlurMaskFilter
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import com.unasp.sobra.ui.theme.Dimens
import com.unasp.sobra.ui.theme.SobraColors

/**
 * Aparência de card: cantos arredondados + fundo + borda interna de 1.
 * A ordem importa: `clip` recorta tudo que vem depois (fundo, borda e o efeito de toque).
 */
fun Modifier.sobraCard(
    radius: Dp,
    background: Color = SobraColors.Surface,
    borderColor: Color? = SobraColors.Border,
): Modifier {
    val shape = RoundedCornerShape(radius)
    val border = if (borderColor != null) Modifier.border(Dimens.BorderWidth, borderColor, shape) else Modifier
    // `then` concatena outra cadeia de modificadores a esta.
    return clip(shape).background(background).then(border)
}

// O "blur" do Figma equivale a um desfoque gaussiano com sigma = blur / 2. O BlurMaskFilter
// do Android usa sigma = raio × 0,57735 + 0,5; esta conta converte um no outro.
private const val FIGMA_BLUR_TO_SIGMA = 0.5f
private const val ANDROID_SIGMA_SCALE = 0.57735f
private const val ANDROID_SIGMA_OFFSET = 0.5f

/**
 * Sombra como no Figma (deslocamento Y, blur e cor), que o `Modifier.shadow` do Material
 * não reproduz. Deve vir ANTES de `clip`/`background` na cadeia, senão é recortada.
 */
fun Modifier.figmaShadow(color: Color, blur: Dp, offsetY: Dp, cornerRadius: Dp): Modifier =
    // drawBehind desenha atrás do conteúdo do próprio composable, usando o Canvas dele.
    drawBehind {
        val sigma = blur.toPx() * FIGMA_BLUR_TO_SIGMA
        val blurRadius = ((sigma - ANDROID_SIGMA_OFFSET) / ANDROID_SIGMA_SCALE).coerceAtLeast(1f)
        val paint = Paint().apply {
            // O Paint do Compose não tem desfoque; pegamos o Paint nativo do Android por baixo.
            asFrameworkPaint().apply {
                this.color = color.toArgb()
                maskFilter = BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL)
            }
        }
        val dy = offsetY.toPx()
        val r = cornerRadius.toPx()
        drawIntoCanvas { canvas ->
            canvas.drawRoundRect(0f, dy, size.width, size.height + dy, r, r, paint)
        }
    }

/** Divisória horizontal de 1. */
@Composable
fun SobraDivider(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(Dimens.BorderWidth)
            .background(SobraColors.Border),
    )
}

/**
 * Ícone do Figma. O tamanho vem do próprio arquivo (ic_nome_16 tem 16dp), por isso não há
 * parâmetro de tamanho: assim o traço mantém a espessura original.
 */
@Composable
fun SobraIcon(
    @DrawableRes id: Int,
    tint: Color,
    modifier: Modifier = Modifier,
    contentDescription: String? = null, // null = decorativo (leitor de tela ignora)
) {
    Icon(
        painter = painterResource(id),
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint,
    )
}
