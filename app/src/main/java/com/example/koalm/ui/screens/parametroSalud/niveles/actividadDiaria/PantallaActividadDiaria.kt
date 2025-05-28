package com.example.koalm.ui.screens.parametroSalud.niveles.actividadDiaria

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import com.example.koalm.ui.components.BarraNavegacionInferior
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.koalm.R
import com.example.koalm.ui.theme.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController

data class ActividadDiaria(
    val tipo: String,
    val meta: Float,
    val datos: List<Float>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaActividadDiaria(navController: NavHostController) {
    val tipos = listOf("Pasos", "Calorías quemadas", "Tiempo activo")
    var actividades by remember { mutableStateOf<List<ActividadDiaria>>(emptyList()) }

    LaunchedEffect(Unit) {
        actividades = listOf(
            ActividadDiaria("Pasos", 8000f, listOf(2000f, 4500f, 1200f, 8000f, 7800f, 4000f, 3000f)),
            ActividadDiaria("Calorías quemadas", 500f, listOf(100f, 200f, 300f, 400f, 350f, 250f, 200f)),
            ActividadDiaria("Tiempo activo", 180f, listOf(30f, 60f, 90f, 120f, 100f, 70f, 60f))
        )
    }

    val listState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    val selectedIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val center = layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset / 2
            layoutInfo.visibleItemsInfo.minByOrNull {
                kotlin.math.abs((it.offset + it.size / 2) - center)
            }?.index?.rem(tipos.size) ?: 0
        }
    }

    LaunchedEffect(Unit) {
        listState.scrollToItem(50000)
    }

    if (actividades.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
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
            BarraNavegacionInferior(navController, "inicio")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            val letrasDias = listOf("L", "M", "X", "J", "V", "S", "D")
            val numerosDias = listOf("6", "7", "8", "9", "10", "11", "12")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
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
                                contentDescription = null,
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
                    contentDescription = null,
                    modifier = Modifier.size(120.dp)
                )

                Box(
                    modifier = Modifier
                        .height(90.dp)
                        .weight(1f)
                        .padding(start = 8.dp)
                ) {
                    LazyColumn(
                        state = listState,
                        flingBehavior = flingBehavior,
                        verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(100000) { index ->
                            val actualIndex = index % tipos.size
                            val isSelected = actualIndex == selectedIndex

                            val alpha = if (isSelected) 1f else 0.4f
                            val fontSize = if (isSelected) 18.sp else 14.sp
                            val itemHeight = if (isSelected) 40.dp else 30.dp

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(itemHeight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tipos[actualIndex],
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize = fontSize,
                                    color = Color.Black.copy(alpha = alpha)
                                )
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

            GraficadorActividad(actividad = actividades[selectedIndex])

            Spacer(modifier = Modifier.height(16.dp))

            val rutas = listOf("meta-diaria-pasos", "meta-diaria-calorias", "meta-diaria-movimiento")
            Button(
                onClick = { navController.navigate(rutas[selectedIndex]) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal)
            ) {
                Text("Editar objetivo")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun GraficadorActividad(actividad: ActividadDiaria) {
    val datos = actividad.datos
    val meta = actividad.meta
    val normalizados = datos.map { it / meta }

    val colores = normalizados.map {
        when {
            it > 0.8f -> MarronKoala
            it > 0.5f -> GrisMedio
            else -> VerdePrincipal
        }
    }

    val diasSemana = listOf("L", "M", "X", "J", "V", "S", "D")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = GrisCard)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
            ) {
                Box(
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .fillMaxHeight()
                        .width(30.dp)
                ) {
                    val pasos = 5
                    val nivelesY = List(pasos) { i ->
                        val valor = meta * (pasos - i) / pasos
                        val proporcion = (pasos - i).toFloat() / pasos
                        valor to proporcion
                    }
                    nivelesY.forEach { (valor, y) ->
                        Text(
                            text = valor.toInt().toString(),
                            fontSize = 11.sp,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(y = -(y * 160).dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val heightPx = size.height
                        val widthPx = size.width

                        val lineY = listOf(0.2f, 0.4f, 0.6f, 0.8f, 1f)
                        val dash = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

                        lineY.forEach { y ->
                            val yPos = heightPx * (1 - y)
                            drawLine(
                                color = Color.LightGray.copy(alpha = 0.5f),
                                start = Offset(0f, yPos),
                                end = Offset(widthPx, yPos),
                                strokeWidth = 2f,
                                pathEffect = dash
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        normalizados.forEachIndexed { index, valor ->
                            Box(
                                modifier = Modifier
                                    .width(8.dp)
                                    .height((valor * 160).dp)
                                    .clip(RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp))
                                    .background(colores[index])
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 41.dp, end = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                normalizados.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier.width(22.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = diasSemana[index],
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}


@Preview(showSystemUi = true)
@Composable
fun VistaPreviaPantallaActividad() {
    PantallaActividadDiaria(navController = rememberNavController())
}
