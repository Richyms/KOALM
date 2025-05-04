package com.example.koalm.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.koalm.R
import com.example.koalm.ui.theme.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaActividadDiaria(navController: NavController) {
    val listState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val items = listOf("Pasos", "Calorías quemadas", "Tiempo activo")

    // Centrar scroll al principio
    LaunchedEffect(Unit) {
        listState.scrollToItem(50000)
    }

    val selectedIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val center = layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset / 2
            layoutInfo.visibleItemsInfo.minByOrNull {
                kotlin.math.abs((it.offset + it.size / 2) - center)
            }?.index?.rem(items.size) ?: 0
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Actividad diaria") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") }, label = { Text("Inicio") }, selected = true, onClick = {})
                NavigationBarItem(icon = { Icon(Icons.Default.Star, contentDescription = "Hábitos") }, label = { Text("Hábitos") }, selected = false, onClick = {})
                NavigationBarItem(icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") }, label = { Text("Perfil") }, selected = false, onClick = {})
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val letrasDias = listOf("S", "M", "T", "W", "T", "F", "S")
                val numerosDias = listOf("6", "7", "8", "9", "10", "11", "12")

                letrasDias.forEachIndexed { index, letra ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = letra,
                            fontWeight = if (index == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (index == 1) VerdePrincipal else Color.Gray
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        if (index == 0) {
                            Image(
                                painter = painterResource(id = R.drawable.running),
                                contentDescription = "Koala día 6",
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = numerosDias[index], color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Image(
                    painter = painterResource(id = R.drawable.running),
                    contentDescription = "Koala corriendo",
                    modifier = Modifier.size(120.dp)
                )

                Box(
                    modifier = Modifier
                        .height(50.dp)
                        .weight(1f)
                        .padding(start = 8.dp)
                ) {
                    LazyColumn(
                        state = listState,
                        flingBehavior = flingBehavior,
                        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(100000) { index ->
                            val text = items[index % items.size]
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = text, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White,
                                        Color.Transparent,
                                        Color.Transparent,
                                        Color.White
                                    )
                                )
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            GraficadorActividad(selectedIndex)

            Spacer(modifier = Modifier.height(16.dp))

            val textosBoton = listOf("Editar pasos", "Editar calorías", "Editar tiempo activo")
            val rutas = listOf("meta-diaria-pasos", "meta-diaria-calorias", "meta-diaria-movimiento")

            Button(
                onClick = { navController.navigate(rutas[selectedIndex]) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal)
            ) {
                Text(textosBoton[selectedIndex])
            }


            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun GraficadorActividad(selectedIndex: Int) {
    val lineColors = listOf(Color.Blue, Color.Red, Color(0xFF795548))
    val lineData = listOf(
        listOf(4f, 6f, 8f, 9f),
        listOf(5f, 7f, 9f, 10f),
        listOf(3f, 6f, 8f, 9f)
    )
    val labels = listOf("L", "M", "Mi", "J")

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(horizontal = 16.dp)
    ) {
        val widthStep = size.width / (lineData[0].size - 1)

        // Dibujar líneas curvas y puntos
        lineData.forEachIndexed { i, line ->
            val path = Path()
            val strokeWidth = if (i == selectedIndex) 5f else 2f
            val color = if (i == selectedIndex) lineColors[i] else lineColors[i].copy(alpha = 0.3f)

            line.forEachIndexed { j, value ->
                val x = j * widthStep
                val y = size.height - (value * 10)

                if (j == 0) {
                    path.moveTo(x, y)
                } else {
                    val prevX = (j - 1) * widthStep
                    val prevY = size.height - (line[j - 1] * 10)
                    val midX = (prevX + x) / 2
                    val midY = (prevY + y) / 2
                    path.quadraticBezierTo(prevX, prevY, midX, midY)
                }

                drawCircle(
                    color = color,
                    radius = 6f,
                    center = androidx.compose.ui.geometry.Offset(x, y)
                )
            }

            drawPath(
                path = path,
                color = color,
                style = Stroke(width = strokeWidth)
            )
        }

        // Etiquetas eje X
        val paint = android.graphics.Paint().apply {
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = 28f
            color = android.graphics.Color.GRAY
            isAntiAlias = true
        }

        drawIntoCanvas { canvas ->
            labels.forEachIndexed { j, label ->
                canvas.nativeCanvas.drawText(
                    label,
                    j * widthStep,
                    size.height + 24f,
                    paint
                )
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun VistaPreviaPantallaActividad() {
    PantallaActividadDiaria(navController = rememberNavController())
}
