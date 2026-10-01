package com.unasp.sobra.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Medidas compartilhadas. Os números do Figma são usados como dp.
 * Medidas que só existem em um componente ficam como constantes nomeadas no arquivo dele.
 */
object Dimens {

    /** Margem lateral das telas. Onde o Figma mostra largura 354: fillMaxWidth() + esta margem. */
    val ScreenMargin = 24.dp

    /** Espessura de bordas e divisórias. */
    val BorderWidth = 1.dp

    /** Área mínima de toque (acessibilidade). O tamanho visual do ícone não muda. */
    val MinTouchTarget = 48.dp

    /** Espaço reservado no fim de listas para o FAB / rodapé fixo não cobrirem o último item. */
    val ListBottomReserve = 120.dp

    // ---- Paddings e gaps usados no design ----
    val Space2 = 2.dp
    val Space4 = 4.dp
    val Space6 = 6.dp
    val Space8 = 8.dp
    val Space10 = 10.dp
    val Space12 = 12.dp
    val Space14 = 14.dp
    val Space16 = 16.dp
    val Space20 = 20.dp
    val Space24 = 24.dp
    val Space28 = 28.dp
    val Space32 = 32.dp
    val Space40 = 40.dp

    // ---- Raios usados no design ----
    val Radius8 = 8.dp     // badges, placeholders do skeleton
    val Radius12 = 12.dp   // caixas de ícone, thumbnails
    val Radius16 = 16.dp   // linhas de lista
    val Radius20 = 20.dp   // cards
    val Radius24 = 24.dp   // cards principais, chips, imagem do onboarding
    val Radius25 = 25.dp   // campos e CTA de 50
    val Radius27 = 27.dp   // botões de 54
    val Radius28 = 28.dp   // FAB
    val Radius32 = 32.dp   // modal
    val RadiusFull = 999.dp // totalmente arredondado (pílula/círculo)
}
