package camejoreig.senda

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

/**
 * Lectura en voz alta con el motor de voz del propio móvil (TextToSpeech).
 * No usa internet si la voz en español está descargada en el teléfono.
 */
class Voz(ctx: Context) : TextToSpeech.OnInitListener {
    private val tts = TextToSpeech(ctx, this)

    /** true cuando hay voz en español disponible. Solo entonces se enseñan los botones de "Escuchar". */
    var lista by mutableStateOf(false)
        private set

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) return
        val r = tts.setLanguage(Locale.forLanguageTag("es-ES"))
        lista = r != TextToSpeech.LANG_MISSING_DATA && r != TextToSpeech.LANG_NOT_SUPPORTED
        tts.setSpeechRate(0.9f) // un poco más despacio de lo normal
    }

    fun decir(texto: String) {
        if (lista) tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "senda")
    }

    fun cerrar() = tts.shutdown()
}
