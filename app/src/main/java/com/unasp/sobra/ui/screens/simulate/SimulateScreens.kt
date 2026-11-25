package com.unasp.sobra.ui.screens.simulate

import com.unasp.sobra.ui.format.toBrlNumber
import com.unasp.sobra.ui.components.TextLink
import com.unasp.sobra.ui.components.StateContent
import com.unasp.sobra.ui.components.SelectChip
import com.unasp.sobra.domain.simulate
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.runtime.remember
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.unasp.sobra.R
import com.unasp.sobra.data.DemoData
import com.unasp.sobra.domain.SimulationResult
import com.unasp.sobra.ui.components.ErrorText
import com.unasp.sobra.ui.components.BackHeader
import com.unasp.sobra.ui.components.Badge
import com.unasp.sobra.ui.components.BottomTab
import com.unasp.sobra.ui.components.ChevronTitleHeader
import com.unasp.sobra.ui.components.FooterActions
import com.unasp.sobra.ui.components.PrimaryButton
import com.unasp.sobra.ui.components.ProgressTrack
import com.unasp.sobra.ui.components.ProgressTrackHeight
import com.unasp.sobra.ui.components.SecondaryButton
import com.unasp.sobra.ui.components.SobraDivider
import com.unasp.sobra.ui.components.SobraIcon
import com.unasp.sobra.ui.components.SobraScreen
import com.unasp.sobra.ui.components.TitleHeader
import com.unasp.sobra.ui.components.WhiteMoneyField
import com.unasp.sobra.ui.components.sobraCard
import com.unasp.sobra.ui.format.toBrl
import com.unasp.sobra.ui.format.toBrlPositive
import com.unasp.sobra.ui.format.toBrlShort
import com.unasp.sobra.ui.format.toShortMonthDate
import com.unasp.sobra.ui.model.SavedSimulation
import com.unasp.sobra.ui.theme.Dimens
import com.unasp.sobra.ui.theme.SobraColors
import com.unasp.sobra.ui.theme.SobraTheme
import com.unasp.sobra.ui.theme.manrope
import java.math.BigDecimal
import kotlin.math.roundToInt

// ---------------------------------------------------------------------------------------
// 16 Simular
// ---------------------------------------------------------------------------------------

// Limites do prazo = os da API (D-02: 1 a 120 meses). Antes era 1 a 38 só para a trilha do
// mockup bater (24 meses = 62%); com os atalhos em chips, a trilha virou ajuste fino.
const val PRAZO_MIN = 1
const val PRAZO_MAX = 120

/** Atalhos de prazo: os mais usados, de 6 meses a 10 anos. */
private val TERM_PRESETS = listOf(6, 12, 24, 36, 60, 120)
private val ThumbSize = 22.dp
private val ThumbBorder = 3.dp

private val TermLabel = manrope(13, FontWeight.W600)
private val TermValue = manrope(13, FontWeight.W700)
private val RateTitle = manrope(13, FontWeight.W700)
private val RateHint = manrope(12, FontWeight.W400)
private val PreviewLabel = manrope(13, FontWeight.W600)
private val PreviewValue = manrope(24, FontWeight.W800)

