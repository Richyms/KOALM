package com.example.koalm.ui.screens.estaditicas

import android.util.Log
import kotlinx.coroutines.tasks.await
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.koalm.R
import com.example.koalm.model.ProgresoDiario
import androidx.compose.material3.Text
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.DayOfWeek
import androidx.navigation.NavHostController
import com.example.koalm.ui.components.BarraNavegacionInferior
import androidx.compose.ui.platform.LocalContext
import com.example.koalm.model.Habito
import com.example.koalm.repository.HabitoRepository


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEstadisticasSaludFisica(
    navController: NavHostController
) {
    val progresoPorHabito = remember { mutableStateMapOf<String, Map<LocalDate, ProgresoDiario>>() }
    val db = FirebaseFirestore.getInstance()
    val habitoRepository = remember { HabitoRepository() }
    val habitosFisicos = remember { mutableStateListOf<Habito>() }
    val selectedIndex = remember { mutableStateOf(0) }
    val contexto = LocalContext.current
    val userEmail = FirebaseAuth.getInstance().currentUser?.email


    // Estado de la UI
    var isLoading by remember { mutableStateOf(true) }

    // Cargar hábitos desde Firestore
    LaunchedEffect(userEmail) {
        if (userEmail == null) return@LaunchedEffect

        try {
            Log.d("Graficador", "Iniciando carga de hábitos físicos")

            // 1. Obtener hábitos mentales
            val resultado = habitoRepository.obtenerHabitosFisicosKary(userEmail)
            if (resultado.isSuccess) {
                val listaHabitos = resultado.getOrNull().orEmpty()
                habitosFisicos.clear()
                habitosFisicos.addAll(listaHabitos)

                Log.d("Graficador", "Hábitos físicos cargados (${listaHabitos.size}):")
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
                Log.e("Graficador", "Error cargando hábitos físicos: ${resultado.exceptionOrNull()?.message}")
            }

        } catch (e: Exception) {
            Log.e("Graficador", "Error inesperado al cargar hábitos físicos: ${e.message}", e)
        } finally {
            isLoading = false
        }
    }

    if (habitosFisicos.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            //navController?.navigate("salud_mental")
            Text("No hay hábitos físicos disponibles")
        }
        return
    }

    val habitoActual = habitosFisicos[selectedIndex.value.coerceIn(habitosFisicos.indices)]
    val progresoActual = progresoPorHabito[habitoActual.titulo] ?: emptyMap()
    val colorHabito = Color(0xFFF6FBF2)


    Log.d("Graficador", "progresoActual size: ${progresoActual.size}")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estadísticas hábitos físicos") },
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
                        habitos = habitosFisicos,
                        selectedIndex = selectedIndex,
                        onSelectedIndexChange = { nuevoIndice ->
                            val nuevoHabito = habitosFisicos[nuevoIndice]
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





