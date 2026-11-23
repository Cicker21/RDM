package camejoreig.senda.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import camejoreig.senda.Voz
import camejoreig.senda.datos.Ajustes
import camejoreig.senda.datos.Icono
import camejoreig.senda.datos.Paso
import camejoreig.senda.datos.Repositorio
import camejoreig.senda.datos.Ruta
import camejoreig.senda.datos.TamanoLetra
import camejoreig.senda.ui.theme.LocalExtra
import camejoreig.senda.ui.theme.SendaTheme
import java.util.UUID

/*
 * AyudaScreen: tutoriales INTERACTIVOS para aprender a usar Senda.
 *
 * Primero se ve un menú con las prácticas. Al abrir una, la persona hace las cosas de verdad,
 * paso a paso:
 *   - Arriba siempre hay una INSTRUCCIÓN corta (se puede escuchar).
 *   - Debajo aparece solo lo necesario para ese paso, y el botón que hay que tocar
 *     parpadea con un borde (resaltado).
 *   - No se avanza hasta que la persona hace lo que se le pide.
 *
 * Prácticas:
 *   1. Hacer un viaje   -> viaje de prueba de 3 pasos, incluido "Me he perdido".
 *   2. Crear una ruta   -> crea una ruta corta de 2 pasos; al final decide si guardarla.
 *   3. Letra y colores  -> prueba tamaños y contraste; al final decide si guardarlos.
 */

// ---------------------------------------------------------------
// Menú de prácticas
// ---------------------------------------------------------------

private enum class Practica(val titulo: String, val detalle: String, val dibujo: ImageVector) {
    VIAJE("Hacer un viaje", "Practica un viaje de prueba", Icono.TREN.vector()),
    CREAR("Crear una ruta", "Mejor con tu persona de apoyo", Icons.Filled.Add),
    AJUSTES("Letra y colores", "Elige cómo ves mejor la app", Icons.Filled.FormatSize),
}

@Composable
fun AyudaScreen(repo: Repositorio, voz: Voz, volver: () -> Unit) {
    var abierta by remember { mutableStateOf<Practica?>(null) }
    val actual = abierta

    if (actual == null) {
        MenuPracticas(abrir = { abierta = it }, volver = volver)
        return
    }
    // El botón "atrás" del móvil vuelve al menú de prácticas, no a la pantalla de inicio
    BackHandler { abierta = null }
    val cerrar = { abierta = null }
    when (actual) {
        Practica.VIAJE -> PracticaViaje(repo, voz, cerrar)
        Practica.CREAR -> PracticaCrearRuta(repo, voz, cerrar)
        Practica.AJUSTES -> PracticaAjustes(repo, voz, cerrar)
    }
}

