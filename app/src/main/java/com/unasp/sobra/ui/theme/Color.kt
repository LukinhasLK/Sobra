package com.unasp.sobra.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Paleta do Sobra, extraída do Figma.
 * Este é o ÚNICO arquivo do app que pode usar Color(0xFF...).
 */
// `object` em Kotlin = singleton (equivale a uma classe Java só com membros static).
object SobraColors {

    // ---- Tokens base ----
    val Background = Color(0xFFFAF8F5)    // fundo de todas as telas
    val Surface = Color(0xFFFFFFFF)       // cards, campos brancos, navegação
    val TextPrimary = Color(0xFF1C1917)   // texto principal
    val TextSecondary = Color(0xFF78716C) // labels, placeholders, ícones inativos
    val Teal = Color(0xFF0D9488)          // marca, CTA, seleção, progresso
    val TealLight = Color(0xFFCCFBF1)     // destaques, chips ativos, badges
    val Soft = Color(0xFFF2EFEA)          // campos suaves, trilhas, botão secundário
    val Border = Color(0xFFE7E5E4)        // bordas, divisórias
    val Blue = Color(0xFF3B82F6)          // categoria Alimentação
    val Amber = Color(0xFFF59E0B)         // categoria Transporte
    val Red = Color(0xFFEF4444)           // Saúde, erro, "Sair da conta"
    val Green = Color(0xFF10B981)         // Lazer, rendimento positivo
    val Violet = Color(0xFF8B5CF6)        // categoria Outros
    val ErrorBg = Color(0xFFFEF2F2)       // círculo da tela de erro

    // Texto/ícone sobre fundo Teal. O Figma usa branco puro (mesmo hex de Surface);
    // o nome separado só deixa a intenção clara em quem lê a tela.
    val OnTeal = Color(0xFFFFFFFF)

    // ---- Transparências ----
    // O Figma informa alpha de duas formas: 0–255 (ex.: 21/255) e 0–1 (ex.: 0.20).
    // Mantemos a forma original de cada uma para não arredondar.
    private const val ALPHA_CATEGORY_ICON_BG = 21f / 255f
    private const val ALPHA_SCRIM = 179f / 255f
    private const val ALPHA_SPLASH_LOGO_SHADOW = 20f / 255f
    private const val ALPHA_SHADOW = 0.20f
    private const val ALPHA_CHART_NO_YIELD = 0.40f

    // copy(alpha = ...) cria uma nova cor igual à original, trocando só o alpha.
    val Scrim = TextPrimary.copy(alpha = ALPHA_SCRIM)                       // fundo do modal
    val TealShadow = Teal.copy(alpha = ALPHA_SHADOW)                        // sombra de FAB e avatar
    val ModalShadow = Color.Black.copy(alpha = ALPHA_SHADOW)                // sombra do modal
    val SplashLogoShadow = Color.Black.copy(alpha = ALPHA_SPLASH_LOGO_SHADOW)
    val ChartNoYield = TextSecondary.copy(alpha = ALPHA_CHART_NO_YIELD)     // série "Sem rendimento"

    /** Fundo da caixa do ícone de categoria: a cor da própria categoria com alpha 21/255. */
    fun categoryIconBackground(categoryColor: Color): Color =
        categoryColor.copy(alpha = ALPHA_CATEGORY_ICON_BG)
}
