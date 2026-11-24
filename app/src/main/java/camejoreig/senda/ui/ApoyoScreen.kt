package camejoreig.senda.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import camejoreig.senda.ui.theme.LocalExtra

/**
 * ApoyoScreen: opciones para la persona de apoyo (se llega manteniendo pulsado «Apoyo» en el inicio).
 * Aquí los botones funcionan con un toque normal.
 */
@Composable
fun ApoyoScreen(inicio: () -> Unit, abrirCreador: () -> Unit, abrirAjustes: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Salir: siempre arriba a la izquierda, con casa y palabra
        BotonIcono("Inicio", Icons.Filled.Home, inicio)
        Text("Apoyo", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Opciones para preparar la app junto a tu persona de apoyo.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OpcionGrande("Crear o cambiar rutas", Icons.Filled.Edit, abrirCreador)
        OpcionGrande("Ajustes", Icons.Filled.Settings, abrirAjustes)
    }
}

/** Botón grande con su dibujo, como los de las rutas del inicio. */
@Composable
private fun OpcionGrande(texto: String, icono: ImageVector, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().heightIn(min = 88.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.clip(RoundedCornerShape(16.dp)).background(LocalExtra.current.fondoIcono),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(10.dp).size(32.dp))
            }
            Spacer(Modifier.size(16.dp))
            Text(texto, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
