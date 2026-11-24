package camejoreig.senda.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import camejoreig.senda.Voz
import camejoreig.senda.datos.Ajustes
import camejoreig.senda.datos.Contacto
import camejoreig.senda.datos.Icono
import camejoreig.senda.datos.Paso
import camejoreig.senda.datos.TamanoLetra
import camejoreig.senda.datos.TemaColor
import camejoreig.senda.ui.theme.SendaTheme
import camejoreig.senda.ui.theme.esquema
import camejoreig.senda.ui.theme.fondoIcono

/*
 * GuiaScreen: formulario sencillo que adapta Senda a la persona.
 * - Aparece la primera vez que se abre la app (no hay ajustes guardados).
 * - Después, todo se cambia en AjustesScreen (una ventana con todos los ajustes juntos).
 *
 * Una pregunta por pantalla, con respuestas en botones grandes. Todas las preguntas
 * tienen una respuesta por defecto, así que el formulario siempre se puede completar.
 * Los cambios se ven al momento (letra, contraste) para que la persona elija viéndolo.
 */

/** Todas las preguntas, en el orden en que salen. */
private enum class Pregunta { LETRA, CONTRASTE, COLORES, DIBUJOS, VOZ, CONTACTO }

/** Preguntas de la guía, en orden. Lo demás (claro u oscuro, botón Apoyo) se cambia en Ajustes. */
private val PREGUNTAS_GUIA = listOf(
    Pregunta.LETRA, Pregunta.CONTRASTE, Pregunta.COLORES, Pregunta.DIBUJOS, Pregunta.VOZ, Pregunta.CONTACTO,
)


