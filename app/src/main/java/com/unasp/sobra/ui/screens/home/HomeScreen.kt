package com.unasp.sobra.ui.screens.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.unasp.sobra.R
import com.unasp.sobra.data.DemoData
import com.unasp.sobra.domain.availableAmount
import com.unasp.sobra.domain.progressFraction
import com.unasp.sobra.domain.total
import com.unasp.sobra.ui.components.BottomTab
import com.unasp.sobra.ui.components.CategoryRow
import com.unasp.sobra.ui.components.CircleIconButton
import com.unasp.sobra.ui.components.ProgressTrack
import com.unasp.sobra.ui.components.ProgressTrackHeight
import com.unasp.sobra.ui.components.SobraIcon
import com.unasp.sobra.ui.components.SobraScreen
import com.unasp.sobra.ui.components.TextLink
import com.unasp.sobra.ui.components.percentLabel
import com.unasp.sobra.ui.components.sobraCard
import com.unasp.sobra.ui.format.toBrl
import com.unasp.sobra.ui.model.CategorySpending
import com.unasp.sobra.ui.model.Goal
import com.unasp.sobra.ui.theme.Dimens
import com.unasp.sobra.ui.theme.SobraColors
import com.unasp.sobra.ui.theme.SobraTheme
import com.unasp.sobra.ui.theme.manrope
import java.math.BigDecimal
import java.math.MathContext
import kotlin.math.roundToInt

// Layout refinado com referência no app Stoic (Mobbin): cards grandes e arredondados, o número
// principal em destaque num card de cor cheia e um carrossel horizontal. As cores são só as da
// paleta do Sobra: o preto do Stoic virou Teal.

private val AvatarSize = 40.dp
private val NotificationButtonSize = 36.dp
private val TileIconCircle = 32.dp
private val SpentTrackHeight = 8.dp
private val GoalCardWidth = 156.dp
private val GoalCardHeight = 172.dp
private val GoalThumbSize = 40.dp
private val AddGoalCircle = 40.dp

private val Greeting = manrope(18, FontWeight.W700)
private val TileLabel = manrope(13, FontWeight.W600)
private val TileValue = manrope(17, FontWeight.W800)
private val AvailableLabel = manrope(14, FontWeight.W600)
private val AvailableValue = manrope(32, FontWeight.W800)
private val AvailableCaption = manrope(13, FontWeight.W500)
private val SectionTitle = manrope(16, FontWeight.W700)
private val GoalMiniName = manrope(14, FontWeight.W700)
private val GoalMiniPercent = manrope(13, FontWeight.W700)
private val MutedText = manrope(14, FontWeight.W500)

// Trilha clara dentro do card Teal: o branco do texto com transparência (não é cor nova).
private const val ON_TEAL_TRACK_ALPHA = 0.25f
private const val PERCENT = 100

// Decisão de fidelidade: no mockup a barra da maior categoria ocupa 82% da trilha
// (230 de 282) e as outras são proporcionais a ela: valor ÷ maior valor × 0,82.
private const val BAR_MAX_FRACTION = 0.82f

/** Fração da barra de uma categoria, pela regra do mockup. */
internal fun categoryBarFraction(amount: BigDecimal, largest: BigDecimal): Float {
    if (largest.signum() <= 0) return 0f
    return amount.divide(largest, MathContext.DECIMAL64).toFloat() * BAR_MAX_FRACTION
}

/** Quanto da renda já foi gasto, entre 0 e 1 (sem renda: 0). */
internal fun spentFraction(income: BigDecimal, expenses: BigDecimal): Float {
    if (income.signum() <= 0) return 0f
    return expenses.divide(income, MathContext.DECIMAL64).toFloat().coerceIn(0f, 1f)
}

/**
 * 10 Home (Início). Rolável: saudação, Renda e Gastos lado a lado, Disponível em destaque,
 * carrossel de objetivos e gastos por categoria.
 */
