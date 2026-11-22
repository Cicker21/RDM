package camejoreig.senda.datos

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

/**
 * Guarda todo (ajustes, rutas y viaje en curso) en un archivo JSON dentro del móvil.
 *
 * - No hay servidor ni cuentas: funciona sin conexión (en el Metro hace falta) y los datos
 *   no salen del dispositivo.
 * - El archivo está en el espacio privado de la app, así que no hace falta pedir permisos.
 * - Las propiedades son "estado de Compose": cuando cambian, la pantalla se redibuja sola.
 *
 * Más adelante se puede cambiar por Room o DataStore sin tocar las pantallas,
 * siempre que se mantengan estas mismas funciones.
 */
class Repositorio(private val ctx: Context) {
    private val archivo = File(ctx.filesDir, "datos.json")

    /** null = todavía no se ha hecho la guía (primera vez que se abre la app). */
    var ajustes by mutableStateOf<Ajustes?>(null)
        private set
    var rutas by mutableStateOf<List<Ruta>>(emptyList())
        private set
    var viaje by mutableStateOf<Viaje?>(null)
        private set

    init {
        cargar()
    }

    fun ruta(id: String): Ruta? = rutas.find { it.id == id }

    // ---------- Ajustes ----------
    fun guardarAjustes(a: Ajustes) {
        ajustes = a; guardar()
    }

    // ---------- Viaje ----------
    fun empezar(rutaId: String) {
        viaje = Viaje(rutaId, 0); guardar()
    }

    fun irAPaso(n: Int) {
        viaje = viaje?.copy(paso = n); guardar()
    }

    fun terminar() {
        viaje = null; guardar()
    }

    // ---------- Rutas ----------
    /** Si la ruta ya existe la sustituye; si no, la añade al final. */
    fun guardarRuta(r: Ruta) {
        rutas = if (rutas.any { it.id == r.id }) rutas.map { if (it.id == r.id) r else it } else rutas + r
        guardar()
    }

    fun borrarRuta(id: String) {
        rutas.find { it.id == id }?.pasos?.mapNotNull { it.foto }?.forEach { File(it).delete() }
        rutas = rutas.filter { it.id != id }
        if (viaje?.rutaId == id) viaje = null
        guardar()
    }

    /** Copia la foto elegida dentro de la app, reducida a unos 1000 px para no ocupar mucho. */
    suspend fun guardarFoto(uri: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            // 1) Leemos solo las medidas para calcular cuánto reducir
            val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            ctx.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, medidas) }
            var muestra = 1
            while (maxOf(medidas.outWidth, medidas.outHeight) / (muestra * 2) >= 1000) muestra *= 2
            // 2) Leemos la imagen ya reducida y la guardamos como JPG
            val opciones = BitmapFactory.Options().apply { inSampleSize = muestra }
            val bmp = ctx.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, opciones)
            } ?: return@runCatching null
            val dir = File(ctx.filesDir, "fotos").apply { mkdirs() }
            val destino = File(dir, "${UUID.randomUUID()}.jpg")
            destino.outputStream().use { bmp.compress(Bitmap.CompressFormat.JPEG, 80, it) }
            destino.absolutePath
        }.getOrNull()
    }

    // ---------- Lectura y escritura del JSON ----------
    private fun cargar() {
        val leido = runCatching {
            val json = JSONObject(archivo.readText())
            ajustes = json.optJSONObject("ajustes")?.let { ajustesDesdeJson(it) }
            val lista = json.getJSONArray("rutas")
            rutas = (0 until lista.length()).map { rutaDesdeJson(lista.getJSONObject(it)) }
            viaje = json.optJSONObject("viaje")?.let { Viaje(it.getString("rutaId"), it.getInt("paso")) }
        }
        if (leido.isFailure) { // primera vez o archivo dañado: empezamos con la ruta de ejemplo
            ajustes = null
            rutas = listOf(RUTA_EJEMPLO)
            viaje = null
        }
    }

    private fun guardar() {
        val json = JSONObject()
        ajustes?.let { json.put("ajustes", ajustesAJson(it)) }
        json.put("rutas", JSONArray(rutas.map { rutaAJson(it) }))
        viaje?.let { json.put("viaje", JSONObject().put("rutaId", it.rutaId).put("paso", it.paso)) }
        // Escritura segura: primero a un archivo temporal y luego se renombra,
        // así un cierre a medias no deja el archivo bueno estropeado.
        val tmp = File(archivo.path + ".tmp")
        tmp.writeText(json.toString())
        if (!tmp.renameTo(archivo)) { // en algunos móviles renameTo falla si el destino existe
            archivo.delete(); tmp.renameTo(archivo)
        }
    }

    private fun ajustesAJson(a: Ajustes) = JSONObject()
        .put("tamanoLetra", a.tamanoLetra.name)
        .put("altoContraste", a.altoContraste)
        .put("dibujosGrandes", a.dibujosGrandes)
        .put("leerEnVozAlta", a.leerEnVozAlta)
        .put("contacto", JSONObject().put("nombre", a.contacto.nombre).put("telefono", a.contacto.telefono))

    private fun ajustesDesdeJson(o: JSONObject): Ajustes {
        val c = o.optJSONObject("contacto")
        return Ajustes(
            tamanoLetra = runCatching { TamanoLetra.valueOf(o.getString("tamanoLetra")) }.getOrDefault(TamanoLetra.NORMAL),
            altoContraste = o.optBoolean("altoContraste"),
            dibujosGrandes = o.optBoolean("dibujosGrandes"),
            leerEnVozAlta = o.optBoolean("leerEnVozAlta"),
            contacto = Contacto(c?.optString("nombre").orEmpty(), c?.optString("telefono").orEmpty()),
        )
    }

    private fun rutaAJson(r: Ruta) = JSONObject()
        .put("id", r.id)
        .put("nombre", r.nombre)
        .put("pasos", JSONArray(r.pasos.map { p ->
            JSONObject().put("texto", p.texto).put("icono", p.icono.name).apply { p.foto?.let { put("foto", it) } }
        }))

    private fun rutaDesdeJson(o: JSONObject): Ruta {
        val pasos = o.getJSONArray("pasos")
        return Ruta(
            id = o.getString("id"),
            nombre = o.getString("nombre"),
            pasos = (0 until pasos.length()).map { i ->
                val p = pasos.getJSONObject(i)
                Paso(
                    texto = p.getString("texto"),
                    icono = runCatching { Icono.valueOf(p.getString("icono")) }.getOrDefault(Icono.CAMINAR),
                    foto = if (p.has("foto")) p.getString("foto") else null,
                )
            },
        )
    }
}