/** 16 Simular. */
@Composable
fun SimulateScreen(
    initial: BigDecimal,
    onInitialChange: (BigDecimal) -> Unit,
    monthly: BigDecimal,
    onMonthlyChange: (BigDecimal) -> Unit,
    months: Int,
    onMonthsChange: (Int) -> Unit,
    onSimulate: () -> Unit,
    onOpenSaved: () -> Unit,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
    loading: Boolean = false,
) {
    // Prévia ao vivo, calculada no app com a mesma regra do servidor (o teste FinanceTest
    // garante 14.680,67 nos dois). O resultado oficial continua vindo da API ao tocar em Simular.
    val preview = remember(initial, monthly, months) { simulate(initial, monthly, months).finalAmount }
    SobraScreen(modifier = modifier, bottomTab = BottomTab.Simulate, onTabSelected = onTabSelected) {
        TitleHeader(
            stringResource(R.string.simulate_title),
            // Caminho para as simulações salvas (antes só se chegava lá salvando uma nova).
            trailing = { TextLink(stringResource(R.string.simulate_saved_link), onOpenSaved) },
        )
        // BoxWithConstraints informa a altura disponível (maxHeight), usada como altura
        // mínima da coluna: assim o SpaceBetween tem espaço para distribuir e, em telas
        // baixas ou com o teclado aberto, o conteúdo rola.
        BoxWithConstraints(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = maxHeight),
                // Três blocos (conteúdo, ação e reserva da navegação) com espaço igual entre
                // eles: o botão fica no meio do espaço livre, não colado na navegação.
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    modifier = Modifier.padding(Dimens.Space24),
                    verticalArrangement = Arrangement.spacedBy(Dimens.Space20),
                ) {
                    WhiteMoneyField(stringResource(R.string.simulate_initial), initial, onInitialChange)
                    WhiteMoneyField(stringResource(R.string.simulate_monthly), monthly, onMonthlyChange)
                    TermControl(months, onMonthsChange)
                    RateCard()
                    ErrorText(errorMessage)
                }
                Column(
                    modifier = Modifier.padding(Dimens.Space24),
                    verticalArrangement = Arrangement.spacedBy(Dimens.Space16),
                ) {
                    PreviewCard(preview, months)
                    PrimaryButton(
                        stringResource(if (loading) R.string.action_simulating else R.string.action_simulate),
                        onSimulate,
                    )
                }
                Spacer(Modifier) // terceiro bloco: a navegação vem logo abaixo
            }
        }
    }
}

/**
 * Prazo: atalhos em chips (6 meses a 10 anos) e, abaixo, a trilha para ajuste fino, agora com
 * uma bolinha mostrando onde tocar e acessível ao leitor de tela (ajuste por gesto de volume).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TermControl(months: Int, onMonthsChange: (Int) -> Unit) {
    // rememberUpdatedState mantém a referência mais recente do callback para o código de
    // gestos, que é criado uma vez só (pointerInput(Unit)).
    val currentOnChange by rememberUpdatedState(onMonthsChange)
    val range = PRAZO_MAX - PRAZO_MIN
    val fraction = (months - PRAZO_MIN).toFloat() / range

    fun monthsAt(x: Float, width: Int): Int =
        (PRAZO_MIN + (x / width).coerceIn(0f, 1f) * range).roundToInt()

    Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space12)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.simulate_term), style = TermLabel, color = SobraColors.TextSecondary)
            Text(pluralStringResource(R.plurals.months_count, months, months), style = TermValue, color = SobraColors.TealDark)
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Dimens.Space8),
            verticalArrangement = Arrangement.spacedBy(Dimens.Space8),
        ) {
            TERM_PRESETS.forEach { option ->
                SelectChip(
                    text = pluralStringResource(R.plurals.months_count, option, option),
                    selected = option == months,
                    onClick = { onMonthsChange(option) },
                )
            }
        }
        val termDescription = pluralStringResource(R.plurals.months_count, months, months)
        // BoxWithConstraints dá a largura da trilha para posicionar a bolinha.
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(ThumbSize)
                // Leitor de tela: anuncia o prazo e permite ajustar sem arrastar.
                .semantics {
                    contentDescription = termDescription
                    progressBarRangeInfo = ProgressBarRangeInfo(
                        months.toFloat(), PRAZO_MIN.toFloat()..PRAZO_MAX.toFloat(), steps = range - 1,
                    )
                    setProgress { target ->
                        currentOnChange(target.roundToInt().coerceIn(PRAZO_MIN, PRAZO_MAX))
                        true
                    }
                }
                // pointerInput recebe os eventos de toque brutos; cada bloco trata um gesto.
                .pointerInput(Unit) {
                    detectTapGestures { offset -> currentOnChange(monthsAt(offset.x, size.width)) }
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, _ ->
                        currentOnChange(monthsAt(change.position.x, size.width))
                    }
                },
            contentAlignment = Alignment.CenterStart,
        ) {
            ProgressTrack(progress = fraction, height = ProgressTrackHeight.Goal)
            Box(
                modifier = Modifier
                    .offset(x = (maxWidth - ThumbSize) * fraction)
                    .size(ThumbSize)
                    .clip(CircleShape)
                    .background(SobraColors.Surface)
                    .border(ThumbBorder, SobraColors.Teal, CircleShape),
            )
        }
    }
}

/** Prévia ao vivo do resultado: muda enquanto a pessoa digita e escolhe o prazo. */
@Composable
private fun PreviewCard(finalAmount: BigDecimal, months: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .sobraCard(Dimens.Radius20, background = SobraColors.TealLight, borderColor = null)
            .padding(horizontal = Dimens.Space20, vertical = Dimens.Space16),
        verticalArrangement = Arrangement.spacedBy(Dimens.Space4),
    ) {
        Text(
            stringResource(R.string.simulate_preview_label, pluralStringResource(R.plurals.months_count, months, months)),
            style = PreviewLabel, color = SobraColors.TealDark,
        )
        Text(finalAmount.toBrl(), style = PreviewValue, color = SobraColors.TealDark)
    }
}

