package com.example.koalm.ui.screens
import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.core.content.edit
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.koalm.R
import com.example.koalm.ui.theme.*
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import com.google.android.gms.auth.api.identity.Identity
import com.example.koalm.model.HabitoPersonalizado
import com.example.koalm.ui.viewmodels.DashboardViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.koalm.model.ProgresoDiario
import com.example.koalm.ui.components.obtenerIconoPorNombre
import com.example.koalm.ui.screens.habitos.personalizados.parseColorFromFirebase
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaMenuPrincipal(navController: NavHostController) {
    val diasDeLaSemana = listOf("L", "M", "M", "J", "V", "S", "D")
    val progreso = listOf(true, true, true, true, false, false, false)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val habitosKoalisticos = listOf(
        Triple("Meditación koalística", "Encuentra tu paz interior como un koala en su árbol favorito.", R.drawable.koala_naturaleza),
        Triple("Alimentación consciente", "Disfruta cada hoja de eucalipto como si fuera la primera.", R.drawable.koala_comiendo),
        Triple("Meditación koalística", "Meditar como un koala: profundo y reparador.", R.drawable.koala_meditando)
    )

    // Obtener el nombre de nuestro usuaio KOOL
    val usuarioEmail = FirebaseAuth.getInstance().currentUser?.email
    val db = FirebaseFirestore.getInstance()
    var username by remember { mutableStateOf("") }

    LaunchedEffect(usuarioEmail) {
        if (usuarioEmail != null) {
            if (usuarioEmail.isNotEmpty()) {
                db.collection("usuarios")
                    .document(usuarioEmail)
                    .get()
                    .addOnSuccessListener { doc ->
                        username = doc.getString("username").orEmpty()
                    }
                    .addOnFailureListener {
                        username = "Kool"
                    }
            }
        }
    }


    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = { DrawerContenido(navController) }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("¡Hola, $username! 🐨✨") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* Configuración */ }) {
                            Icon(Icons.Default.Settings, contentDescription = "Configuración")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            },
            bottomBar = {
                BarraNavegacionInferior(
                    navController = navController,
                    rutaActual = "menu"
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SeccionTitulo("Racha")
                FormatoRacha(dias = diasDeLaSemana.zip(progreso))

                SeccionTitulo("Hábitos koalísticos")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(habitosKoalisticos) { (titulo, descripcion, imagenId) ->
                        HabitoCarruselItem(titulo, descripcion, imagenId)
                    }
                }

                SeccionTitulo("Mis hábitos")
                if (usuarioEmail != null) {
                    DashboardScreen(usuarioEmail = usuarioEmail)
                }

                SeccionTitulo("Estadísticas")
                EstadisticasCard()
                val context = LocalContext.current
                Text(
                    text = "Cerrar sesión (debug)",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clickable {
                            cerrarSesion(context, navController)
                        }
                        .padding(bottom = 24.dp)
                )
            }
        }
    }
}


@Suppress("DEPRECATION")
fun cerrarSesion(context: Context, navController: NavHostController) {
    FirebaseAuth.getInstance().signOut()

    @Suppress("DEPRECATION")
    Identity.getSignInClient(context)
        .signOut()
        .addOnCompleteListener {
            // …
        }
    // 3. Borra SharedPreferences con extensión KTX
    context.getSharedPreferences(
        context.getString(R.string.prefs_file),
        Context.MODE_PRIVATE
    ).edit {
        clear()
    }

    // 4. Redirige a la pantalla de inicio y limpia el back stack
    navController.navigate("iniciar") {
        popUpTo("menu") { inclusive = true }
    }
}
@Composable
fun SeccionTitulo(texto: String) {
    Text(
        text = texto,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = VerdePrincipal
    )
}


@Composable
fun EstadisticasCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .border(1.dp, VerdeBorde, RoundedCornerShape(16.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(VerdeContenedor),
            contentAlignment = Alignment.Center
        ) {
            Text("Gráficos de estadísticas", color = GrisMedio)
        }
    }
}




