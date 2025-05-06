package com.example.koalm.ui.screens
import android.content.Context
import androidx.core.content.edit
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
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
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import com.google.android.gms.auth.api.identity.Identity
import com.example.koalm.ui.components.*

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
                NavigationBar(tonalElevation = 8.dp) {
                    listOf("Inicio", "Hábitos", "Perfil").forEachIndexed { index, label ->
                        val icon = listOf(Icons.Default.Home, Icons.AutoMirrored.Filled.List, Icons.Default.Person)[index]
                        NavigationBarItem(
                            selected = index == 0,
                            onClick = { 
                                when (index) {
                                    0 -> navController.navigate( "menu" )
                                    1 -> navController.navigate("tipos_habitos")
                                    2 -> navController.navigate( "personalizar" )
                                }
                            },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) }
                        )
                    }
                }
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
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(3) { index ->
                        HabitoItem(
                            titulo = "Hábito ${index + 1}",
                            descripcion = "Descripción del hábito ${index + 1}",
                            imagenId = R.drawable.koala_durmiendo
                        )
                    }
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
fun EstadisticasCard(datos: DatosSueno = datosMockSueño) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, VerdeBorde, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = VerdeContenedor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Sueño: ${datos.puntos} pts", fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(2.dp))

            val dias = listOf("L", "M", "X", "J", "V", "S", "D")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                dias.zip(datos.historialSemanal).forEach { (dia, sueno) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        BarraSueno(sueno.ligero, sueno.profundo, sueno.despierto)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(dia, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                LeyendaColor("${datos.sueñoLigero} h", "Sueño ligero", GrisMedio)
                LeyendaColor("${datos.sueñoProfundo} h", "Sueño profundo", VerdePrincipal)
                LeyendaColor("${datos.tiempoDespierto} h", "Tiempo despierto", MarronKoala)
            }

        }
    }
}





@Composable
fun DrawerContenido(navController: NavHostController) {
    ModalDrawerSheet {
        Text("Koalm", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.headlineMedium)
        HorizontalDivider()
        listOf("Inicio", "Racha", "Estadísticas", "Test de emociones").forEach {
            NavigationDrawerItem(label = { Text(it) }, selected = it == "Inicio", onClick = {
                when (it) {
                    //"Inicio" -> navController.navigate("inicio")
                    "Racha" -> navController.navigate("racha_habitos")
                    "Estadísticas" -> navController.navigate("estadisticas")
                    //"Test de emociones" -> navController.navigate("test_emociones")
                }
            })
        }
        HorizontalDivider()
        Text("Hábitos", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleSmall)
        listOf("Salud física", "Salud mental", "Personalizados").forEach {
            NavigationDrawerItem(label = { Text(it) }, selected = false, onClick = {
                when (it){
                    "Salud mental" -> navController.navigate("estadisticas_salud_mental")
                    "Salud física" -> navController.navigate("estadisticas_salud_fisica")
                }
            })
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
fun HabitoItem(titulo: String, descripcion: String, imagenId: Int) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, VerdeBorde, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = VerdeContenedor
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Image(
                painter = painterResource(id = imagenId),
                contentDescription = titulo,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(titulo, fontWeight = FontWeight.Bold)
                Text(descripcion, fontSize = 12.sp, color = GrisMedio)
            }
        }
    }
}

