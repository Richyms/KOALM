// PantallaActividadDiaria.kt
package com.example.koalm.ui.screens.parametroSalud.niveles.actividadDiaria

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
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

            // --- Gráfica de barras sin Canvas, sólo Boxes proporcionales ---
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

    // 1) Normalizamos cada valor en un rango [0f..1f]
    val normalizados = datos.map { (it / meta).coerceIn(0f, 1f) }

    // 2) Elegimos un color según el porcentaje
    val colores = normalizados.map {
        when {
            it > 0.8f -> MarronKoala
            it > 0.5f -> GrisMedio
            else       -> VerdePrincipal
        }
    }

    // 3) Etiquetas de días (fijas)
    val diasSemana = listOf("L", "M", "X", "J", "V", "S", "D")

    // 4) Altura total que usará la “gráfica” (ajústala si quieres más o menos alto)
    val graficoHeight = 160.dp

    Row(modifier = Modifier.fillMaxWidth()) {
        // ┌───────────────────────────────────────────────────────────────────┐
        // │    Eje Y: Mostramos 5 etiquetas de valores (“100%”, “75%”, etc.)   │
        // │    en un Column con Arrangement.SpaceBetween para que queden   │
        // │    espaciadas uniformemente dentro de graficoHeight.            │
        // └───────────────────────────────────────────────────────────────────┘
        Column(
            modifier = Modifier
                .width(40.dp)
                .height(graficoHeight),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Si quieres mostrar valores absolutos en lugar de “%”:
            // Text(text = meta.toInt().toString(), fontSize = 10.sp, color = Color.Gray)
            // Text(text = (meta * 0.75f).toInt().toString(), fontSize = 10.sp, color = Color.Gray)
            // Text(text = (meta * 0.5f).toInt().toString(), fontSize = 10.sp, color = Color.Gray)
            // Text(text = (meta * 0.25f).toInt().toString(), fontSize = 10.sp, color = Color.Gray)
            // Text(text = "0", fontSize = 10.sp, color = Color.Gray)

            // O, si prefieres mostrar porcentajes:
            Text(text = "100%", fontSize = 10.sp, color = Color.Gray)
            Text(text = "75%",  fontSize = 10.sp, color = Color.Gray)
            Text(text = "50%",  fontSize = 10.sp, color = Color.Gray)
            Text(text = "25%",  fontSize = 10.sp, color = Color.Gray)
            Text(text = "0%",   fontSize = 10.sp, color = Color.Gray)
        }

        // ┌───────────────────────────────────────────────────────────────────┐
        // │    Contenedor del “área de la gráfica”:                            │
        // │    • Dibuja las líneas de grilla (Divider) en background.         │
        // │    • Encima, coloca un Row con las barras (Boxes)                │
        // │      cuya altura viene dada por normalizados[index].             │
        // └───────────────────────────────────────────────────────────────────┘
        Box(
            modifier = Modifier
                .weight(1f)
                .height(graficoHeight)
        ) {
            // a) Column que dibuja 4 Divider + espacios, para que queden 4 líneas
            Column(modifier = Modifier.fillMaxSize()) {
                // 4 repeticiones de: 1 Spacer + 1 Divider
                repeat(4) {
                    Spacer(modifier = Modifier.weight(1f))
                    Divider(color = Color.LightGray, thickness = 1.dp)
                }
                // Finalmente un Spacer al pie para completar el espacio
                Spacer(modifier = Modifier.weight(1f))
            }

            // b) Row superpuesto (z-index default) para dibujar las 7 barras
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
                        // 4.1) Mostramos el valor crudo por encima de cada barra
                        Text(
                            text = datos[index].toInt().toString(),
                            fontSize = 10.sp,
                            color = Color.DarkGray,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        // 4.2) La “barra” en sí misma (Box) con altura proporcional
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .fillMaxHeight(proporcion)
                                .background(
                                    color = colores[index],
                                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                )
                        )
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // ┌───────────────────────────────────────────────────────────────────┐
    // │    Etiquetas de los días debajo de cada barra, alineadas al eje  │
    // │    X. Hacemos un padding izquierdo igual a 40.dp para            │
    // │    compensar el ancho del eje Y que mostramos arriba.            │
    // └───────────────────────────────────────────────────────────────────┘
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

@Preview(showSystemUi = true)
@Composable
fun VistaPreviaPantallaActividad() {
    PantallaActividadDiaria(navController = rememberNavController())
}
