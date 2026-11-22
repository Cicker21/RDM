package camejoreig.senda.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import camejoreig.senda.Voz
import camejoreig.senda.datos.Ajustes
import camejoreig.senda.datos.Icono
import camejoreig.senda.datos.Paso
import camejoreig.senda.datos.Repositorio
import camejoreig.senda.ui.theme.LocalExtra

/*
 * Orquestador: la parte central de Senda. Lee la ruta del viaje en curso y la
 * enseña paso a paso. Incluye la ayuda para cuando algo va mal ("Me he perdido").
 *
 * Pantallas de este archivo:
 *   ViajeScreen    -> un solo paso en pantalla, con dibujo/foto, frase y voz
 *   PerdidoScreen  -> calma y opciones: volver al paso anterior, pedir ayuda, avisar
 *   PersonalScreen -> tarjeta para enseñar al personal del Metro
 *   LlegadaScreen  -> "¡Has llegado!"
 *
 * PENDIENTE (siguientes fases, según el README):
 *   - Comprobar el estado de la línea antes de empezar (falta ver si hay fuente pública y gratuita).
 *   - Generar los pasos a partir de los datos GTFS del Consorcio en vez de escribirlos a mano.
 *   - Detectar si el usuario se ha pasado de parada (necesitaría el permiso de ubicación).
 */

@Composable
fun ViajeScreen(repo: Repositorio, voz: Voz, perdido: () -> Unit, llegada: () -> Unit, salir: () -> Unit) {
    // Si por lo que sea no hay viaje o la ruta se ha borrado, no se dibuja nada
    // (SendaApp devuelve al inicio en ese caso).
    val v = repo.viaje ?: return
    val ruta = repo.ruta(v.rutaId) ?: return
    if (ruta.pasos.isEmpty()) return
    val ajustes = repo.ajustes ?: Ajustes()
    val i = v.paso.coerceIn(0, ruta.pasos.lastIndex)
    val paso = ruta.pasos[i]
    val total = ruta.pasos.size
    val extra = LocalExtra.current

    // Lectura automática: cada vez que cambia el paso, se lee si el usuario lo eligió en la guía
    LaunchedEffect(i, voz.lista) {
        if (ajustes.leerEnVozAlta) voz.decir(paso.texto)
    }

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            BotonBorde("Dejar la ruta", salir)
            Spacer(Modifier.weight(1f))
            Text("Paso ${i + 1} de $total", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        // Barra de progreso por segmentos: se ve cuánto queda sin tener que leer números
        Row(
            Modifier.fillMaxWidth().semantics { contentDescription = "Paso ${i + 1} de $total" },
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            repeat(total) { k ->
                val color = when {
                    k < i -> extra.ok
                    k == i -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.outline
                }
                Box(Modifier.weight(1f).height(10.dp).clip(RoundedCornerShape(5.dp)).background(color))
            }
        }

        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        ) {
            VisualPaso(paso, tamano = if (ajustes.dibujosGrandes) 200.dp else 140.dp, modifier = Modifier.fillMaxWidth())
            Text(paso.texto, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            if (voz.lista) {
                OutlinedButton(onClick = { voz.decir(paso.texto) }, modifier = Modifier.heightIn(min = 56.dp)) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Escuchar")
                }
            }
        }

        BotonGrande(
            texto = if (i == total - 1) "He llegado ✓" else "Hecho ✓",
            onClick = { if (i == total - 1) llegada() else repo.irAPaso(i + 1) },
            fondo = extra.ok, letra = extra.okTexto,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BotonBorde("← Atrás", { repo.irAPaso(i - 1) }, Modifier.weight(1f), enabled = i > 0)
            BotonGrande("Me he perdido", perdido, Modifier.weight(1f), fondo = extra.ayuda, letra = extra.ayudaTexto)
        }
    }
}

