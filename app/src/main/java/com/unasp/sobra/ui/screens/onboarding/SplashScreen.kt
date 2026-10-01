package com.unasp.sobra.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unasp.sobra.R
import com.unasp.sobra.ui.components.SobraIcon
import com.unasp.sobra.ui.components.figmaShadow
import com.unasp.sobra.ui.theme.Dimens
import com.unasp.sobra.ui.theme.SobraColors
import com.unasp.sobra.ui.theme.SobraTheme
import com.unasp.sobra.ui.theme.manrope

private val LogoCircleSize = 96.dp
private val LogoShadowBlur = 24.dp
private val LogoShadowOffsetY = 10.dp

private val BrandName = manrope(44, FontWeight.W800, tracking = (-2).sp)
private val Tagline = manrope(14, FontWeight.W500, tracking = 2.sp)
private val SloganMain = manrope(28, FontWeight.W700, lineHeight = 33.6.sp)      // linha 120%
private val SloganSecondary = manrope(16, FontWeight.W500, lineHeight = 24.sp)   // linha 150%

/**
 * 01 Splash — marca completa sobre fundo Teal.
 *
 * A abertura do sistema (SplashScreen API, configurada em themes.xml) já mostra fundo Teal
 * com o símbolo; esta tela aparece em seguida, sem corte visível, e a navegação a troca
 * pelo onboarding após um instante. Assim não há duas aberturas longas.
 */
@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SobraColors.Teal)
            .systemBarsPadding(), // afasta das barras do sistema (status e gestos)
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.ScreenMargin),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.Space24), // marca → slogans
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.Space16), // círculo → nome
            ) {
                Box(
                    modifier = Modifier
                        .size(LogoCircleSize)
                        .figmaShadow(SobraColors.SplashLogoShadow, LogoShadowBlur, LogoShadowOffsetY, LogoCircleSize / 2)
                        .clip(CircleShape)
                        .background(SobraColors.Surface),
                    contentAlignment = Alignment.Center,
                ) {
                    SobraIcon(R.drawable.ic_wallet_44, tint = SobraColors.Teal)
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Dimens.Space10), // nome → assinatura
                ) {
                    Text(stringResource(R.string.brand_name), style = BrandName, color = SobraColors.OnTeal)
                    Text(stringResource(R.string.splash_tagline), style = Tagline, color = SobraColors.TealLight)
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.Space12), // slogan principal → secundário
            ) {
                Text(
                    stringResource(R.string.splash_slogan_main),
                    style = SloganMain, color = SobraColors.OnTeal, textAlign = TextAlign.Center,
                )
                Text(
                    stringResource(R.string.splash_slogan_secondary),
                    style = SloganSecondary, color = SobraColors.TealLight, textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Preview(widthDp = 402, heightDp = 874)
@Composable
private fun SplashScreenPreview() {
    SobraTheme { SplashScreen() }
}
