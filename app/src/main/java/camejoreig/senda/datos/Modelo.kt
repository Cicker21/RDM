package camejoreig.senda.datos

/*
 * Modelo de datos de Senda.
 * Son clases sencillas (data class) que solo guardan información; no tienen lógica.
 */

/** Pictogramas disponibles para cada paso (se ven cuando el paso no tiene foto). */
enum class Icono(val etiqueta: String) {
    CAMINAR("Caminar"),
    TORNO("Torno / tarjeta"),
    ANDEN("Andén / sentido"),
    TREN("Tren"),
    TRANSBORDO("Transbordo"),
    ESCALERA("Escaleras"),
    SALIDA("Salida"),
    TRABAJO("Trabajo"),
    CASA("Casa"),
}

/** Un paso de la ruta: una sola acción, explicada con una frase corta. */
data class Paso(
    val texto: String,
    val icono: Icono = Icono.CAMINAR,
    val foto: String? = null, // ruta del archivo de la foto dentro de la app (o null si no hay)
)

/** Una ruta guardada, por ejemplo "De casa al trabajo". */
data class Ruta(
    val id: String,
    val nombre: String,
    val pasos: List<Paso>,
)

/** Viaje en curso: qué ruta y en qué paso va. Se guarda para retomarlo si se cierra la app. */
data class Viaje(val rutaId: String, val paso: Int)

/** Persona a la que se puede avisar, solo si el usuario lo decide. */
data class Contacto(val nombre: String = "", val telefono: String = "")

/** Tamaño de letra que elige el usuario en la guía. El número multiplica el tamaño base. */
enum class TamanoLetra(val escala: Float, val etiqueta: String) {
    NORMAL(1.0f, "Normal"),
    GRANDE(1.2f, "Grande"),
    MUY_GRANDE(1.4f, "Muy grande"),
}

/**
 * Configuración del usuario, rellenada en la guía la primera vez (GuiaScreen)
 * y modificable después en AjustesScreen.
 */
data class Ajustes(
    val tamanoLetra: TamanoLetra = TamanoLetra.NORMAL,
    val altoContraste: Boolean = false,
    val dibujosGrandes: Boolean = false, // para quien se apoya más en imágenes que en texto
    val leerEnVozAlta: Boolean = false,  // lee cada paso automáticamente al aparecer
    val contacto: Contacto = Contacto(),
)

/**
 * Ruta de ejemplo en Madrid: Lavapiés (L3) -> Sol (transbordo) -> Tribunal (L1).
 * OJO: escrita de memoria. Hay que comprobarla en el plano oficial de Metro antes de la demo.
 */
val RUTA_EJEMPLO = Ruta(
    id = "ejemplo",
    nombre = "De casa al trabajo",
    pasos = listOf(
        Paso("Sal de casa. Camina hasta el Metro de Lavapiés.", Icono.CAMINAR),
        Paso("Pasa el torno con tu tarjeta de transporte.", Icono.TORNO),
        Paso("Busca la línea 3, de color amarillo. Ve al andén que pone «Moncloa».", Icono.ANDEN),
        Paso("Sube al tren. Bájate en la primera parada: Sol.", Icono.TREN),
        Paso("En Sol, sigue los carteles de la línea 1, de color azul claro.", Icono.TRANSBORDO),
        Paso("Ve al andén que pone «Pinar de Chamartín».", Icono.ANDEN),
        Paso("Sube al tren. Bájate en la segunda parada: Tribunal.", Icono.TREN),
        Paso("Sube las escaleras. Sal por la salida que te enseñaron.", Icono.SALIDA),
        Paso("Camina hasta tu trabajo. ¡Ya casi estás!", Icono.TRABAJO),
    ),
)