/** Card da taxa: padding 16, gap 12, raio 16; ícone 20 Teal + taxa 13/700 + explicação 11/400. */
@Composable
private fun RateCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .sobraCard(Dimens.Radius16)
            .padding(Dimens.Space16),
        horizontalArrangement = Arrangement.spacedBy(Dimens.Space12),
    ) {
        SobraIcon(R.drawable.ic_trending_up_20, tint = SobraColors.Teal)
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space2)) {
            Text(stringResource(R.string.simulate_rate_title), style = RateTitle, color = SobraColors.TextPrimary)
            Text(stringResource(R.string.simulate_rate_hint), style = RateHint, color = SobraColors.TextSecondary)
        }
    }
}

// ---------------------------------------------------------------------------------------
// 17 Resultado da simulação
// ---------------------------------------------------------------------------------------

private val BannerLabel = manrope(13, FontWeight.W600)
private val BannerValue = manrope(16, FontWeight.W800)
private val BannerHero = manrope(32, FontWeight.W800)
private val AxisText = manrope(11, FontWeight.W600)
private val ChartTitle = manrope(13, FontWeight.W700)
private val LegendText = manrope(11, FontWeight.W600)
private val BreakdownLabel = manrope(13, FontWeight.W400)
private val BreakdownValue = manrope(13, FontWeight.W700)

private val ChartHeight = 120.dp
private val GridStroke = 1.dp
private val YieldStroke = 3.dp
private val NoYieldStroke = 1.5.dp
private val DashLength = 3.dp
private val LegendMarkWidth = 10.dp
private val LegendMarkHeight = 4.dp
private val LegendMarkRadius = 2.dp
private const val GRID_LINES = 4 // y = 0, 40, 80 e 120

// No mockup a série termina em x ≈ 310 de uma área de 314, e o topo da série com
// rendimento fica por volta de y = 10 de 120 (valor extrapolado, veja DemoData).
private const val CHART_END_X_FRACTION = 310f / 314f
private const val CHART_TOP_FRACTION = 110f / 120f

