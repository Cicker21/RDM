package camejoreig.senda.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import camejoreig.senda.Voz
import camejoreig.senda.datos.Ajustes
import camejoreig.senda.datos.AperturaApoyo
import camejoreig.senda.datos.Contacto
import camejoreig.senda.datos.ModoColor
import camejoreig.senda.datos.Repositorio
import camejoreig.senda.datos.TamanoLetra
import camejoreig.senda.datos.TemaColor
import camejoreig.senda.ui.theme.esquema
import camejoreig.senda.ui.theme.fondoIcono

/*
 * AjustesScreen: una ventana de ajustes normal, con todo a la vista y agrupado.
 * (La primera vez se usa GuiaScreen, que va pregunta a pregunta.)
 *
 * - Cada cambio se guarda solo al momento, como en los ajustes de Android: no hay botón «Guardar».
 *   Como la app entera usa los ajustes guardados, el cambio (letra, colores...) se ve enseguida.
 * - El nombre y el teléfono se guardan al salir del campo o al salir de la pantalla,
 *   para no guardar a cada letra.
 * - Arriba, fijo, «Inicio» para salir. El botón atrás del móvil también sale.
 */

@Composable
fun AjustesScreen(repo: Repositorio, voz: Voz, volver: () -> Unit) {
    var a by remember { mutableStateOf(repo.ajustes ?: Ajustes()) }

    /** Cambia un ajuste y lo guarda en el momento. */
    fun cambiar(nuevo: Ajustes) {
        a = nuevo
        val c = nuevo.contacto
        repo.guardarAjustes(nuevo.copy(contacto = Contacto(c.nombre.trim(), c.telefono.trim())))
    }

    // Al salir de la pantalla se guarda lo último (por si se estaba escribiendo el contacto)
    val ultimo by rememberUpdatedState(a)
    DisposableEffect(Unit) {
        onDispose {
            val c = ultimo.contacto
            repo.guardarAjustes(ultimo.copy(contacto = Contacto(c.nombre.trim(), c.telefono.trim())))
        }
    }

    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // ----- Barra fija de arriba -----
        Row(verticalAlignment = Alignment.CenterVertically) {
            BotonIcono("Inicio", Icons.Filled.Home, volver)
            Spacer(Modifier.size(16.dp))
            Text("Ajustes", style = MaterialTheme.typography.headlineMedium)
        }

        // ----- Ajustes (lo único que se desplaza) -----
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TituloSeccion("Leer")
            Tarjeta {
                Etiqueta("Tamaño de la letra")
                Opciones(TamanoLetra.entries, a.tamanoLetra, porFila = 3, elegir = { cambiar(a.copy(tamanoLetra = it)) }) { t ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Aa", fontSize = (24 * t.escala).sp)
                        TextoOpcion(t.etiqueta)
                    }
                }
                Interruptor("Leer cada paso en voz alta", a.leerEnVozAlta) {
                    cambiar(a.copy(leerEnVozAlta = it))
                    if (it) voz.decir("Así te leeré cada paso.")
                }
                if (!voz.lista) {
                    Text(
                        "Este móvil no tiene voz en español. Se puede instalar en los ajustes de Android (Texto a voz).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            TituloSeccion("Pantalla")
            Tarjeta {
                Etiqueta("Colores")
                Opciones(TemaColor.entries, a.tema, porFila = 2, elegir = { cambiar(a.copy(tema = it)) }) { t ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Muestra(t, elegida = t == a.tema)
                        TextoOpcion(t.etiqueta)
                    }
                }
                Etiqueta("Claro u oscuro")
                Opciones(ModoColor.entries, a.modo, porFila = 3, elegir = { cambiar(a.copy(modo = it)) }) { m -> TextoOpcion(m.etiqueta) }
                Interruptor("Más contraste", a.altoContraste) { cambiar(a.copy(altoContraste = it)) }
                Interruptor("Dibujos grandes en el viaje", a.dibujosGrandes) { cambiar(a.copy(dibujosGrandes = it)) }
            }

            TituloSeccion("Persona de ayuda")
            Tarjeta {
                Text(
                    "A quién avisar si te pierdes. Solo se le avisa si tú lo decides.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                // Se guarda al salir del campo (cuando pierde el foco)
                val guardarAlSalir = Modifier.fillMaxWidth().onFocusChanged { if (!it.isFocused) cambiar(a) }
                OutlinedTextField(
                    value = a.contacto.nombre, onValueChange = { a = a.copy(contacto = a.contacto.copy(nombre = it)) },
                    label = { Text("Nombre") }, placeholder = { Text("Ej.: Mamá") },
                    singleLine = true, modifier = guardarAlSalir,
                )
                OutlinedTextField(
                    value = a.contacto.telefono, onValueChange = { a = a.copy(contacto = a.contacto.copy(telefono = it)) },
                    label = { Text("Teléfono") }, placeholder = { Text("Ej.: +34 600 000 000") },
                    singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = guardarAlSalir,
                )
                Text(
                    "Pon el prefijo +34 para que funcione WhatsApp.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            TituloSeccion("Botón «Apoyo»")
            Tarjeta {
                Text(
                    "Cómo se abre desde el inicio. Si hay que mantenerlo pulsado, es más difícil entrar sin querer.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Opciones(AperturaApoyo.entries, a.aperturaApoyo, porFila = 2, elegir = { cambiar(a.copy(aperturaApoyo = it)) }) { o ->
                    TextoOpcion(o.etiqueta)
                }
            }
        }
    }
}

/** Título pequeño encima de cada grupo de ajustes. */
@Composable
private fun TituloSeccion(texto: String) {
    Text(texto, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 8.dp))
}

/** Nombre de un ajuste dentro de la tarjeta. */
@Composable
private fun Etiqueta(texto: String) {
    Text(texto, style = MaterialTheme.typography.bodyLarge)
}

/** Texto centrado dentro de una opción. */
@Composable
private fun TextoOpcion(texto: String) {
    Text(texto, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
}

/**
 * Ajuste de sí o no: el texto a la izquierda y, a la derecha, «Sí» o «No» escrito
 * junto al interruptor, para que no haga falta saber leer el interruptor.
 */
@Composable
private fun Interruptor(texto: String, activo: Boolean, cambiar: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(texto, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(
            if (activo) "Sí" else "No",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        Switch(checked = activo, onCheckedChange = cambiar)
    }
}

/** Opciones de un ajuste como botones grandes, [porFila] en cada fila. La elegida lleva borde grueso. */
@Composable
private fun <T> Opciones(
    lista: List<T>,
    elegida: T,
    porFila: Int,
    elegir: (T) -> Unit,
    contenido: @Composable (T) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        lista.chunked(porFila).forEach { fila ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                fila.forEach { op ->
                    BotonOpcion(
                        elegido = op == elegida, onClick = { elegir(op) },
                        modifier = Modifier.weight(1f), relleno = PaddingValues(8.dp),
                    ) { contenido(op) }
                }
                // Si la fila no está llena, se deja el hueco para que los botones no se estiren
                repeat(porFila - fila.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

/** Muestra de un tema: círculo pastel con el color de sus botones; la elegida lleva un visto. */
@Composable
private fun Muestra(t: TemaColor, elegida: Boolean) {
    Box(
        Modifier.size(40.dp).clip(CircleShape).background(fondoIcono(t, oscuro = false)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.size(24.dp).clip(CircleShape).background(esquema(t, oscuro = false).primary),
            contentAlignment = Alignment.Center,
        ) {
            if (elegida) Icon(Icons.Filled.Check, contentDescription = "Elegido", tint = esquema(t, oscuro = false).onPrimary, modifier = Modifier.size(18.dp))
        }
    }
}
