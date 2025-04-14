//Paquete base del proyecto
package com.example.koalm

//Importación de clases y librerias necesarias para Android y JetpackCompose
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.* // Layouts como Column, Row, Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.* // Componentes de Material 3 (Botones, TextFields, etc.)
import androidx.compose.runtime.* // Funciones y estados de Compose
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext // Contexto para Toast, etc.
import androidx.compose.ui.res.painterResource // Carga de imágenes
import androidx.compose.ui.unit.dp
import com.example.koalm.ui.theme.* // Acceso a colores y tema personalizado
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import java.util.Calendar

class PersonalizarPerfil : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PerfilApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilApp() {
    KoalmTheme {
        val navController = rememberNavController()
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            NavHost(navController = navController, startDestination = "personalize") {
                composable("personalize") {
                    PantallaPersonalizar(navController)
                }
                composable("login") {
                    PantallaPersonalizar(navController)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPersonalizar(navController: NavHostController) {
    //Variables a usar
    val context = LocalContext.current
    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var fechasec by remember { mutableStateOf("") }
    var peso by remember { mutableStateOf("") }
    var altura by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    val op_gen = listOf("Masculino", "Femenino", "Prefiero no decirlo")
    var opcionSeleccionada by remember { mutableStateOf("") }
    
    // DatePicker con Material3
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            val datePickerState = rememberDatePickerState()
            DatePicker(
                state = datePickerState,
                showModeToggle = false
            )
            
            LaunchedEffect(datePickerState.selectedDateMillis) {
                datePickerState.selectedDateMillis?.let { millis ->
                    val calendar = Calendar.getInstance().apply { 
                        timeInMillis = millis 
                    }
                    fechasec = String.format(
                        "%02d/%02d/%04d",
                        calendar.get(Calendar.MONTH) + 1,
                        calendar.get(Calendar.DAY_OF_MONTH),
                        calendar.get(Calendar.YEAR)
                    )
                }
            }
        }
    }

    //Barra de navegación inferior
    val items = listOf("Inicio", "Hábitos", "Perfil")
    val icons = listOf(Icons.Default.Home, Icons.Default.List, Icons.Default.Person)
    var selectedIndex by remember { mutableStateOf(2) }  // Set to 2 for "Perfil" selected

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personalizar perfil de usuario") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("registro")
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item, fontSize = 10.sp) },
                        selected = selectedIndex == index,
                        onClick = { 
                            selectedIndex = index
                            navController.navigate("personalizar") 
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            // Icono del usuario
            Image(
                painter = painterResource(id = R.drawable.koala_perfil),
                contentDescription = "Usuario",
                modifier = Modifier.size(200.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            //Campo de Texto nombre de usuario
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VerdePrincipal,
                    unfocusedBorderColor = GrisMedio
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            //Campo de Texto apellidos del usuario
            OutlinedTextField(
                value = apellidos,
                onValueChange = { apellidos = it },
                label = { Text("Apellidos") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VerdePrincipal,
                    unfocusedBorderColor = GrisMedio
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            //Campo de Texto fecha de nacimiento
            OutlinedTextField(
                value = fechasec,
                onValueChange = {},
                label = { Text("Fecha de nacimiento") },
                placeholder = { Text("MM/DD/YYYY") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Seleccionar fecha",
                        modifier = Modifier.clickable { showDatePicker = true }
                    )
                },
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VerdePrincipal,
                    unfocusedBorderColor = GrisMedio
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            //Campo de Texto de peso del usuario
            OutlinedTextField(
                value = peso,
                onValueChange = { peso = it },
                label = { Text("Peso") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                trailingIcon = {
                    Text("kg", color = GrisMedio)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VerdePrincipal,
                    unfocusedBorderColor = GrisMedio
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            //Campo de Texto de altura del usuario
            OutlinedTextField(
                value = altura,
                onValueChange = { altura = it },
                label = { Text("Altura") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                trailingIcon = {
                    Text("cm", color = GrisMedio)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VerdePrincipal,
                    unfocusedBorderColor = GrisMedio
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            //Radio botones de genero
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Género", style = MaterialTheme.typography.bodyMedium)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    op_gen.forEach { opcion ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            RadioButton(
                                selected = (opcion == opcionSeleccionada),
                                onClick = { opcionSeleccionada = opcion },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = VerdePrincipal
                                )
                            )
                            Text(
                                text = opcion,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            //Botón principal para guardar los cambios
            Button(
                onClick = {
                    Toast.makeText(context, "Modificaciones Guardadas", Toast.LENGTH_SHORT).show()
                    navController.navigate("habitos")
                },
                modifier = Modifier
                    .width(200.dp)
                    .padding(vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal)
            ) {
                Text("Guardar", color = Blanco)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
