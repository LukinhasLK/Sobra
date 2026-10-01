package com.unasp.sobra.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unasp.sobra.ui.theme.Dimens
import com.unasp.sobra.ui.theme.SobraColors

private val FabBottomMargin = 18.dp

/**
 * Esqueleto de tela: fundo, espaço da status bar, conteúdo, FAB opcional e navegação opcional.
 *
 * A status bar e o indicador de gestos do Figma NÃO são desenhados: são do sistema.
 * `statusBarsPadding()` / `navigationBarsPadding()` só afastam o conteúdo deles.
 *
 * @param bottomTab aba ativa; `null` = tela sem navegação inferior.
 */
@Composable
fun SobraScreen(
    modifier: Modifier = Modifier,
    bottomTab: BottomTab? = null,
    onTabSelected: (BottomTab) -> Unit = {},
    floatingAction: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SobraColors.Background)
            .statusBarsPadding(),
    ) {
        // weight(1f): esta área fica com toda a altura que a navegação não usar.
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    // Sem navegação, é a própria tela que precisa respeitar a barra de gestos.
                    .then(if (bottomTab == null) Modifier.navigationBarsPadding() else Modifier),
                content = content,
            )
            if (floatingAction != null) {
                // Margem direita 24 e 18 acima da navegação.
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = Dimens.ScreenMargin, bottom = FabBottomMargin),
                ) { floatingAction() }
            }
        }
        if (bottomTab != null) {
            BottomNav(selected = bottomTab, onSelect = onTabSelected)
        }
    }
}

/**
 * Esqueleto das telas de formulário: parte de cima rolável e ações presas no rodapé.
 * `imePadding()` sobe o conteúdo quando o teclado abre.
 * TODO: comportamento com teclado além do imePadding() não definido (ex.: rolar até o campo focado?).
 */
@Composable
fun FormScreen(
    modifier: Modifier = Modifier,
    footer: @Composable ColumnScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SobraColors.Background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                // rememberScrollState guarda a posição da rolagem entre recomposições.
                .verticalScroll(rememberScrollState()),
            content = content,
        )
        footer()
    }
}

/** Bloco de ações do rodapé: padding 24 e o gap informado entre os botões. */
@Composable
fun FooterActions(
    gap: Dp,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SobraColors.Background)
            .padding(Dimens.Space24),
        verticalArrangement = Arrangement.spacedBy(gap),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content,
    )
}
