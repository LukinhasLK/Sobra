package com.unasp.sobra.ui.screens.states

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.unasp.sobra.R
import com.unasp.sobra.ui.components.BottomTab
import com.unasp.sobra.ui.components.ChevronTitleHeader
import com.unasp.sobra.ui.components.CompactButton
import com.unasp.sobra.ui.components.SobraDivider
import com.unasp.sobra.ui.components.SobraIcon
import com.unasp.sobra.ui.components.SobraScreen
import com.unasp.sobra.ui.components.StateContent
import com.unasp.sobra.ui.components.figmaShadow
import com.unasp.sobra.ui.components.sobraCard
import com.unasp.sobra.ui.theme.Dimens
import com.unasp.sobra.ui.theme.SobraColors
import com.unasp.sobra.ui.theme.SobraTheme
import com.unasp.sobra.ui.theme.SobraType
import com.unasp.sobra.ui.theme.manrope

// ---------------------------------------------------------------------------------------
// 21 Skeleton da Home
// ---------------------------------------------------------------------------------------

// O Figma não define a animação do skeleton (só o gradiente parado). Duração escolhida:
// 1200 ms por passada, velocidade constante, repetindo sem parar.
// TODO: confirmar duração/velocidade do shimmer com o design.
private const val SHIMMER_DURATION_MS = 1200
private val ShimmerBandWidth = 200.dp   // comprimento da faixa do gradiente
private val ShimmerTravel = 402.dp      // largura de referência percorrida pela faixa

private val SkeletonAvatar = 40.dp
private val SkeletonNotification = 36.dp
private val SkeletonIcon = 36.dp
private val SkeletonLine = 16.dp        // altura dos placeholders de texto
private val SkeletonTitle = 20.dp       // altura da saudação e do título da seção
private val SkeletonHighlightHeight = 32.dp
private val SkeletonBar = 8.dp
private val SkeletonBarRadius = 4.dp
private val SkeletonRowHeight = 60.dp
private val W60 = 60.dp
private val W70 = 70.dp
private val W80 = 80.dp
private val W90 = 90.dp
private val W100 = 100.dp
private val W110 = 110.dp
private val W120 = 120.dp
private val W160 = 160.dp
private const val SKELETON_ROWS = 3

/** Pincel animado compartilhado por todos os placeholders: Border → Soft (0,5) → Border, na diagonal. */
@Composable
private fun rememberShimmerBrush(): Brush {
    // rememberInfiniteTransition cria uma animação que se repete enquanto a tela existir.
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(SHIMMER_DURATION_MS, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmerProgress",
    )
    // LocalDensity converte dp em pixels fora de um Canvas.
    val (band, travel) = with(LocalDensity.current) { ShimmerBandWidth.toPx() to ShimmerTravel.toPx() }
    // A faixa começa antes da borda esquerda e termina depois da direita.
    val start = -band + (travel + band) * progress
    return Brush.linearGradient(
        colorStops = arrayOf(0f to SobraColors.Border, 0.5f to SobraColors.Soft, 1f to SobraColors.Border),
        start = Offset(start, 0f),
        end = Offset(start + band, band), // x e y crescem juntos: gradiente diagonal
    )
}

@Composable
private fun Placeholder(brush: Brush, width: Dp, height: Dp, shape: Shape = RoundedCornerShape(Dimens.Radius8)) {
    Box(
        Modifier
            .size(width, height)
            .clip(shape)
            .background(brush),
    )
}

