package camejoreig.senda.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import camejoreig.senda.datos.Ajustes
import camejoreig.senda.datos.ModoColor

/** Permite leer los colores extra desde cualquier pantalla con LocalExtra.current */
val LocalExtra = staticCompositionLocalOf { ExtraClaro }

/**
 * Tema de Senda. Se adapta a lo que se eligió en la guía y en Ajustes:
 * - tamaño de letra,
 * - tema de color (Melocotón por defecto, Azul, Salvia, Lavanda),
 * - claro u oscuro («Según el móvil» usa el del teléfono; si el teléfono no tiene, claro),
 * - alto contraste, que tiene prioridad sobre el tema de color.
 */
@Composable
fun SendaTheme(
    ajustes: Ajustes = Ajustes(),
    content: @Composable () -> Unit,
) {
    val oscuro = when (ajustes.modo) {
        ModoColor.SEGUN_MOVIL -> isSystemInDarkTheme()
        ModoColor.CLARO -> false
        ModoColor.OSCURO -> true
    }
    val contraste = ajustes.altoContraste
    val colores = when {
        contraste && oscuro -> OscuroContraste
        contraste -> ClaroContraste
        else -> esquema(ajustes.tema, oscuro)
    }
    BarrasDelSistema(oscuro)
    CompositionLocalProvider(LocalExtra provides coloresExtra(ajustes.tema, oscuro, contraste)) {
        MaterialTheme(
            colorScheme = colores,
            typography = tipografia(ajustes.tamanoLetra.escala),
            content = content,
        )
    }
}

/**
 * Pone los iconos de la barra de arriba (hora, batería) y de la de abajo en claro u oscuro
 * para que se vean sobre el fondo de la app, aunque el móvil esté en el otro modo.
 * Al salir (por ejemplo, al cancelar Ajustes) se deja como estaba.
 */
@Composable
private fun BarrasDelSistema(oscuro: Boolean) {
    val vista = LocalView.current
    if (vista.isInEditMode) return
    DisposableEffect(oscuro) {
        val ventana = (vista.context as? Activity)?.window
        val control = ventana?.let { WindowCompat.getInsetsController(it, vista) }
        val antesArriba = control?.isAppearanceLightStatusBars
        val antesAbajo = control?.isAppearanceLightNavigationBars
        control?.isAppearanceLightStatusBars = !oscuro
        control?.isAppearanceLightNavigationBars = !oscuro
        onDispose {
            if (control != null && antesArriba != null && antesAbajo != null) {
                control.isAppearanceLightStatusBars = antesArriba
                control.isAppearanceLightNavigationBars = antesAbajo
            }
        }
    }
}
