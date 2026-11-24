package camejoreig.senda.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import camejoreig.senda.datos.TemaColor

/*
 * Colores de Senda.
 *
 * Hay 4 temas (Azul calma, Melocotón, Salvia y Lavanda), cada uno en claro y en oscuro,
 * y además los dos esquemas de alto contraste, que tienen prioridad sobre el tema.
 *
 * Reglas para que cambiar de tema no despiste:
 * - El pastel solo va en fondos, tarjetas y el recuadro de los dibujos.
 * - El texto es siempre muy oscuro (en claro) o muy claro (en oscuro).
 * - Los botones principales tienen un color intenso con texto blanco (o muy oscuro en modo oscuro).
 * - Los colores con significado (verde = avanzar, naranja = pedir ayuda) NO cambian con el tema.
 * - Nada de rojo ni amarillo como color principal.
 * - Se usan siempre los mismos colores (no los "dinámicos" del fondo de pantalla de Android 12+).
 *
 * Contraste comprobado (mínimo recomendado 4,5 : 1):
 *
 *   Tema / modo        texto sobre fondo   texto del botón principal   texto secundario
 *   Azul claro              14,8                  7,2                       6,6
 *   Melocotón claro         14,9                  5,9                       6,7
 *   Salvia claro            14,7                  6,2                       6,5
 *   Lavanda claro           14,6                  7,8                       6,5
 *   Azul oscuro             16,3                  7,3                       8,6
 *   Melocotón oscuro        15,7                  8,6                       9,4
 *   Salvia oscuro           16,0                  8,3                       9,9
 *   Lavanda oscuro          15,7                  8,4                       9,2
 *
 *   Botón «Hecho» (verde): 5,4 en claro y 6,3 en oscuro.
 *   Botón «Me he perdido» (naranja): 5,1 en claro y 8,7 en oscuro.
 */

/** Colores con significado propio: verde = avanzar, naranja = pedir ayuda (no rojo, para no asustar). */
data class ColoresExtra(
    val ok: Color,
    val okTexto: Color,
    val ayuda: Color,
    val ayudaTexto: Color,
    val fondoIcono: Color, // recuadro detrás de los dibujos: este sí cambia con el tema
)

// ---------- Temas claros ----------

private val TextoClaro = Color(0xFF14213A)
private val TextoSecundarioClaro = Color(0xFF4A5873)

/** Crea un esquema claro: solo cambian el color principal, el fondo y los bordes. */
private fun claro(principal: Color, fondo: Color, borde: Color) = lightColorScheme(
    primary = principal, onPrimary = Color.White,
    background = fondo, onBackground = TextoClaro,
    surface = Color.White, onSurface = TextoClaro,
    onSurfaceVariant = TextoSecundarioClaro, outline = borde,
)

val AzulClaro = claro(principal = Color(0xFF1F4FBF), fondo = Color(0xFFF3F6FB), borde = Color(0xFFD5DCE8))
val MelocotonClaro = claro(principal = Color(0xFFA8405E), fondo = Color(0xFFFFF5EE), borde = Color(0xFFF0D6C6))
val SalviaClaro = claro(principal = Color(0xFF1E6B73), fondo = Color(0xFFF1F6F1), borde = Color(0xFFD2E2D5))
val LavandaClaro = claro(principal = Color(0xFF5A3FA6), fondo = Color(0xFFF6F3FB), borde = Color(0xFFDDD3EE))

// ---------- Temas oscuros ----------

/** Crea un esquema oscuro: fondo oscuro con un toque del color del tema y botones en tono claro. */
private fun oscuro(
    principal: Color, textoPrincipal: Color,
    fondo: Color, tarjeta: Color, texto: Color, textoSecundario: Color, borde: Color,
) = darkColorScheme(
    primary = principal, onPrimary = textoPrincipal,
    background = fondo, onBackground = texto,
    surface = tarjeta, onSurface = texto,
    onSurfaceVariant = textoSecundario, outline = borde,
)

