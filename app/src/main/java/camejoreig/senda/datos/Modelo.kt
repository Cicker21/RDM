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
 * Tema de color de la app. Solo cambia el «ambiente» (fondos y botones principales);
 * el verde de «Hecho» y el naranja de «Me he perdido» son iguales en todos.
 */
enum class TemaColor(val etiqueta: String) {
    MELOCOTON("Melocotón"), // el de por defecto: cálido y suave
    AZUL("Azul calma"),
    SALVIA("Salvia"),
    LAVANDA("Lavanda"),
}

/** Claro u oscuro. «Según el móvil» sigue lo que tenga puesto el teléfono (si no tiene, claro). */
enum class ModoColor(val etiqueta: String) {
    SEGUN_MOVIL("Según el móvil"),
    CLARO("Claro"),
    OSCURO("Oscuro"),
}

/**
 * Cuánto rato sigue cayendo confeti desde arriba al llegar. Después, el que ya está cayendo
 * termina de bajar hasta salir por abajo. Los segundos no se enseñan en pantalla, solo «Corta» o «Larga».
 */
enum class DuracionCelebracion(val milisegundos: Int, val etiqueta: String) {
    CORTA(2000, "Corta"),
    LARGA(5000, "Larga"),
}

/**
 * Cómo se abre el botón «Apoyo» del inicio (lo elige la persona de apoyo en Ajustes).
 * Mantenerlo pulsado unos segundos evita que se abra sin querer. 0 segundos = un toque normal.
 */
enum class AperturaApoyo(val segundos: Int, val etiqueta: String) {
    AL_TOQUE(0, "Al momento, con un toque"),
    UN_SEGUNDO(1, "Mantener 1 segundo"),
    DOS_SEGUNDOS(2, "Mantener 2 segundos"),
    TRES_SEGUNDOS(3, "Mantener 3 segundos"),
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
    val aperturaApoyo: AperturaApoyo = AperturaApoyo.DOS_SEGUNDOS, // solo se cambia en Ajustes
    val tema: TemaColor = TemaColor.MELOCOTON,
    val modo: ModoColor = ModoColor.SEGUN_MOVIL, // solo se cambia en Ajustes
    val celebrar: Boolean = true, // confeti al llegar al destino
    val duracionCelebracion: DuracionCelebracion = DuracionCelebracion.LARGA,
)
