package camejoreig.senda.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import camejoreig.senda.datos.Icono
import camejoreig.senda.datos.Paso
import camejoreig.senda.datos.Repositorio
import camejoreig.senda.datos.Ruta
import camejoreig.senda.ui.theme.LocalExtra
import kotlinx.coroutines.launch
import java.util.UUID

/*
 * CreadorScreen: crear y cambiar rutas.
 * Está pensado para hacerlo junto a la persona de apoyo, idealmente durante
 * un viaje acompañado, haciendo fotos de cada sitio importante.
 */

/** Lista de rutas guardadas, con botón para editar cada una o crear una nueva. */
@Composable
fun CreadorScreen(repo: Repositorio, editar: (ruta: Ruta, esNueva: Boolean) -> Unit, listo: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Mis rutas", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
            BotonBorde("Listo", listo)
        }
        Text(
            "Preparad la ruta juntos: haced fotos de cada sitio importante durante un viaje acompañado.",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        repo.rutas.forEach { r ->
            Tarjeta {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(r.nombre, style = MaterialTheme.typography.titleLarge)
                        Text("${r.pasos.size} pasos", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    BotonBorde("Cambiar", { editar(r, false) })
                }
            }
        }
        BotonGrande("+ Nueva ruta", {
            editar(Ruta(id = "r" + UUID.randomUUID(), nombre = "Nueva ruta", pasos = listOf(Paso(""))), true)
        })
    }
}

/**
 * Editor de una ruta: nombre y lista de pasos.
 * Los cambios se hacen sobre una copia y solo se guardan al pulsar "Guardar ruta".
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditorRutaScreen(repo: Repositorio, original: Ruta, esNueva: Boolean, cerrar: () -> Unit) {
    var nombre by remember { mutableStateOf(original.nombre) }
    val pasos = remember { mutableStateListOf(*original.pasos.toTypedArray()) }
    var abierto by remember { mutableStateOf<Int?>(if (esNueva) 0 else null) } // paso que se está editando
    var aviso by remember { mutableStateOf("") }
    var confirmarBorrado by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Selector de fotos del sistema. No necesita pedir permisos.
    val elegirFoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        val k = abierto
        if (uri != null && k != null) scope.launch {
            val ruta = repo.guardarFoto(uri)
            if (ruta != null) pasos[k] = pasos[k].copy(foto = ruta) else aviso = "No se ha podido abrir esa foto."
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(if (esNueva) "Nueva ruta" else "Cambiar ruta", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
            BotonBorde("Cancelar", cerrar)
        }
        OutlinedTextField(nombre, { nombre = it }, label = { Text("Nombre de la ruta") }, singleLine = true, modifier = Modifier.fillMaxWidth())

        Text("Pasos", style = MaterialTheme.typography.headlineSmall)
        pasos.forEachIndexed { k, p ->
            if (k == abierto) {
                // ----- Paso abierto para editar -----
                Tarjeta {
                    Text("Paso ${k + 1}", style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(
                        p.texto, { pasos[k] = p.copy(texto = it) },
                        label = { Text("Qué tiene que hacer (frases cortas)") },
                        placeholder = { Text("Ej.: Ve al andén que pone «Moncloa».") },
                        minLines = 2, modifier = Modifier.fillMaxWidth(),
                    )
                    Text("Dibujo", style = MaterialTheme.typography.bodyMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icono.entries.forEach { ic ->
                            BotonOpcion(
                                elegido = p.icono == ic,
                                onClick = { pasos[k] = p.copy(icono = ic) },
                                modifier = Modifier.size(72.dp),
                                relleno = PaddingValues(0.dp),
                            ) {
                                Icon(ic.vector(), contentDescription = ic.etiqueta, modifier = Modifier.size(36.dp))
                            }
                        }
                    }
                    if (p.foto != null) {
                        VisualPaso(p, tamano = 80.dp, modifier = Modifier.fillMaxWidth())
                        BotonBorde("Quitar foto", { pasos[k] = p.copy(foto = null) }, Modifier.fillMaxWidth())
                    }
                    BotonBorde(
                        if (p.foto == null) "Añadir foto del sitio (opcional)" else "Cambiar foto",
                        { elegirFoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        Modifier.fillMaxWidth(),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        BotonGrande("Hecho", { abierto = null }, Modifier.weight(1f))
                        BotonBorde("Borrar paso", { pasos.removeAt(k); abierto = null }, Modifier.weight(1f))
                    }
                }
            } else {
                // ----- Paso cerrado: resumen y flechas para cambiar el orden -----
                Tarjeta {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        VisualPaso(p, tamano = 40.dp)
                        Spacer(Modifier.size(12.dp))
                        Text("${k + 1}. ${p.texto.ifBlank { "(sin texto)" }}", modifier = Modifier.weight(1f))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { abierto = k }) { Text("Cambiar") }
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { pasos.add(k - 1, pasos.removeAt(k)) }, enabled = k > 0) {
                            Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Subir paso ${k + 1}")
                        }
                        IconButton(onClick = { pasos.add(k + 1, pasos.removeAt(k)) }, enabled = k < pasos.lastIndex) {
                            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Bajar paso ${k + 1}")
                        }
                    }
                }
            }
        }
        BotonBorde("+ Añadir paso", { pasos.add(Paso("")); abierto = pasos.lastIndex }, Modifier.fillMaxWidth())

        if (aviso.isNotEmpty()) Text(aviso, color = MaterialTheme.colorScheme.error)

        // "¿Ruta completa?" del diagrama: hace falta al menos un paso con texto o foto
        BotonGrande("Guardar ruta", {
            val validos = pasos.filter { it.texto.isNotBlank() || it.foto != null }
            if (validos.isEmpty()) {
                aviso = "Añade al menos un paso con texto o foto."
            } else {
                repo.guardarRuta(original.copy(nombre = nombre.trim().ifBlank { "Ruta" }, pasos = validos))
                cerrar()
            }
        }, fondo = LocalExtra.current.ok, letra = LocalExtra.current.okTexto)

        if (!esNueva) {
            // Sin ventanas emergentes: se pide un segundo toque para borrar
            TextButton(onClick = {
                if (confirmarBorrado) {
                    repo.borrarRuta(original.id); cerrar()
                } else {
                    confirmarBorrado = true
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text(
                    if (confirmarBorrado) "Toca otra vez para borrarla del todo" else "Borrar esta ruta",
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
