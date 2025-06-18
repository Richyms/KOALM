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
fun PantallaEstadisticasSaludFisica(
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
                .collection("predeterminados")
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
                .collection("predeterminados")
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
                title = { Text("Estadísticas hábitos de salud física") },
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
                onClick = { navController.navigate("salud_fisica") },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF478D4F))
            ) {
                Text("Gestionar hábito")
            }
        }

    }
}