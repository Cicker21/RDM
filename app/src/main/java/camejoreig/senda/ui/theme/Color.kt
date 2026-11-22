package camejoreig.senda.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/*
 * Colores de Senda. Hay cuatro juegos: claro, oscuro y sus versiones de alto contraste.
 * Se usan siempre los mismos colores (no los "dinámicos" del fondo de pantalla de Android 12+)
 * para que la app se vea igual en todos los móviles y el usuario la reconozca.
 */

/** Colores con significado propio: verde = avanzar, naranja = pedir ayuda (no rojo, para no asustar). */
data class ColoresExtra(
    val ok: Color,
    val okTexto: Color,
    val ayuda: Color,
    val ayudaTexto: Color,
    val fondoIcono: Color,
)

val Claro = lightColorScheme(
    primary = Color(0xFF1F4FBF), onPrimary = Color.White,
    background = Color(0xFFF5F7FA), onBackground = Color(0xFF14213A),
    surface = Color.White, onSurface = Color(0xFF14213A),
    onSurfaceVariant = Color(0xFF4A5873), outline = Color(0xFFD5DCE8),
)
val Oscuro = darkColorScheme(
    primary = Color(0xFF7AA2FF), onPrimary = Color(0xFF0B1430),
    background = Color(0xFF0F1522), onBackground = Color(0xFFEEF2F9),
    surface = Color(0xFF182133), onSurface = Color(0xFFEEF2F9),
    onSurfaceVariant = Color(0xFFB3BED2), outline = Color(0xFF2C3850),
)

// Alto contraste: blanco y negro puros con bordes marcados
val ClaroContraste = lightColorScheme(
    primary = Color(0xFF002B80), onPrimary = Color.White,
    background = Color.White, onBackground = Color.Black,
    surface = Color.White, onSurface = Color.Black,
    onSurfaceVariant = Color.Black, outline = Color.Black,
)
val OscuroContraste = darkColorScheme(
    primary = Color(0xFFFFE14D), onPrimary = Color.Black,
    background = Color.Black, onBackground = Color.White,
    surface = Color.Black, onSurface = Color.White,
    onSurfaceVariant = Color.White, outline = Color.White,
)

val ExtraClaro = ColoresExtra(Color(0xFF157A46), Color.White, Color(0xFFC45A00), Color.White, Color(0xFFE6EDF9))
val ExtraOscuro = ColoresExtra(Color(0xFF2FA96A), Color(0xFF04140B), Color(0xFFFF9640), Color(0xFF1D0D00), Color(0xFF22304A))
val ExtraClaroContraste = ColoresExtra(Color(0xFF00602F), Color.White, Color(0xFF9E3D00), Color.White, Color.White)
val ExtraOscuroContraste = ColoresExtra(Color(0xFF3DDC84), Color.Black, Color(0xFFFFA040), Color.Black, Color.Black)
