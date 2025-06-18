/*HabitosKoalisticos.kt*/
package com.example.koalm.ui.screens.habitosKoalisticos

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.example.koalm.R
import com.example.koalm.ui.components.BarraNavegacionInferior
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.example.koalm.ui.theme.VerdePrincipal
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaHabitosKoalisticos(navController: NavHostController, tituloHabito: String) {
    val context = LocalContext.current

    // Aquí haces la conversión robusta dentro de la pantalla:
    val tipoHabito = when (tituloHabito) {
        "Meditación koalística" -> HabitoKoalistico.MEDITACION
        "Alimentación consciente" -> HabitoKoalistico.ALIMENTACION
        "Desconexión koalística" -> HabitoKoalistico.DESCONEXION
        "Hidratación koalística" -> HabitoKoalistico.HIDRATACION
        "Descanso koalístico" -> HabitoKoalistico.DESCANSO
        "Escritura koalística" -> HabitoKoalistico.ESCRITURA
        "Lectura koalística" -> HabitoKoalistico.LECTURA
        else -> HabitoKoalistico.MEDITACION
    }
    val datos = obtenerDatosHabito(tipoHabito)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Hábitos Koalisticos",
                        color = VerdePrincipal,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
            bottomBar = {
                BarraNavegacionInferior(navController, "Racha_Habitos")
            }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // BOX con toda la info
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(datos.titulo),
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Image(
                        painter = painterResource(id = datos.imagenResId),
                        contentDescription = null,
                        modifier = Modifier.size(400.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(datos.mensaje),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Justify
                    )
                }
            }
        }
    }
}

enum class HabitoKoalistico {
    DESCONEXION,
    ALIMENTACION,
    MEDITACION,
    HIDRATACION,
    DESCANSO,
    ESCRITURA,
    LECTURA
}

data class DatosHabitoKoalistico(
    val titulo: Int,
    val mensaje: Int,
    val imagenResId: Int
)

fun obtenerDatosHabito(habito: HabitoKoalistico): DatosHabitoKoalistico {
    return when (habito) {
        HabitoKoalistico.DESCONEXION -> DatosHabitoKoalistico(
            titulo = R.string.titulo_kdesconexion,
            mensaje = R.string.mensaje_kdesconexion,
            imagenResId = R.drawable.koala_naturaleza
        )
        HabitoKoalistico.ALIMENTACION -> DatosHabitoKoalistico(
            titulo = R.string.titulo_kalimentacion,
            mensaje = R.string.mensaje_kalimentacion,
            imagenResId = R.drawable.koala_comiendo
        )
        HabitoKoalistico.MEDITACION -> DatosHabitoKoalistico(
            titulo = R.string.titulo_kmeditacion,
            mensaje = R.string.mensaje_kmeditacion,
            imagenResId = R.drawable.koala_meditando
        )
        HabitoKoalistico.HIDRATACION -> DatosHabitoKoalistico(
            titulo = R.string.titulo_khidratacion,
            mensaje = R.string.mensaje_khidratacion,
            imagenResId = R.drawable.koala_bebiendo
        )
        HabitoKoalistico.DESCANSO -> DatosHabitoKoalistico(
            titulo = R.string.titulo_kdescanso,
            mensaje = R.string.mensaje_kdescanso,
            imagenResId = R.drawable.koala_durmiendo
        )
        HabitoKoalistico.ESCRITURA -> DatosHabitoKoalistico(
            titulo = R.string.titulo_kescritura,
            mensaje = R.string.mensaje_kescritura,
            imagenResId = R.drawable.koala_escribiendo
        )
        HabitoKoalistico.LECTURA -> DatosHabitoKoalistico(
            titulo = R.string.titulo_klectura,
            mensaje = R.string.mensaje_klectura,
            imagenResId = R.drawable.koala_leyendo
        )
    }
}