// ---------------------------------------------------------------
// Me he perdido: primero calma, después opciones. Decide el usuario.
// ---------------------------------------------------------------
@Composable
fun PerdidoScreen(repo: Repositorio, volverAlPaso: () -> Unit, personal: () -> Unit) {
    val v = repo.viaje ?: return
    val ruta = repo.ruta(v.rutaId) ?: return
    val i = v.paso.coerceIn(0, ruta.pasos.lastIndex)
    val anterior = if (i > 0) ruta.pasos[i - 1] else null
    val c = (repo.ajustes ?: Ajustes()).contacto
    val tel = c.telefono.filter { it.isDigit() || it == '+' } // quitamos espacios y guiones
    val ctx = LocalContext.current
    val extra = LocalExtra.current

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("No pasa nada. Para un momento y respira.", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Text("Tú decides qué hacer ahora.", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())

        if (anterior != null) {
            Tarjeta {
                Text("Volver al último sitio que conoces", style = MaterialTheme.typography.titleLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    VisualPaso(anterior, tamano = 48.dp)
                    Spacer(Modifier.size(12.dp))
                    Text(anterior.texto)
                }
                BotonGrande("Volver a este paso", { repo.irAPaso(i - 1); volverAlPaso() })
            }
        }

        BotonGrande("Pedir ayuda al personal del Metro", personal)

        if (tel.isNotEmpty()) {
            Tarjeta {
                Text("Avisar a ${c.nombre.ifBlank { "mi contacto" }}", style = MaterialTheme.typography.titleLarge)
                SelectionContainer { Text(c.telefono, style = MaterialTheme.typography.headlineSmall) }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BotonGrande("Llamar", { abrir(ctx, Intent(Intent.ACTION_DIAL, Uri.parse("tel:$tel"))) }, Modifier.weight(1f))
                    BotonGrande("WhatsApp", {
                        val msg = "Hola, me he perdido en el paso ${i + 1}: \"${ruta.pasos[i].texto}\""
                        val url = "https://wa.me/${tel.removePrefix("+")}?text=${Uri.encode(msg)}"
                        abrir(ctx, Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }, Modifier.weight(1f))
                }
                Text("Solo se avisa si tú pulsas uno de estos botones.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            Text("No hay ningún contacto guardado. Puedes añadirlo en Ajustes.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        BotonGrande("Ya estoy bien, seguir", volverAlPaso, fondo = extra.ok, letra = extra.okTexto)
    }
}

/** "Llamar" abre el marcador con el número puesto; la llamada la hace el usuario. */
private fun abrir(ctx: Context, intent: Intent) {
    try {
        ctx.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        // No hay app para esto (por ejemplo, una tablet sin teléfono). El número sigue visible en pantalla.
    }
}

// ---------------------------------------------------------------
// Tarjeta para enseñar al personal del Metro
// ---------------------------------------------------------------
@Composable
fun PersonalScreen(repo: Repositorio, voz: Voz, volver: () -> Unit) {
    val ruta = repo.viaje?.let { repo.ruta(it.rutaId) } ?: return
    // Destino: la estación del último paso de tren ("...: Tribunal."); si no, el nombre de la ruta
    val destino = ruta.pasos.lastOrNull { it.icono == Icono.TREN }
        ?.let { Regex(""":\s*([^.]+)\.?$""").find(it.texto)?.groupValues?.get(1) }
        ?: ruta.nombre
    val frase = "Hola. Me he perdido. Voy a $destino. ¿Me puede ayudar, por favor?"

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Enseña esta pantalla a una persona del Metro.", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Card(
            border = BorderStroke(4.dp, MaterialTheme.colorScheme.onSurface),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
        ) {
            Text(
                "Hola. Me he perdido.\nVoy a $destino.\n¿Me puede ayudar, por favor?",
                style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(24.dp),
            )
        }
        if (voz.lista) BotonBorde("Leerlo en voz alta", { voz.decir(frase) }, Modifier.fillMaxWidth())
        Spacer(Modifier.weight(1f))
        BotonGrande("Volver a mi ruta", volver, fondo = LocalExtra.current.ok, letra = LocalExtra.current.okTexto)
    }
}

// ---------------------------------------------------------------
// Llegada
// ---------------------------------------------------------------
@Composable
fun LlegadaScreen(inicio: () -> Unit) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Spacer(Modifier.weight(1f))
        VisualPaso(Paso("", Icono.TRABAJO), tamano = 140.dp)
        Text("¡Has llegado!", style = MaterialTheme.typography.headlineMedium)
        Text("Lo has hecho tú.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.weight(1f))
        BotonGrande("Volver al inicio", inicio)
    }
}
