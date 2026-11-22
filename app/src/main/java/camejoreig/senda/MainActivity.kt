package camejoreig.senda

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import camejoreig.senda.datos.Ajustes
import camejoreig.senda.datos.Repositorio
import camejoreig.senda.ui.SendaApp
import camejoreig.senda.ui.theme.SendaTheme

/**
 * Única actividad de la app. Solo prepara los datos y la voz y dibuja SendaApp;
 * toda la navegación entre pantallas está en ui/SendaApp.kt.
 */
class MainActivity : ComponentActivity() {
    private lateinit var voz: Voz

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repo = Repositorio(applicationContext)
        voz = Voz(this)
        setContent {
            // El tema se adapta a los ajustes guardados (letra, contraste)
            SendaTheme(ajustes = repo.ajustes ?: Ajustes()) {
                SendaApp(repo, voz)
            }
        }
    }

    override fun onDestroy() {
        voz.cerrar()
        super.onDestroy()
    }
}