@Composable
fun GuiaScreen(
    inicial: Ajustes,
    voz: Voz,
    guardar: (Ajustes) -> Unit,
) {
    var borrador by remember { mutableStateOf(inicial) }
    val preguntas = PREGUNTAS_GUIA
    val totalPreguntas = preguntas.size
    // Página 0 = bienvenida (solo la primera vez); 1..totalPreguntas = preguntas
    var pagina by remember { mutableIntStateOf(0) }

    // El tema de la guía usa el borrador, así se ve el efecto de cada respuesta al instante
    SendaTheme(ajustes = borrador) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(Modifier.safeDrawingPadding().padding(16.dp)) {
                Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (pagina > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Pregunta $pagina de $totalPreguntas",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    // Contenido de la página actual (se puede desplazar si la letra es muy grande)
                    Column(
                        Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        if (pagina == 0) {
                            Bienvenida(totalPreguntas)
                        } else when (preguntas[pagina - 1]) {
                            Pregunta.LETRA -> PreguntaLetra(borrador.tamanoLetra) { borrador = borrador.copy(tamanoLetra = it) }
                            Pregunta.CONTRASTE -> PreguntaContraste(borrador.altoContraste) { borrador = borrador.copy(altoContraste = it) }
                            Pregunta.COLORES -> PreguntaColores(borrador.tema) { borrador = borrador.copy(tema = it) }
                            Pregunta.DIBUJOS -> PreguntaDibujos(borrador.dibujosGrandes) { borrador = borrador.copy(dibujosGrandes = it) }
                            Pregunta.VOZ -> PreguntaVoz(borrador.leerEnVozAlta, voz) { borrador = borrador.copy(leerEnVozAlta = it) }
                            Pregunta.CONTACTO -> PreguntaContacto(borrador.contacto) { borrador = borrador.copy(contacto = it) }
                        }
                    }

                    // Botones para moverse por la guía
                    val ultima = pagina == totalPreguntas
                    BotonGrande(
                        texto = when {
                            pagina == 0 -> "Empezar"
                            ultima -> "Guardar"
                            else -> "Siguiente"
                        },
                        onClick = {
                            if (ultima) {
                                val c = borrador.contacto
                                guardar(borrador.copy(contacto = Contacto(c.nombre.trim(), c.telefono.trim())))
                            } else {
                                pagina++
                            }
                        },
                    )
                    if (pagina > 1) BotonBorde("← Atrás", { pagina-- }, Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun Bienvenida(totalPreguntas: Int) {
    Spacer(Modifier.size(24.dp))
    VisualPaso(Paso("", Icono.ANDEN), tamano = 120.dp, modifier = Modifier.fillMaxWidth())
    Text("Hola. Soy Senda.", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    Text(
        "Te ayudo a moverte en Metro paso a paso. Antes vamos a preparar la app para ti con $totalPreguntas preguntas.",
        style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
    )
    Text(
        "Puedes hacerlo tú solo o con alguien de confianza. Lo podrás cambiar después en Ajustes.",
        color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun PreguntaLetra(actual: TamanoLetra, elegir: (TamanoLetra) -> Unit) {
    Text("¿Cómo lees mejor las letras?", style = MaterialTheme.typography.headlineSmall)
    TamanoLetra.entries.forEach { t ->
        BotonOpcion(elegido = actual == t, onClick = { elegir(t) }, modifier = Modifier.fillMaxWidth()) {
            // Cada opción se escribe con su propio tamaño para que se vea la diferencia
            Text(t.etiqueta, fontSize = (20 * t.escala).sp, modifier = Modifier.weight(1f))
            Text("Aa", fontSize = (28 * t.escala).sp)
        }
    }
}

@Composable
private fun PreguntaContraste(actual: Boolean, elegir: (Boolean) -> Unit) {
    Text("¿Qué colores ves mejor?", style = MaterialTheme.typography.headlineSmall)
    BotonOpcion(elegido = !actual, onClick = { elegir(false) }, modifier = Modifier.fillMaxWidth()) {
        Text("Colores suaves", style = MaterialTheme.typography.labelLarge)
    }
    BotonOpcion(elegido = actual, onClick = { elegir(true) }, modifier = Modifier.fillMaxWidth()) {
        Text("Más contraste", style = MaterialTheme.typography.labelLarge)
    }
    Text("Mira cómo cambia la pantalla al tocar cada opción.", color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun PreguntaDibujos(actual: Boolean, elegir: (Boolean) -> Unit) {
    Text("¿Qué te ayuda más a entender cada paso?", style = MaterialTheme.typography.headlineSmall)
    BotonOpcion(elegido = !actual, onClick = { elegir(false) }, modifier = Modifier.fillMaxWidth()) {
        Text("Leer la frase", style = MaterialTheme.typography.labelLarge)
    }
    BotonOpcion(elegido = actual, onClick = { elegir(true) }, modifier = Modifier.fillMaxWidth()) {
        Icon(Icono.TREN.vector(), contentDescription = null, modifier = Modifier.size(40.dp))
        Spacer(Modifier.size(12.dp))
        Text("Ver dibujos grandes", style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun PreguntaVoz(actual: Boolean, voz: Voz, elegir: (Boolean) -> Unit) {
    Text("¿Quieres que Senda te lea cada paso en voz alta?", style = MaterialTheme.typography.headlineSmall)
    BotonOpcion(elegido = actual, onClick = { elegir(true); voz.decir("Así te leeré cada paso.") }, modifier = Modifier.fillMaxWidth()) {
        Text("Sí, léemelo", style = MaterialTheme.typography.labelLarge)
    }
    BotonOpcion(elegido = !actual, onClick = { elegir(false) }, modifier = Modifier.fillMaxWidth()) {
        Text("No, gracias", style = MaterialTheme.typography.labelLarge)
    }
    if (!voz.lista) {
        Text(
            "Ahora mismo el móvil no tiene voz en español. Se puede instalar en los ajustes de Android (Texto a voz).",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PreguntaContacto(actual: Contacto, cambiar: (Contacto) -> Unit) {
    Text("¿A quién quieres poder avisar si te pierdes?", style = MaterialTheme.typography.headlineSmall)
    Text(
        "Solo se le avisa si tú lo decides. Puedes dejarlo vacío.",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    OutlinedTextField(
        value = actual.nombre, onValueChange = { cambiar(actual.copy(nombre = it)) },
        label = { Text("Nombre") }, placeholder = { Text("Ej.: Mamá") },
        singleLine = true, modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = actual.telefono, onValueChange = { cambiar(actual.copy(telefono = it)) },
        label = { Text("Teléfono") }, placeholder = { Text("Ej.: +34 600 000 000") },
        singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        modifier = Modifier.fillMaxWidth(),
    )
    Text("Pon el prefijo +34 para que funcione WhatsApp.", color = MaterialTheme.colorScheme.onSurfaceVariant)
}


/**
 * ¿Qué colores te gustan más? Cada opción lleva una muestra de su tema (el fondo de los dibujos
 * y el color de los botones). Al tocarla, toda la pantalla cambia para verlo al momento.
 */
@Composable
private fun PreguntaColores(actual: TemaColor, elegir: (TemaColor) -> Unit) {
    Text("¿Qué colores te gustan más?", style = MaterialTheme.typography.headlineSmall)
    TemaColor.entries.forEach { t ->
        BotonOpcion(elegido = actual == t, onClick = { elegir(t) }, modifier = Modifier.fillMaxWidth()) {
            // Muestra: un círculo pastel con el color de los botones dentro
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(fondoIcono(t, oscuro = false)),
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.size(20.dp).clip(CircleShape).background(esquema(t, oscuro = false).primary))
            }
            Spacer(Modifier.size(12.dp))
            Text(t.etiqueta, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
        }
    }
}