@Composable
private fun MenuPracticas(abrir: (Practica) -> Unit, volver: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Ayuda", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
            BotonBorde("Volver", volver)
        }
        Text("¿Qué quieres practicar?", style = MaterialTheme.typography.titleLarge)
        Practica.entries.forEach { p ->
            OutlinedButton(
                onClick = { abrir(p) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 96.dp),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Dibujo(p.dibujo, 40.dp)
                    Spacer(Modifier.size(16.dp))
                    Column {
                        Text(p.titulo, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                        Text(p.detalle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------
// Piezas comunes de las prácticas
// ---------------------------------------------------------------

/**
 * Marco de cada práctica: botón para salir, la instrucción arriba y el contenido debajo.
 * Si el usuario eligió lectura automática, la instrucción se lee cada vez que cambia.
 */
@Composable
private fun MarcoPractica(
    titulo: String,
    instruccion: String,
    voz: Voz,
    ajustes: Ajustes,
    salir: () -> Unit,
    contenido: @Composable () -> Unit,
) {
    LaunchedEffect(instruccion, voz.lista) {
        if (ajustes.leerEnVozAlta) voz.decir(instruccion)
    }
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(titulo, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            BotonBorde("Salir", salir)
        }
        // Instrucción: siempre en el mismo sitio y con el mismo color, para que se reconozca
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.primary).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                instruccion, style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.weight(1f),
            )
            if (voz.lista) {
                IconButton(onClick = { voz.decir(instruccion) }) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Escuchar", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) { contenido() }
    }
}

/** Borde que parpadea alrededor del botón que hay que tocar. */
@Composable
private fun Modifier.resaltado(): Modifier {
    val anim = rememberInfiniteTransition(label = "resaltado")
    val alfa by anim.animateFloat(
        initialValue = 0.25f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "alfa",
    )
    return this
        .border(4.dp, MaterialTheme.colorScheme.primary.copy(alpha = alfa), RoundedCornerShape(20.dp))
        .padding(6.dp)
}

/** Pictograma dentro de un recuadro de color suave. */
@Composable
private fun Dibujo(icono: ImageVector, tamano: androidx.compose.ui.unit.Dp) {
    Box(
        Modifier.clip(RoundedCornerShape(16.dp)).background(LocalExtra.current.fondoIcono),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(12.dp).size(tamano))
    }
}

/** Pantalla final de una práctica. */
@Composable
private fun Felicitacion(texto: String) {
    Spacer(Modifier.size(24.dp))
    Text("¡Muy bien!", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    Text(texto, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
}

// ---------------------------------------------------------------
// Práctica 1: hacer un viaje de prueba
// ---------------------------------------------------------------

/** Ruta de prueba genérica (no es una ruta real de Metro). */
private val PASOS_PRUEBA = listOf(
    Paso("Camina hasta la entrada del Metro.", Icono.CAMINAR),
    Paso("Pasa el torno con tu tarjeta.", Icono.TORNO),
    Paso("Sube al tren y bájate en tu parada.", Icono.TREN),
)

@Composable
private fun PracticaViaje(repo: Repositorio, voz: Voz, salir: () -> Unit) {
    val ajustes = repo.ajustes ?: Ajustes()
    val extra = LocalExtra.current
    var empezado by remember { mutableStateOf(false) }
    var i by remember { mutableIntStateOf(0) }           // paso del viaje de prueba
    var perdido by remember { mutableStateOf(false) }    // se está viendo "Me he perdido"
    var practicoPerdido by remember { mutableStateOf(false) }
    var terminado by remember { mutableStateOf(false) }
    val ultimo = i == PASOS_PRUEBA.lastIndex
    // En el paso 2 se practica "Me he perdido" antes de seguir
    val tocaPerdido = i == 1 && !practicoPerdido

    val instruccion = when {
        terminado -> "Has terminado la práctica."
        !empezado -> "Vamos a practicar un viaje. Es de prueba: no tienes que moverte. Toca «Empezar»."
        perdido -> "Aquí eliges qué hacer. Ahora toca «Ya estoy bien, seguir»."
        tocaPerdido -> "Imagina que te has perdido. Toca el botón naranja «Me he perdido»."
        ultimo -> "Es el último paso. Toca «He llegado»."
        i == 0 -> "Mira el dibujo y lee la frase. Cuando lo hayas hecho, toca «Hecho»."
        else -> "Muy bien. Ahora toca «Hecho» para seguir."
    }

    MarcoPractica("Hacer un viaje", instruccion, voz, ajustes, salir) {
        when {
            terminado -> {
                Felicitacion("Ya sabes hacer un viaje con Senda.")
                BotonGrande("Terminar", salir, fondo = extra.ok, letra = extra.okTexto)
            }
            !empezado -> {
                Dibujo(Icono.TREN.vector(), 120.dp)
                BotonGrande("Empezar", { empezado = true }, Modifier.resaltado())
            }
            perdido -> {
                // Versión simplificada de la pantalla "Me he perdido"
                Text("No pasa nada. Para un momento y respira.", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Text(
                    "Desde aquí puedes volver al paso anterior, pedir ayuda al personal del Metro o llamar a tu contacto.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                BotonGrande(
                    "Ya estoy bien, seguir",
                    { perdido = false; practicoPerdido = true },
                    Modifier.resaltado(), fondo = extra.ok, letra = extra.okTexto,
                )
            }
            else -> {
                // Versión simplificada de la pantalla de viaje
                val paso = PASOS_PRUEBA[i]
                Text("Paso ${i + 1} de ${PASOS_PRUEBA.size}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                VisualPaso(paso, tamano = 100.dp, modifier = Modifier.fillMaxWidth())
                Text(paso.texto, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                // Solo funciona el botón que toca en cada momento (el resaltado)
                BotonGrande(
                    texto = if (ultimo) "He llegado ✓" else "Hecho ✓",
                    onClick = {
                        if (!tocaPerdido) {
                            if (ultimo) terminado = true else i++
                        }
                    },
                    modifier = if (tocaPerdido) Modifier else Modifier.resaltado(),
                    fondo = extra.ok, letra = extra.okTexto,
                )
                BotonGrande(
                    "Me he perdido",
                    { if (tocaPerdido) perdido = true },
                    if (tocaPerdido) Modifier.resaltado() else Modifier,
                    fondo = extra.ayuda, letra = extra.ayudaTexto,
                )
            }
        }
    }
}

// ---------------------------------------------------------------
// Práctica 2: crear una ruta corta
// ---------------------------------------------------------------

private const val PASOS_RUTA_PRACTICA = 2

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PracticaCrearRuta(repo: Repositorio, voz: Voz, salir: () -> Unit) {
    val ajustes = repo.ajustes ?: Ajustes()
    val extra = LocalExtra.current
    // Fases: 0 nueva ruta, 1 nombre, 2 texto del paso, 3 dibujo, 4 añadir otro paso, 5 guardar, 6 fin
    var fase by remember { mutableIntStateOf(0) }
    var nombre by remember { mutableStateOf("") }
    var texto by remember { mutableStateOf("") }
    var icono by remember { mutableStateOf<Icono?>(null) }
    val pasos = remember { mutableStateListOf<Paso>() }
    val numPaso = pasos.size + 1

    val instruccion = when (fase) {
        0 -> "Vamos a crear una ruta corta, de $PASOS_RUTA_PRACTICA pasos. Toca «+ Nueva ruta»."
        1 -> "Escribe un nombre para la ruta. Por ejemplo: «A la panadería». Después toca «Siguiente»."
        2 -> "Escribe el paso $numPaso con una frase corta. Después toca «Siguiente»."
        3 -> "Elige un dibujo para el paso $numPaso."
        4 -> "¡Bien! Ya tienes un paso. Toca «+ Añadir paso» para escribir el siguiente."
        5 -> "Ya tienes $PASOS_RUTA_PRACTICA pasos. Toca «Guardar ruta»."
        else -> "Has terminado la práctica."
    }

    MarcoPractica("Crear una ruta", instruccion, voz, ajustes, salir) {
        // Resumen de lo que ya se ha hecho (a partir de la fase 2)
        if (fase in 2..5) {
            Text("Ruta: ${nombre.trim()}", style = MaterialTheme.typography.titleLarge)
            pasos.forEachIndexed { k, p ->
                Tarjeta {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        VisualPaso(p, tamano = 36.dp)
                        Spacer(Modifier.size(12.dp))
                        Text("${k + 1}. ${p.texto}", modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        when (fase) {
            0 -> BotonGrande("+ Nueva ruta", { fase = 1 }, Modifier.resaltado())
            1 -> {
                OutlinedTextField(
                    nombre, { nombre = it }, label = { Text("Nombre de la ruta") },
                    singleLine = true, modifier = Modifier.fillMaxWidth().resaltado(),
                )
                BotonGrande("Siguiente", { fase = 2 }, enabled = nombre.isNotBlank())
            }
            2 -> {
                OutlinedTextField(
                    texto, { texto = it }, label = { Text("Paso $numPaso") },
                    placeholder = { Text(if (numPaso == 1) "Ej.: Sal de casa y gira a la derecha." else "Ej.: Entra en la tienda azul.") },
                    minLines = 2, modifier = Modifier.fillMaxWidth().resaltado(),
                )
                BotonGrande("Siguiente", { fase = 3 }, enabled = texto.isNotBlank())
            }
            3 -> {
                Text(texto.trim(), style = MaterialTheme.typography.bodyLarge)
                FlowRow(
                    Modifier.resaltado(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icono.entries.forEach { ic ->
                        BotonOpcion(
                            elegido = icono == ic, onClick = { icono = ic },
                            modifier = Modifier.size(72.dp), relleno = PaddingValues(0.dp),
                        ) {
                            Icon(ic.vector(), contentDescription = ic.etiqueta, modifier = Modifier.size(36.dp))
                        }
                    }
                }
                BotonGrande("Siguiente", {
                    pasos.add(Paso(texto.trim(), icono ?: Icono.CAMINAR))
                    texto = ""; icono = null
                    fase = if (pasos.size < PASOS_RUTA_PRACTICA) 4 else 5
                }, enabled = icono != null)
            }
            4 -> BotonBorde("+ Añadir paso", { fase = 2 }, Modifier.fillMaxWidth().resaltado())
            5 -> BotonGrande("Guardar ruta", { fase = 6 }, Modifier.resaltado(), fondo = extra.ok, letra = extra.okTexto)
            else -> {
                Felicitacion("Has creado la ruta «${nombre.trim()}» con ${pasos.size} pasos.")
                Text("¿Quieres quedártela en tus rutas?", style = MaterialTheme.typography.titleLarge)
                BotonGrande("Sí, guardarla", {
                    repo.guardarRuta(Ruta(id = "r" + UUID.randomUUID(), nombre = nombre.trim(), pasos = pasos.toList()))
                    salir()
                }, fondo = extra.ok, letra = extra.okTexto)
                BotonBorde("No, era solo práctica", salir, Modifier.fillMaxWidth())
            }
        }
    }
}

// ---------------------------------------------------------------
// Práctica 3: letra y colores
// ---------------------------------------------------------------

@Composable
private fun PracticaAjustes(repo: Repositorio, voz: Voz, salir: () -> Unit) {
    val guardados = repo.ajustes ?: Ajustes()
    var borrador by remember { mutableStateOf(guardados) }
    // Fases: 0 letra, 1 contraste, 2 guardar
    var fase by remember { mutableIntStateOf(0) }

    val instruccion = when (fase) {
        0 -> "Toca el tamaño de letra que lees mejor."
        1 -> "Mira cómo ha cambiado. Ahora toca los colores que ves mejor."
        else -> "¿Te gusta así? Toca «Guardar» para quedártelo."
    }

    // La práctica se dibuja con el borrador, así se ve el cambio al momento
    SendaTheme(ajustes = borrador) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            MarcoPractica("Letra y colores", instruccion, voz, borrador, salir) {
                when (fase) {
                    0 -> Column(Modifier.resaltado(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        TamanoLetra.entries.forEach { t ->
                            BotonOpcion(
                                elegido = borrador.tamanoLetra == t,
                                onClick = { borrador = borrador.copy(tamanoLetra = t); fase = 1 },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(t.etiqueta, fontSize = (20 * t.escala).sp, modifier = Modifier.weight(1f))
                                Text("Aa", fontSize = (28 * t.escala).sp)
                            }
                        }
                    }
                    1 -> Column(Modifier.resaltado(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        BotonOpcion(
                            elegido = !borrador.altoContraste,
                            onClick = { borrador = borrador.copy(altoContraste = false); fase = 2 },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Colores suaves", style = MaterialTheme.typography.labelLarge) }
                        BotonOpcion(
                            elegido = borrador.altoContraste,
                            onClick = { borrador = borrador.copy(altoContraste = true); fase = 2 },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Más contraste", style = MaterialTheme.typography.labelLarge) }
                    }
                    else -> {
                        Text("Así se verá Senda.", style = MaterialTheme.typography.headlineSmall)
                        BotonGrande("Guardar", {
                            repo.guardarAjustes(borrador)
                            salir()
                        }, Modifier.resaltado(), fondo = LocalExtra.current.ok, letra = LocalExtra.current.okTexto)
                        BotonBorde("Probar otra vez", { fase = 0 }, Modifier.fillMaxWidth())
                        BotonBorde("Salir sin guardar", salir, Modifier.fillMaxWidth())
                        Text(
                            "La voz y el contacto se cambian en «Ajustes», desde el inicio.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