/** 21 Skeleton da Home — mesma estrutura da Home, sem dados e sem spinner. */
@Composable
fun HomeSkeletonScreen(onTabSelected: (BottomTab) -> Unit, modifier: Modifier = Modifier) {
    val brush = rememberShimmerBrush()
    SobraScreen(modifier = modifier, bottomTab = BottomTab.Home, onTabSelected = onTabSelected) {
        // Header: padding 16/24/8/24.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Dimens.ScreenMargin, top = Dimens.Space16, end = Dimens.ScreenMargin, bottom = Dimens.Space8),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.Space12)) {
                Placeholder(brush, SkeletonAvatar, SkeletonAvatar, CircleShape)
                Placeholder(brush, W120, SkeletonTitle)
            }
            Placeholder(brush, SkeletonNotification, SkeletonNotification, CircleShape)
        }
        // Card: padding 24, gap 20, raio 24.
        Column(
            modifier = Modifier
                .padding(Dimens.Space24)
                .fillMaxWidth()
                .sobraCard(Dimens.Radius24)
                .padding(Dimens.Space24),
            verticalArrangement = Arrangement.spacedBy(Dimens.Space20),
        ) {
            SkeletonLinePair(brush, W80, W100)
            SkeletonLinePair(brush, W90, W100)
            SobraDivider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Placeholder(brush, W70, SkeletonLine)
                Placeholder(brush, W110, SkeletonHighlightHeight, RoundedCornerShape(Dimens.Radius12))
            }
        }
        // Seção: padding 0/24/24/24, gap 16 (título → lista) e 12 (entre linhas).
        Column(
            modifier = Modifier.padding(start = Dimens.ScreenMargin, end = Dimens.ScreenMargin, bottom = Dimens.Space24),
            verticalArrangement = Arrangement.spacedBy(Dimens.Space16),
        ) {
            Placeholder(brush, W160, SkeletonTitle)
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space12)) {
                repeat(SKELETON_ROWS) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(SkeletonRowHeight)
                            .sobraCard(Dimens.Radius16)
                            .padding(Dimens.Space12),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.Space12),
                    ) {
                        Placeholder(brush, SkeletonIcon, SkeletonIcon, RoundedCornerShape(Dimens.Radius12))
                        Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space8)) {
                            SkeletonLinePair(brush, W80, W60)
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(SkeletonBar)
                                    .clip(RoundedCornerShape(SkeletonBarRadius))
                                    .background(brush),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SkeletonLinePair(brush: Brush, leftWidth: Dp, rightWidth: Dp) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Placeholder(brush, leftWidth, SkeletonLine)
        Placeholder(brush, rightWidth, SkeletonLine)
    }
}

// ---------------------------------------------------------------------------------------
// 22 Estado de erro
// ---------------------------------------------------------------------------------------

/** 22 Estado de erro. Sem card e sem snackbar: só o que existe no Figma. */
@Composable
fun ErrorScreen(
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    // TODO: política de retry não definida (quantas tentativas? o botão muda durante a nova tentativa?).
    SobraScreen(modifier = modifier, bottomTab = BottomTab.Home, onTabSelected = onTabSelected) {
        ChevronTitleHeader(
            title = stringResource(R.string.error_header),
            onBack = onBack,
            backDescription = stringResource(R.string.action_back),
            chevronTint = SobraColors.Teal,
        )
        StateContent(
            icon = R.drawable.ic_alert_64,
            circleColor = SobraColors.ErrorBg,
            iconTint = SobraColors.Red,
            title = stringResource(R.string.error_title),
            description = stringResource(R.string.error_description),
            buttonText = stringResource(R.string.action_retry),
            onButtonClick = onRetry,
        )
    }
}

// ---------------------------------------------------------------------------------------
// 23 Confirmação (modal)
// ---------------------------------------------------------------------------------------

private val ModalWidth = 320.dp
private val ModalIconCircle = 72.dp
private val ModalShadowBlur = 24.dp
private val ModalShadowOffsetY = 10.dp
private val ModalButtonHeight = 48.dp
private val ModalTitle = manrope(20, FontWeight.W800)
private val ModalDescription = manrope(14, FontWeight.W400, lineHeight = 21.sp) // linha 150%

/** Propriedades da janela do modal: largura livre e scrim cobrindo as barras do sistema. */
val ConfirmationDialogProperties = DialogProperties(
    usePlatformDefaultWidth = false, // deixa o conteúdo ocupar a largura toda
    decorFitsSystemWindows = false,  // o scrim cobre também as barras do sistema
)

/**
 * 23 Confirmação, como Dialog avulso. Na navegação o app usa [ConfirmationModal] dentro de
 * um destino `dialog(...)`, que já cria a janela de diálogo.
 */
