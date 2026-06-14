// Caminho: app/src/main/java/com/temaerovendas/ui/theme/Theme.kt
package com.temaerovendas.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

// Paleta TEM Aerovendas — dark premium
val NavyDeep = Color(0xFF0D1B2A)        // fundo principal
val NavyMid = Color(0xFF1A2C3E)         // superfícies / cards
val NavyLight = Color(0xFF243447)       // bordas e divisores
val SilverAccent = Color(0xFFC0C8D0)    // texto secundário / ícones
val GoldAccent = Color(0xFFB8860B)      // destaque premium
val GoldLight = Color(0xFFD4AF37)       // botões primários
val WhitePrimary = Color(0xFFF0F4F8)    // texto principal
val ErrorRed = Color(0xFFCF6679)

private val DarkColorScheme = darkColorScheme(
    primary = GoldLight,
    onPrimary = NavyDeep,
    primaryContainer = GoldAccent,
    onPrimaryContainer = WhitePrimary,
    secondary = SilverAccent,
    onSecondary = NavyDeep,
    secondaryContainer = NavyLight,
    onSecondaryContainer = WhitePrimary,
    tertiary = Color(0xFF5B90B0),
    background = NavyDeep,
    onBackground = WhitePrimary,
    surface = NavyMid,
    onSurface = WhitePrimary,
    surfaceVariant = NavyLight,
    onSurfaceVariant = SilverAccent,
    outline = NavyLight,
    error = ErrorRed,
    onError = NavyDeep
)

/**
 * Aplica o tema visual e o modo de tela imersiva do app: a faixa de status
 * (horário, notificações, bateria) e os botões/gestos de navegação do
 * Android ficam ocultos por padrão, aparecendo de volta temporariamente
 * apenas quando o usuário realiza o gesto de deslizar a partir da borda
 * correspondente (comportamento padrão do Android para imersão), e se
 * escondem novamente em seguida — maximizando o espaço de tela disponível
 * para o conteúdo (fotos das aeronaves, principalmente em paisagem).
 */
@Composable
fun TEMAerovendasTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = NavyDeep.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = false

            // Modo imersivo: oculta status bar + navigation bar, retornando-as
            // temporariamente apenas com o gesto de swipe a partir da borda.
            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
        }
    }

    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = AppTypography,
        content = content
    )
}
