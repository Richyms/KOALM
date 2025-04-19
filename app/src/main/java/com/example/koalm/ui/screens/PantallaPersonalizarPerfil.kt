package com.example.koalm.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.navigation.NavHostController
import com.example.koalm.R
import com.example.koalm.model.Usuario
import com.example.koalm.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.Calendar
import java.util.Locale
import java.util.UUID

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

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { showDatePicker = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } }
        ) {
            val datePickerState = rememberDatePickerState()
            DatePicker(
                state = datePickerState,
                showModeToggle = false,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = VerdePrincipal,
                    todayDateBorderColor = VerdePrincipal
                )
            )
            LaunchedEffect(datePickerState.selectedDateMillis) {
                datePickerState.selectedDateMillis?.let { millis ->
                    val cal = Calendar.getInstance().apply { timeInMillis = millis }
                    fechasec = String.format(
                        Locale("es", "MX"),
                        "%02d/%02d/%04d",
                        cal.get(Calendar.MONTH) + 1,
                        cal.get(Calendar.DAY_OF_MONTH),
                        cal.get(Calendar.YEAR)
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personalizar perfil") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            ImagenUsuario()
            Spacer(modifier = Modifier.height(20.dp))
            CampoNombre(nombre) { nombre = it }
            CampoApellidos(apellidos) { apellidos = it }
            CampoFechaNacimiento(fechasec) { showDatePicker = true }
            CampoPeso(peso) { peso = it }
            CampoAltura(altura) { altura = it }
            SelectorGenero(opcionesGenero, generoSeleccionado) { generoSeleccionado = it }

            Spacer(modifier = Modifier.weight(1f))

            BotonGuardarPerfil {
                // 1) Obtener usuario actual y sus datos de login
                val auth = FirebaseAuth.getInstance()
                val currentUser = auth.currentUser
                val uid = currentUser?.uid ?: UUID.randomUUID().toString()
                val email    = currentUser?.email
                val username = email?.substringBefore("@")

                // 2) Construir objeto Usuario incluyendo email/username
                val usuario = Usuario(
                    id         = uid,
                    userId     = uid,
                    email      = email,
                    username   = username,
                    nombre     = nombre,
                    apellido   = apellidos,
                    nacimiento = fechasec,
                    peso       = peso.toIntOrNull(),
                    altura     = altura.toIntOrNull()
                )

                // 3) Guardar o mergear en Firestore
                FirebaseFirestore.getInstance()
                    .collection("usuarios")
                    .document(uid)
                    .set(usuario.toMap(), SetOptions.merge())
                    .addOnSuccessListener {
                        Toast.makeText(context, "Perfil guardado correctamente", Toast.LENGTH_SHORT).show()
                        navController.navigate("menu") {
                            popUpTo("personalizar") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error al guardar: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ImagenUsuario() {
    val isDark = isSystemInDarkTheme()
    val tintColor = if (isDark) Color.White else Color.Black
    Image(
        painter = painterResource(id = R.drawable.profile),
        contentDescription = "Usuario",
        modifier = Modifier.size(200.dp),
        colorFilter = ColorFilter.tint(tintColor)
    )
}

@Composable
fun CampoNombre(value: String, onValueChange: (String) -> Unit) {
    val filtered = value.filter { it.isLetter() || it.isWhitespace() }
    OutlinedTextField(
        value = filtered,
        onValueChange = { onValueChange(it.filter { c -> c.isLetter() || c.isWhitespace() }) },
        label = { Text("Nombre") },
        modifier = Modifier
            .fillMaxWidth(0.97f)
            .clip(RoundedCornerShape(16.dp)),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VerdePrincipal,
            unfocusedBorderColor = GrisMedio,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    )
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
fun CampoApellidos(value: String, onValueChange: (String) -> Unit) {
    val filtered = value.filter { it.isLetter() || it.isWhitespace() }
    OutlinedTextField(
        value = filtered,
        onValueChange = { onValueChange(it.filter { c -> c.isLetter() || c.isWhitespace() }) },
        label = { Text("Apellidos") },
        modifier = Modifier
            .fillMaxWidth(0.97f)
            .clip(RoundedCornerShape(16.dp)),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VerdePrincipal,
            unfocusedBorderColor = GrisMedio,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    )
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
fun CampoFechaNacimiento(value: String, onClick: () -> Unit) {
    val iconTint = if (isSystemInDarkTheme()) Color.White else Color.Black
    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text("Fecha de nacimiento") },
        placeholder = { Text("MM/DD/YYYY") },
        modifier = Modifier
            .fillMaxWidth(0.97f)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        readOnly = true,
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Seleccionar fecha",
                tint = iconTint,
                modifier = Modifier.clickable { onClick() }
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VerdePrincipal,
            unfocusedBorderColor = GrisMedio,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    )
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
fun CampoPeso(value: String, onValueChange: (String) -> Unit) {
    val filtered = value.filter { it.isDigit() }
    OutlinedTextField(
        value = filtered,
        onValueChange = { onValueChange(it.filter { c -> c.isDigit() }) },
        label = { Text("Peso") },
        modifier = Modifier
            .fillMaxWidth(0.97f)
            .clip(RoundedCornerShape(16.dp)),
        singleLine = true,
        trailingIcon = { Text("kg", color = GrisMedio) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VerdePrincipal,
            unfocusedBorderColor = GrisMedio,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    )
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
fun CampoAltura(value: String, onValueChange: (String) -> Unit) {
    val filtered = value.filter { it.isDigit() }
    OutlinedTextField(
        value = filtered,
        onValueChange = { onValueChange(it.filter { c -> c.isDigit() }) },
        label = { Text("Altura") },
        modifier = Modifier
            .fillMaxWidth(0.97f)
            .clip(RoundedCornerShape(16.dp)),
        singleLine = true,
        trailingIcon = { Text("cm", color = GrisMedio) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VerdePrincipal,
            unfocusedBorderColor = GrisMedio,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    )
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
fun SelectorGenero(opciones: List<String>, seleccion: String, onSelect: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(0.97f)) {
        Text("Género", style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            opciones.forEach { opcion ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(horizontal = 4.dp)
                ) {
                    RadioButton(
                        selected = (opcion == seleccion),
                        onClick = { onSelect(opcion) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = VerdePrincipal,
                            unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Text(
                        text = opcion,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
fun BotonGuardarPerfil(onClick: () -> Unit) {
    val buttonModifier = Modifier
        .width(200.dp)
        .padding(50.dp)

    Button(
        onClick = onClick,
        modifier = buttonModifier,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Text("Guardar", color = MaterialTheme.colorScheme.onPrimary)
    }
}