/** 17 Resultado da simulação. */
@Composable
fun SimulationResultScreen(
    result: SimulationResult,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onNewSimulation: () -> Unit,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
    saving: Boolean = false,
) {
    SobraScreen(modifier = modifier) {
        BackHeader(stringResource(R.string.simulate_title), onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = Dimens.ScreenMargin, end = Dimens.ScreenMargin, bottom = Dimens.Space24),
        ) {
            // Banner: a estimativa final é o número principal (grande); investido e rendimento
            // ficam abaixo, menores. Teal escuro com texto branco (contraste 5,47).
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .sobraCard(Dimens.Radius20, background = SobraColors.TealDark, borderColor = null)
                    .padding(Dimens.Space20),
                verticalArrangement = Arrangement.spacedBy(Dimens.Space16),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space4)) {
                    Text(
                        stringResource(R.string.result_final_in, pluralStringResource(R.plurals.months_count, result.months, result.months)),
                        style = BannerLabel, color = SobraColors.TealLight,
                    )
                    Text(result.finalAmount.toBrl(), style = BannerHero, color = SobraColors.OnTeal)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    BannerGroup(stringResource(R.string.result_total_invested), result.totalInvested.toBrl(), Alignment.Start)
                    BannerGroup(stringResource(R.string.result_yield), result.estimatedYield.toBrlPositive(), Alignment.End)
                }
            }

            // Card do gráfico: 20 abaixo do banner; padding 20, gap 16, raio 20.
            Column(
                modifier = Modifier
                    .padding(top = Dimens.Space20)
                    .fillMaxWidth()
                    .sobraCard(Dimens.Radius20)
                    .padding(Dimens.Space20),
                verticalArrangement = Arrangement.spacedBy(Dimens.Space16),
            ) {
                Text(stringResource(R.string.result_chart_title), style = ChartTitle, color = SobraColors.TextPrimary)
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space6)) {
                    SimulationChart(result)
                    // Referência de tempo: começo e fim do período.
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.day_today), style = AxisText, color = SobraColors.TextSecondary)
                        Text(
                            pluralStringResource(R.plurals.months_count, result.months, result.months),
                            style = AxisText, color = SobraColors.TextSecondary,
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Space16)) {
                    LegendItem(SobraColors.Teal, stringResource(R.string.result_legend_with_yield))
                    LegendItem(SobraColors.ChartNoYield, stringResource(R.string.result_legend_without_yield))
                }
            }

            // Card de composição: 24 abaixo do gráfico; padding 20, gap 12, raio 20.
            Column(
                modifier = Modifier
                    .padding(top = Dimens.Space24)
                    .fillMaxWidth()
                    .sobraCard(Dimens.Radius20)
                    .padding(Dimens.Space20),
                verticalArrangement = Arrangement.spacedBy(Dimens.Space12),
            ) {
                BreakdownLine(stringResource(R.string.result_initial), result.initial.toBrl(), SobraColors.TextPrimary)
                BreakdownLine(
                    stringResource(R.string.result_contributions, result.months),
                    result.totalContributions.toBrl(), SobraColors.TextPrimary,
                )
                BreakdownLine(stringResource(R.string.result_yield), result.estimatedYield.toBrlPositive(), SobraColors.Green)
                // A taxa usada, para a pessoa saber de onde vem o rendimento.
                BreakdownLine(stringResource(R.string.result_rate), stringResource(R.string.simulate_rate_title), SobraColors.TextPrimary)
            }
            ErrorText(errorMessage, Modifier.padding(top = Dimens.Space16))
        }
        // Rodapé fixo, fora da rolagem: o conteúdo nunca fica escondido atrás dos botões.
        FooterActions(gap = Dimens.Space8) {
            PrimaryButton(stringResource(if (saving) R.string.action_saving else R.string.action_save), onSave)
            SecondaryButton(stringResource(R.string.action_new_simulation), onNewSimulation)
        }
    }
}

@Composable
private fun BannerGroup(label: String, value: String, alignment: Alignment.Horizontal) {
    Column(horizontalAlignment = alignment, verticalArrangement = Arrangement.spacedBy(Dimens.Space4)) {
        Text(label, style = BannerLabel, color = SobraColors.TealLight)
        Text(value, style = BannerValue, color = SobraColors.OnTeal)
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.Space6)) {
        Box(
            modifier = Modifier
                .size(width = LegendMarkWidth, height = LegendMarkHeight)
                .clip(RoundedCornerShape(LegendMarkRadius))
                .background(color),
        )
        Text(label, style = LegendText, color = SobraColors.TextSecondary)
    }
}

