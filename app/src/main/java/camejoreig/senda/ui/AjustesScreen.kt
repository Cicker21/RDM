package camejoreig.senda.ui

import androidx.compose.runtime.Composable
import camejoreig.senda.Voz
import camejoreig.senda.datos.Ajustes
import camejoreig.senda.datos.Repositorio

/**
 * AjustesScreen: cambiar lo que se eligió en la guía.
 * Reutiliza el formulario de GuiaScreen, empezando con los valores guardados
 * y con un botón de "Cancelar" para volver sin cambiar nada.
 */
@Composable
fun AjustesScreen(repo: Repositorio, voz: Voz, volver: () -> Unit) {
    GuiaScreen(
        inicial = repo.ajustes ?: Ajustes(),
        voz = voz,
        esPrimeraVez = false,
        guardar = { repo.guardarAjustes(it); volver() },
        cancelar = volver,
    )
}
