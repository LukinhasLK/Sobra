package com.unasp.sobra.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unasp.sobra.R
import com.unasp.sobra.ui.theme.Dimens
import com.unasp.sobra.ui.theme.SobraColors
import com.unasp.sobra.ui.theme.SobraType
import com.unasp.sobra.ui.theme.manrope

private val LinkText = manrope(13, FontWeight.W600)

// Sobre a área de toque de 48dp: o Compose já amplia sozinho a área clicável de qualquer
// elemento `clickable` menor que 48dp, SEM mudar o tamanho no layout. Por isso os links e
// ícones abaixo mantêm o tamanho visual do Figma. `minimumInteractiveComponentSize()` não é
// usado porque ele reserva 48dp no layout e aumentaria a altura dos headers (42 → 60).

/** Link de texto 13/600 (Teal por padrão): "Pular", "Mostrar", "Esqueci minha senha"... */
@Composable
fun TextLink(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = SobraColors.Teal,
    textAlign: TextAlign? = null,
) {
    Text(
        text = text,
        style = LinkText,
        color = color,
        textAlign = textAlign,
        modifier = modifier.clickable(role = Role.Button, onClick = onClick),
    )
}

/** "← Voltar": ícone 16, gap 6, texto 13/600 Teal. */
@Composable
fun BackLink(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.clickable(role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.Space6),
    ) {
        SobraIcon(R.drawable.ic_arrow_left_16, tint = SobraColors.Teal)
        Text(text = text, style = LinkText, color = SobraColors.Teal)
    }
}

/**
 * Header das telas de autenticação e configuração: voltar + título 24/700 + descrição 15/400.
 * Padding 12/24/24/24, gap 20 (voltar → textos) e 8 (título → descrição).
 */
@Composable
fun AuthHeader(
    backText: String,
    onBack: () -> Unit,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = Dimens.ScreenMargin, top = Dimens.Space12,
                end = Dimens.ScreenMargin, bottom = Dimens.Space24,
            ),
        verticalArrangement = Arrangement.spacedBy(Dimens.Space20),
    ) {
        BackLink(text = backText, onClick = onBack)
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space8)) {
            Text(text = title, style = SobraType.AuthTitle, color = SobraColors.TextPrimary)
            Text(text = description, style = SobraType.AuthBody, color = SobraColors.TextSecondary)
        }
    }
}

/**
 * Header com título 22/800 à esquerda e uma ação opcional à direita.
 * Padding 12/24/12/24 (em Gastos o inferior é 0: use `bottomPadding`).
 */
@Composable
fun TitleHeader(
    title: String,
    modifier: Modifier = Modifier,
    bottomPadding: Dp = Dimens.Space12,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = Dimens.ScreenMargin, top = Dimens.Space12,
                end = Dimens.ScreenMargin, bottom = bottomPadding,
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = title, style = SobraType.ScreenTitle, color = SobraColors.TextPrimary)
        trailing?.invoke()
    }
}

/** Header com chevron de 24 + título 22/800 (telas 18 e 22). Gap 12. */
@Composable
fun ChevronTitleHeader(
    title: String,
    onBack: () -> Unit,
    backDescription: String,
    chevronTint: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.ScreenMargin, vertical = Dimens.Space12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.Space12),
    ) {
        SobraIcon(
            R.drawable.ic_chevron_left_24,
            tint = chevronTint,
            contentDescription = backDescription,
            modifier = Modifier.clickable(role = Role.Button, onClick = onBack),
        )
        Text(text = title, style = SobraType.ScreenTitle, color = SobraColors.TextPrimary)
    }
}

/** Header de voltar (ícone 16 + texto) com ação opcional à direita (telas 15 e 17). */
@Composable
fun BackHeader(
    backText: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.ScreenMargin, vertical = Dimens.Space12),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BackLink(text = backText, onClick = onBack)
        trailing?.invoke()
    }
}

/**
 * Botão redondo de header com borda (notificações 36, adicionar 34, fechar 26).
 * @param borderColor `null` = sem borda (o "fechar" usa fundo Soft sem borda).
 */
@Composable
fun CircleIconButton(
    @DrawableRes icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
    size: Dp,
    modifier: Modifier = Modifier,
    background: Color = SobraColors.Surface,
    borderColor: Color? = SobraColors.Border,
    tint: Color = SobraColors.TextPrimary,
) {
    Box(
        modifier = modifier
            .size(size)
            .sobraCard(Dimens.Radius20, background, borderColor)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        SobraIcon(icon, tint = tint, contentDescription = contentDescription)
    }
}

// ---------------------------------------------------------------------------------------
// Estado (vazio, erro, sessão expirada)
// ---------------------------------------------------------------------------------------

private val IllustrationSize = 140.dp
private val StateTitle = manrope(18, FontWeight.W700)
private val StateDescription = manrope(14, FontWeight.W400, lineHeight = 21.sp) // 14/400, linha 150%

/** Círculo de 140 com ícone de 64 (traço 3) no centro. */
@Composable
fun StateIllustration(
    @DrawableRes icon: Int,
    circleColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(IllustrationSize)
            .clip(CircleShape)
            .background(circleColor),
        contentAlignment = Alignment.Center,
    ) {
        SobraIcon(icon, tint = iconTint)
    }
}

/**
 * Bloco das telas de estado: ilustração, título, descrição e CTA. Padding 40, gap 28,
 * tudo centralizado na horizontal. O chamador decide a posição vertical.
 */
@Composable
fun StateContent(
    @DrawableRes icon: Int,
    circleColor: Color,
    iconTint: Color,
    title: String,
    description: String,
    buttonText: String,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
    titleStyle: androidx.compose.ui.text.TextStyle = StateTitle,
    buttonTextStyle: androidx.compose.ui.text.TextStyle = SobraType.ButtonCompact,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Dimens.Space40),
        verticalArrangement = Arrangement.spacedBy(Dimens.Space28),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StateIllustration(icon, circleColor, iconTint)
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.Space12),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = title, style = titleStyle, color = SobraColors.TextPrimary, textAlign = TextAlign.Center)
            Text(
                text = description,
                style = StateDescription,
                color = SobraColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
        }
        CompactButton(text = buttonText, onClick = onButtonClick, textStyle = buttonTextStyle)
    }
}