@Composable
private fun BreakdownLine(label: String, value: String, valueColor: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = BreakdownLabel, color = SobraColors.TextSecondary)
        Text(value, style = BreakdownValue, color = valueColor)
    }
}

/**
 * Gráfico das duas séries. Sem eixos, sem rótulos, sem marcadores e sem curvas: só quatro
 * linhas de grade e segmentos retos, como no Figma.
 */
@Composable
private fun SimulationChart(result: SimulationResult, modifier: Modifier = Modifier) {
    // As duas séries começam no mesmo valor (o inicial), que fica na base do gráfico;
    // o maior valor da série com rendimento fica perto do topo.
    val floor = result.seriesWithYield.first().toFloat()
    val span = (result.seriesWithYield.last().toFloat() - floor).takeIf { it > 0f } ?: 1f
    fun fractions(series: List<BigDecimal>) = series.map { (it.toFloat() - floor) / span * CHART_TOP_FRACTION }
    val withYield = fractions(result.seriesWithYield)
    val withoutYield = fractions(result.seriesWithoutYield)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(ChartHeight),
    ) {
        // Grade: quatro linhas horizontais igualmente espaçadas, traço 1, Soft.
        repeat(GRID_LINES) { index ->
            val y = size.height * index / (GRID_LINES - 1)
            drawLine(SobraColors.Soft, Offset(0f, y), Offset(size.width, y), GridStroke.toPx())
        }

        // Monta o caminho de uma série: move até o primeiro ponto e liga os demais com
        // retas (lineTo). No Canvas o y cresce para baixo, por isso "1 − fração".
        fun seriesPath(points: List<Float>) = Path().apply {
            val stepX = size.width * CHART_END_X_FRACTION / (points.size - 1).coerceAtLeast(1)
            points.forEachIndexed { index, fraction ->
                val x = stepX * index
                val y = size.height * (1f - fraction)
                if (index == 0) moveTo(x, y) else lineTo(x, y)
            }
        }

        // Sem rendimento: traço 1,5, tracejado 3/3, cinza com alpha 0,40.
        val dash = DashLength.toPx()
        drawPath(
            path = seriesPath(withoutYield),
            color = SobraColors.ChartNoYield,
            style = Stroke(
                width = NoYieldStroke.toPx(),
                // dashPathEffect: alterna "dash" de traço e "dash" de espaço.
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, dash)),
            ),
        )
        // Com rendimento: traço 3, Teal, contínuo.
        drawPath(
            path = seriesPath(withYield),
            color = SobraColors.Teal,
            style = Stroke(width = YieldStroke.toPx()),
        )
    }
}

// ---------------------------------------------------------------------------------------
// 18 Simulações salvas
// ---------------------------------------------------------------------------------------

private val SavedSubtitle = manrope(16, FontWeight.W700)
private val SavedDate = manrope(13, FontWeight.W600)
private val SavedBadge = manrope(11, FontWeight.W700)
private val ParamLabel = manrope(12, FontWeight.W400)
private val ParamValue = manrope(14, FontWeight.W700)
private val SavedResultLabel = manrope(13, FontWeight.W600)
private val SavedResultValue = manrope(16, FontWeight.W800)

