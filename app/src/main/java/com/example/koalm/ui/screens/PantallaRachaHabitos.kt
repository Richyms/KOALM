//PantallaRachaHabitos.kt
package com.example.koalm.ui.screens

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
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
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
import com.example.koalm.ui.theme.VerdeContenedor
import androidx.compose.foundation.border
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRachaHabitos (navController: NavHostController){
    val context = LocalContext.current

    //------------------------- ESTADOS ---------------------------------
    var estadoRacha by remember { mutableStateOf(EstadoRacha.SIN_RACHA) }
    val datos = obtenerDatosRacha(estadoRacha)
    val estadoDias = obtenerEstadoDias(estadoRacha)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titulo_racha_habitos)) },
                navigationIcon ={
                    IconButton(onClick = {navController.navigateUp()}) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        bottomBar = {
            BarraNavegacionInferior(navController, "Racha_Habitos")
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            //Titulo
            Text(
                text = stringResource(datos.titulo),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            //Imagen
            Image(
                painter = painterResource(id = datos.imagenResId),
                contentDescription = null,
                modifier = Modifier
                    .size(200.dp)
                    .padding(bottom=16.dp)
            )

            Text(
                text = stringResource(datos.mensaje),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            //Boton de inicio
            if (datos.textoBoton != null) {
                Button(
                    onClick = {
                        estadoRacha = EstadoRacha.EN_PROGRESO //Modificación del estado
                        Toast.makeText(context, "Racha empezada", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .width(200.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(text = datos.textoBoton!!)
                }
            }
            //Racha de habitos
            if (estadoDias.isNotEmpty()) {
                TarjetaRacha(dias = estadoDias)
            }
        }
    }
}

//Tipos de racha
//Nota sustituir esto por la función para recuperar el tipo de racha en la bd
enum class EstadoRacha {
    SIN_RACHA,
    EN_PROGRESO,
    COMPLETADA
}
//Estados del día para la asingación del panel
enum class EstadoDia{
    COMPLETADO,
    EN_PROGRESO,
    NO_COMPLETADO
}

/*Dato para los estados visuales con base en el tipo de racha*/
data class DatosRachaUI(
    val titulo: Int,
    val mensaje: Int,
    val imagenResId: Int,
    val textoBoton: String? = null
)

/*Función para activar los elementos segun el estado*/
fun obtenerDatosRacha(estado: EstadoRacha): DatosRachaUI {
    return when (estado) {
        EstadoRacha.SIN_RACHA -> DatosRachaUI(
            titulo = R.string.label_sin_racha,
            mensaje = R.string.mensaje_sin_racha,
            imagenResId = R.drawable.koala_durmiendo, //MODIFICAR POR LA IMAGEN CORRECTA
            textoBoton = "Comenzar nueva racha"
        )
        EstadoRacha.EN_PROGRESO -> DatosRachaUI(
            titulo = R.string.titulo_racha_en_progreso,
            mensaje = R.string.mensaje_racha_en_progreso,
            imagenResId = R.drawable.koala_tomando //MODIFICAR POR LA IMAGEN CORRECTA
        )
        EstadoRacha.COMPLETADA -> DatosRachaUI(
            titulo = R.string.titulo_racha_finalizada,
            mensaje = R.string.mensaje_racha_finzalizada,
            imagenResId = R.drawable.koala_meditando //MODIFICAR POR LA IMAGEN CORRECTA
        )
    }
}

//Logica de las casillas de racha
@Composable
fun DiaRacha(icono: EstadoDia) {
    val colorBorde: Color
    val contenido: @Composable () -> Unit

    when (icono) {
        EstadoDia.COMPLETADO -> {
            colorBorde = Color(0xFF4CAF50)
            contenido = {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50)
                )
            }
        }

        EstadoDia.EN_PROGRESO -> {
            colorBorde = Color(0xFF81C784)
            contenido = {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(16.dp),
                    color = Color(0xFF81C784)
                )
            }
        }

        EstadoDia.NO_COMPLETADO -> {
            colorBorde = Color(0xFFBDBDBD)
            contenido = {}
        }
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .background(color = VerdeContenedor, shape = CircleShape)
            .border(width = 2.dp, color = colorBorde, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        contenido()
    }
}

@Composable
fun TarjetaRacha(dias: List<EstadoDia>) {
    val diasSemana = listOf("L", "M", "M", "J", "V", "S", "D")

    Card(
        modifier = Modifier
            .padding(top = 32.dp)
            .fillMaxWidth(),
        colors  = CardDefaults.cardColors(containerColor = VerdeContenedor),
        shape   = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Racha", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                dias.forEachIndexed { i, estado ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        DiaRacha(icono = estado)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(diasSemana[i])
                    }
                }
            }
        }
    }
}
/*Obtención de racha de habitos con base al tipo de racha */
/*Modificar con base en la bd*/
fun obtenerEstadoDias(estado: EstadoRacha): List<EstadoDia> {
    return when (estado) {
        EstadoRacha.SIN_RACHA -> emptyList()
        EstadoRacha.EN_PROGRESO -> listOf(
            EstadoDia.COMPLETADO,
            EstadoDia.COMPLETADO,
            EstadoDia.COMPLETADO,
            EstadoDia.EN_PROGRESO,
            EstadoDia.NO_COMPLETADO,
            EstadoDia.NO_COMPLETADO,
            EstadoDia.NO_COMPLETADO
        )
        EstadoRacha.COMPLETADA -> List(7) { EstadoDia.COMPLETADO }
    }
}