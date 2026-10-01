package com.unasp.sobra.ui.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

// As telas leem as cores direto de SobraColors. Este esquema só existe para que um
// componente Material usado por engano apareça com a paleta do app, e não em roxo.
private val SobraColorScheme = lightColorScheme(
    primary = SobraColors.Teal,
    onPrimary = SobraColors.OnTeal,
    primaryContainer = SobraColors.TealLight,
    onPrimaryContainer = SobraColors.Teal,
    secondary = SobraColors.Teal,
    onSecondary = SobraColors.OnTeal,
    secondaryContainer = SobraColors.Soft,
    onSecondaryContainer = SobraColors.TextPrimary,
    background = SobraColors.Background,
    onBackground = SobraColors.TextPrimary,
    surface = SobraColors.Surface,
    onSurface = SobraColors.TextPrimary,
    surfaceVariant = SobraColors.Soft,
    onSurfaceVariant = SobraColors.TextSecondary,
    outline = SobraColors.Border,
    outlineVariant = SobraColors.Border,
    error = SobraColors.Red,
    onError = SobraColors.OnTeal,
    errorContainer = SobraColors.ErrorBg,
    onErrorContainer = SobraColors.Red,
    scrim = SobraColors.Scrim,
)

/**
 * Tema do app. O Figma só define o tema claro, então não há tema escuro nem
 * cores dinâmicas do Android 12+ (elas trocariam o Teal pela cor do papel de parede).
 */
@Composable
fun SobraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SobraColorScheme,
        typography = SobraMaterialTypography,
    ) {
        // CompositionLocalProvider define valores "ambiente" lidos por tudo que estiver dentro:
        // aqui, o estilo e a cor padrão de qualquer Text que não informe os seus.
        CompositionLocalProvider(
            LocalTextStyle provides BaseTextStyle,
            LocalContentColor provides SobraColors.TextPrimary,
            content = content,
        )
    }
}
