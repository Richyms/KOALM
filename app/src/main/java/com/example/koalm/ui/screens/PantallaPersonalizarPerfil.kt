package com.example.koalmV1.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.koalmV1.R
import com.example.koalmV1.ui.theme.*
import java.util.Calendar
import java.util.Locale;


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPersonalizarPerfil(navController: NavHostController) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var fechasec by remember { mutableStateOf("") }
    var peso by remember { mutableStateOf("") }
    var altura by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    val opcionesGenero = listOf("Masculino", "Femenino", "Prefiero no decirlo")
    var generoSeleccionado by remember { mutableStateOf("") }
    var indiceSeleccionado by remember { mutableStateOf(2) }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            val datePickerState = rememberDatePickerState()
            DatePicker(state = datePickerState, showModeToggle = false)
            LaunchedEffect(datePickerState.selectedDateMillis) {
                datePickerState.selectedDateMillis?.let { millis ->
                    val calendar = Calendar.getInstance().apply { timeInMillis = millis }
                    fechasec = String.format(
                        Locale("es", "MX"),
                        "%02d/%02d/%04d",
                        calendar.get(Calendar.MONTH) + 1,
                        calendar.get(Calendar.DAY_OF_MONTH),
                        calendar.get(Calendar.YEAR)
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personalizar perfil de usuario") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            BarraInferiorPersonalizar(indiceSeleccionado) { nuevo ->
                indiceSeleccionado = nuevo
                when (nuevo) {
                    0 -> navController.navigate("menu")
                    1 -> navController.navigate("habitos")
                    2 -> navController.navigate("personalize")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier  = Modifier.height(20.dp))
            ImagenUsuario()
            Spacer(modifier = Modifier.height(20.dp))
            CampoNombre(nombre) { nombre = it }
            CampoApellidos(apellidos) { apellidos = it }
            CampoFechaNacimiento(fechasec, onClick = { showDatePicker = true })
            CampoPeso(peso) { peso = it }
            CampoAltura(altura) { altura = it }
            SelectorGenero(opcionesGenero, generoSeleccionado) { generoSeleccionado = it }
            Spacer(modifier = Modifier.weight(1f))
            BotonGuardarPerfil {
                Toast.makeText(context, "Modificaciones Guardadas", Toast.LENGTH_SHORT).show()
                navController.navigate("habitos") {
                    popUpTo("personalize") { inclusive = true }
                    launchSingleTop = true
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


@Composable
fun ImagenUsuario() {
    Image(
        painter = painterResource(id = R.drawable.koala_perfil),
        contentDescription = "Usuario",
        modifier = Modifier.size(200.dp)
    )
}


@Composable
fun CampoNombre(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
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
}


@Composable
fun CampoApellidos(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
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
}


@Composable
fun CampoFechaNacimiento(value: String, onClick: () -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text("Fecha de nacimiento") },
        placeholder = { Text("MM/DD/YYYY") },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        readOnly = true,
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Seleccionar fecha",
                modifier = Modifier.clickable { onClick() }
            )
        },
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VerdePrincipal,
            unfocusedBorderColor = GrisMedio
        )
    )
    Spacer(modifier = Modifier.height(8.dp))
}



@Composable
fun CampoPeso(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Peso") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        trailingIcon = { Text("kg", color = GrisMedio) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VerdePrincipal,
            unfocusedBorderColor = GrisMedio
        )
    )
    Spacer(modifier = Modifier.height(8.dp))
}



@Composable
fun CampoAltura(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Altura") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        trailingIcon = { Text("cm", color = GrisMedio) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VerdePrincipal,
            unfocusedBorderColor = GrisMedio
        )
    )
    Spacer(modifier = Modifier.height(8.dp))
}




@Composable
fun SelectorGenero(opciones: List<String>, seleccion: String, onSelect: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Género", style = MaterialTheme.typography.bodyMedium)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            opciones.forEach { opcion ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    RadioButton(
                        selected = (opcion == seleccion),
                        onClick = { onSelect(opcion) },
                        colors = RadioButtonDefaults.colors(selectedColor = VerdePrincipal)
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
}



@Composable
fun BotonGuardarPerfil(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(200.dp)
            .padding(vertical = 16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal)
    ) {
        Text("Guardar", color = Blanco)
    }
}


@Composable
fun BarraInferiorPersonalizar(seleccionado: Int, onSelect: (Int) -> Unit) {
    val items = listOf("Inicio", "Hábitos", "Perfil")
    val icons = listOf(Icons.Default.Home, Icons.Default.List, Icons.Default.Person)

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = { Icon(icons[index], contentDescription = item) },
                label = { Text(item, fontSize = 10.sp) },
                selected = seleccionado == index,
                onClick = { onSelect(index) },
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







