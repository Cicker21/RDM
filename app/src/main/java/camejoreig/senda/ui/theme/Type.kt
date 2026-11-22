package camejoreig.senda.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Letra grande en toda la app: con escala 1.0 el texto más pequeño es de 18 sp.
 * La escala sale de los ajustes del usuario (Normal 1.0, Grande 1.2, Muy grande 1.4).
 * Además se suma la escala de letra que tenga puesta el propio móvil, porque usamos "sp".
 */
fun tipografia(escala: Float) = Typography(
    headlineMedium = TextStyle(fontSize = (30 * escala).sp, lineHeight = (36 * escala).sp, fontWeight = FontWeight.Bold),
    headlineSmall = TextStyle(fontSize = (26 * escala).sp, lineHeight = (34 * escala).sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = (22 * escala).sp, lineHeight = (28 * escala).sp, fontWeight = FontWeight.Bold),
    bodyLarge = TextStyle(fontSize = (20 * escala).sp, lineHeight = (28 * escala).sp),
    bodyMedium = TextStyle(fontSize = (18 * escala).sp, lineHeight = (24 * escala).sp),
    labelLarge = TextStyle(fontSize = (20 * escala).sp, lineHeight = (26 * escala).sp, fontWeight = FontWeight.Bold),
)
