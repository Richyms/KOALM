package com.example.koalm.ui.screens.parametroSalud.niveles.ritmoCardiaco

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarViewDay
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.*
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.theme.*
import androidx.compose.ui.draw.clip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import java.io.File
import android.widget.Toast

@Serializable
data class DetailedMeasurement(
    val timestamp: String,
    val value: Double
)

@Serializable
data class Measurement(
    val avg: Double,
    val count: Int,
    val detailedMeasurements: List<DetailedMeasurement>,
    val endTime: String,
    val max: Double,
    val min: Double,
    val startTime: String
)

@Serializable
data class DayData(
    val date: String,
    val measurements: List<Measurement>
)

data class DatosRitmoCardiaco(
    val ritmo: Int,
    val fechaUltimaInfo: String,
    val datos: List<Float>,
    val etiquetasX: List<String> = emptyList(),
    val minValue: Float = 0f,
    val maxValue: Float = 200f
)

enum class TipoVista {
    DIARIA,
    SEMANAL
}

private fun processHeartRateData(heartRateData: List<DayData>, tipo: TipoVista): DatosRitmoCardiaco {
    return when (tipo) {
        TipoVista.DIARIA -> {
            // Procesamos los datos del día más reciente
            val latestDay = heartRateData.maxByOrNull { it.date }
            if (latestDay != null) {
                // Obtenemos todas las mediciones detalladas del día
                val allDetailedMeasurements = latestDay.measurements.flatMap { it.detailedMeasurements }
                    .sortedBy { it.timestamp }

                // Calculamos el promedio general del día
                val avgHeartRate = allDetailedMeasurements.map { it.value }.average().toInt()

                // Agrupamos las mediciones por hora para reducir la densidad de puntos
                val measurementsByHour = allDetailedMeasurements.groupBy { 
                    it.timestamp.split(":")[0].toInt() 
                }.mapValues { (_, values) ->
                    values.map { it.value }.average()
                }

                // Creamos una lista ordenada de horas y valores
                val sortedHourlyData = (0..23)
                    .map { hour ->
                        hour to (measurementsByHour[hour] ?: 0.0)
                    }
                    .filter { it.second > 0 } // Eliminamos las horas sin datos

                DatosRitmoCardiaco(
                    ritmo = avgHeartRate,
                    fechaUltimaInfo = latestDay.date,
                    datos = sortedHourlyData.map { it.second.toFloat() },
                    etiquetasX = sortedHourlyData.map { "${it.first}:00" },
                    minValue = 50f, // Base fija en 50
                    maxValue = maxOf(allDetailedMeasurements.maxOf { it.value }.toFloat(), 120f) // Aseguramos un máximo razonable
                )
            } else {
                DatosRitmoCardiaco(0, "", emptyList(), emptyList())
            }
        }
        TipoVista.SEMANAL -> {
            // Para la vista semanal, usamos los promedios diarios
            val avgHeartRate = heartRateData.flatMap { day -> 
                day.measurements.flatMap { it.detailedMeasurements }
            }.map { it.value }.average().toInt()

            // Calculamos el promedio por día
            val dailyAverages = heartRateData.map { day ->
                val dayMeasurements = day.measurements.flatMap { it.detailedMeasurements }
                dayMeasurements.map { it.value }.average().toFloat()
            }

            // Formateamos las fechas para mostrar solo el día
            val fechasFormateadas = heartRateData.map { it.date.split("/")[0] }

            DatosRitmoCardiaco(
                ritmo = avgHeartRate,
                fechaUltimaInfo = "Promedio semanal",
                datos = dailyAverages,
                etiquetasX = fechasFormateadas,
                minValue = 50f, // Base fija en 50
                maxValue = maxOf(dailyAverages.maxOrNull() ?: 120f, 120f) // Aseguramos un máximo razonable
            )
        }
    }
}

