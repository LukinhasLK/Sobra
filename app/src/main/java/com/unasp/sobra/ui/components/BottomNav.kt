package com.unasp.sobra.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.unasp.sobra.R
import com.unasp.sobra.ui.theme.Dimens
import com.unasp.sobra.ui.theme.SobraColors
import com.unasp.sobra.ui.theme.SobraTheme
import com.unasp.sobra.ui.theme.SobraType

private val BarHeight = 67.dp
private val ItemWidth = 64.dp
private val ItemHeight = 43.dp

/** As cinco abas, na ordem do Figma. */
enum class BottomTab(@param:StringRes val label: Int, @param:DrawableRes val icon: Int) {
    Home(R.string.nav_home, R.drawable.ic_home_24),
    Expenses(R.string.nav_expenses, R.drawable.ic_wallet_24),
    Goals(R.string.nav_goals, R.drawable.ic_target_24),
    Simulate(R.string.nav_simulate, R.drawable.ic_chart_line_24),
    Profile(R.string.nav_profile, R.drawable.ic_user_24),
}

/**
 * Navegação inferior própria (não é a NavigationBar do Material: aquela tem 80 de altura e
 * uma cápsula atrás do ícone ativo, que o design não tem).
 */
@Composable
fun BottomNav(
    selected: BottomTab,
    onSelect: (BottomTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = SobraColors.Border
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(SobraColors.Surface)
            // Borda só em cima: uma linha de 1 desenhada no topo, por dentro da barra.
            .drawBehind {
                val stroke = Dimens.BorderWidth.toPx()
                drawLine(borderColor, Offset(0f, stroke / 2), Offset(size.width, stroke / 2), stroke)
            }
            // Depois do fundo: o branco continua por baixo da barra de gestos do sistema,
            // e os itens ficam acima dela.
            .navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(BarHeight)
                .padding(horizontal = Dimens.Space16, vertical = Dimens.Space12),
            // SpaceBetween: primeiro item na margem esquerda, último na direita, resto distribuído.
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // `entries` lista todos os valores do enum, na ordem em que foram declarados.
            BottomTab.entries.forEach { tab ->
                NavItem(tab = tab, isSelected = tab == selected, onClick = { onSelect(tab) })
            }
        }
    }
}

@Composable
private fun NavItem(tab: BottomTab, isSelected: Boolean, onClick: () -> Unit) {
    val color = if (isSelected) SobraColors.Teal else SobraColors.TextSecondary
    Column(
        modifier = Modifier
            .size(width = ItemWidth, height = ItemHeight)
            // selectable = clickable que informa "selecionado" ao leitor de tela.
            .selectable(selected = isSelected, role = Role.Tab, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.Space4),
    ) {
        SobraIcon(tab.icon, tint = color)
        Text(
            text = stringResource(tab.label),
            style = if (isSelected) SobraType.NavLabelActive else SobraType.NavLabel,
            color = color,
            maxLines = 1,
        )
    }
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun BottomNavPreview() {
    SobraTheme { BottomNav(selected = BottomTab.Home, onSelect = {}) }
}
