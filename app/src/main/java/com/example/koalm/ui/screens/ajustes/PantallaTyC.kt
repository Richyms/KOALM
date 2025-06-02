/*PantallaTyC.kt*/
package com.example.koalm.ui.screens.ajustes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.koalm.R
import com.example.koalm.model.Usuario
import com.example.koalm.ui.theme.*
import androidx.compose.foundation.BorderStroke

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaTyC (navController: NavHostController){
    val context = LocalContext.current

    //--------------------------- UI --------------------------------------
    Scaffold(
        topBar = {
            TopAppBar(
                title = {Text("Terminos y Condiciones")},
                navigationIcon = {
                    IconButton(onClick =  { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ){ innerPadding ->

        Column(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape   = RoundedCornerShape(16.dp),
                border  = BorderStroke(1.dp, VerdeBorde),
                colors  = CardDefaults.cardColors(containerColor = VerdeContenedor)
            ){
                Column(
                    Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Text("Al descargar, instalar o utilizar la aplicación KOALM, usted acepta los presentes Términos y Condiciones de uso.\n" +
                            "Si no está de acuerdo con alguno de ellos, le recomendamos no utilizar la aplicación.\n" + "KOALM es una aplicación móvil que permite al usuario monitorear su actividad física diaria, incluyendo pasos, " +
                            "calidad y duración del sueño, tiempo de ejercicio y seguimiento de hábitos personales, tanto establecidos como nuevos.\n" +
                            "El usuario se compromete a utilizar la aplicación únicamente para fines personales y lícitos.\n" +
                            "Queda prohibido:\n" +
                            "•\tUsar la app para recopilar datos de otros usuarios sin consentimiento.\n" +
                            "•\tIntentar vulnerar la seguridad o integridad del sistema.\n" +
                            "•\tModificar, copiar, distribuir o comercializar el software sin autorización.\n" +
                            "Todos los derechos de propiedad intelectual sobre el software, diseño, código fuente y contenido visual pertenecen a los desarrolladores del proyecto KOALM.\n" +
                            "No se otorgan licencias de uso más allá de lo expresamente permitido.\n\n" +
                            "KOALM es una herramienta de acompañamiento personal. No constituye un servicio médico ni reemplaza el consejo de profesionales de la salud.\n" +
                            "El equipo desarrollador no se hace responsable por daños derivados del uso incorrecto de la aplicación.\n" +
                            "KOALM puede recopilar información como:\n" +
                            "•\tDatos personales (nombre, correo electrónico).\n" +
                            "•\tDatos de actividad física (pasos, sueño, hábitos).\n" +
                            "•\tGeolocalización (opcional y solo con consentimiento).\n" +
                            "Toda la información se maneja conforme a lo establecido en nuestra Política de Privacidad.\n" +
                            "La aplicación puede integrar servicios externos como Firebase, los cuales cuentan con sus propias políticas de privacidad y tratamiento de datos.\n" +
                            "Al usar KOALM, usted también acepta los términos de dichos servicios.\n" +
                            "Nos reservamos el derecho de modificar estos términos en cualquier momento, en caso de cambios sustanciales, se notificará a los usuarios dentro de la app o por correo electrónico.\n" +
                            "Este documento se rige por las leyes mexicanas, incluyendo la Ley Federal de Protección de Datos Personales en Posesión de los Particulares.\n" +
                            "Para dudas, aclaraciones o ejercicio de derechos ARCO (Acceso, Rectificación, Cancelación u Oposición), puedes contactarnos al correo:\n" +
                            "adslatex@gmail.com")
                }
            }
        }
    }
}