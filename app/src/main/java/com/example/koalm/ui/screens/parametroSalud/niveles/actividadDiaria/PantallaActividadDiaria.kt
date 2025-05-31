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
import com.example.koalm.ui.theme.GrisMedio
import com.example.koalm.ui.theme.MarronKoala
import com.example.koalm.ui.theme.VerdePrincipal
import com.example.koalm.viewmodels.ActividadDiariaViewModel
import java.time.DayOfWeek
import java.time.LocalDate

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
                            .clickable { diaSeleccionado = index }  // Al hacer clic, actualizamos el día seleccionado
                    ) {
                        // 1) Letra del día (verde + negritas si está seleccionado; gris en caso contrario)
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

                        // 3) Número del día para los que no están seleccionados
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
 * Ahora esta función grafica el valor del día que se seleccione:
 *   - Obtiene el Float en  `actividad.datos[indiceSeleccionado]`
 *   - Normaliza contra su meta
 *   - Dibuja UNA sola "barra" (o punto) centrada
 *   - Debajo muestra la letra del día correspondiente a índiceSeleccionado
 */
@Composable
fun GraficadorActividadDia(
    actividad: ActividadDiaria,
    indiceSeleccionado: Int
) {
    // 1) Extraemos únicamente el dato del día seleccionado
    val valorDia = actividad.datos.getOrNull(indiceSeleccionado) ?: 0f
    val meta = actividad.meta

    // 2) Normalizamos en [0f..1f]
    val proporcion = (valorDia / meta).coerceIn(0f, 1f)

    // 3) Elegimos color según porcentaje
    val colorBarra = when {
        proporcion > 0.8f -> MarronKoala
        proporcion > 0.5f -> GrisMedio
        else               -> VerdePrincipal
    }

    // 4) Altura total de la "gráfica"
    val graficoHeight = 160.dp

    // 5) Letra del día con base en índice (L, M, X, J, V, S, D)
    val letrasDias = listOf("L", "M", "X", "J", "V", "S", "D")
    val letraDia = letrasDias.getOrNull(indiceSeleccionado) ?: ""

    Row(modifier = Modifier.fillMaxWidth()) {
        // ┌───────────────────────────────────────────────────────────────────┐
        // │    Eje Y: mostramos 5 etiquetas (“100%”, “75%”, etc.)           │
        // └───────────────────────────────────────────────────────────────────┘
        Column(
            modifier = Modifier
                .width(40.dp)
                .height(graficoHeight),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "100%", fontSize = 10.sp, color = Color.Gray)
            Text(text = "75%",  fontSize = 10.sp, color = Color.Gray)
            Text(text = "50%",  fontSize = 10.sp, color = Color.Gray)
            Text(text = "25%",  fontSize = 10.sp, color = Color.Gray)
            Text(text = "0%",   fontSize = 10.sp, color = Color.Gray)
        }

        // ┌───────────────────────────────────────────────────────────────────┐
        // │    Contenedor de la "gráfica" con UNA sola barra                │
        // └───────────────────────────────────────────────────────────────────┘
        Box(
            modifier = Modifier
                .weight(1f)
                .height(graficoHeight)
        ) {
            // a) Líneas de grilla (4 divisores)
            Column(modifier = Modifier.fillMaxSize()) {
                repeat(4) {
                    Spacer(modifier = Modifier.weight(1f))
                    Divider(color = Color.LightGray, thickness = 1.dp)
                }
                Spacer(modifier = Modifier.weight(1f))
            }

            // b) Dibujamos UNA sola barra, centrada
            Row(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    // 1) Valor crudo encima
                    Text(
                        text = valorDia.toInt().toString(),
                        fontSize = 10.sp,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    // 2) "Barra" proporcional
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .fillMaxHeight(proporcion)
                            .background(
                                color = colorBarra,
                                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                            )
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // ┌───────────────────────────────────────────────────────────────────┐
    // │  Etiqueta DEBAJO de la barra: solo la letra del día seleccionado│
    // └───────────────────────────────────────────────────────────────────┘
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 40.dp),  // Misma compensación que antes
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = letraDia,
            fontSize = 10.sp,
            color = Color.DarkGray,
            modifier = Modifier.width(20.dp)
        )
    }
}

@Preview(showSystemUi = true)
@Composable
fun VistaPreviaPantallaActividad() {
    PantallaActividadDiaria(navController = rememberNavController())
}
