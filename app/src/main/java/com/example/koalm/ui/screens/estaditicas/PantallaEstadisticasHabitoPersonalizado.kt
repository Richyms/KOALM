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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEstadisticasHabitoPersonalizado(
    navController: NavHostController
) {
    val habitos = remember { mutableStateListOf<HabitoPersonalizado>() }
    val progresoPorHabito = remember { mutableStateMapOf<String, Map<LocalDate, ProgresoDiario>>() }
    val selectedIndex = remember { mutableStateOf(0) }
    val userEmail = FirebaseAuth.getInstance().currentUser?.email
    val db = FirebaseFirestore.getInstance()


    // Cargar hábitos desde Firestore
    LaunchedEffect(Unit) {
        Log.d("Graficador", "Iniciando cargaaaa")
        val habitosSnapshot = userEmail?.let {
            db.collection("habitos")
                .document(it)
                .collection("personalizados")
                .get()
                .await()
        }
        val listaHabitos = habitosSnapshot?.documents?.mapNotNull { doc ->
            doc.toObject(HabitoPersonalizado::class.java)?.copy(nombre = doc.getString("nombre") ?: "")
        }
        habitos.clear()
        if (listaHabitos != null) {
            habitos.addAll(listaHabitos)
        }

        Log.d("Graficador", "Hábitos cargados (${listaHabitos?.size}):")
        listaHabitos?.forEach {
            Log.d("Graficador", " - ${it.nombre}")
        }

        // Para cada hábito, cargar su progreso diario
        listaHabitos?.forEach { habito ->
            val idDoc = habito.nombre.replace(" ", "_")
            val progresoSnapshot = db.collection("habitos")
                .document(userEmail)
                .collection("personalizados")
                .document(idDoc)
                .collection("progreso")
                .get().await()

            val progresoMap = progresoSnapshot.documents.mapNotNull { doc ->
                val fechaStr = doc.getString("fecha") ?: return@mapNotNull null
                val progreso = doc.toObject(ProgresoDiario::class.java) ?: return@mapNotNull null
                Log.d("Graficador", "Progreso cargado para fecha $fechaStr: $progreso")
                try {
                    val fecha = LocalDate.parse(fechaStr)
                    fecha to progreso
                } catch (e: Exception) {
                    null
                }
            }.toMap()

            progresoPorHabito[habito.nombre] = progresoMap
        }
    }

    if (habitos.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Cargando hábitos...")
        }
        return
        }

    val habitoActual = habitos.getOrNull(selectedIndex.value) ?: habitos.first()
    val progresoActual = progresoPorHabito[habitoActual.nombre] ?: emptyMap()
    val colorHabito = parseColorFromFirebase(habitoActual.colorEtiqueta)

    Log.d("Graficador", "progresoActual size: ${progresoActual.size}")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estadísticas hábitos personalizados") },
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
                return habitoActual.frecuencia
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
                            habitos = habitos,
                            selectedIndex = selectedIndex,
                            onSelectedIndexChange = { nuevoIndice ->
                                val nuevoHabito = habitos[nuevoIndice]
                                val fechaInicio = nuevoHabito.fechaInicio?.let {
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
                    frecuenciaPorDefecto = habitoActual.frecuencia,
                    colorHabito = colorHabito,
                    fechaInicioHabito = habitoActual.fechaInicio,
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


@Composable
public fun IndicadorCircular(titulo: String, valor: Int, maximo: Int) {
    val progreso = if (maximo == 0) 0f else valor.toFloat() / maximo.toFloat()
    val colorProgreso = if (progreso == 0f) Color(0xFF9E9E9E) else Color(0xFF2F6B3A)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(64.dp)
        ) {
            CircularProgressIndicator(
                progress = { progreso },
                modifier = Modifier.fillMaxSize(),
                color = colorProgreso,
                strokeWidth = 6.dp,
                trackColor = Color(0xFF9E9E9E),
            )
            Text("$valor días", fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(titulo, fontSize = 14.sp)
    }
}


public fun prepararDatosParaGrafica(
    progresoPorDia: Map<LocalDate, ProgresoDiario>,
    frecuenciaPorDefecto: List<Boolean>?,
    semanaReferencia: LocalDate
): Triple<List<LocalDate>, List<Pair<Float, Float>>, List<String>> {
    val inicioSemana = semanaReferencia
    val fechasSemana = (0..6).map { inicioSemana.plusDays(it.toLong()) }

    val fechasActivas = fechasSemana.filter { fecha ->
        val index = fecha.dayOfWeek.ordinal

        // 1. Progreso del día actual
        val progresoDelDia = progresoPorDia[fecha]

        // 2. Buscar frecuencia válida
        val frecuenciaDia: List<Boolean>? = when {
            progresoDelDia != null -> progresoDelDia.frecuencia
            else -> {
                // Buscar el último progreso anterior a esa fecha
                val frecuenciaAnterior = progresoPorDia
                    .filterKeys { it.isBefore(fecha) }
                    .maxByOrNull { it.key }?.value?.frecuencia

                frecuenciaAnterior ?: frecuenciaPorDefecto
            }
        }

        frecuenciaDia?.getOrNull(index) == true
    }

    val valores = fechasActivas.map { fecha ->
        val progreso = progresoPorDia[fecha]
        val realizados = progreso?.realizados?.toFloat() ?: 0f
        val total = progreso?.totalObjetivoDiario?.toFloat() ?: 1f
        Pair(realizados, total)
    }

    val etiquetas = fechasActivas.map {
        when (it.dayOfWeek) {
            DayOfWeek.MONDAY -> "L"
            DayOfWeek.TUESDAY -> "M"
            DayOfWeek.WEDNESDAY -> "X"
            DayOfWeek.THURSDAY -> "J"
            DayOfWeek.FRIDAY -> "V"
            DayOfWeek.SATURDAY -> "S"
            DayOfWeek.SUNDAY -> "D"
        }
    }

    return Triple(fechasActivas, valores, etiquetas)
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
public fun GraficadorProgresoHabitoSwipe(
    progresoPorDia: Map<LocalDate, ProgresoDiario>,
    frecuenciaPorDefecto: List<Boolean>?, // nombre actualizado
    colorHabito: Color,
    fechaInicioHabito: String?,
    semanaReferencia: LocalDate,
    onSemanaChange: (LocalDate) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val formatter = DateTimeFormatter.ofPattern("dd", Locale("es"))
    val formatterMesAnio = DateTimeFormatter.ofPattern("MMM yyyy", Locale("es"))

    val fechaInicio = fechaInicioHabito?.let {
        LocalDate.parse(it).with(DayOfWeek.MONDAY)
    } ?: LocalDate.MIN

    val semanaActual = LocalDate.now().with(DayOfWeek.MONDAY)
    var semanaAnterior by remember { mutableStateOf(semanaReferencia) }
    var dragAccumulated by remember { mutableStateOf(0f) }
    val offsetX = remember { Animatable(0f) }

    val direccionAnimacion = remember(semanaReferencia) {
        if (semanaReferencia > semanaAnterior) 1 else -1
    }
    semanaAnterior = semanaReferencia

    val inicioSemana = semanaReferencia
    val finSemana = semanaReferencia.plusDays(6)
    val tituloSemana = "Semana del ${inicioSemana.format(formatter)} al ${finSemana.format(formatter)} ${finSemana.format(formatterMesAnio)}"

    val metaMaxima = prepararDatosParaGrafica(
        progresoPorDia = progresoPorDia,
        frecuenciaPorDefecto = frecuenciaPorDefecto,
        semanaReferencia = semanaReferencia
    ).second.maxOfOrNull { it.second.toInt() }?.coerceAtLeast(1) ?: 1

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .offset { IntOffset(offsetX.value.roundToInt(), 0) }
            .pointerInput(fechaInicio, semanaReferencia) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        val siguienteSemana = semanaReferencia.plusWeeks(1)
                        val semanaAnteriorTemp = semanaReferencia.minusWeeks(1)

                        when {
                            dragAccumulated < -100f && !siguienteSemana.isAfter(semanaActual) -> {
                                onSemanaChange(siguienteSemana)
                            }
                            dragAccumulated > 100f && !semanaAnteriorTemp.isBefore(fechaInicio) -> {
                                onSemanaChange(semanaAnteriorTemp)
                            }
                            dragAccumulated < -100f && siguienteSemana.isAfter(semanaActual) -> {
                                coroutineScope.launch {
                                    offsetX.animateTo(-50f, tween(150))
                                    offsetX.animateTo(0f, tween(300, easing = LinearOutSlowInEasing))
                                }
                            }
                            dragAccumulated > 100f && semanaAnteriorTemp.isBefore(fechaInicio) -> {
                                coroutineScope.launch {
                                    offsetX.animateTo(50f, tween(150))
                                    offsetX.animateTo(0f, tween(300, easing = LinearOutSlowInEasing))
                                }
                            }
                        }
                        dragAccumulated = 0f
                    }
                ) { change, dragAmount ->
                    change.consume()
                    dragAccumulated += dragAmount
                }
            }
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = {
                    val nuevaSemana = semanaReferencia.minusWeeks(1)
                    if (!nuevaSemana.isBefore(fechaInicio)) {
                        onSemanaChange(nuevaSemana)
                    } else {
                        coroutineScope.launch {
                            offsetX.animateTo(50f, tween(150))
                            offsetX.animateTo(0f, tween(300, easing = LinearOutSlowInEasing))
                        }
                    }
                },
                enabled = !semanaReferencia.minusWeeks(1).isBefore(fechaInicio)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Semana anterior"
                )
            }

            Text(
                text = tituloSemana,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )

            IconButton(
                onClick = {
                    val nuevaSemana = semanaReferencia.plusWeeks(1)
                    if (!nuevaSemana.isAfter(semanaActual)) {
                        onSemanaChange(nuevaSemana)
                    } else {
                        coroutineScope.launch {
                            offsetX.animateTo(-50f, tween(150))
                            offsetX.animateTo(0f, tween(300, easing = LinearOutSlowInEasing))
                        }
                    }
                },
                enabled = semanaReferencia.isBefore(semanaActual)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Semana siguiente",
                    tint = if (semanaReferencia.isBefore(semanaActual)) LocalContentColor.current else Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        AnimatedContent(
            targetState = semanaReferencia,
            transitionSpec = {
                val direction = direccionAnimacion
                slideInHorizontally { width -> width * direction } + fadeIn() togetherWith
                        slideOutHorizontally { width -> -width * direction } + fadeOut()
            },
            label = "AnimacionSemana"
        ) { semana ->
            val (_, valores, etiquetas) = prepararDatosParaGrafica(
                progresoPorDia = progresoPorDia,
                frecuenciaPorDefecto = frecuenciaPorDefecto,
                semanaReferencia = semana
            )

            GraficadorProgreso(
                valores = valores,
                etiquetas = etiquetas,
                meta = metaMaxima,
                colorHabito = colorHabito
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "Desliza o usa las flechas para cambiar de semana",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

// Función de extensión para formatear la semana
public fun LocalDate.formatSemana(): String {
    val formatter = DateTimeFormatter.ofPattern("d MMM", Locale("es", "ES"))
    val lunes = this.with(DayOfWeek.MONDAY)
    val domingo = this.with(DayOfWeek.SUNDAY)
    return "${formatter.format(lunes)} - ${formatter.format(domingo)}"
}

@Composable
public fun GraficadorProgreso(
    valores: List<Pair<Float, Float>>,
    etiquetas: List<String>,
    colorHabito: Color,
    meta: Int,
    labelEjeY: String = "Objetivo (veces por día)",
    labelEjeX: String = "Días activos del hábito"
){
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(Color(0xFFF0F0F0), shape = RoundedCornerShape(12.dp))
            .padding(start = 43.dp, end = 16.dp, top = 14.dp, bottom = 34.dp)
    ) {
        if (valores.isEmpty() || etiquetas.isEmpty()) return@Canvas

        val barWidth = size.width / valores.size
        val nivelMaximo = meta.coerceAtLeast(1)
        val stepY = size.height / nivelMaximo.toFloat()

        val paintY = android.graphics.Paint().apply {
            textAlign = android.graphics.Paint.Align.RIGHT
            textSize = 24f
            color = android.graphics.Color.GRAY
            isAntiAlias = true
        }

        // Título del eje Y (rotado verticalmente)
        drawContext.canvas.nativeCanvas.apply {
            save()
            rotate(-90f, 0f, size.height / 2)
            drawText(
                labelEjeY,
                -size.height / 12,
                70f,
                android.graphics.Paint().apply {
                    textAlign = android.graphics.Paint.Align.CENTER
                    textSize = 32f
                    color = android.graphics.Color.DKGRAY
                    isAntiAlias = true
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
            )
            restore()
        }

        // Dibujar líneas y etiquetas en eje Y
        for (i in 0..nivelMaximo) {
            val y = size.height - i * stepY
            drawLine(
                color = Color.LightGray.copy(alpha = 0.4f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f,10f),0f)
            )
            drawContext.canvas.nativeCanvas.drawText(
                "$i",
                -30f,
                y + 8f,
                paintY
            )
        }

        // Dibujar barras
        valores.forEachIndexed { index, (realizados, total) ->
            val barHeight = realizados * stepY
            val barCenter = index * barWidth + barWidth / 2
            val barThickness = 8.dp.toPx()
            val barLeft = barCenter - barThickness / 2
            val barRight = barCenter + barThickness / 2
            val barTop = size.height - barHeight
            val barColor = if (realizados >= total) colorHabito else Color.LightGray

            drawRoundRect(
                color = barColor,
                topLeft = Offset(barLeft, barTop),
                size = Size(barRight - barLeft, barHeight),
                cornerRadius = CornerRadius(x = 12f, y = 12f)
            )
        }

        // Etiquetas en eje X
        val paintX = android.graphics.Paint().apply {
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = 28f
            color = android.graphics.Color.DKGRAY
            isAntiAlias = true
        }

        etiquetas.forEachIndexed { index, label ->
            val x = (index * barWidth) + barWidth / 2
            drawContext.canvas.nativeCanvas.drawText(label, x, size.height + 50f, paintX)
        }
        // Título del eje X
        drawContext.canvas.nativeCanvas.drawText(
            labelEjeX,
            size.width / 2,
            size.height + 130f,
            android.graphics.Paint().apply {
                textAlign = android.graphics.Paint.Align.CENTER
                textSize = 32f
                color = android.graphics.Color.DKGRAY
                isAntiAlias = true
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
        )
    }
}

@Composable
public fun SelectorHabitosCentrado(
    habitos: List<HabitoPersonalizado>,
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
                            text = habito.nombre,
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

public fun obtenerDiasActivos(frecuencia: List<Boolean>?): List<Int> {
    if (frecuencia == null) return emptyList()
    return frecuencia.mapIndexedNotNull { index, activo -> if (activo) index + 1 else null }
}

public fun obtenerLunesDeLaSemana(fecha: LocalDate): LocalDate {
    return fecha.with(java.time.DayOfWeek.MONDAY)
}

public fun obtenerFechasActivasDeSemana(fechaReferencia: LocalDate, frecuencia: List<Boolean>?): List<LocalDate> {
    val lunes = obtenerLunesDeLaSemana(fechaReferencia)
    val diasActivos = obtenerDiasActivos(frecuencia)
    return diasActivos.map { diaSemana -> lunes.plusDays((diaSemana - 1).toLong()) }
}

