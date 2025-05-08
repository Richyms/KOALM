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
    pesoActual: Float,
    pesoObjetivo: Float
) {
    val pesoInicial = pesoActual + (pesoActual - pesoObjetivo)
    val pesoPerdido = pesoActual - pesoObjetivo

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Control de peso") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = { /* guardar cambios */ }) {
                        Icon(Icons.Default.Check, contentDescription = "Guardar")
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
            Spacer(modifier = Modifier.height(16.dp))

            // 🐨 Koala + progreso
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Imagen central
                Image(
                    painter = painterResource(id = R.drawable.weightcontrol),
                    contentDescription = "Koala",
                    modifier = Modifier
                        .size(200.dp)
                        .align(Alignment.TopCenter)
                )

                // Peso Inicial (izquierda)
                Text(
                    text = String.format("%.1f", pesoInicial),
                    fontSize = 14.sp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 65.dp, top = 170.dp)
                )

                // Peso Objetivo (derecha)
                Text(
                    text = String.format("%.1f", pesoObjetivo),
                    fontSize = 14.sp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 65.dp, top = 170.dp)
                )

                // Texto debajo del koala (centrado más abajo)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(top = 210.dp), // 🔧 esto lo empuja para abajo, debajo del koala
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${String.format("%.1f", pesoPerdido)} kg perdidos",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Text(
                        text = "Actual: ${pesoActual.toInt()} kg",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }


            Spacer(modifier = Modifier.height(26.dp))

            // 📊 Progreso
            Text("Progreso", fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Text("Peso actual", fontSize = 14.sp)
            Text("$pesoActual kg", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            TextButton(onClick = { /* actualizar peso */ }) {
                Text("Actualizar peso", color = VerdePrincipal)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Objetivo", fontSize = 14.sp)

            Text("$pesoObjetivo kg", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            TextButton(onClick = { /* actualizar objetivo */ }) {
                Text("Actualizar objetivo", color = VerdePrincipal)
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun VistaPreviaControlPeso() {
    val navController = rememberNavController()
    navController.navigate("objetivos/74.5/01%20de%20febrero%20del%202025/72/69")
    PantallaControlPeso(navController = navController, pesoActual = 72f, pesoObjetivo = 69f)
}
