package camejoreig.senda.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import camejoreig.senda.Voz
import camejoreig.senda.datos.Ajustes
import camejoreig.senda.datos.Icono
import camejoreig.senda.datos.Paso
import camejoreig.senda.datos.Repositorio
import kotlinx.coroutines.delay

/**
 * InicioScreen: menú principal.
 * - Arriba, fijo (no se mueve al bajar): «Ayuda» (un toque, para la persona) y «Apoyo»
 *   (mantener pulsado, para la persona de apoyo), y el título «¿A dónde vas?».
 * - Debajo, lo único que se desplaza: el viaje a medias (si lo hay) y las rutas guardadas.
 * - Si todavía no hay rutas: «Crear mi primera ruta», grande.
 */
@Composable
fun InicioScreen(
    repo: Repositorio,
    voz: Voz,
    empezar: (rutaId: String) -> Unit,
    continuar: () -> Unit,
    abrirCreador: () -> Unit,
    abrirApoyo: () -> Unit,
    abrirAyuda: () -> Unit,
) {
    val ajustes = repo.ajustes ?: Ajustes()
    // Cada toque corto en «Apoyo» suma 1: así el aviso se vuelve a enseñar aunque ya estuviera visible
    var toquesApoyo by remember { mutableIntStateOf(0) }
    var verAviso by remember { mutableIntStateOf(0) } // 0 = oculto

    LaunchedEffect(toquesApoyo) {
        if (toquesApoyo == 0) return@LaunchedEffect
        verAviso = toquesApoyo
        if (ajustes.leerEnVozAlta) voz.decir(AVISO_MANTENER)
        delay(4000)
        verAviso = 0
    }

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // ----- Barra fija de arriba -----
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            BotonIcono("Ayuda", Icons.AutoMirrored.Filled.Help, abrirAyuda)
            Spacer(Modifier.weight(1f))
            BotonMantener("Apoyo", Icons.Filled.Lock, alCompletar = abrirApoyo, alTocar = { toquesApoyo++ })
        }
        if (verAviso != 0) {
            Text(
                AVISO_MANTENER,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primary).padding(12.dp),
            )
        }
        Text(
            "¿A dónde vas?",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        // ----- Lo que se desplaza -----
        LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Viaje a medias: se ofrece seguir donde se quedó
            val v = repo.viaje
            val enCurso = v?.let { repo.ruta(it.rutaId) }
            if (v != null && enCurso != null) {
                item { BotonGrande("Seguir con «${enCurso.nombre}» (paso ${v.paso + 1})", continuar) }
            }

            if (repo.rutas.isEmpty()) {
                // Sin rutas la app no sirve todavía: crear la primera va con un toque normal
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            "Todavía no tienes rutas. Prepara la primera con tu persona de apoyo.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        BotonGrande("Crear mi primera ruta", abrirCreador)
                    }
                }
            }

            items(repo.rutas, key = { it.id }) { r ->
                OutlinedButton(
                    onClick = { empezar(r.id) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 96.dp),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                ) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        // El dibujo del último paso suele ser el destino (trabajo, casa...)
                        VisualPaso(Paso("", r.pasos.lastOrNull()?.icono ?: Icono.TRABAJO), tamano = 40.dp)
                        Spacer(Modifier.size(16.dp))
                        Column {
                            Text(r.nombre, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                            Text("${r.pasos.size} pasos", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
