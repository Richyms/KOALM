package com.example.koalm.ui.screens.parametroSalud.niveles.peso

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.theme.*
import java.util.Locale
import com.example.koalm.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaControlPeso(
    navController: NavHostController,
    pesoActual: Float = 73f,
    pesoObjetivo: Float = 72f
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Control de peso") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        bottomBar = {
            BarraNavegacionInferior(navController, "inicio")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 10.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.weightcontrol),
                    contentDescription = "Koala",
                    modifier = Modifier
                        .size(200.dp)
                        .align(Alignment.TopCenter)
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(top = 210.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (pesoObjetivo > pesoActual) {
                            String.format(Locale.getDefault(), "Debes ganar %.1f kg", kotlin.math.abs(pesoObjetivo - pesoActual))
                        } else if (pesoObjetivo < pesoActual) {
                            String.format(Locale.getDefault(), "Debes perder %.1f kg", kotlin.math.abs(pesoObjetivo - pesoActual))
                        } else {
                            "No hay cambio de peso"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )

                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ComponenteObjetivos("Peso actual", "Actualizar peso", pesoActual, navController, "actualizar-peso")
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ComponenteObjetivos("Objetivo", "Editar objetivo", pesoObjetivo, navController, "objetivos-peso")
                }
            }

        }
    }
}

@Composable
fun ComponenteObjetivos(titulo : String, textoBoton: String, valor: Float, navController: NavHostController, ruta: String) {
    Text(titulo, fontSize = 14.sp)
    Spacer(modifier = Modifier.height(6.dp))
    Text("$valor kg", fontWeight = FontWeight.Bold, fontSize = 16.sp)
    Spacer(modifier = Modifier.height(12.dp))
    Button(
        onClick = { navController.navigate(ruta) },
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
    ) {
        Text(textoBoton)
    }
}