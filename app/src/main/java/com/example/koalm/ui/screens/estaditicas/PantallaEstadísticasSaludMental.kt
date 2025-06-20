package com.example.koalm.ui.screens.estaditicas

import androidx.compose.animation.*
import android.util.Log
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import kotlinx.coroutines.tasks.await
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.koalm.R
import androidx.compose.ui.tooling.preview.Preview
import com.example.koalm.model.ProgresoDiario
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.material3.Text
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import com.example.koalm.model.HabitoPersonalizado
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import com.example.koalm.ui.theme.*
import java.time.DayOfWeek
import androidx.compose.ui.text.style.TextAlign
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import com.example.koalm.ui.screens.habitos.personalizados.parseColorFromFirebase
import androidx.compose.animation.core.*
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavHostController
import com.example.koalm.ui.components.BarraNavegacionInferior
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import android.graphics.Typeface
import android.widget.Toast
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import com.example.koalm.model.Habito
import com.example.koalm.repository.HabitoRepository
import com.example.koalm.ui.screens.habitos.saludFisica.PantallaSaludFisica
import com.example.koalm.ui.screens.habitos.saludMental.PantallaSaludMental
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEstadísticasSaludMental(
    navController: NavHostController
) {
    //val habitos = remember { mutableStateListOf<HabitoPersonalizado>() }
    val progresoPorHabito = remember { mutableStateMapOf<String, Map<LocalDate, ProgresoDiario>>() }
    val db = FirebaseFirestore.getInstance()
    val habitoRepository = remember { HabitoRepository() }
    val habitosMentales = remember { mutableStateListOf<Habito>() }
    val selectedIndex = remember { mutableStateOf(0) }
    val contexto = LocalContext.current
    val userEmail = FirebaseAuth.getInstance().currentUser?.email


    // Estado de la UI
    var isLoading by remember { mutableStateOf(true) }

    // Cargar hábitos desde Firestore
    LaunchedEffect(userEmail) {
        if (userEmail == null) return@LaunchedEffect

        try {
            Log.d("Graficador", "Iniciando carga de hábitos mentales")

            // 1. Obtener hábitos mentales
            val resultado = habitoRepository.obtenerHabitosMentalesKary(userEmail)
            if (resultado.isSuccess) {
                val listaHabitos = resultado.getOrNull().orEmpty()
                habitosMentales.clear()
                habitosMentales.addAll(listaHabitos)

                Log.d("Graficador", "Hábitos mentales cargados (${listaHabitos.size}):")
                listaHabitos.forEach {
                    Log.d("Graficador", " - ${it.titulo} (${it.id})")
                }

                // 2. Obtener progreso por cada hábito
                progresoPorHabito.clear()

                listaHabitos.forEach { habito ->
                    val progresoSnapshot = db.collection("habitos")
                        .document(userEmail)
                        .collection("predeterminados")
                        .document(habito.id)
                        .collection("progreso")
                        .get()
                        .await()

                    val progresoMap = progresoSnapshot.documents.mapNotNull { doc ->
                        val fechaStr = doc.id // El ID del documento es la fecha (yyyy-MM-dd)
                        val progreso = doc.toObject(ProgresoDiario::class.java)
                        if (progreso != null && fechaStr.isNotBlank()) {
                            try {
                                val fecha = LocalDate.parse(fechaStr)
                                Log.d("Graficador", "Progreso cargado para ${habito.titulo} en $fechaStr")
                                fecha to progreso
                            } catch (e: Exception) {
                                Log.e("Graficador", "Error al parsear fecha: $fechaStr", e)
                                null
                            }
                        } else {
                            null
                        }
                    }.toMap()

                    progresoPorHabito[habito.titulo] = progresoMap
                }
            } else {
                Log.e("Graficador", "Error cargando hábitos mentales: ${resultado.exceptionOrNull()?.message}")
            }

        } catch (e: Exception) {
            Log.e("Graficador", "Error inesperado al cargar hábitos mentales: ${e.message}", e)
        } finally {
            isLoading = false
        }
    }


    if (habitosMentales.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            //navController?.navigate("salud_mental")
            Text("No hay hábitos mentales disponibles")
        }
        return
    }

    val habitoActual = habitosMentales[selectedIndex.value.coerceIn(habitosMentales.indices)]
    val progresoActual = progresoPorHabito[habitoActual.titulo] ?: emptyMap()
    val colorHabito = Color(0xFFF6FBF2)


    Log.d("Graficador", "progresoActual size: ${progresoActual.size}")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estadísticas hábitos mentales") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController?.navigate("menu")
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        bottomBar = {
            BarraNavegacionInferior(navController, "inicio")
        }
    ) { paddingValues ->
        var semanaVisible by remember { mutableStateOf(LocalDate.now().with(DayOfWeek.MONDAY)) }

        val inicioSemana = semanaVisible.with(DayOfWeek.MONDAY)
        val finSemana = inicioSemana.plusDays(6)

        val progresoSemanaActual = progresoActual.filterKeys { fecha ->
            fecha in inicioSemana..finSemana
        }

        val diasSemana = (0..6).map { inicioSemana.plusDays(it.toLong()) }

        fun frecuenciaParaDia(fecha: LocalDate): List<Boolean>? {
            // Si hay progreso para ese día, usarlo
            progresoSemanaActual[fecha]?.frecuencia?.let { return it }

            // Si no, buscar el documento de progreso anterior más reciente
            val fechasAnteriores = progresoActual.keys.filter { it < fecha }.sortedDescending()
            for (fechaAnterior in fechasAnteriores) {
                progresoActual[fechaAnterior]?.frecuencia?.let { return it }
            }
            // Si no hay ningún progreso previo, fallback a frecuencia del hábito general
            return habitoActual.diasSeleccionados
        }

        val diasPlaneados = diasSemana.count { dia ->
            val frecuencia = frecuenciaParaDia(dia)
            if (frecuencia != null) {
                val diaSemanaIndex = (dia.dayOfWeek.value + 6) % 7
                frecuencia.getOrNull(diaSemanaIndex) == true
            } else false
        }

        val diasRegistrados = progresoSemanaActual.count { it.value.completado}

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Calcular rachaActual, rachaMaxima
            val rachaActual = habitoActual.rachaActual
            val rachaMaxima = habitoActual.rachaMaxima

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IndicadorCircular("Racha actual", rachaActual, rachaMaxima)
                IndicadorCircular("Racha máxima", rachaMaxima, rachaMaxima)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.habitosperestadisticas),
                    contentDescription = null,
                    modifier = Modifier
                        .size(120.dp)
                        .weight(0.3f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .weight(0.7f)
                        .height(90.dp)
                ) {
                    SelectorHabitosCentrado(
                        habitos = habitosMentales,
                        selectedIndex = selectedIndex,
                        onSelectedIndexChange = { nuevoIndice ->
                            val nuevoHabito = habitosMentales[nuevoIndice]
                            val fechaInicio = nuevoHabito.fechaCreacion?.let {
                                LocalDate.parse(it).with(DayOfWeek.MONDAY)
                            } ?: LocalDate.now().with(DayOfWeek.MONDAY)

                            semanaVisible = maxOf(fechaInicio, LocalDate.now().with(DayOfWeek.MONDAY))
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "$diasRegistrados/$diasPlaneados días",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.BarChart, contentDescription = "Gráfico")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Log.d("Graficador", "==== LLAVES DEL MAPA progresoPorDia ====")
            progresoActual.keys.forEach { fecha ->
                Log.d("Graficador", "Fecha en progresoPorDia: $fecha")
            }

            GraficadorProgresoHabitoSwipe(
                progresoPorDia = progresoActual,
                frecuenciaPorDefecto = habitoActual.diasSeleccionados,
                colorHabito = colorHabito,
                fechaInicioHabito = habitoActual.fechaCreacion,
                semanaReferencia = semanaVisible,
                onSemanaChange = { nuevaSemana -> semanaVisible = nuevaSemana }
            )

            Spacer(modifier = Modifier.height(15.dp))

            Button(
                onClick = { navController.navigate("gestion_habitos_personalizados") },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF478D4F))
            ) {
                Text("Gestionar hábito")
            }
        }

    }
}


