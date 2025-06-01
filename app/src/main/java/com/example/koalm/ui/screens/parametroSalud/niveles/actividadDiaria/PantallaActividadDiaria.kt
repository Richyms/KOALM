package com.example.koalm.ui.screens.parametroSalud.niveles.actividadDiaria

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.material3.*
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.koalm.R
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.theme.*
import com.example.koalm.viewmodels.ActividadDiariaViewModel

// ---------------------------------------------------------------------
// Data class ActividadDiaria (si la tienes en otro archivo,
// quita esta definición y usa el import adecuado)
data class ActividadDiaria(
    val tipo: String,
    val meta: Float,
    val datos: List<Float>
)
// ---------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaActividadDiaria(
    navController: NavHostController,
    viewModel: ActividadDiariaViewModel = viewModel()
) {
    // 1) Recolectamos los datos del ViewModel
    val actividadesState by viewModel.actividades.collectAsState(initial = emptyList())

    // 2) Si aún no hay datos, mostramos un indicador de carga
    if (actividadesState.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // 3) Extraemos los tres tipos (Pasos, Calorías quemadas, Tiempo activo)
    val tipos = actividadesState.map { it.tipo }

    // 4) Configuramos el LazyColumn "infinito" para seleccionar tipo
    val listState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    val selectedIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            // Centro del viewport
            val center = layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset / 2
            // Buscamos el ítem más cercano al centro
            layoutInfo.visibleItemsInfo.minByOrNull {
                kotlin.math.abs((it.offset + it.size / 2) - center)
            }?.index?.rem(tipos.size) ?: 0
        }
    }

    // 5) Scroll inicial a un número alto para simular scroll infinito
    LaunchedEffect(Unit) {
        listState.scrollToItem(50_000)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Actividad diaria") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás"
                        )
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

            // --- Encabezado de días (fijos en este ejemplo) ---
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

            // --- Selector de tipo (“Pasos”, “Calorías quemadas”, “Tiempo activo”) ---
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
                        // items(count) proviene de androidx.compose.foundation.lazy.items
                        items(100_000) { index ->
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

                    // Gradiente blanco arriba y abajo (opcional)
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                androidx.compose.ui.graphics.Brush.verticalGradient(
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

            GraficadorActividad(actividad = actividadesState[selectedIndex])

            Spacer(modifier = Modifier.height(16.dp))

            // --- Botón para editar meta según el tipo seleccionado ---
            val rutas = listOf(
                "meta-diaria-pasos",
                "meta-diaria-calorias",
                "meta-diaria-movimiento"
            )
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

    val normalizados = datos.map { (it / meta).coerceIn(0f, 1f) }

    val colores = normalizados.map {
        when {
            it > 0.8f -> MarronKoala
            it > 0.5f -> GrisMedio
            else -> VerdePrincipal
        }
    }

    val diasSemana = listOf("L", "M", "X", "J", "V", "S", "D")
    val graficoHeight = 160.dp

    Row(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .width(40.dp)
                .height(graficoHeight),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "100%", fontSize = 10.sp, color = GrisMedio)
            Text(text = "75%",  fontSize = 10.sp, color = GrisMedio)
            Text(text = "50%",  fontSize = 10.sp, color = GrisMedio)
            Text(text = "25%",  fontSize = 10.sp, color = GrisMedio)
            Spacer(modifier = Modifier.height(0.dp))
        }

        Box(
            modifier = Modifier
                .padding(top = 7.dp)
                .weight(1f)
                .height(graficoHeight)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val heightPx = size.height
                val widthPx = size.width
                val lineY = listOf(0.25f, 0.5f, 0.75f, 1f)


                lineY.forEach { y ->
                    val yPos = heightPx * (1f - y)
                    drawLine(
                        color = GrisClaro,
                        start = Offset(0f, yPos),
                        end = Offset(widthPx, yPos),
                        strokeWidth = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                normalizados.forEachIndexed { index, proporcion ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Box(
                            modifier = Modifier
                                .width(10.dp)
                                .height((proporcion * 160).dp.coerceAtLeast(4.dp))
                                .clip(RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp))
                                .background(colores[index])
                        )
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 40.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        diasSemana.forEach { dia ->
            Text(
                text = dia,
                fontSize = 10.sp,
                color = Color.DarkGray,
                modifier = Modifier.width(20.dp)
            )
        }
    }
}