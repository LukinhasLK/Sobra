package com.unasp.sobra.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unasp.sobra.R
import com.unasp.sobra.data.DemoData
import com.unasp.sobra.ui.components.BottomTab
import com.unasp.sobra.ui.components.SobraIcon
import com.unasp.sobra.ui.components.SobraScreen
import com.unasp.sobra.ui.components.SobraSwitch
import com.unasp.sobra.ui.components.TitleHeader
import com.unasp.sobra.ui.components.figmaShadow
import com.unasp.sobra.ui.components.sobraCard
import com.unasp.sobra.ui.theme.Dimens
import com.unasp.sobra.ui.theme.SobraColors
import com.unasp.sobra.ui.theme.SobraTheme
import com.unasp.sobra.ui.theme.manrope

private val AvatarSize = 72.dp
private val AvatarShadowBlur = 12.dp
private val AvatarShadowOffsetY = 4.dp
private val RowHeight = 52.dp
private val SwitchRowHeight = 56.dp

private val Initials = manrope(24, FontWeight.W800)
private val UserName = manrope(18, FontWeight.W700)
private val UserEmail = manrope(13, FontWeight.W400)
private val RowText = manrope(15, FontWeight.W500)

/** 19 Perfil. Rolável: os 897 do frame do Figma são o conteúdo crescendo. */
@Composable
fun ProfileScreen(
    initials: String,
    name: String,
    email: String,
    notificationsEnabled: Boolean,
    onNotificationsChange: (Boolean) -> Unit,
    onPersonalDataClick: () -> Unit,
    onSecurityClick: () -> Unit,
    onAboutClick: () -> Unit,
    onTermsClick: () -> Unit,
    onLogout: () -> Unit,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    SobraScreen(modifier = modifier, bottomTab = BottomTab.Profile, onTabSelected = onTabSelected) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            TitleHeader(stringResource(R.string.profile_title))

            // Usuário: padding 24, gap 16, centralizado.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.Space24),
                verticalArrangement = Arrangement.spacedBy(Dimens.Space16),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(AvatarSize)
                        .figmaShadow(SobraColors.TealShadow, AvatarShadowBlur, AvatarShadowOffsetY, AvatarSize / 2)
                        .clip(CircleShape)
                        .background(SobraColors.Teal),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(initials, style = Initials, color = SobraColors.OnTeal)
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(Dimens.Space4),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(name, style = UserName, color = SobraColors.TextPrimary)
                    Text(email, style = UserEmail, color = SobraColors.TextSecondary)
                }
            }

            // Três cards com gap 16.
            Column(
                modifier = Modifier.padding(
                    start = Dimens.ScreenMargin, end = Dimens.ScreenMargin, bottom = Dimens.Space24,
                ),
                verticalArrangement = Arrangement.spacedBy(Dimens.Space16),
            ) {
                SettingsCard {
                    SettingsRow(stringResource(R.string.profile_personal_data), onPersonalDataClick)
                    SettingsRow(stringResource(R.string.profile_security), onSecurityClick)
                    SettingsRow(
                        text = stringResource(R.string.profile_notifications),
                        onClick = { onNotificationsChange(!notificationsEnabled) },
                        height = SwitchRowHeight,
                        showDivider = false,
                        trailing = { SobraSwitch(notificationsEnabled, onNotificationsChange) },
                    )
                }
                SettingsCard {
                    SettingsRow(stringResource(R.string.profile_about), onAboutClick)
                    SettingsRow(stringResource(R.string.profile_terms), onTermsClick, showDivider = false)
                }
                SettingsCard {
                    // A linha tem 52 de altura. O espaço vazio de 100×100 que existe no Figma
                    // nesta linha é uma anomalia do arquivo e NÃO é reproduzido.
                    SettingsRow(
                        text = stringResource(R.string.profile_logout),
                        onClick = onLogout,
                        textColor = SobraColors.Red,
                        showDivider = false,
                        trailing = {},
                    )
                }
            }
        }
    }
}

/** Card de configurações: padding 16, raio 20, borda 1. */
@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .sobraCard(Dimens.Radius20)
            .padding(Dimens.Space16),
        content = content,
    )
}

/**
 * Linha de configuração: 52 de altura, texto 15/500, chevron 16 à direita (por padrão) e
 * separador de 1 embaixo, exceto na última linha do card.
 */
@Composable
private fun SettingsRow(
    text: String,
    onClick: () -> Unit,
    height: Dp = RowHeight,
    textColor: Color = SobraColors.TextPrimary,
    showDivider: Boolean = true,
    trailing: @Composable () -> Unit = { SobraIcon(R.drawable.ic_chevron_right_16, tint = SobraColors.TextSecondary) },
) {
    val dividerColor = SobraColors.Border
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clickable(role = Role.Button, onClick = onClick)
            // O separador faz parte da linha (por isso 3 linhas somam exatamente 52+52+56):
            // é desenhado por dentro, na borda inferior.
            .drawBehind {
                if (showDivider) {
                    val stroke = Dimens.BorderWidth.toPx()
                    val y = size.height - stroke / 2
                    drawLine(dividerColor, Offset(0f, y), Offset(size.width, y), stroke)
                }
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, style = RowText, color = textColor)
        trailing()
    }
}

@Preview(widthDp = 402, heightDp = 897)
@Composable
private fun ProfileScreenPreview() {
    SobraTheme {
        ProfileScreen(
            initials = DemoData.USER_INITIALS, name = DemoData.USER_FULL_NAME, email = DemoData.USER_EMAIL,
            notificationsEnabled = true, onNotificationsChange = {},
            onPersonalDataClick = {}, onSecurityClick = {}, onAboutClick = {}, onTermsClick = {},
            onLogout = {}, onTabSelected = {},
        )
    }
}