@Composable
fun HomeScreen(
    userName: String,
    @DrawableRes avatar: Int,
    income: BigDecimal,
    spending: List<CategorySpending>,
    goals: List<Goal>,
    onGoalClick: (Goal) -> Unit,
    onSeeAllGoals: () -> Unit,
    onAddGoal: () -> Unit,
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

            // Renda e Gastos lado a lado; weight(1f) divide a largura igualmente.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = Dimens.ScreenMargin, end = Dimens.ScreenMargin, top = Dimens.Space16),
                horizontalArrangement = Arrangement.spacedBy(Dimens.Space12),
            ) {
                AmountTile(
                    label = stringResource(R.string.home_income),
                    value = income.toBrl(),
                    icon = R.drawable.ic_trending_up_20,
                    iconTint = SobraColors.Teal,
                    iconBackground = SobraColors.TealLight,
                    modifier = Modifier.weight(1f),
                )
                AmountTile(
                    label = stringResource(R.string.home_expenses),
                    value = expenses.toBrl(),
                    icon = R.drawable.ic_arrow_up_right_16,
                    iconTint = SobraColors.Red,
                    iconBackground = SobraColors.ErrorBg,
                    modifier = Modifier.weight(1f),
                )
            }

            AvailableCard(
                income = income,
                expenses = expenses,
                available = available,
                modifier = Modifier.padding(horizontal = Dimens.ScreenMargin, vertical = Dimens.Space12),
            )

            GoalsCarousel(goals, onGoalClick, onSeeAllGoals, onAddGoal)

            Text(
                stringResource(R.string.home_section_title),
                style = SectionTitle,
                color = SobraColors.TextPrimary,
                modifier = Modifier.padding(start = Dimens.ScreenMargin, end = Dimens.ScreenMargin, top = Dimens.Space24),
            )
            Column(
                modifier = Modifier.padding(
                    start = Dimens.ScreenMargin, top = Dimens.Space16,
                    end = Dimens.ScreenMargin, bottom = Dimens.Space24,
                ),
                verticalArrangement = Arrangement.spacedBy(Dimens.Space12),
            ) {
                if (spending.isEmpty()) {
                    Text(stringResource(R.string.home_spending_empty), style = MutedText, color = SobraColors.TextSecondary)
                }
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

/** Card branco pequeno: ícone em círculo, label e valor (Renda e Gastos). */
@Composable
private fun AmountTile(
    label: String,
    value: String,
    @DrawableRes icon: Int,
    iconTint: Color,
    iconBackground: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .sobraCard(Dimens.Radius24)
            .padding(Dimens.Space16),
        verticalArrangement = Arrangement.spacedBy(Dimens.Space12),
    ) {
        Box(
            modifier = Modifier
                .size(TileIconCircle)
                .clip(CircleShape)
                .background(iconBackground),
            contentAlignment = Alignment.Center,
        ) {
            SobraIcon(icon, tint = iconTint)
        }
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space4)) {
            Text(label, style = TileLabel, color = SobraColors.TextSecondary)
            Text(value, style = TileValue, color = SobraColors.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

/**
 * Disponível em destaque: card Teal cheio com texto branco, o valor grande e uma barra de
 * quanto da renda já foi gasto.
 */
@Composable
private fun AvailableCard(income: BigDecimal, expenses: BigDecimal, available: BigDecimal, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .sobraCard(Dimens.Radius24, background = SobraColors.Teal, borderColor = null)
            .padding(Dimens.Space24),
        verticalArrangement = Arrangement.spacedBy(Dimens.Space12),
    ) {
        Text(stringResource(R.string.home_available), style = AvailableLabel, color = SobraColors.TealLight)
        Text(available.toBrl(), style = AvailableValue, color = SobraColors.OnTeal, maxLines = 1)
        // Sem renda informada não há proporção para mostrar.
        if (income.signum() > 0) {
            val spent = spentFraction(income, expenses)
            val pill = RoundedCornerShape(Dimens.RadiusFull)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(SpentTrackHeight)
                    .clip(pill)
                    .background(SobraColors.OnTeal.copy(alpha = ON_TEAL_TRACK_ALPHA)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(spent)
                        .fillMaxHeight()
                        .clip(pill)
                        .background(SobraColors.OnTeal),
                )
            }
            Text(
                text = if (available.signum() < 0) {
                    stringResource(R.string.home_overspent)
                } else {
                    stringResource(R.string.home_spent_caption, (spent * PERCENT).roundToInt())
                },
                style = AvailableCaption,
                color = SobraColors.TealLight,
            )
        }
    }
}

