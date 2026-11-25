package com.unasp.sobra.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.unasp.sobra.ui.theme.SobraColors

/**
 * Tela de espera simples (indicador Teal no centro), para as telas que o design não tem skeleton.
 * Evita mostrar o estado "vazio" por engano enquanto a lista ainda está chegando.
 */
@Composable
fun LoadingScreen(bottomTab: BottomTab? = null, onTabSelected: (BottomTab) -> Unit = {}) {
    SobraScreen(bottomTab = bottomTab, onTabSelected = onTabSelected) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = SobraColors.Teal)
        }
    }
}