/*-----------------Composables--------------------*/
@Composable
public fun SelectorHabitosCentrado(
    habitos: List<Habito>,
    selectedIndex: MutableState<Int>,
    onSelectedIndexChange: (Int) -> Unit
) {
    if (habitos.isEmpty()) return

    val itemHeight = 40.dp
    val visibleItems = 3
    val listState = rememberLazyListState()

    val density = LocalDensity.current
    val centerOffsetPx = with(density) { (itemHeight * visibleItems / 2).toPx() }

    val centeredIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val center = layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset / 2
            val closest = layoutInfo.visibleItemsInfo.minByOrNull { item ->
                kotlin.math.abs((item.offset + item.size / 2) - center)
            }
            closest?.index ?: selectedIndex.value
        }
    }

    // Actualiza cuando el ítem centrado cambia
    LaunchedEffect(centeredIndex) {
        if (selectedIndex.value != centeredIndex) {
            selectedIndex.value = centeredIndex
            onSelectedIndexChange(centeredIndex)
        }
    }

    // Anima scroll cuando el valor externo cambia
    LaunchedEffect(selectedIndex.value) {
        listState.animateScrollToItem(selectedIndex.value)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Selecciona un hábito",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.DarkGray,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .height(itemHeight * visibleItems)
                .fillMaxWidth()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(vertical = itemHeight)
            ) {
                itemsIndexed(habitos) { index, habito ->
                    val layoutInfo = listState.layoutInfo
                    val itemInfo = layoutInfo.visibleItemsInfo.find { it.index == index }
                    val center = layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset / 2

                    val itemCenter = itemInfo?.let { it.offset + it.size / 2 } ?: 0
                    val distanceFromCenter = kotlin.math.abs(itemCenter - center).toFloat()

                    // Controla escala y transparencia
                    val maxDistance = centerOffsetPx
                    val scaleFactor = 1f - (distanceFromCenter / maxDistance).coerceIn(0f, 0.5f)
                    val alpha = 1f - (distanceFromCenter / maxDistance).coerceIn(0f, 0.6f)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(itemHeight)
                            .graphicsLayer {
                                scaleX = scaleFactor
                                scaleY = scaleFactor
                                this.alpha = alpha
                            }
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (index == selectedIndex.value)
                                    Color(0xFF81C784).copy(alpha = 0.3f)
                                else Color.Transparent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = habito.titulo,
                            fontSize = 16.sp,
                            fontWeight = if (index == selectedIndex.value) FontWeight.SemiBold else FontWeight.Normal,
                            color = Color.Black
                        )
                    }
                }
            }

            // Gradiente arriba y abajo para profundidad
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Color.White,
                            0.15f to Color.Transparent,
                            0.85f to Color.Transparent,
                            1f to Color.White
                        )
                    )

            )
        }
    }
}


