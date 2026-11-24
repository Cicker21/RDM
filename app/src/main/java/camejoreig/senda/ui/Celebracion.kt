package camejoreig.senda.ui

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import camejoreig.senda.ui.theme.LocalExtra
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/*
 * Celebración al llegar: confeti suave que va cayendo desde arriba durante un rato
 * («Corta» o «Larga», se elige en Ajustes) y al final se desvanece.
 *
 * Pensado para que anime sin agobiar:
 * - Cae despacio, con un ligero balanceo. Al terminar el tiempo elegido se desvanece en 1 segundo
 *   (el fundido va después, no lo acorta). Nada parpadea ni destella
 *   (regla de fotosensibilidad: nada cambia de brillo más de 3 veces por segundo).
 * - Usa los colores del tema elegido, así no aparecen colores nuevos de golpe.
 * - Se dibuja encima de la pantalla pero no tapa los botones: los toques pasan a través.
 * - Sin sonido. Solo una vibración corta al empezar.
 * - Si el móvil tiene las animaciones quitadas (accesibilidad), no hay confeti:
 *   la pantalla de llegada ya enseña el dibujo y el mensaje.
 *
 * No usa ninguna librería: se dibuja con Canvas de Compose.
 */

private const val FUNDIDO_MS = 1000 // desvanecerse al final, después del tiempo elegido
private const val CANTIDAD = 150   // trozos de confeti en total

/**
 * Un trozo de confeti. Empieza un poco por encima de la pantalla (cada uno a una altura distinta)
 * y cae a su propia velocidad. Al salir por abajo vuelve a entrar por arriba, en otro sitio,
 * mientras dure la celebración. Así siempre hay confeti a todas las alturas, sin huecos.
 */
private class Trozo(
    val x: Float,          // posición horizontal de la primera caída (0 = izquierda, 1 = derecha)
    val salida: Float,     // a qué altura por encima de la pantalla empieza (0 = justo arriba, 1 = una pantalla más arriba)
    val caidaMs: Int,      // cuánto tarda en cruzar la pantalla (su velocidad)
    val vaiven: Float,     // cuánto se balancea a los lados (en dp)
    val fase: Float,       // para que no se balanceen todos a la vez
    val giro: Float,       // vueltas que da mientras cae (como mucho una)
    val color: Int,        // cuál de los colores del tema usa
    val circulo: Boolean,  // círculo o rectángulo
)

/**
 * Confeti encima de la pantalla. Ponerlo al final de un Box, después del contenido.
 * [activa] = el ajuste «Celebrar al llegar»; [duracionMs] = cuánto rato cae (Corta 3 s, Larga 6 s).
 */
@Composable
fun Celebracion(activa: Boolean, duracionMs: Int, modifier: Modifier = Modifier) {
    if (!activa) return

    // Vibración corta al llegar (no necesita permisos)
    val vibracion = LocalHapticFeedback.current
    LaunchedEffect(Unit) { vibracion.performHapticFeedback(HapticFeedbackType.LongPress) }

    // Si el móvil tiene las animaciones desactivadas, no se dibuja confeti
    val contexto = LocalContext.current
    val hayAnimaciones = remember {
        Settings.Global.getFloat(contexto.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
    }
    if (!hayAnimaciones) return

    val colores = listOf(MaterialTheme.colorScheme.primary, LocalExtra.current.ok, LocalExtra.current.fondoIcono)
    val trozos = remember {
        List(CANTIDAD) {
            Trozo(
                x = Random.nextFloat(),
                salida = Random.nextFloat(),
                caidaMs = 2600 + Random.nextInt(1400),
                vaiven = 8f + Random.nextFloat() * 16f,
                fase = Random.nextFloat() * 2f * PI.toFloat(),
                giro = Random.nextFloat(),
                color = Random.nextInt(3),
                circulo = Random.nextBoolean(),
            )
        }
    }

    // Reloj de la animación en milisegundos: el tiempo elegido y, después, el fundido
    val total = duracionMs + FUNDIDO_MS
    val reloj = remember { Animatable(0f) }
    LaunchedEffect(Unit) { reloj.animateTo(total.toFloat(), tween(total, easing = LinearEasing)) }
    if (reloj.value >= total) return

    Canvas(modifier.fillMaxSize()) {
        val ms = reloj.value
        // Durante el tiempo elegido se ve del todo; en el último segundo se desvanece poco a poco
        val opacidad = if (ms > duracionMs) (total - ms) / FUNDIDO_MS else 1f
        val ancho = 8.dp.toPx()
        val alto = 5.dp.toPx()
        val radio = 3.5.dp.toPx()

        val margen = 20.dp.toPx()
        val recorrido = size.height + 2 * margen // de justo encima de la pantalla a justo debajo

        trozos.forEach { trozo ->
            val velocidad = recorrido / trozo.caidaMs // píxeles por milisegundo
            // Distancia caída desde la altura de salida (que está por encima de la pantalla)
            val distancia = ms * velocidad - trozo.salida * size.height
            if (distancia < 0f) {
                return@forEach // todavía por encima de la pantalla: no se ve
            }
            // Cuántas veces ha cruzado ya la pantalla, y cuánto lleva de la caída actual (0 a 1)
            val vuelta = (distancia / recorrido).toInt()
            val p = (distancia % recorrido) / recorrido
            // Pasado el tiempo elegido, los trozos que salen por abajo ya no vuelven a entrar
            if (vuelta > 0) {
                val empezoEstaVuelta = (vuelta * recorrido + trozo.salida * size.height) / velocidad
                if (empezoEstaVuelta > duracionMs) return@forEach
            }
            // En cada vuelta entra por un sitio distinto (desplazamiento con la proporción áurea)
            val xVuelta = (trozo.x + vuelta * 0.618034f) % 1f
            val x = xVuelta * size.width + sin(p * 3f * PI.toFloat() + trozo.fase) * trozo.vaiven.dp.toPx()
            val y = -margen + p * recorrido
            val color: Color = colores[trozo.color].copy(alpha = opacidad)

            if (trozo.circulo) {
                drawCircle(color, radio, Offset(x, y))
            } else {
                rotate(trozo.giro * 360f * p, pivot = Offset(x, y)) {
                    drawRect(color, topLeft = Offset(x - ancho / 2, y - alto / 2), size = Size(ancho, alto))
                }
            }
        }
    }
}