val AzulOscuro = oscuro(
    principal = Color(0xFF7AA2FF), textoPrincipal = Color(0xFF0B1430),
    fondo = Color(0xFF0F1522), tarjeta = Color(0xFF182133),
    texto = Color(0xFFEEF2F9), textoSecundario = Color(0xFFB3BED2), borde = Color(0xFF2C3850),
)
val MelocotonOscuro = oscuro(
    principal = Color(0xFFF4A3B8), textoPrincipal = Color(0xFF3A0E1C),
    fondo = Color(0xFF1C1412), tarjeta = Color(0xFF2A1F1C),
    texto = Color(0xFFF7EDE8), textoSecundario = Color(0xFFD6C3BA), borde = Color(0xFF4A3832),
)
val SalviaOscuro = oscuro(
    principal = Color(0xFF7FCDD3), textoPrincipal = Color(0xFF062B2F),
    fondo = Color(0xFF121815), tarjeta = Color(0xFF1C2420),
    texto = Color(0xFFECF3EE), textoSecundario = Color(0xFFBFD0C5), borde = Color(0xFF33423A),
)
val LavandaOscuro = oscuro(
    principal = Color(0xFFC3B0F5), textoPrincipal = Color(0xFF23164A),
    fondo = Color(0xFF16131F), tarjeta = Color(0xFF221D30),
    texto = Color(0xFFF0ECF8), textoSecundario = Color(0xFFC7BFD9), borde = Color(0xFF3B3350),
)

// ---------- Alto contraste (tiene prioridad sobre el tema) ----------

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

// ---------- Elegir colores ----------

/** Esquema de colores de un tema, en claro u oscuro. */
fun esquema(tema: TemaColor, oscuro: Boolean): ColorScheme = when (tema) {
    TemaColor.AZUL -> if (oscuro) AzulOscuro else AzulClaro
    TemaColor.MELOCOTON -> if (oscuro) MelocotonOscuro else MelocotonClaro
    TemaColor.SALVIA -> if (oscuro) SalviaOscuro else SalviaClaro
    TemaColor.LAVANDA -> if (oscuro) LavandaOscuro else LavandaClaro
}

/** Color del recuadro de los dibujos: un pastel del tema (en oscuro, un tono apagado). */
fun fondoIcono(tema: TemaColor, oscuro: Boolean): Color = when (tema) {
    TemaColor.AZUL -> if (oscuro) Color(0xFF22304A) else Color(0xFFE3EBF8)
    TemaColor.MELOCOTON -> if (oscuro) Color(0xFF3D2A24) else Color(0xFFFFE2D2)
    TemaColor.SALVIA -> if (oscuro) Color(0xFF2A3830) else Color(0xFFDCEBDF)
    TemaColor.LAVANDA -> if (oscuro) Color(0xFF322A47) else Color(0xFFE7DFF5)
}

/** Verde y naranja fijos (iguales en todos los temas). El naranja claro se oscureció para llegar a 5,1 : 1. */
fun coloresExtra(tema: TemaColor, oscuro: Boolean, contraste: Boolean): ColoresExtra = when {
    oscuro && contraste -> ColoresExtra(Color(0xFF3DDC84), Color.Black, Color(0xFFFFA040), Color.Black, Color.Black)
    contraste -> ColoresExtra(Color(0xFF00602F), Color.White, Color(0xFF9E3D00), Color.White, Color.White)
    oscuro -> ColoresExtra(Color(0xFF2FA96A), Color(0xFF04140B), Color(0xFFFF9640), Color(0xFF1D0D00), fondoIcono(tema, true))
    else -> ColoresExtra(Color(0xFF157A46), Color.White, Color(0xFFB35200), Color.White, fondoIcono(tema, false))
}

/** Valor inicial de LocalExtra (por si alguna pantalla se dibuja fuera de SendaTheme). */
val ExtraClaro = coloresExtra(TemaColor.AZUL, oscuro = false, contraste = false)
