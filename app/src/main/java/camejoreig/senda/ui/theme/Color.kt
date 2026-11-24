package camejoreig.senda.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import camejoreig.senda.datos.TemaColor

/*
 * Colores de Senda.
 *
 * Hay 4 temas (Melocotón, que es el de por defecto, Azul calma, Salvia y Lavanda), cada uno en claro y en oscuro.
 * «Más contraste» no cambia de tema: es el mismo tema, pero más marcado
 * (botones y bordes en un tono más fuerte del tema; ver la tabla más abajo).
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

// ---------- Más contraste: el mismo tema, pero más marcado ----------
//
// No usa blanco y negro puros: sigue pareciéndose al tema, con un tinte muy suave en el fondo.
// Respecto al modo normal sube sobre todo el texto de los botones, el texto secundario
// y los bordes (que pasan de pastel a un tono oscuro del tema).
//
//   Tema / modo        texto/fondo  texto/tarjeta  botón  texto secundario  borde/fondo
//   Melocotón claro       17,2          17,7         9,0        13,1             6,3
//   Azul claro            16,8          17,4         8,9        12,0             6,1
//   Salvia claro          16,8          17,5         8,5        11,8             5,6
//   Lavanda claro         17,4          18,0         9,3        13,2             5,7
//   Melocotón oscuro      17,2          15,9        11,2        12,8             8,5
//   Azul oscuro           17,5          15,8        10,7        12,9             9,2
//   Salvia oscuro         17,5          15,9        11,1        12,9            10,0
//   Lavanda oscuro        17,5          16,1        10,6        12,7             9,0
//
// Verde y naranja con alto contraste (iguales en los 4 temas):
//   claro  -> «Hecho» 7,7 · «Me he perdido» 8,1 (naranja oscurecido para llegar a 7 : 1)
//   oscuro -> «Hecho» 11,8 · «Me he perdido» 10,4

/** Crea un esquema de alto contraste a partir de sus colores. */
private fun contraste(
    oscuro: Boolean, fondo: Color, tarjeta: Color, texto: Color, textoSecundario: Color,
    borde: Color, principal: Color, textoPrincipal: Color,
): ColorScheme = if (oscuro) {
    darkColorScheme(
        primary = principal, onPrimary = textoPrincipal,
        background = fondo, onBackground = texto, surface = tarjeta, onSurface = texto,
        onSurfaceVariant = textoSecundario, outline = borde,
    )
} else {
    lightColorScheme(
        primary = principal, onPrimary = textoPrincipal,
        background = fondo, onBackground = texto, surface = tarjeta, onSurface = texto,
        onSurfaceVariant = textoSecundario, outline = borde,
    )
}

/** Esquema de alto contraste de un tema, en claro u oscuro. */
fun esquemaContraste(tema: TemaColor, oscuro: Boolean): ColorScheme = when (tema) {
    TemaColor.MELOCOTON -> if (oscuro) {
        contraste(true, Color(0xFF120B0C), Color(0xFF1D1415), Color(0xFFF7EFEF), Color(0xFFE6D5D7), Color(0xFFD99AAA), Color(0xFFFFB8C9), Color(0xFF2A0A14))
    } else {
        contraste(false, Color(0xFFFFF9F5), Color(0xFFFFFDFB), Color(0xFF1E1416), Color(0xFF3D2B2F), Color(0xFF8A4A5C), Color(0xFF7A2944), Color(0xFFFFF8F5))
    }
    TemaColor.AZUL -> if (oscuro) {
        contraste(true, Color(0xFF0A0E17), Color(0xFF131A28), Color(0xFFF1F4FA), Color(0xFFD6DEEC), Color(0xFF9AB3E6), Color(0xFFAFC6FF), Color(0xFF0A1430))
    } else {
        contraste(false, Color(0xFFF7F9FD), Color(0xFFFCFDFF), Color(0xFF111827), Color(0xFF2A3550), Color(0xFF4A5E8A), Color(0xFF1D4294), Color(0xFFF8FAFF))
    }
    TemaColor.SALVIA -> if (oscuro) {
        contraste(true, Color(0xFF0A100E), Color(0xFF131C19), Color(0xFFF0F6F2), Color(0xFFD2E2D8), Color(0xFF8CC7B9), Color(0xFF97E0E4), Color(0xFF052326))
    } else {
        contraste(false, Color(0xFFF6FAF6), Color(0xFFFCFEFC), Color(0xFF121A17), Color(0xFF2A3A33), Color(0xFF4C6B5E), Color(0xFF0F5259), Color(0xFFF5FCFB))
    }
    TemaColor.LAVANDA -> if (oscuro) {
        contraste(true, Color(0xFF0E0B15), Color(0xFF191424), Color(0xFFF4F1FA), Color(0xFFDDD5EE), Color(0xFFB8A6E8), Color(0xFFD2C2FF), Color(0xFF1E1240))
    } else {
        contraste(false, Color(0xFFFAF8FD), Color(0xFFFDFCFF), Color(0xFF17121F), Color(0xFF33294A), Color(0xFF6A5A96), Color(0xFF48309A), Color(0xFFFBF9FF))
    }
}

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
    oscuro && contraste -> ColoresExtra(Color(0xFF3DDC84), Color.Black, Color(0xFFFFA040), Color.Black, fondoIcono(tema, true))
    contraste -> ColoresExtra(Color(0xFF00602F), Color.White, Color(0xFF8A3500), Color.White, fondoIcono(tema, false))
    oscuro -> ColoresExtra(Color(0xFF2FA96A), Color(0xFF04140B), Color(0xFFFF9640), Color(0xFF1D0D00), fondoIcono(tema, true))
    else -> ColoresExtra(Color(0xFF157A46), Color.White, Color(0xFFB35200), Color.White, fondoIcono(tema, false))
}

/** Valor inicial de LocalExtra (por si alguna pantalla se dibuja fuera de SendaTheme). */
val ExtraClaro = coloresExtra(TemaColor.MELOCOTON, oscuro = false, contraste = false)
