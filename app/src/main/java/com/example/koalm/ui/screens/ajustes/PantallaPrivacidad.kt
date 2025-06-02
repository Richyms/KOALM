/*PantallaPrivacidad.kt*/
package com.example.koalm.ui.screens.ajustes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.koalm.ui.theme.*
import androidx.compose.foundation.BorderStroke

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrivacidad (navController: NavHostController){
    val context = LocalContext.current

    //--------------------------- UI --------------------------------------
    Scaffold(
        topBar = {
            TopAppBar(
                title = {Text("Privacidad")},
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
                    Text("El responsable del tratamiento de tus datos es el equipo de desarrollo de la aplicación KOALM.\n" +
                            "Puedes contactarnos en:\n" +
                            "adslatex@gmail.com" +
                            "Al utilizar KOALM, podríamos recopilar los siguientes tipos de información:\n" +
                            "\n" +
                            "Datos personales básicos: nombre, correo electrónico, edad (si es necesario).\n" +
                            "\n" +
                            "Datos de salud y actividad: número de pasos, tiempo y calidad de sueño, hábitos registrados, duración de ejercicio.\n" +
                            "\n" +
                            "Geolocalización: únicamente si el usuario otorga permiso.\n" +
                            "\n" +
                            "Datos técnicos: tipo de dispositivo, sistema operativo, versión de la app, identificadores anónimos, etc.\n" +
                            "Usamos tus datos para los siguientes fines:\n" +
                            "\n" +
                            "Mostrarte estadísticas personalizadas sobre tu actividad física.\n" +
                            "\n" +
                            "Dar seguimiento a tus hábitos existentes y sugerirte nuevas rutinas.\n" +
                            "\n" +
                            "Mejorar el rendimiento y seguridad de la aplicación.\n" +
                            "\n" +
                            "Comunicarnos contigo en caso de soporte técnico o notificaciones relevantes.\n" +
                            "\n" +
                            "En ningún caso utilizaremos tus datos con fines publicitarios sin tu consentimiento explícito.\n" +
                            "Tu uso de la app implica el consentimiento para el tratamiento de tus datos bajo los términos descritos en esta política, en el caso de datos sensibles (como salud o ubicación), siempre solicitaremos tu consentimiento explícito.\n" +
                            "No compartimos tu información personal con terceros, salvo en los siguientes casos:\n" +
                            "\n" +
                            "Proveedores tecnológicos como Firebase o Google Analytics, que nos ayudan a ofrecer el servicio.\n" +
                            "\n" +
                            "Obligaciones legales o requerimientos de autoridad competente.\n" +
                            "\n" +
                            "En todos los casos, nos aseguramos de que estos terceros cumplan con estándares adecuados de privacidad.\n" +
                            "Como titular de tus datos, tienes derecho a:\n" +
                            "\n" +
                            "Acceder a tus datos personales.\n" +
                            "\n" +
                            "Rectificar cualquier dato incorrecto o desactualizado.\n" +
                            "\n" +
                            "Cancelar tu información si ya no deseas que sea tratada.\n" +
                            "\n" +
                            "Oponerte al tratamiento de tus datos para ciertos fines.\n" +
                            "\n" +
                            "Para ejercer estos derechos, envía una solicitud al correo: adslatex@gmail.com\n" +
                            "Adoptamos medidas técnicas, administrativas y físicas para proteger tu información, incluyendo:\n" +
                            "\n" +
                            "Cifrado de datos sensibles.\n" +
                            "\n" +
                            "Autenticación segura.\n" +
                            "\n" +
                            "Copias de seguridad regulares.\n" +
                            "\n" +
                            "Restricción de acceso a datos.\n" +
                            "Conservamos tu información únicamente el tiempo necesario para cumplir con las finalidades señaladas." +
                            "\nPuedes solicitar la eliminación de tus datos en cualquier momento.\n" +
                            "Nos reservamos el derecho de modificar esta Política en cualquier momento. En caso de cambios sustanciales, notificaremos a través de la app o por correo electrónico.\n" +
                            "Al usar KOALM, aceptas esta Política de Privacidad. Si no estás de acuerdo, por favor no utilices la aplicación.")
                }
            }
        }
    }
}