package camejoreig.senda.ui

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
import camejoreig.senda.ui.theme.SendaTheme

/*
 * GuiaScreen: formulario sencillo que adapta Senda a la persona.
 * - Aparece la primera vez que se abre la app (no hay ajustes guardados).
 * - AjustesScreen reutiliza este mismo formulario para cambiar lo elegido.
 *
 * Una pregunta por pantalla, con respuestas en botones grandes. Todas las preguntas
 * tienen una respuesta por defecto, así que el formulario siempre se puede completar.
 * Los cambios se ven al momento (letra, contraste) para que la persona elija viéndolo.
 */

private const val TOTAL_PREGUNTAS = 5

@Composable
fun GuiaScreen(
    inicial: Ajustes,
    voz: Voz,
    esPrimeraVez: Boolean,
    guardar: (Ajustes) -> Unit,
    cancelar: (() -> Unit)? = null, // null en la primera vez: no hay a dónde volver
) {
    var borrador by remember { mutableStateOf(inicial) }
    // Página 0 = bienvenida (solo la primera vez); 1..TOTAL_PREGUNTAS = preguntas
    var pagina by remember { mutableIntStateOf(if (esPrimeraVez) 0 else 1) }

    // El tema de la guía usa el borrador, así se ve el efecto de cada respuesta al instante
    SendaTheme(ajustes = borrador) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(Modifier.safeDrawingPadding().padding(16.dp)) {
                Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (pagina > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Pregunta $pagina de $TOTAL_PREGUNTAS",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f),
                            )
                            if (cancelar != null) BotonBorde("Cancelar", cancelar)
                        }
                    }

                    // Contenido de la página actual (se puede desplazar si la letra es muy grande)
                    Column(
                        Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        when (pagina) {
                            0 -> Bienvenida()
                            1 -> PreguntaLetra(borrador.tamanoLetra) { borrador = borrador.copy(tamanoLetra = it) }
                            2 -> PreguntaContraste(borrador.altoContraste) { borrador = borrador.copy(altoContraste = it) }
                            3 -> PreguntaDibujos(borrador.dibujosGrandes) { borrador = borrador.copy(dibujosGrandes = it) }
                            4 -> PreguntaVoz(borrador.leerEnVozAlta, voz) { borrador = borrador.copy(leerEnVozAlta = it) }
                            else -> PreguntaContacto(borrador.contacto) { borrador = borrador.copy(contacto = it) }
                        }
                    }

                    // Botones para moverse por la guía
                    val ultima = pagina == TOTAL_PREGUNTAS
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
private fun Bienvenida() {
    Spacer(Modifier.size(24.dp))
    VisualPaso(Paso("", Icono.ANDEN), tamano = 120.dp, modifier = Modifier.fillMaxWidth())
    Text("Hola. Soy Senda.", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    Text(
        "Te ayudo a moverte en Metro paso a paso. Antes vamos a preparar la app para ti con 5 preguntas.",
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
