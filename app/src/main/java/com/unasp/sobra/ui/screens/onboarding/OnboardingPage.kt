package com.unasp.sobra.ui.screens.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unasp.sobra.R
import com.unasp.sobra.ui.components.FooterActions
import com.unasp.sobra.ui.components.PrimaryButton
import com.unasp.sobra.ui.components.SobraScreen
import com.unasp.sobra.ui.components.TextLink
import com.unasp.sobra.ui.theme.Dimens
import com.unasp.sobra.ui.theme.SobraColors
import com.unasp.sobra.ui.theme.SobraTheme
import com.unasp.sobra.ui.theme.SobraType
import com.unasp.sobra.ui.theme.manrope

private val ImageHeight = 320.dp
private val DotSize = 8.dp
private val ActiveDotWidth = 24.dp
private val DotRadius = 4.dp
private val HeaderBrand = manrope(22, FontWeight.W800, tracking = (-1.5).sp)

/** Conteúdo de uma página de onboarding. */
data class OnboardingContent(
    @param:DrawableRes val image: Int,
    @param:StringRes val title: Int,
    @param:StringRes val description: Int,
    @param:StringRes val button: Int,
)

/** As três páginas (telas 02, 03 e 04), na ordem. */
val OnboardingPages = listOf(
    OnboardingContent(
        R.drawable.img_onboarding_1, R.string.onboarding_1_title,
        R.string.onboarding_1_description, R.string.onboarding_next,
    ),
    OnboardingContent(
        R.drawable.img_onboarding_2, R.string.onboarding_2_title,
        R.string.onboarding_2_description, R.string.onboarding_next,
    ),
    OnboardingContent(
        R.drawable.img_onboarding_3, R.string.onboarding_3_title,
        R.string.onboarding_3_description, R.string.onboarding_start,
    ),
)

/**
 * 02, 03, 04 Onboarding — uma única tela reutilizável; muda só o conteúdo e o marcador ativo.
 */
@Composable
fun OnboardingPage(
    content: OnboardingContent,
    pageIndex: Int,
    pageCount: Int,
    onSkip: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SobraScreen(modifier = modifier) {
        // Header: padding 12/24/0/24.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Dimens.ScreenMargin, top = Dimens.Space12, end = Dimens.ScreenMargin),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.brand_name), style = HeaderBrand, color = SobraColors.Teal)
            TextLink(stringResource(R.string.onboarding_skip), onSkip, color = SobraColors.TextSecondary)
        }

        // Corpo: padding 24, gap 32. weight(1f) faz o corpo ocupar o espaço flexível entre
        // header e rodapé; a rolagem só entra em ação em telas baixas.
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(Dimens.Space24),
            verticalArrangement = Arrangement.spacedBy(Dimens.Space32),
        ) {
            Image(
                painter = painterResource(content.image),
                contentDescription = null,
                contentScale = ContentScale.Crop, // equivale ao FILL do Figma
                modifier = Modifier
                    .fillMaxWidth() // 354 no Figma = largura total − 24 de cada lado
                    .height(ImageHeight)
                    .clip(RoundedCornerShape(Dimens.Radius24)),
            )
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space12)) {
                Text(stringResource(content.title), style = SobraType.AuthTitle, color = SobraColors.TextPrimary)
                Text(stringResource(content.description), style = SobraType.AuthBody, color = SobraColors.TextSecondary)
            }
        }

        // Rodapé: padding 24, gap 24.
        FooterActions(gap = Dimens.Space24) {
            PageDots(activeIndex = pageIndex, count = pageCount)
            PrimaryButton(stringResource(content.button), onNext)
        }
    }
}

/** Paginação: marcador ativo 24×8 Teal, inativos 8×8 Soft, raio 4, gap 8. */
@Composable
private fun PageDots(activeIndex: Int, count: Int) {
    // TODO: o Figma informa só a caixa da paginação (354×8), não o alinhamento dos
    //  marcadores dentro dela. Estão à esquerda, alinhados com o texto. Confirmar.
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.Space8, Alignment.Start),
    ) {
        repeat(count) { index ->
            val active = index == activeIndex
            Box(
                modifier = Modifier
                    .size(width = if (active) ActiveDotWidth else DotSize, height = DotSize)
                    .clip(RoundedCornerShape(DotRadius))
                    .background(if (active) SobraColors.Teal else SobraColors.Soft),
            )
        }
    }
}

@Preview(widthDp = 402, heightDp = 874)
@Composable
private fun OnboardingPagePreview() {
    SobraTheme {
        OnboardingPage(OnboardingPages[0], pageIndex = 0, pageCount = OnboardingPages.size, onSkip = {}, onNext = {})
    }
}

@Preview(widthDp = 402, heightDp = 874)
@Composable
private fun OnboardingLastPagePreview() {
    SobraTheme {
        OnboardingPage(OnboardingPages[2], pageIndex = 2, pageCount = OnboardingPages.size, onSkip = {}, onNext = {})
    }
}
