package com.example.koalm.ui.screens.parametroSalud.niveles.peso

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.theme.VerdePrincipal
import com.example.koalm.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaControlPeso(
    navController: NavHostController,
    pesoActual: Float ?= 72f,
    pesoObjetivo: Float ?= 69f
) {
    val pesoA = pesoActual ?: 72f
    val pesoO = pesoObjetivo ?: 69f
    val pesoPerdido = pesoA - pesoO

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
                        text = "${String.format("%.1f", pesoPerdido)} kg perdidos",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            Text("Progreso", fontSize = 23.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Text("Peso actual", fontSize = 14.sp)
            Text("$pesoActual kg", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            TextButton(onClick = {
                navController.navigate("actualizar-peso")
            }) {
                Text("Actualizar peso", color = VerdePrincipal)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Objetivo", fontSize = 14.sp)

            Text("$pesoObjetivo kg", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            TextButton(onClick = {
                navController.navigate("objetivos-peso")
            }) {
                Text("Actualizar objetivo", color = VerdePrincipal)
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun VistaPreviaPantallaControlPeso() {
    PantallaControlPeso(
        navController = rememberNavController(),
    )
}
