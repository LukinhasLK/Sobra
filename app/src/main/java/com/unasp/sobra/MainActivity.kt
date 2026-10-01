package com.unasp.sobra

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.unasp.sobra.navigation.SobraNavHost
import com.unasp.sobra.ui.theme.SobraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Splash de sistema (SplashScreen API): precisa vir antes do super.onCreate.
        // Fundo Teal e ícone são definidos em res/values/themes.xml.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        // Edge-to-edge: o app desenha por baixo da status bar e da barra de gestos; as
        // telas usam statusBarsPadding()/navigationBarsPadding() para não ficar atrás delas.
        // "light" = barras transparentes com ícones escuros, pois o app só tem tema claro.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        setContent {
            SobraTheme {
                SobraNavHost()
            }
        }
    }
}
