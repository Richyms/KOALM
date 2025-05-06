package com.example.koalm.ui.screens
import android.content.Context
import android.util.Log
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.graphics.toColorInt
import com.example.koalm.data.HabitosRepository
import com.example.koalm.model.ProgresoDiario
import com.example.koalm.ui.components.obtenerIconoPorNombre
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


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



    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = { DrawerContenido(navController) }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("¡Hola, Kool! 🐨✨") },
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
                val usuarioEmail = FirebaseAuth.getInstance().currentUser?.email
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
            NavigationDrawerItem(label = { Text(it) }, selected = false, onClick = { })
        }
        HorizontalDivider()
        Text("Labels", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleSmall)
        listOf("Recomendaciones de Kool", "Insignias y logros", "Información de la app").forEach {
            NavigationDrawerItem(label = { Text(it) }, selected = false, onClick = { })
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
    val tipos = listOf("todos", "personalizado", "fisico", "mental")

    // Cargar los hábitos cuando cambia el correo electrónico
    LaunchedEffect(usuarioEmail) {
        viewModel.cargarHabitos(usuarioEmail)
    }

        // Filtros de tipo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tipos.forEach { tipo ->
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
            val habitosFiltrados = if (tipoSeleccionado == "todos") {
                habitos
            } else {
                habitos.filter { it.tipo.equals(tipoSeleccionado, ignoreCase = true) }
            }

            // Mostrar los hábitos filtrados
            val coroutineScope = rememberCoroutineScope()

            if (habitosFiltrados.isNotEmpty()) {
                habitosFiltrados.forEach { habito ->
                    HabitoCard(
                        habito = habito,
                        onIncrementar = {
                            coroutineScope.launch {
                                HabitosRepository.incrementarProgresoHabito(
                                    email = usuarioEmail,
                                    habito = habito
                                )
                            }
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
fun HabitoCard(
    habito: HabitoPersonalizado,
    onIncrementar: () -> Unit
) {
    // Obtenemos el progreso de este hábito
    val habitoId = habito.nombre.replace(" ", "_")
    val progreso = obtenerProgresoDelDia(habitoId)

    // Si no existe progreso (por ejemplo, es el primer día), asignamos valores predeterminados
    val realizados = progreso?.realizados ?: 0
    val completado = progreso?.completado ?: false

    // Calculamos el porcentaje de progreso para la barra
    val progresoPorcentaje = if (habito.unaVezPorHabito == 1) {
        if (completado) 1f else 0f // Si es una vez por día y está completado, el progreso es 100%
    } else {
        realizados.toFloat() / (habito.recordatorios?.horas?.size ?: 1) // Si tiene más recordatorios, calculamos el porcentaje
    }

    // Obtenemos el color de fondo
    val colorFondo = parseColorFromFirebase(habito.colorEtiqueta)

    // Extensión para oscurecer el color
    fun Color.darken(factor: Float): Color {
        return Color(
            red = (red * (1 - factor)).coerceIn(0f, 1f),
            green = (green * (1 - factor)).coerceIn(0f, 1f),
            blue = (blue * (1 - factor)).coerceIn(0f, 1f),
            alpha = alpha
        )
    }

    // Función para hacer el parseo de color desde FB
    fun parseColorFromFirebase(colorString: String, darken: Boolean = false, darkenFactor: Float = 0.15f): Color {
        val regex = Regex("""Color\(([\d.]+), ([\d.]+), ([\d.]+), ([\d.]+),.*\)""")
        val match = regex.find(colorString)
        return if (match != null) {
            val (r, g, b, a) = match.destructured
            val baseColor = Color(r.toFloat(), g.toFloat(), b.toFloat(), a.toFloat())
            if (darken) baseColor.darken(darkenFactor) else baseColor
        } else {
            Log.e("ColorParse", "No se pudo parsear el color: $colorString")
            Color.Gray
        }
    }

    //Obtener el ícono y color
    val icono = obtenerIconoPorNombre(habito.iconoEtiqueta)
    val colorIcono = parseColorFromFirebase(habito.colorEtiqueta, darken = true)

    // Texto que muestra los "Realizados / Total"
    val progresoText = if (habito.unaVezPorHabito == 1) {
        // Caso cuando es una vez por día
        if (completado) {
            "Realizado: 1/1"
        } else {
            "Realizado: 0/1"
        }
    } else {
        // Caso cuando tiene varios recordatorios
        "$realizados/${habito.recordatorios?.horas?.size ?: 0}"
    }

    // Column conteniendo toda la card
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(colorFondo.copy(alpha = 0.2f), shape = RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        // Primera fila: Icono y nombre del hábito
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
                        .size(40.dp)
                        .padding(end = 12.dp)
                )
                Column {
                    Text(
                        text = habito.nombre,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = progresoText,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Si ya está completado, mostramos una palomita
            if (completado) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completado",
                    tint = Color.Green
                )
            } else {
                // Si no está completado, mostramos el icono de suma
                IconButton(
                    onClick = onIncrementar,
                    modifier = Modifier
                        .size(36.dp)
                        .border(3.dp, colorIcono, shape = CircleShape)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Sumar", tint = Color.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Barra de progreso visual
        LinearProgressIndicator(
            progress = { progresoPorcentaje },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = colorFondo,
            trackColor = Color.LightGray,
        )
    }
}

@Composable
fun obtenerProgresoDelDia(habitoId: String): ProgresoDiario? {
    val db = FirebaseFirestore.getInstance()
    val progresoRef = db.collection("usuarios")
        .document("usuario@example.com") // Cambiar al email del usuario real
        .collection("personalizados")
        .document(habitoId)
        .collection("progreso")
        .document("progresoDelDia") // Documento donde se guarda el progreso diario

    // Utilizamos un estado composable para obtener el progreso
    val progresoState = remember { mutableStateOf<ProgresoDiario?>(null) }

    // Usamos un coroutineScope para obtener los datos de Firestore
    LaunchedEffect(habitoId) {
        try {
            val docSnapshot = progresoRef.get().await()
            progresoState.value = docSnapshot.toObject(ProgresoDiario::class.java)
        } catch (e: Exception) {
            // Manejo de errores en caso de que la solicitud falle
            e.printStackTrace()
            progresoState.value = ProgresoDiario(realizados = 0, completado = false, totalRecordatoriosPorDia = 1)
        }
    }

    // Devolvemos el estado
    return progresoState.value
}