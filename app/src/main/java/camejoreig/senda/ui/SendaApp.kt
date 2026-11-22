package camejoreig.senda.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import camejoreig.senda.Voz
import camejoreig.senda.datos.Ajustes
import camejoreig.senda.datos.Repositorio
import camejoreig.senda.datos.Ruta

/*
 * Navegación de la app (sigue el diagrama del README):
 *
 *   ¿Hay ajustes guardados?
 *     No -> GuiaScreen (primera vez) -> guarda ajustes -> InicioScreen
 *     Sí -> InicioScreen
 *   InicioScreen -> Viaje (Orquestador) | CreadorScreen | AjustesScreen
 *
 * Para no añadir dependencias, la navegación es una simple variable de estado
 * ("pantalla"). Si la app crece mucho se puede pasar a Navigation Compose.
 */

/** Todas las pantallas a las que se puede ir. */
sealed interface Pantalla {
    data object Inicio : Pantalla
    data object Viaje : Pantalla
    data object Perdido : Pantalla
    data object Personal : Pantalla
    data object Llegada : Pantalla
    data object Creador : Pantalla
    data class Editor(val ruta: Ruta, val esNueva: Boolean) : Pantalla
    data object CambiarAjustes : Pantalla
}

@Composable
fun SendaApp(repo: Repositorio, voz: Voz) {
    // Primera vez: no hay ajustes guardados -> guía de configuración
    val ajustes = repo.ajustes
    if (ajustes == null) {
        GuiaScreen(
            inicial = Ajustes(),
            voz = voz,
            esPrimeraVez = true,
            guardar = { repo.guardarAjustes(it) }, // al guardarse, repo.ajustes deja de ser null y se ve el inicio
        )
        return
    }

    // Si había un viaje a medias, la app se abre directamente en ese paso
    var pantalla by remember { mutableStateOf<Pantalla>(if (repo.viaje != null) Pantalla.Viaje else Pantalla.Inicio) }

    // Si se está en una pantalla de viaje pero ya no hay viaje (p. ej. se borró la ruta), se enseña el inicio
    val enViaje = pantalla == Pantalla.Viaje || pantalla == Pantalla.Perdido || pantalla == Pantalla.Personal
    val hayViaje = repo.viaje?.let { repo.ruta(it.rutaId) } != null
    val actual = if (enViaje && !hayViaje) Pantalla.Inicio else pantalla

    // Botón "atrás" del sistema
    BackHandler(enabled = actual != Pantalla.Inicio) {
        pantalla = when (actual) {
            Pantalla.Perdido, Pantalla.Personal -> Pantalla.Viaje
            is Pantalla.Editor -> Pantalla.Creador
            else -> Pantalla.Inicio // el viaje queda guardado y se puede seguir desde el inicio
        }
    }

    // AjustesScreen dibuja su propio fondo (usa el tema con los cambios sin guardar)
    if (actual == Pantalla.CambiarAjustes) {
        AjustesScreen(repo, voz, volver = { pantalla = Pantalla.Inicio })
        return
    }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(Modifier.safeDrawingPadding().padding(16.dp)) {
            when (val p = actual) {
                Pantalla.Inicio -> InicioScreen(
                    repo = repo,
                    empezar = { id -> repo.empezar(id); pantalla = Pantalla.Viaje },
                    continuar = { pantalla = Pantalla.Viaje },
                    abrirCreador = { pantalla = Pantalla.Creador },
                    abrirAjustes = { pantalla = Pantalla.CambiarAjustes },
                )
                Pantalla.Viaje -> ViajeScreen(
                    repo = repo, voz = voz,
                    perdido = { pantalla = Pantalla.Perdido },
                    llegada = { repo.terminar(); pantalla = Pantalla.Llegada },
                    salir = { repo.terminar(); pantalla = Pantalla.Inicio },
                )
                Pantalla.Perdido -> PerdidoScreen(
                    repo = repo,
                    volverAlPaso = { pantalla = Pantalla.Viaje },
                    personal = { pantalla = Pantalla.Personal },
                )
                Pantalla.Personal -> PersonalScreen(repo = repo, voz = voz, volver = { pantalla = Pantalla.Viaje })
                Pantalla.Llegada -> LlegadaScreen(inicio = { pantalla = Pantalla.Inicio })
                Pantalla.Creador -> CreadorScreen(
                    repo = repo,
                    editar = { r, nueva -> pantalla = Pantalla.Editor(r, nueva) },
                    listo = { pantalla = Pantalla.Inicio },
                )
                is Pantalla.Editor -> EditorRutaScreen(
                    repo = repo, original = p.ruta, esNueva = p.esNueva,
                    cerrar = { pantalla = Pantalla.Creador },
                )
                Pantalla.CambiarAjustes -> Unit // se dibuja arriba
            }
        }
    }
}
