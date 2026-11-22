package camejoreig.senda.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Stairs
import androidx.compose.material.icons.filled.Subway
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.TransferWithinAStation
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import camejoreig.senda.datos.Icono
import camejoreig.senda.datos.Paso
import camejoreig.senda.ui.theme.LocalExtra

/*
 * Piezas de interfaz que se repiten en varias pantallas.
 * Todas tienen zonas de pulsación grandes (mínimo 56-64 dp) para que sean fáciles de tocar.
 */

/** Dibujo (pictograma) que corresponde a cada tipo de paso. */
fun Icono.vector(): ImageVector = when (this) {
    Icono.CAMINAR -> Icons.AutoMirrored.Filled.DirectionsWalk
    Icono.TORNO -> Icons.Filled.CreditCard
    Icono.ANDEN -> Icons.Filled.Subway
    Icono.TREN -> Icons.Filled.Train
    Icono.TRANSBORDO -> Icons.Filled.TransferWithinAStation
    Icono.ESCALERA -> Icons.Filled.Stairs
    Icono.SALIDA -> Icons.AutoMirrored.Filled.ExitToApp
    Icono.TRABAJO -> Icons.Filled.Work
    Icono.CASA -> Icons.Filled.Home
}

/** Botón principal grande (mínimo 64 dp de alto). */
@Composable
fun BotonGrande(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fondo: Color = MaterialTheme.colorScheme.primary,
    letra: Color = MaterialTheme.colorScheme.onPrimary,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().heightIn(min = 64.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = fondo, contentColor = letra),
    ) {
        Text(texto, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
    }
}

/** Botón secundario con borde. */
@Composable
fun BotonBorde(texto: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 56.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
    ) {
        Text(texto, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
    }
}

/**
 * Botón para elegir una opción de una lista (se usa en la guía y en el editor).
 * La opción elegida se marca con un borde grueso del color principal.
 */
@Composable
fun BotonOpcion(
    elegido: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    relleno: PaddingValues = ButtonDefaults.ContentPadding, // margen interior del botón
    contenido: @Composable RowScope.() -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 64.dp),
        contentPadding = relleno,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            if (elegido) 4.dp else 2.dp,
            if (elegido) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        ),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
    ) {
        contenido()
    }
}

/** Foto del paso si la hay; si no, su pictograma. */
@Composable
fun VisualPaso(paso: Paso, tamano: Dp, modifier: Modifier = Modifier) {
    // remember: la foto solo se lee del disco cuando cambia, no en cada redibujado
    val bitmap = remember(paso.foto) {
        paso.foto?.let { runCatching { BitmapFactory.decodeFile(it)?.asImageBitmap() }.getOrNull() }
    }
    Box(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(LocalExtra.current.fondoIcono),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "Foto del paso",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().heightIn(max = tamano * 2),
            )
        } else {
            Icon(
                imageVector = paso.icono.vector(),
                contentDescription = paso.icono.etiqueta,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(16.dp).size(tamano),
            )
        }
    }
}

/** Tarjeta con borde para agrupar contenido. */
@Composable
fun Tarjeta(contenido: @Composable () -> Unit) {
    Card(
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { contenido() }
    }
}
