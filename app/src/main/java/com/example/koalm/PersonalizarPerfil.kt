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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KoalmTheme {

                val navController = rememberNavController()

                Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPersonalizar(navController: NavHostController) {
    //Variables a usar
    val context = LocalContext.current //Muestra el texto
    var nombre by remember { mutableStateOf("Usuario") }
    var apellidos by remember { mutableStateOf("ApellidoP ApellidoM") }
    val calendar = Calendar.getInstance()
    // Estado para la fecha seleccionada
    val fechasec = remember { mutableStateOf("") }
    // Dialogo de fechas
    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val fecha = String.format("%02d/%02d/%04d", month + 1, dayOfMonth, year)
            fechasec.value = fecha
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    var peso by remember { mutableStateOf("XX")}
    var altura by remember { mutableStateOf("XXX")}
    val op_gen = listOf("Masculino", "Femenino", "Prefiero no decirlo")
    val buttonModifier = Modifier.width(200.dp)
    var opcionSeleccionada by remember { mutableStateOf(op_gen[0]) }
    //Barra de navegación inferior
    val items = listOf("Inicio", "Hábitos", "Perfil")
    val icons = listOf(Icons.Default.Home, Icons.Default.List, Icons.Default.Person)
    var selectedIndex by remember { mutableStateOf(0) }

    Scaffold(

        topBar = {
            TopAppBar(
                title = { Text("Personalizar Perfil de Usuario")},
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("registro")
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item, fontSize = 10.sp) },
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index
                            navController.navigate("personalizar") }
                    )
                }
            }
        }

    ) { innerPadding ->
        Column(
            modifier = Modifier.
                padding(innerPadding).
                fillMaxSize().
                padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
        ) {
            // Icono del usuario
            Image(
                painter = painterResource(id = R.drawable.perfilus),
                contentDescription = "Usuario", // descripción para accesibilidad
                modifier = Modifier.size(150.dp) // tamaño de la imagen
            )

            Spacer(modifier = Modifier.height(15.dp)) // espacio vertical

            //Campo de Texto nombre de usuario
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it }, //actualización del estado
                label = { Text("Nombre de Usuario")}, //Etiqueta del campo
                modifier = Modifier.fillMaxWidth(0.85f),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VerdePrincipal, // borde activo verde
                    unfocusedBorderColor = GrisMedio // borde inactivo gris
                )
            )

            Spacer(modifier = Modifier.height(10.dp)) // espacio vertical

            //Campo de Texto apellidos del usuario
            OutlinedTextField(
                value = apellidos,
                onValueChange = { apellidos = it }, //actualización del estado
                label = { Text("Apellidos")}, //Etiqueta del campo
                modifier = Modifier.fillMaxWidth(0.85f),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VerdePrincipal, // borde activo verde
                    unfocusedBorderColor = GrisMedio // borde inactivo gris
                )
            )

            Spacer(modifier = Modifier.height(10.dp)) // espacio vertical

            //Campo de Texto fecha de nacimiento
            OutlinedTextField(
                value = fechasec.value,
                onValueChange = {}, // No editable directamente
                label = { Text("Fecha de nacimiento") },
                placeholder = {Text("MM/DD/YYYY")},
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .clickable { datePickerDialog.show() }, //Muestra el calendario
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Seleccionar fecha",
                        modifier = Modifier.clickable { datePickerDialog.show() }
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VerdePrincipal, // borde activo verde
                    unfocusedBorderColor = GrisMedio // borde inactivo gris
                )
            )

            Spacer(modifier = Modifier.height(10.dp)) // espacio vertical

            //Campo de Texto de peso del usuario
            OutlinedTextField(
                value = peso,
                onValueChange = { peso = it }, //actualización del estado
                label = { Text("Peso")}, //Etiqueta del campo
                modifier = Modifier.fillMaxWidth(0.85f),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                trailingIcon = {
                    Text("Kg", color = GrisMedio) // Unidad de medida
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VerdePrincipal, // borde activo verde
                    unfocusedBorderColor = GrisMedio // borde inactivo gris
                )
            )

            Spacer(modifier = Modifier.height(10.dp)) // espacio vertical

            //Campo de Texto de altura del usuario
            OutlinedTextField(
                value = altura,
                onValueChange = { altura = it }, //actualización del estado
                label = { Text("Altura")}, //Etiqueta del campo
                modifier = Modifier.fillMaxWidth(0.85f),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                trailingIcon = {
                    Text("cm", color = GrisMedio) // Unidad de medida
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VerdePrincipal, // borde activo verde
                    unfocusedBorderColor = GrisMedio // borde inactivo gris
                )
            )

            Spacer(modifier = Modifier.height(10.dp)) // espacio vertical

            //Radio botones de genero
            Column(modifier = Modifier.fillMaxWidth(0.95f)) {
                Text("Género", style = MaterialTheme.typography.titleMedium) //Titulo de la sección

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    op_gen.forEach { opcion ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f) // que cada opción tome espacio equitativo
                        ) {
                            RadioButton(
                                selected = (opcion == opcionSeleccionada),
                                onClick = { opcionSeleccionada = opcion }
                            )
                            Text(
                                text = opcion,
                                fontSize = 12.sp, //Reducción de texto
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp)) // espacio vertical

            //Botón principal para guardar los cambios
            Button(
                onClick = {
                    Toast.makeText(context, "Modificaciones Guardadas", Toast.LENGTH_SHORT)
                        .show() // muestra Toast
                },
                modifier = buttonModifier, // ancho común
                colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal) // fondo verde
            ) {
                Text("Guardar", color = Blanco) // texto blanco
            }
        }
    }
}
