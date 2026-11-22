package camejoreig.senda.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import camejoreig.senda.datos.Ajustes

/** Permite leer los colores extra desde cualquier pantalla con LocalExtra.current */
val LocalExtra = staticCompositionLocalOf { ExtraClaro }

/**
 * Tema de Senda. Se adapta a lo que el usuario eligió en la guía:
 * tamaño de letra y alto contraste. El modo claro/oscuro sigue al del móvil.
 */
@Composable
fun SendaTheme(
    ajustes: Ajustes = Ajustes(),
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val contraste = ajustes.altoContraste
    val colores = when {
        darkTheme && contraste -> OscuroContraste
        darkTheme -> Oscuro
        contraste -> ClaroContraste
        else -> Claro
    }
    val extra = when {
        darkTheme && contraste -> ExtraOscuroContraste
        darkTheme -> ExtraOscuro
        contraste -> ExtraClaroContraste
        else -> ExtraClaro
    }
    CompositionLocalProvider(LocalExtra provides extra) {
        MaterialTheme(
            colorScheme = colores,
            typography = tipografia(ajustes.tamanoLetra.escala),
            content = content,
        )
    }
}
