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
 * Celebración al llegar: confeti suave que cae desde arriba durante unos segundos.
 *
 * Pensado para que anime sin agobiar:
 * - Cae despacio, con un ligero balanceo, y al final se desvanece. Nada parpadea ni destella
 *   (regla de fotosensibilidad: nada cambia de brillo más de 3 veces por segundo).
 * - Usa los colores del tema elegido, así no aparecen colores nuevos de golpe.
 * - Se dibuja encima de la pantalla pero no tapa los botones: los toques pasan a través.
 * - Sin sonido. Solo una vibración corta al empezar.
 * - Si el móvil tiene las animaciones quitadas (accesibilidad), no hay confeti:
 *   la pantalla de llegada ya enseña el dibujo y el mensaje.
 *
 * No usa ninguna librería: se dibuja con Canvas de Compose.
 */

private const val DURACION_MS = 3500
private const val CANTIDAD = 70

/** Un trozo de confeti. Las posiciones y tiempos van de 0 a 1 (proporción de la pantalla o del tiempo). */
private class Trozo(
    val x: Float,          // posición horizontal de salida
    val retraso: Float,    // cuándo empieza a caer
    val velocidad: Float,  // qué parte de la pantalla recorre
    val vaiven: Float,     // cuánto se balancea a los lados (en dp)
    val fase: Float,       // para que no se balanceen todos a la vez
    val giro: Float,       // vueltas que da mientras cae (como mucho una)
    val color: Int,        // cuál de los colores del tema usa
    val circulo: Boolean,  // círculo o rectángulo
)

/**
 * Confeti encima de la pantalla. Ponerlo al final de un Box, después del contenido.
 * [activa] = el ajuste «Celebrar al llegar».
 */
@Composable
fun Celebracion(activa: Boolean, modifier: Modifier = Modifier) {
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
                retraso = Random.nextFloat() * 0.35f,
                velocidad = 0.8f + Random.nextFloat() * 0.4f,
                vaiven = 8f + Random.nextFloat() * 16f,
                fase = Random.nextFloat() * 2f * PI.toFloat(),
                giro = Random.nextFloat(),
                color = Random.nextInt(3),
                circulo = Random.nextBoolean(),
            )
        }
    }

    // Reloj de la animación: va de 0 a 1 una sola vez
    val tiempo = remember { Animatable(0f) }
    LaunchedEffect(Unit) { tiempo.animateTo(1f, tween(DURACION_MS, easing = LinearEasing)) }
    if (tiempo.value >= 1f) return

    Canvas(modifier.fillMaxSize()) {
        val t = tiempo.value
        // El último cuarto del tiempo, todo se desvanece poco a poco
        val opacidad = if (t > 0.75f) (1f - t) / 0.25f else 1f
        val ancho = 8.dp.toPx()
        val alto = 5.dp.toPx()
        val radio = 3.5.dp.toPx()

        trozos.forEach { trozo ->
            // Avance de este trozo (0 = todavía arriba, 1 = ha terminado de caer)
            val p = ((t - trozo.retraso) / (1f - trozo.retraso)).coerceIn(0f, 1f)
            if (p <= 0f) return@forEach
            val x = trozo.x * size.width + sin(p * 3f * PI.toFloat() + trozo.fase) * trozo.vaiven.dp.toPx()
            val y = -20.dp.toPx() + p * trozo.velocidad * (size.height + 40.dp.toPx())
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