/** 18 Simulações salvas. */
@Composable
fun SavedSimulationsScreen(
    simulations: List<SavedSimulation>,
    onBack: () -> Unit,
    onSimulationClick: (SavedSimulation) -> Unit,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    SobraScreen(modifier = modifier, bottomTab = BottomTab.Simulate, onTabSelected = onTabSelected) {
        ChevronTitleHeader(
            title = stringResource(R.string.saved_title),
            onBack = onBack,
            backDescription = stringResource(R.string.action_back),
            // TODO: a cor do chevron desta tela não foi informada no Figma (usando TextPrimary).
            chevronTint = SobraColors.TextPrimary,
        )
        if (simulations.isEmpty()) {
            StateContent(
                icon = R.drawable.ic_target_64,
                circleColor = SobraColors.Soft,
                iconTint = SobraColors.Teal,
                title = stringResource(R.string.saved_empty_title),
                description = stringResource(R.string.saved_empty_description),
                buttonText = stringResource(R.string.saved_empty_button),
                onButtonClick = onBack,
            )
            return@SobraScreen
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(Dimens.Space24),
            verticalArrangement = Arrangement.spacedBy(Dimens.Space16),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space4)) {
                    Text(stringResource(R.string.saved_subtitle), style = SavedSubtitle, color = SobraColors.TextPrimary)
                    Text(stringResource(R.string.saved_hint), style = ParamLabel, color = SobraColors.TextSecondary)
                }
            }
            items(simulations, key = { it.id }) { simulation ->
                SavedSimulationCard(simulation, onClick = { onSimulationClick(simulation) })
            }
        }
    }
}

/** Card de simulação salva: padding 20, gap 16, raio 20, borda 1. */
@Composable
private fun SavedSimulationCard(simulation: SavedSimulation, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .sobraCard(Dimens.Radius20)
            // Tocar refaz a simulação com os mesmos valores e abre o resultado.
            .clickable(role = Role.Button, onClick = onClick)
            .padding(Dimens.Space20),
        verticalArrangement = Arrangement.spacedBy(Dimens.Space16),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(simulation.date.toShortMonthDate(), style = SavedDate, color = SobraColors.TextSecondary)
            // O selo mostra a taxa usada (gravada com a simulação), no lugar de "Salva", que
            // se repetia em todos os cards de uma lista que já é só de salvas.
            Badge(
                text = simulation.monthlyRate?.let { stringResource(R.string.saved_rate, it.movePointRight(2).toBrlNumber()) }
                    ?: stringResource(R.string.saved_badge),
                textStyle = SavedBadge,
                horizontalPadding = Dimens.Space10,
                verticalPadding = Dimens.Space4,
            )
        }
        // Três colunas distribuídas pelo espaço (SpaceBetween), sem larguras iguais.
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Param(stringResource(R.string.saved_initial), simulation.initial.toBrlShort())
            Param(stringResource(R.string.saved_monthly), simulation.monthlyContribution.toBrlShort())
            Param(stringResource(R.string.saved_term), stringResource(R.string.saved_term_value, simulation.months))
        }
        SobraDivider()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.saved_result), style = SavedResultLabel, color = SobraColors.TextSecondary)
            Text(simulation.result.toBrl(), style = SavedResultValue, color = SobraColors.TealDark)
        }
    }
}

@Composable
private fun Param(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.Space4)) {
        Text(label, style = ParamLabel, color = SobraColors.TextSecondary)
        Text(value, style = ParamValue, color = SobraColors.TextPrimary)
    }
}

@Preview(widthDp = 402, heightDp = 874)
@Composable
private fun SimulateScreenPreview() {
    SobraTheme {
        SimulateScreen(
            initial = DemoData.simulationInitial, onInitialChange = {},
            monthly = DemoData.simulationMonthly, onMonthlyChange = {},
            months = DemoData.SIMULATION_MONTHS, onMonthsChange = {},
            onSimulate = {}, onOpenSaved = {}, onTabSelected = {},
        )
    }
}

@Preview(widthDp = 402, heightDp = 874)
@Composable
private fun SimulationResultScreenPreview() {
    SobraTheme {
        SimulationResultScreen(DemoData.simulationResult, onBack = {}, onSave = {}, onNewSimulation = {})
    }
}

@Preview(widthDp = 402, heightDp = 874)
@Composable
private fun SavedSimulationsScreenPreview() {
    SobraTheme { SavedSimulationsScreen(DemoData.savedSimulations, onBack = {}, onSimulationClick = {}, onTabSelected = {}) }
}