private suspend fun readHeartRateData(
    context: android.content.Context,
    scope: kotlinx.coroutines.CoroutineScope,
    tipo: TipoVista,
    onDataLoaded: (DatosRitmoCardiaco) -> Unit
) {
    try {
        withContext(Dispatchers.IO) {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, "heart_rate_data.json")
            
            if (file.exists()) {
                val jsonString = file.readText()
                val heartRateData = Json.decodeFromString<List<DayData>>(jsonString)
                val processedData = processHeartRateData(heartRateData, tipo)
                withContext(Dispatchers.Main) {
                    onDataLoaded(processedData)
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Archivo no encontrado", Toast.LENGTH_SHORT).show()
                }
            }
        }
    } catch (e: Exception) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Error al leer el archivo: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRitmoCardiaco(
    navController: NavHostController,
    datos: DatosRitmoCardiaco = datosMockRitmo
) {
    val context = LocalContext.current
    var datosState by remember { mutableStateOf(datos) }
    var tipoVista by remember { mutableStateOf(TipoVista.DIARIA) }
    val scope = rememberCoroutineScope()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            scope.launch {
                readHeartRateData(context, scope, tipoVista) { newData ->
                    datosState = newData
                }
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
            } else {
                Toast.makeText(
                    context,
                    "Se necesita permiso para acceder a los archivos",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun checkAndRequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                scope.launch {
                    readHeartRateData(context, scope, tipoVista) { newData ->
                        datosState = newData
                    }
                }
            } else {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
            }
        } else {
            val permission = Manifest.permission.READ_EXTERNAL_STORAGE
            when {
                ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED -> {
                    scope.launch {
                        readHeartRateData(context, scope, tipoVista) { newData ->
                            datosState = newData
                        }
                    }
                }
                else -> {
                    permissionLauncher.launch(permission)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ritmo cardíaco") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { 
                            tipoVista = if (tipoVista == TipoVista.DIARIA) TipoVista.SEMANAL else TipoVista.DIARIA
                            scope.launch {
                                readHeartRateData(context, scope, tipoVista) { newData ->
                                    datosState = newData
                                }
                            }
                        }
                    ) {
                        Icon(
                            if (tipoVista == TipoVista.DIARIA) Icons.Default.CalendarViewWeek else Icons.Default.CalendarViewDay,
                            contentDescription = "Cambiar vista"
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
                .padding(horizontal = 24.dp, vertical = 10.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(50.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("${datosState.ritmo} LPM", fontSize = 36.sp, fontWeight = FontWeight.Bold)
            }

            Text(
                if (tipoVista == TipoVista.DIARIA) 
                    "Datos del día ${datosState.fechaUltimaInfo}" 
                else 
                    "Promedio general",
                fontSize = 11.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = GrisCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .padding(start = 40.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val stepX = size.width / (datosState.datos.size - 1)
                            val valueRange = datosState.maxValue - datosState.minValue
                            val stepY = size.height / valueRange

                            val puntos = datosState.datos.mapIndexed { i, v ->
                                Offset(
                                    x = i * stepX,
                                    y = size.height - ((v - datosState.minValue) * stepY)
                                )
                            }

                            val path = Path().apply {
                                moveTo(puntos.first().x, size.height)
                                puntos.forEach { lineTo(it.x, it.y) }
                                lineTo(puntos.last().x, size.height)
                                close()
                            }

                            drawPath(
                                path = path,
                                brush = Brush.verticalGradient(colors = listOf(Color(0xFFB7EACB), Color.Transparent))
                            )

                            for (i in 0 until puntos.size - 1) {
                                drawLine(
                                    color = Color(0xFFF8844F),
                                    start = puntos[i],
                                    end = puntos[i + 1],
                                    strokeWidth = 3f
                                )
                            }

                            datosState.datos.forEachIndexed { index, valor ->
                                drawCircle(
                                    color = colorZona(valor),
                                    radius = 4.dp.toPx(),
                                    center = puntos[index]
                                )
                            }

                            // Etiquetas del eje Y con valores fijos y distribuidos
                            val etiquetasTextoY = listOf(50, 70, 90, 110, datosState.maxValue.toInt())
                            etiquetasTextoY.forEach { valor ->
                                val posY = size.height - ((valor - datosState.minValue) * stepY)
                                drawContext.canvas.nativeCanvas.drawText(
                                    valor.toString(),
                                    -82f,
                                    posY + 4.dp.toPx(),
                                    android.graphics.Paint().apply {
                                        color = GrisMedio.toArgb()
                                        textSize = 28f
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Etiquetas del eje X
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 36.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        datosState.etiquetasX.forEach { etiqueta ->
                            Text(
                                text = if (tipoVista == TipoVista.DIARIA) {
                                    etiqueta.split(":")[0] + "h"
                                } else {
                                    etiqueta
                                },
                                fontSize = 10.sp,
                                color = GrisMedio
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            val zonasFormateadas = listOf(
                Pair("Ligero", AzulLigero),
                Pair("Aeróbico", VerdeAerobico),
                Pair("Intensivo", AmarilloIntensivo),
                Pair("Anaeróbico", NaranjaAnaerobico),
                Pair("VO Máx", RojoVOMAx)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Primera fila
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    zonasFormateadas.forEach { (zona, color) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(zona, fontSize = 12.sp, color = GrisMedio)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { checkAndRequestPermission() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text("Recuperar datos")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

fun colorZona(valor: Float): Color {
    return when {
        valor <= 60f -> AzulLigero // Ligero
        valor <= 100f -> VerdeAerobico // Aeróbico
        valor <= 140f -> AmarilloIntensivo // Intensivo
        valor <= 170f -> NaranjaAnaerobico // Anaeróbico
        else -> RojoVOMAx // VO Máx
    }
}

val datosMockRitmo = DatosRitmoCardiaco(
    ritmo = 135,
    fechaUltimaInfo = "23/04/25",
    datos = listOf(180f, 60f, 140f, 90f, 88f, 112f, 50f, 145f, 160f, 190f)
)