/** "Seus objetivos": carrossel horizontal com um card por objetivo e, no fim, "Novo objetivo". */
@Composable
private fun GoalsCarousel(
    goals: List<Goal>,
    onGoalClick: (Goal) -> Unit,
    onSeeAllGoals: () -> Unit,
    onAddGoal: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = Dimens.ScreenMargin, end = Dimens.ScreenMargin, top = Dimens.Space12, bottom = Dimens.Space12),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(R.string.home_goals_title), style = SectionTitle, color = SobraColors.TextPrimary)
        if (goals.isNotEmpty()) {
            TextLink(stringResource(R.string.home_goals_see_all), onSeeAllGoals)
        }
    }
    // LazyRow: lista horizontal que só compõe os cards visíveis.
    LazyRow(
        contentPadding = PaddingValues(horizontal = Dimens.ScreenMargin),
        horizontalArrangement = Arrangement.spacedBy(Dimens.Space12),
    ) {
        items(goals, key = { it.id }) { goal -> GoalMiniCard(goal, onClick = { onGoalClick(goal) }) }
        item(key = "add") { AddGoalCard(onAddGoal) }
    }
}

@Composable
private fun GoalMiniCard(goal: Goal, onClick: () -> Unit) {
    val progress = progressFraction(goal.current, goal.target)
    Column(
        modifier = Modifier
            .width(GoalCardWidth)
            .height(GoalCardHeight)
            .sobraCard(Dimens.Radius24)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(Dimens.Space16),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Image(
            painter = painterResource(goal.thumbnail),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(GoalThumbSize)
                .clip(RoundedCornerShape(Dimens.Radius12)),
        )
        Text(
            goal.name,
            style = GoalMiniName,
            color = SobraColors.TextPrimary,
            maxLines = 2,
            minLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space8)) {
            ProgressTrack(progress = progress, height = ProgressTrackHeight.Step)
            Text(percentLabel(progress), style = GoalMiniPercent, color = SobraColors.Teal)
        }
    }
}

/** Último card do carrossel: atalho para criar um objetivo (é o único card quando não há nenhum). */
@Composable
private fun AddGoalCard(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(GoalCardWidth)
            .height(GoalCardHeight)
            .sobraCard(Dimens.Radius24, background = SobraColors.Soft, borderColor = null)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(Dimens.Space16),
        verticalArrangement = Arrangement.spacedBy(Dimens.Space12, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(AddGoalCircle)
                .clip(CircleShape)
                .background(SobraColors.Surface),
            contentAlignment = Alignment.Center,
        ) {
            SobraIcon(R.drawable.ic_plus_18, tint = SobraColors.Teal)
        }
        Text(stringResource(R.string.home_goals_add), style = GoalMiniName, color = SobraColors.TextSecondary)
    }
}

@Preview(widthDp = 402, heightDp = 1100)
@Composable
private fun HomeScreenPreview() {
    SobraTheme {
        HomeScreen(
            userName = DemoData.USER_FIRST_NAME,
            avatar = R.drawable.img_avatar,
            income = DemoData.homeIncome,
            spending = DemoData.homeSpending,
            goals = DemoData.goals,
            onGoalClick = {},
            onSeeAllGoals = {},
            onAddGoal = {},
            onNotificationsClick = {},
            onTabSelected = {},
        )
    }
}

@Preview(name = "Conta nova", widthDp = 402, heightDp = 1000)
@Composable
private fun HomeScreenEmptyPreview() {
    SobraTheme {
        HomeScreen(
            userName = "Ana",
            avatar = R.drawable.img_avatar,
            income = BigDecimal("5200"),
            spending = emptyList(),
            goals = emptyList(),
            onGoalClick = {},
            onSeeAllGoals = {},
            onAddGoal = {},
            onNotificationsClick = {},
            onTabSelected = {},
        )
    }
}
