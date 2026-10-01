package com.unasp.sobra.ui.screens.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.unasp.sobra.R
import com.unasp.sobra.data.DemoData
import com.unasp.sobra.domain.availableAmount
import com.unasp.sobra.domain.total
import com.unasp.sobra.ui.components.Badge
import com.unasp.sobra.ui.components.BottomTab
import com.unasp.sobra.ui.components.CategoryRow
import com.unasp.sobra.ui.components.CircleIconButton
import com.unasp.sobra.ui.components.SobraDivider
import com.unasp.sobra.ui.components.SobraScreen
import com.unasp.sobra.ui.components.sobraCard
import com.unasp.sobra.ui.format.toBrl
import com.unasp.sobra.ui.model.CategorySpending
import com.unasp.sobra.ui.theme.Dimens
import com.unasp.sobra.ui.theme.SobraColors
import com.unasp.sobra.ui.theme.SobraTheme
import com.unasp.sobra.ui.theme.manrope
import java.math.BigDecimal
import java.math.MathContext

private val AvatarSize = 40.dp
private val NotificationButtonSize = 36.dp

private val Greeting = manrope(18, FontWeight.W700)
private val SummaryLabel = manrope(14, FontWeight.W500)
private val SummaryValue = manrope(16, FontWeight.W700)
private val AvailableLabel = manrope(15, FontWeight.W600)
private val AvailableValue = manrope(16, FontWeight.W800)
private val SectionTitle = manrope(16, FontWeight.W700) // caixa de 22 de altura no Figma

// Decisão de fidelidade: no mockup a barra da maior categoria ocupa 82% da trilha
// (230 de 282) e as outras são proporcionais a ela: valor ÷ maior valor × 0,82.
private const val BAR_MAX_FRACTION = 0.82f

/** Fração da barra de uma categoria, pela regra do mockup. */
internal fun categoryBarFraction(amount: BigDecimal, largest: BigDecimal): Float {
    if (largest.signum() <= 0) return 0f
    return amount.divide(largest, MathContext.DECIMAL64).toFloat() * BAR_MAX_FRACTION
}

/**
 * 10 Home (Início). Rolável: o frame de 916 do Figma é só o conteúdo crescendo, não a
 * altura de um aparelho.
 */
@Composable
fun HomeScreen(
    userName: String,
    @DrawableRes avatar: Int,
    income: BigDecimal,
    spending: List<CategorySpending>,
    onNotificationsClick: () -> Unit,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Gastos e disponível são calculados, nunca digitados: gastos = soma das categorias.
    val expenses = spending.map { it.amount }.total()
    val available = availableAmount(income, expenses)
    // maxOfOrNull devolve null se a lista estiver vazia; `?:` troca o null por zero.
    val largest = spending.maxOfOrNull { it.amount } ?: BigDecimal.ZERO

    SobraScreen(modifier = modifier, bottomTab = BottomTab.Home, onTabSelected = onTabSelected) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            HomeHeader(userName, avatar, onNotificationsClick)
            SummaryCard(income, expenses, available, modifier = Modifier.padding(Dimens.Space24))
            Text(
                stringResource(R.string.home_section_title),
                style = SectionTitle,
                color = SobraColors.TextPrimary,
                modifier = Modifier.padding(horizontal = Dimens.ScreenMargin),
            )
            Column(
                modifier = Modifier.padding(
                    start = Dimens.ScreenMargin, top = Dimens.Space16,
                    end = Dimens.ScreenMargin, bottom = Dimens.Space24,
                ),
                verticalArrangement = Arrangement.spacedBy(Dimens.Space12),
            ) {
                spending.forEach { item ->
                    CategoryRow(item, barFraction = categoryBarFraction(item.amount, largest))
                }
            }
        }
    }
}

/** Header: avatar 40 + saudação à esquerda, notificações 36 à direita. Padding 16/24/8/24. */
@Composable
private fun HomeHeader(userName: String, @DrawableRes avatar: Int, onNotificationsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = Dimens.ScreenMargin, top = Dimens.Space16, end = Dimens.ScreenMargin, bottom = Dimens.Space8),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.Space12),
        ) {
            Image(
                painter = painterResource(avatar),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(AvatarSize)
                    .clip(CircleShape),
            )
            Text(stringResource(R.string.home_greeting, userName), style = Greeting, color = SobraColors.TextPrimary)
        }
        CircleIconButton(
            icon = R.drawable.ic_bell_20,
            contentDescription = stringResource(R.string.home_notifications),
            onClick = onNotificationsClick,
            size = NotificationButtonSize,
        )
    }
}

/** Card financeiro: Renda, Gastos, divisória e Disponível em badge. Padding 24, gap 20, raio 24. */
@Composable
private fun SummaryCard(income: BigDecimal, expenses: BigDecimal, available: BigDecimal, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .sobraCard(Dimens.Radius24)
            .padding(Dimens.Space24),
        verticalArrangement = Arrangement.spacedBy(Dimens.Space20),
    ) {
        SummaryLine(stringResource(R.string.home_income), income.toBrl())
        SummaryLine(stringResource(R.string.home_expenses), expenses.toBrl())
        SobraDivider()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.home_available), style = AvailableLabel, color = SobraColors.TextPrimary)
            // No Figma o badge mede 120×34; aqui a largura acompanha o valor (padding 6/14).
            Badge(
                text = available.toBrl(),
                textStyle = AvailableValue,
                horizontalPadding = Dimens.Space14,
                verticalPadding = Dimens.Space6,
                radius = Dimens.Radius12,
            )
        }
    }
}

@Composable
private fun SummaryLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = SummaryLabel, color = SobraColors.TextSecondary)
        Text(value, style = SummaryValue, color = SobraColors.TextPrimary)
    }
}

@Preview(widthDp = 402, heightDp = 916)
@Composable
private fun HomeScreenPreview() {
    SobraTheme {
        HomeScreen(
            userName = DemoData.USER_FIRST_NAME,
            avatar = R.drawable.img_avatar,
            income = DemoData.homeIncome,
            spending = DemoData.homeSpending,
            onNotificationsClick = {},
            onTabSelected = {},
        )
    }
}
