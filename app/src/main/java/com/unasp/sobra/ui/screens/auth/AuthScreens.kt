package com.unasp.sobra.ui.screens.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import com.unasp.sobra.R
import com.unasp.sobra.ui.components.AuthHeader
import com.unasp.sobra.ui.components.FooterActions
import com.unasp.sobra.ui.components.FormScreen
import com.unasp.sobra.ui.components.PrimaryButton
import com.unasp.sobra.ui.components.SecondaryButton
import com.unasp.sobra.ui.components.SoftField
import com.unasp.sobra.ui.components.TextLink
import com.unasp.sobra.ui.theme.Dimens
import com.unasp.sobra.ui.theme.SobraColors
import com.unasp.sobra.ui.theme.SobraTheme
import com.unasp.sobra.ui.theme.manrope

// TODO: regras de validação (e-mail válido, tamanho mínimo da senha, senhas iguais) e a
//  aparência das mensagens de erro não foram definidas no design.

private val EmailKeyboard = KeyboardOptions(keyboardType = KeyboardType.Email)
private val PasswordKeyboard = KeyboardOptions(keyboardType = KeyboardType.Password)
private val LoginHint = manrope(13, FontWeight.W400)

/** Coluna dos campos: padding horizontal 24, gap 16. */
@Composable
private fun AuthForm(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.ScreenMargin),
        verticalArrangement = Arrangement.spacedBy(Dimens.Space16),
    ) { content() }
}

/** 05 Login. Tela sem estado: tudo que muda chega por parâmetro e sai por callback. */
@Composable
fun LoginScreen(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    onBack: () -> Unit,
    onForgotPassword: () -> Unit,
    onLogin: () -> Unit,
    onCreateAccount: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FormScreen(
        modifier = modifier,
        footer = {
            FooterActions(gap = Dimens.Space20) {
                PrimaryButton(stringResource(R.string.action_login), onLogin)
                SecondaryButton(stringResource(R.string.action_create_account), onCreateAccount)
            }
        },
    ) {
        AuthHeader(
            backText = stringResource(R.string.action_back),
            onBack = onBack,
            title = stringResource(R.string.login_title),
            description = stringResource(R.string.login_description),
        )
        AuthForm {
            SoftField(
                label = stringResource(R.string.field_email),
                value = email,
                onValueChange = onEmailChange,
                placeholder = stringResource(R.string.login_email_placeholder),
                keyboardOptions = EmailKeyboard,
            )
            SoftField(
                label = stringResource(R.string.field_password),
                value = password,
                onValueChange = onPasswordChange,
                placeholder = stringResource(R.string.login_password_placeholder),
                keyboardOptions = PasswordKeyboard,
                // Esconde os caracteres com bolinhas enquanto a senha não está visível.
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailing = {
                    TextLink(
                        text = stringResource(if (passwordVisible) R.string.action_hide else R.string.action_show),
                        onClick = onTogglePasswordVisibility,
                    )
                },
            )
            TextLink(
                text = stringResource(R.string.login_forgot_password),
                onClick = onForgotPassword,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
            )
        }
    }
}

/** 06 Cadastro. */
@Composable
fun RegisterScreen(
    name: String,
    onNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    onBack: () -> Unit,
    onCreateAccount: () -> Unit,
    onGoToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FormScreen(
        modifier = modifier,
        footer = {
            FooterActions(gap = Dimens.Space20) {
                PrimaryButton(stringResource(R.string.action_create_account), onCreateAccount)
                // Texto com dois estilos na mesma linha: AnnotatedString aplica um estilo
                // diferente a cada trecho.
                val text = buildAnnotatedString {
                    withStyle(SpanStyle(color = SobraColors.TextSecondary, fontWeight = FontWeight.W400)) {
                        append(stringResource(R.string.register_have_account))
                    }
                    append(" ")
                    withStyle(SpanStyle(color = SobraColors.Teal, fontWeight = FontWeight.W600)) {
                        append(stringResource(R.string.register_login_link))
                    }
                }
                Text(
                    text = text,
                    style = LoginHint,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.Button, onClick = onGoToLogin),
                )
            }
        },
    ) {
        AuthHeader(
            backText = stringResource(R.string.action_back),
            onBack = onBack,
            title = stringResource(R.string.register_title),
            description = stringResource(R.string.register_description),
        )
        AuthForm {
            SoftField(
                label = stringResource(R.string.field_full_name),
                value = name,
                onValueChange = onNameChange,
                placeholder = stringResource(R.string.register_name_placeholder),
            )
            SoftField(
                label = stringResource(R.string.field_email),
                value = email,
                onValueChange = onEmailChange,
                placeholder = stringResource(R.string.register_email_placeholder),
                keyboardOptions = EmailKeyboard,
            )
            SoftField(
                label = stringResource(R.string.field_password),
                value = password,
                onValueChange = onPasswordChange,
                placeholder = stringResource(R.string.register_password_placeholder),
                keyboardOptions = PasswordKeyboard,
                visualTransformation = PasswordVisualTransformation(),
            )
            SoftField(
                label = stringResource(R.string.field_confirm_password),
                value = confirmPassword,
                onValueChange = onConfirmPasswordChange,
                placeholder = stringResource(R.string.register_confirm_placeholder),
                keyboardOptions = PasswordKeyboard,
                visualTransformation = PasswordVisualTransformation(),
            )
        }
    }
}

/** 07 Recuperar senha. */
@Composable
fun RecoverPasswordScreen(
    email: String,
    onEmailChange: (String) -> Unit,
    onBack: () -> Unit,
    onSend: () -> Unit,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // TODO: o estado depois do envio (confirmação, erro) não existe no Figma.
    FormScreen(
        modifier = modifier,
        footer = {
            FooterActions(gap = Dimens.Space20) {
                PrimaryButton(stringResource(R.string.recover_send), onSend)
                TextLink(
                    text = stringResource(R.string.recover_back_to_login),
                    onClick = onBackToLogin,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }
        },
    ) {
        AuthHeader(
            backText = stringResource(R.string.action_back),
            onBack = onBack,
            title = stringResource(R.string.recover_title),
            description = stringResource(R.string.recover_description),
        )
        AuthForm {
            SoftField(
                label = stringResource(R.string.recover_email_label),
                value = email,
                onValueChange = onEmailChange,
                placeholder = stringResource(R.string.register_email_placeholder),
                keyboardOptions = EmailKeyboard,
            )
        }
    }
}

@Preview(widthDp = 402, heightDp = 874)
@Composable
private fun LoginScreenPreview() {
    SobraTheme {
        LoginScreen(
            email = "", onEmailChange = {}, password = "", onPasswordChange = {},
            passwordVisible = false, onTogglePasswordVisibility = {},
            onBack = {}, onForgotPassword = {}, onLogin = {}, onCreateAccount = {},
        )
    }
}

@Preview(widthDp = 402, heightDp = 874)
@Composable
private fun RegisterScreenPreview() {
    SobraTheme {
        RegisterScreen(
            name = "", onNameChange = {}, email = "", onEmailChange = {},
            password = "", onPasswordChange = {}, confirmPassword = "", onConfirmPasswordChange = {},
            onBack = {}, onCreateAccount = {}, onGoToLogin = {},
        )
    }
}

@Preview(widthDp = 402, heightDp = 874)
@Composable
private fun RecoverPasswordScreenPreview() {
    SobraTheme {
        RecoverPasswordScreen(email = "", onEmailChange = {}, onBack = {}, onSend = {}, onBackToLogin = {})
    }
}