@Composable
fun ConfirmationDialog(onDismiss: () -> Unit) {
    // onDismissRequest: botão/gesto de voltar do sistema.
    Dialog(onDismissRequest = onDismiss, properties = ConfirmationDialogProperties) {
        ConfirmationModal(onDismiss)
    }
}

/**
 * Conteúdo do modal de confirmação: scrim #1C1917 (alpha 179/255) + cartão central.
 * Aparece por cima da tela que já está aberta (a Home real continua atrás, sem ser
 * redesenhada).
 */
@Composable
fun ConfirmationModal(onDismiss: () -> Unit) {
    // A janela de diálogo do Android já escurece o fundo com um cinza próprio. Zeramos
    // esse escurecimento para valer só o scrim do Figma, desenhado abaixo.
    // `as?` é um cast seguro: fora de um diálogo (ex.: no @Preview) o resultado é null.
    val window = (LocalView.current.parent as? DialogWindowProvider)?.window
    SideEffect { window?.setDimAmount(0f) } // SideEffect: roda após cada composição bem-sucedida

    // TODO: tocar fora do modal fecha? O design não define (hoje só "OK" e o voltar fecham).
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SobraColors.Scrim),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .width(ModalWidth)
                .figmaShadow(SobraColors.ModalShadow, ModalShadowBlur, ModalShadowOffsetY, Dimens.Radius32)
                .sobraCard(Dimens.Radius32, borderColor = null)
                .padding(Dimens.Space32),
            verticalArrangement = Arrangement.spacedBy(Dimens.Space24),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(ModalIconCircle)
                    .clip(CircleShape)
                    .background(SobraColors.TealLight),
                contentAlignment = Alignment.Center,
            ) {
                // O ícone do Figma lembra brilhos, não um check: a geometria é preservada.
                SobraIcon(R.drawable.ic_sparkles_32, tint = SobraColors.Teal)
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(Dimens.Space8),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(stringResource(R.string.confirmation_title), style = ModalTitle, color = SobraColors.TextPrimary)
                Text(
                    stringResource(R.string.confirmation_description),
                    style = ModalDescription, color = SobraColors.TextSecondary, textAlign = TextAlign.Center,
                )
            }
            CompactButton(
                text = stringResource(R.string.action_ok),
                onClick = onDismiss,
                height = ModalButtonHeight,
                radius = Dimens.Radius24,
            )
        }
    }
}

// ---------------------------------------------------------------------------------------
// 24 Sessão expirada
// ---------------------------------------------------------------------------------------

/** 24 Sessão expirada — sem header e sem navegação; bloco centralizado na tela. */
@Composable
fun SessionExpiredScreen(onLogin: () -> Unit, modifier: Modifier = Modifier) {
    SobraScreen(modifier = modifier) {
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            StateContent(
                icon = R.drawable.ic_user_64, // usuário, não cadeado: é o ícone que está no Figma
                circleColor = SobraColors.Soft,
                iconTint = SobraColors.Teal,
                title = stringResource(R.string.session_title),
                titleStyle = SobraType.ScreenTitle,
                description = stringResource(R.string.session_description),
                buttonText = stringResource(R.string.session_button),
                buttonTextStyle = SobraType.Button,
                onButtonClick = onLogin,
            )
        }
    }
}

@Preview(widthDp = 402, heightDp = 874)
@Composable
private fun HomeSkeletonScreenPreview() {
    SobraTheme { HomeSkeletonScreen(onTabSelected = {}) }
}

@Preview(widthDp = 402, heightDp = 874)
@Composable
private fun ErrorScreenPreview() {
    SobraTheme { ErrorScreen(onBack = {}, onRetry = {}, onTabSelected = {}) }
}

@Preview(widthDp = 402, heightDp = 874)
@Composable
private fun ConfirmationPreview() {
    SobraTheme { ConfirmationModal(onDismiss = {}) }
}

@Preview(widthDp = 402, heightDp = 874)
@Composable
private fun SessionExpiredScreenPreview() {
    SobraTheme { SessionExpiredScreen(onLogin = {}) }
}
