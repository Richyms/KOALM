// PantallaActividadDiaria.kt
package com.example.koalm.ui.screens.parametroSalud.niveles.actividadDiaria

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue                // <-- para “by remember { … }”
import androidx.compose.runtime.setValue                // <-- para “var x by remember { … }”
import androidx.compose.runtime.collectAsState          // <-- para “collectAsState(…)”
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import java.time.DayOfWeek
import java.time.LocalDate

// Partes para las graficas
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.draw.clip


// ---------------------------------------------------------------------
// Data class ActividadDiaria. Si ya la tienes en otro archivo,
// coméntala aquí y haz import de donde esté definida.
// ---------------------------------------------------------------------
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
    // 1) Recolectamos los datos del ViewModel (StateFlow) y los convertimos a State<…>
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

    // 4) Configuramos el LazyColumn “infinito” para seleccionar tipo
    val listState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    //  -- derivedStateOf devuelve un State<Int>, así que “by” necesita getValue
    val selectedIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val center = layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset / 2
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

            // --------------------------------------------------------
            // 1) Calculamos "hoy" y la semana actual (lunes a domingo)
            val hoy = LocalDate.now()
            val lunesSemana = hoy.with(DayOfWeek.MONDAY)
            val fechasSemana = List(7) { lunesSemana.plusDays(it.toLong()) }
            val letrasDias = listOf("L", "M", "X", "J", "V", "S", "D")
            val numerosDias = fechasSemana.map { it.dayOfMonth.toString() }
            val indiceHoy = hoy.dayOfWeek.value - 1

            // 2) Estado local para el día seleccionado (inicial = hoy)
            var diaSeleccionado by remember { mutableStateOf(indiceHoy) }
            // --------------------------------------------------------

            // --- Encabezado de días (dinámico y clicable) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                letrasDias.forEachIndexed { index, letra ->
                    val esSeleccionado = index == diaSeleccionado
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { diaSeleccionado = index }
                    ) {
                        // 1) Letra del día (verde + negrita si está seleccionado; gris si no)
                        Text(
                            text = letra,
                            fontWeight = if (esSeleccionado) FontWeight.Bold else FontWeight.Normal,
                            color = if (esSeleccionado) VerdePrincipal else Color.Gray
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // 2) Ícono de koala solo en el día seleccionado
                        if (esSeleccionado) {
                            Image(
                                painter = painterResource(id = R.drawable.running),
                                contentDescription = "Día seleccionado",
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // 3) Mostrar número del día para los que NO están seleccionados
                        if (!esSeleccionado) {
                            Text(
                                text = numerosDias[index],
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
            // --------------------------------------------------------

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
                        // EN LUGAR DE “items(100_000) { … }” (que daba ambigüedad),
                        // usamos “items(count = 100_000) { … }” para forzar el overload correcto:
                        items(count = 100_000) { index ->
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

            // --- Gráfica que muestra el dato del día seleccionado ---
            GraficadorActividadDia(
                actividad = actividadesState[selectedIndex],
                indiceSeleccionado = diaSeleccionado
            )

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


/**
 *  Esta función dibuja una sola barra en vertical, correspondiente
 *  a “actividad.datos[indiceSeleccionado]”.  Dado que el ViewModel ya
 *  alineó cada posición 0..6 con lunes..domingo, aquí solo hacemos:
 *    - Valor crudo arriba de la barra.
 *    - Barra (Box) con altura proporcional.
 *    - Debajo, la letra del día que se corresponda.
 */
@Composable
fun GraficadorActividadDia(
    actividad: ActividadDiaria,
    indiceSeleccionado: Int
) {
    val valorDia = actividad.datos.getOrNull(indiceSeleccionado) ?: 0f
    val meta = actividad.meta
    val proporcion = (valorDia / meta).coerceIn(0f, 1f)

    val colorBarra = when {
        proporcion > 0.8f -> MarronKoala
        proporcion > 0.5f -> GrisMedio
        else              -> VerdePrincipal
    }

    val graficoHeight = 180.dp
    val ejeYWidth = 40.dp
    val letrasDias = listOf("L", "M", "X", "J", "V", "S", "D")
    val letraDia = letrasDias.getOrNull(indiceSeleccionado) ?: ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(graficoHeight)
    ) {
        Box(
            modifier = Modifier
                .width(ejeYWidth)
                .fillMaxHeight()
                .padding(end = 8.dp)
        ) {
            val niveles = listOf("100%",  "75%", "50%", "25%", "0%")
            val posiciones = listOf(1f, 0.75f, 0.5f, 0.25f, 0f)
            niveles.forEachIndexed { i, texto ->
                Text(
                    text = texto,
                    fontSize = 10.sp,
                    color = Color.Gray,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = -(posiciones[i] * (graficoHeight.value - 10)).dp)
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
                val lineY = listOf(0f, 0.25f, 0.5f, 0.75f, 1f)
                lineY.forEach { y ->
                    val yPos = heightPx * (1 - y)
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        start = Offset(0f, yPos),
                        end = Offset(widthPx, yPos),
                        strokeWidth = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .height((proporcion * (graficoHeight.value - 40)).dp)
                            .clip(RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp))
                            .background(colorBarra)
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = ejeYWidth),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = letraDia,
            fontSize = 10.sp,
            color = Color.DarkGray,
            modifier = Modifier.width(6.dp)
        )
    }
}