@Composable
fun DrawerContenido(navController: NavHostController) {
    ModalDrawerSheet {
        Text("Koalm", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.headlineMedium)
        HorizontalDivider()
        listOf("Inicio", "Racha", "Parametros de salud", "Test de emociones").forEach {
            NavigationDrawerItem(label = { Text(it) }, selected = it == "Inicio", onClick = {
                when (it) {
                    //"Inicio" -> navController.navigate("inicio")
                    "Racha" -> navController.navigate("racha_habitos")
                    "Parametros de salud" -> navController.navigate("estadisticas")
                    //"Test de emociones" -> navController.navigate("test_emociones")
                }
            })
        }
        HorizontalDivider()
        Text("Hábitos", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleSmall)
        listOf("Salud física", "Salud mental", "Personalizados").forEach {
            NavigationDrawerItem(label = { Text(it) }, selected = false, onClick = {
                when(it){
                    "Salud mental" -> navController.navigate("estadisticas_salud_mental")
                    "Salud física" -> navController.navigate("estadisticas_salud_fisica")
                }
            })
        }
        HorizontalDivider()
        Text("Labels", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleSmall)
        listOf("Recomendaciones de Kool", "Insignias y logros", "Información de la app").forEach {
            NavigationDrawerItem(label = { Text(it) }, selected = false, onClick = {
                when (it){
                    "Salud mental" -> navController.navigate("estadisticas_salud_mental")
                    "Salud física" -> navController.navigate("estadisticas_salud_fisica")
                }
            })
        }
    }
}


@Composable
fun FormatoRacha(dias: List<Pair<String, Boolean>>) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, VerdeBorde, RoundedCornerShape(16.dp))
            .padding(vertical = 24.dp, horizontal = 12.dp)
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(dias) { dia ->
                val (letra, completado) = dia
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (completado) VerdePrincipal else GrisClaro,
                        modifier = Modifier.size(48.dp)
                    ) {
                        if (completado) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Completado",
                                tint = Blanco,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                    Text(
                        text = letra,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Black,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HabitoCarruselItem(titulo: String, descripcion: String, imagenId: Int) {
    Box(
        modifier = Modifier
            .size(width = 200.dp, height = 120.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, VerdeBorde, RoundedCornerShape(16.dp))
    ) {
        Image(
            painter = painterResource(id = imagenId),
            contentDescription = titulo,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.2f),
                            Color.Black.copy(alpha = 0.4f),
                            Color.Black.copy(alpha = 0.6f)
                        )
                    )
                )
                .padding(8.dp)
        ) {
            Column {
                Text(
                    titulo,
                    color = Blanco,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    descripcion,
                    color = Blanco,
                    fontSize = 10.sp,
                    maxLines = 2
                )
            }
        }
    }
}


