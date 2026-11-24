package camejoreig.senda.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** Mensaje que se enseña (y se puede leer en voz alta) si se da un toque corto a un BotonMantener. */
const val AVISO_MANTENER = "Para abrir, mantén el dedo encima 2 segundos."

/**
 * Botón con dibujo y palabra (mínimo 56 dp de alto). Se usa para «Ayuda» e «Inicio»:
 * el dibujo ayuda a reconocerlo y la palabra dice qué hace.
 */
@Composable
fun BotonIcono(texto: String, icono: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 56.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
    ) {
        Icon(icono, contentDescription = null, modifier = Modifier.size(24.dp))
        Text(texto, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 8.dp))
    }
}

/**
 * Botón que solo se abre MANTENIENDO el dedo encima (unos 2 segundos).
 * Sirve para las opciones de la persona de apoyo, para que no se abran sin querer.
 *
 * - Mientras se mantiene, el fondo se va llenando de izquierda a derecha.
 * - Si se suelta antes, se vacía y no pasa nada (se llama a [alTocar] para poder enseñar un aviso).
 * - Al llenarse del todo, el móvil vibra un poco y se llama a [alCompletar].
 * - Lleva un texto fijo pequeño («Mantén pulsado») para que se sepa sin tener que probar.
 * - Con TalkBack se puede abrir con la acción de «mantener pulsado» del propio lector.
 */
@Composable
fun BotonMantener(
    texto: String,
    icono: ImageVector,
    alCompletar: () -> Unit,
    alTocar: () -> Unit,
    modifier: Modifier = Modifier,
    duracionMs: Int = 2000,
) {
    val progreso = remember { Animatable(0f) }
    val ambito = rememberCoroutineScope()
    val vibracion = LocalHapticFeedback.current
    // rememberUpdatedState: el gesto siempre llama a la versión más reciente de las funciones
    val completar by rememberUpdatedState(alCompletar)
    val tocar by rememberUpdatedState(alTocar)
    val relleno = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
    val forma = RoundedCornerShape(16.dp)

    Box(
        modifier
            .heightIn(min = 56.dp)
            .clip(forma)
            .background(MaterialTheme.colorScheme.surface)
            // Relleno de progreso: una franja que crece con el valor de "progreso" (0 a 1)
            .drawBehind { drawRect(relleno, size = Size(size.width * progreso.value, size.height)) }
            .border(2.dp, MaterialTheme.colorScheme.outline, forma)
            .semantics {
                role = Role.Button
                contentDescription = "$texto. Mantén pulsado para abrir."
                onLongClick(label = "Abrir") { completar(); true }
                onClick { tocar(); true }
            }
            .pointerInput(duracionMs) {
                awaitEachGesture {
                    awaitFirstDown()
                    // Al poner el dedo empieza a llenarse; al llenarse del todo, se abre
                    val llenado: Job = ambito.launch {
                        progreso.animateTo(1f, tween(duracionMs, easing = LinearEasing))
                        vibracion.performHapticFeedback(HapticFeedbackType.LongPress)
                        completar()
                        progreso.snapTo(0f)
                    }
                    waitForUpOrCancellation()
                    // Si se suelta antes de llenarse: se vacía y se avisa de cómo abrirlo
                    if (llenado.isActive) {
                        llenado.cancel()
                        ambito.launch { progreso.animateTo(0f, tween(250)) }
                        tocar()
                    }
                }
            }
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp))
            Column {
                Text(texto, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "Mantén pulsado",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = MaterialTheme.typography.bodyMedium.fontSize * 0.7f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
