package camejoreig.senda.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import camejoreig.senda.datos.Icono
import camejoreig.senda.datos.Paso
import camejoreig.senda.datos.Repositorio

/**
 * InicioScreen: menú principal (desplazable).
 * - Si hay un viaje a medias, lo primero es poder seguirlo.
 * - Después, las rutas guardadas: al tocar una, empieza el viaje.
 * - Abajo, crear/cambiar rutas y ajustes.
 */
@Composable
fun InicioScreen(
    repo: Repositorio,
    empezar: (rutaId: String) -> Unit,
    continuar: () -> Unit,
    abrirCreador: () -> Unit,
    abrirAjustes: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("¿A dónde vas?", style = MaterialTheme.typography.headlineMedium)

        // Viaje a medias: se ofrece seguir donde se quedó
        val v = repo.viaje
        val enCurso = v?.let { repo.ruta(it.rutaId) }
        if (v != null && enCurso != null) {
            BotonGrande("Seguir con «${enCurso.nombre}» (paso ${v.paso + 1})", continuar)
        }

        if (repo.rutas.isEmpty()) {
            Text(
                "Todavía no hay rutas. Prepara una con tu persona de apoyo en «Crear o cambiar rutas».",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        repo.rutas.forEach { r ->
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

        Spacer(Modifier.height(16.dp))
        BotonBorde("Crear o cambiar rutas", abrirCreador, Modifier.fillMaxWidth())
        BotonBorde("Ajustes", abrirAjustes, Modifier.fillMaxWidth())
    }
}
