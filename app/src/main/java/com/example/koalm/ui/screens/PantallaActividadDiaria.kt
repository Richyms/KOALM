package com.example.koalm.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.koalm.R
import com.example.koalm.ui.theme.*
import androidx.compose.ui.tooling.preview.Preview

data class ActividadDiaria(
    val tipo: String,
    val meta: Float,
    val datos: List<Float>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaActividadDiaria(navController: NavController) {
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
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Días
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

            // Selector
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
                            val text = tipos[index % tipos.size]
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
    val lineColors = mapOf(
        "Pasos" to MarronKoala,
        "Calorías quemadas" to GrisOscuro,
        "Tiempo activo" to VerdePrincipal
    )
    val labelsX = listOf("L", "M", "X", "J", "V", "S", "D")
    val maxY = actividad.meta
    val datos = actividad.datos

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(GrisCard, shape = RoundedCornerShape(12.dp))
            .padding(start = 43.dp, end = 16.dp, top = 14.dp, bottom = 34.dp)

    ) {
        val stepX = size.width / (labelsX.size - 1)
        val stepY = size.height / maxY

        val paintY = android.graphics.Paint().apply {
            textAlign = android.graphics.Paint.Align.RIGHT
            textSize = 24f
            color = android.graphics.Color.GRAY
            isAntiAlias = true
        }

        drawIntoCanvas { canvas ->
            for (i in 0..4) {
                val yVal = i * (maxY / 4)
                val y = size.height - yVal * stepY
                canvas.nativeCanvas.drawText(
                    "${yVal.toInt()}",
                    -30f,
                    y + 10f,
                    paintY
                )
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
            }
        }

        val path = Path()
        val color = lineColors[actividad.tipo] ?: Color.Black
        val strokeWidth = 6f

        datos.forEachIndexed { j, value ->
            val x = j * stepX
            val y = size.height - (value * stepY)
            if (j == 0) path.moveTo(x, y) else path.lineTo(x, y)
            drawCircle(color = color, radius = 6f, center = Offset(x, y))
        }

        drawPath(path, color = color, style = Stroke(width = strokeWidth))

        val paintX = android.graphics.Paint().apply {
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = 28f
            setColor(android.graphics.Color.DKGRAY)
            isAntiAlias = true
        }

        drawIntoCanvas { canvas ->
            labelsX.forEachIndexed { j, label ->
                canvas.nativeCanvas.drawText(label, j * stepX, size.height + 70f, paintX)
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun VistaPreviaPantallaActividad() {
    PantallaActividadDiaria(navController = rememberNavController())
}
