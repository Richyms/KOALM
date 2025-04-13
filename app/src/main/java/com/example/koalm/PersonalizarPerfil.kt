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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext // Contexto para Toast, etc.
import androidx.compose.ui.res.painterResource // Carga de imágenes
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString // Texto con diferentes estilos
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.koalm.ui.theme.* // Acceso a colores y tema personalizado
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Ícono de flecha para regresar
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import java.util.Calendar

class PersonalizarPerfil : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KoalmTheme {
                Scaffold(modifier = Modifier.fillMaxSize()/*, color = Color.White*/) { innerPadding ->
                    PantallaPersonalizar(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPersonalizar(name: String, modifier: Modifier = Modifier) { //navController: NavController
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
    val buttonModifier = Modifier.width(200.dp)

    Scaffold(

        topBar = {
            TopAppBar(
                title = { Text("Personalizar Perfil de Usuario")},
                /*navigationIcon = {
                    IconButton(onClick = {
                        navController.navigateUp()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }*/
            )
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
                painter = painterResource(id = R.drawable.koala),
                contentDescription = "Usuario", // descripción para accesibilidad
                modifier = Modifier.size(150.dp) // tamaño de la imagen
            )

            Spacer(modifier = Modifier.height(20.dp)) // espacio vertical

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

            Spacer(modifier = Modifier.height(14.dp)) // espacio vertical

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

            Spacer(modifier = Modifier.height(14.dp)) // espacio vertical

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

            Spacer(modifier = Modifier.height(14.dp)) // espacio vertical

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

            Spacer(modifier = Modifier.height(14.dp)) // espacio vertical

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

            Spacer(modifier = Modifier.height(14.dp)) // espacio vertical

            //Radio botones de genero

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

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    KoalmTheme {
        PantallaPersonalizar("Android")
    }
}