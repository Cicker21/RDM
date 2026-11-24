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
private const val CANTIDAD = 150   // trozos de confeti a la vez

/**
 * Un trozo de confeti. Cae de arriba abajo una y otra vez hasta que se acaba el tiempo;
 * al llegar abajo vuelve a empezar por encima de la pantalla, donde no se ve.
 */
private class Trozo(
    val x: Float,          // posición horizontal de salida (0 = izquierda, 1 = derecha)
    val retrasoMs: Int,    // cuándo empieza a caer la primera vez
    val caidaMs: Int,      // cuánto tarda en cruzar la pantalla
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
                retrasoMs = Random.nextInt(1500),
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

        trozos.forEach { trozo ->
            val desdeQueEmpezo = ms - trozo.retrasoMs
            if (desdeQueEmpezo < 0f) return@forEach
            // Avance dentro de la caída actual (0 = arriba, 1 = abajo); al llegar abajo vuelve a empezar
            val p = (desdeQueEmpezo % trozo.caidaMs) / trozo.caidaMs
            val x = trozo.x * size.width + sin(p * 3f * PI.toFloat() + trozo.fase) * trozo.vaiven.dp.toPx()
            val y = -20.dp.toPx() + p * (size.height + 40.dp.toPx())
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