@Composable
fun DashboardScreen(
    usuarioEmail: String,
    viewModel: DashboardViewModel = viewModel()
) {
    val habitos = viewModel.habitos
    val cargando = viewModel.cargando

    var tipoSeleccionado by remember { mutableStateOf("todos") }
    val tipos = listOf("todos", "personalizado", "físico", "mental")

    // Cargar los hábitos
    LaunchedEffect(usuarioEmail) {
        viewModel.cargarHabitos(usuarioEmail)
    }

        // Filtros de tipo
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tipos) { tipo ->
                val isSelected = tipo == tipoSeleccionado

                TextButton(
                    onClick = { tipoSeleccionado = tipo },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) Color(0xFFF6FBF2) else Color.Transparent,
                        contentColor = if (isSelected) Color.Black else Color.Gray
                    ),
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(tipo.replaceFirstChar { it.uppercaseChar() })
                }
            }
        }

        if (cargando) {
            // Mostrar un indicador de carga mientras se están obteniendo los datos
            CircularProgressIndicator()
        } else {
            // Filtrar los hábitos según el tipo seleccionado
            val diaActual = (LocalDate.now().dayOfWeek.value + 6) % 7

            val habitosFiltrados = habitos.filter { habito ->
                val tipoCoincide = tipoSeleccionado == "todos" || habito.tipo.equals(tipoSeleccionado, ignoreCase = true)

                val frecuencia = habito.frecuencia

                val frecuenciaEsDiaria = frecuencia == null || frecuencia.all { it == false }

                val diaActivo = frecuenciaEsDiaria || (frecuencia?.getOrNull(diaActual) == true)

                tipoCoincide && diaActivo
            }

            // Mostrar los hábitos filtrados
            if (habitosFiltrados.isNotEmpty()) {
                habitosFiltrados.forEach { habito ->
                    HabitoCardPersonalizado (
                        habito = habito,
                        progreso = viewModel.progresos[habito.nombre.replace(" ", "_")],
                        onIncrementar = {
                            viewModel.incrementarProgreso(usuarioEmail,habito)
                        }
                    )
                }
            } else {
                // Mensaje si no hay hábitos para mostrar
                Text(
                    text = "No tienes hábitos de tipo ${tipoSeleccionado.lowercase()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
}

@Composable
fun HabitoCardPersonalizado(
    habito: HabitoPersonalizado,
    progreso: ProgresoDiario?,
    onIncrementar: () -> Unit
) {

    val realizados = progreso?.realizados ?: 0
    val completado = progreso?.completado ?: false

    // Progreso del hábito visualmente
    val progresoPorcentaje = if (habito.unaVezPorHabito == 1) {
        if (completado) 1f else 0f
    } else {
        realizados.toFloat() / (habito.recordatorios?.horas?.size ?: 1)
    }

    var progresoAnimado by remember { mutableFloatStateOf(progresoPorcentaje) }

    LaunchedEffect(progresoPorcentaje) {
        progresoAnimado = progresoPorcentaje
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progresoAnimado,
        animationSpec = tween(durationMillis = 300)
    )


    // Obtener colores
    val colorFondo = parseColorFromFirebase(habito.colorEtiqueta)
    val icono = obtenerIconoPorNombre(habito.iconoEtiqueta)
    val colorIcono = parseColorFromFirebase(habito.colorEtiqueta, darken = true)

    // Visualizar recordatorios
    val total = habito.recordatorios?.horas?.size ?: 0
    val progresoText = if (habito.unaVezPorHabito == 1) {
        if (completado) "Completado: 1/1" else "Realizado: 0/1"
    } else {
        if (realizados >= total && total > 0) {
            "Completado: $realizados/$total"
        } else {
            "Realizado: $realizados/$total"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(colorFondo.copy(alpha = 0.2f), shape = RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icono,
                    contentDescription = "Icono del Hábito",
                    tint = colorIcono,
                    modifier = Modifier
                        .size(33.dp)
                        .padding(end = 12.dp)
                )
                Column {
                    Text(
                        text = habito.nombre,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = progresoText,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (completado) {
                Icon(
                    imageVector = Icons.Default.Check,
                    modifier = Modifier
                        .size(40.dp),
                    contentDescription = "Completado",
                    tint = colorIcono
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Transparent, shape = CircleShape)
                        .drawBehind {
                            val strokeWidth = 4.dp.toPx()
                            val radius = size.minDimension / 2 - strokeWidth / 2

                            // Fondo base gris
                            drawCircle(
                                color = Color.LightGray,
                                radius = radius,
                                center = center,
                                style = Stroke(width = strokeWidth)
                            )

                            // Fondo animado sobrepuesto
                            drawArc(
                                color = colorIcono,
                                startAngle = -90f,
                                sweepAngle = 360 * animatedProgress,
                                useCenter = false,
                                style = Stroke(width = strokeWidth),
                                topLeft = Offset(
                                    (size.width - radius * 2) / 2,
                                    (size.height - radius * 2) / 2
                                ),
                                size = Size(radius * 2, radius * 2)
                            )
                        }
                )  {
                    IconButton(
                            onClick = onIncrementar,
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Sumar", tint = Color.Black)
                        }
                    }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(50)),
            color = colorIcono,
            trackColor = Color(0xFFE0E0E0),
        )
    }
}


