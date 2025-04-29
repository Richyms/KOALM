package com.example.koalm.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.navigation.NavHostController
import com.example.koalm.R
import com.example.koalm.model.Usuario
import com.example.koalm.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.layout.ContentScale
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.asImageBitmap


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPersonalizarPerfil(navController: NavHostController) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Estados para contenido de campos
    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var fechasec by remember { mutableStateOf("") }
    var peso by remember { mutableStateOf("") }
    var altura by remember { mutableStateOf("") }
    var generoSeleccionado by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    val opcionesGenero = listOf("Masculino", "Femenino", "Prefiero no decirlo")
    var username by remember { mutableStateOf("") }  // Aquí almacenamos el username

    // Instancias de Auth y Firestore
    val auth = FirebaseAuth.getInstance()
    val uid  = auth.currentUser?.uid
    val email  = auth.currentUser?.email
    val db   = FirebaseFirestore.getInstance()

    // Subir imagen a Firebase
    var imagenUri by remember { mutableStateOf<Uri?>(null) }
    var imagenBase64 by remember { mutableStateOf<String?>(null) }

    // Launcher para abrir la galería y seleccionar una imagen
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imagenUri = it
            val inputStream = context.contentResolver.openInputStream(it)
            val bytes = inputStream?.readBytes()
            imagenBase64 = Base64.encodeToString(bytes, Base64.DEFAULT)
            inputStream?.close()
        }
    }
    // Leer los datos guardados una sola vez al componer
    LaunchedEffect(email) {
        if (email != null) {
            db.collection("usuarios")
                .document(email)
                .get()
                .addOnSuccessListener { doc ->
                    username = doc.getString("username").orEmpty()
                    val u = doc.toObject(Usuario::class.java)
                    u?.let {
                        imagenBase64       = it.imagenBase64.orEmpty()
                        nombre             = it.nombre.orEmpty()
                        apellidos          = it.apellido.orEmpty()
                        fechasec           = it.nacimiento.orEmpty()
                        peso               = it.peso?.toString().orEmpty()
                        altura             = it.altura?.toString().orEmpty()
                        generoSeleccionado = it.genero.orEmpty()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error cargando perfil: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    // UI
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
        },
        bottomBar = {
            NavigationBar(tonalElevation = 8.dp) {
                listOf("Inicio", "Hábitos", "Perfil").forEachIndexed { index, label ->
                    val icon = listOf(Icons.Default.Home, Icons.Default.List, Icons.Default.Person)[index]
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
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            ImagenUsuario(imagenBase64 = imagenBase64)
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BotonAnadirImagenPerfil(onClick = { launcher.launch("image/*") })
                    BotonEliminarImagenPerfil(onClick = {
                        imagenUri = null
                        imagenBase64 = null  })
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            CampoNombre(nombre)            { nombre = it }
            CampoApellidos(apellidos)      { apellidos = it }
            CampoFechaNacimiento(fechasec) { showDatePicker = true }
            CampoPeso(peso)                { peso = it }
            CampoAltura(altura)            { altura = it }
            SelectorGenero(opcionesGenero, generoSeleccionado) { generoSeleccionado = it }
            Spacer(modifier = Modifier.weight(1f))

            BotonGuardarPerfil {
                if (uid != null) {
                    if (email == null) {
                        Toast.makeText(context, "No se pudo obtener el correo", Toast.LENGTH_SHORT).show()
                        return@BotonGuardarPerfil
                    }

                    // Validar campos requeridos
                    if (
                    // imagenBase64 Es solo si quieeere
                        username.isBlank() ||
                        nombre.isBlank() ||
                        apellidos.isBlank() ||
                        fechasec.isEmpty() ||
                        peso.isEmpty() ||
                        peso.toFloatOrNull() == null ||
                        altura.isEmpty() ||
                        altura.toIntOrNull() == null ||
                        generoSeleccionado.isEmpty()
                    ) {
                        Toast.makeText(context, "Por favor completa todos los campos correctamente.", Toast.LENGTH_SHORT).show()
                        return@BotonGuardarPerfil
                    }

                    // Construir objeto usuario
                    val usuario = Usuario(
                        imagenBase64 = imagenBase64,
                        userId     = uid,
                        email      = email,
                        username   = username,
                        nombre     = nombre,
                        apellido   = apellidos,
                        nacimiento = fechasec,
                        peso       = String.format(Locale.US, "%.2f", peso.toFloatOrNull() ?: 0f).toFloat(),
                        altura     = altura.toIntOrNull(),
                        genero     = generoSeleccionado
                    )

                    // Guardar (merge) en Firestore
                    db.collection("usuarios")
                        .document(email)
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
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // DatePickerDialog
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
                    todayDateBorderColor    = VerdePrincipal
                )
            )
            LaunchedEffect(datePickerState.selectedDateMillis) {
                datePickerState.selectedDateMillis?.let { millis ->
                    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                        timeInMillis = millis
                    }
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
}

//Función auxiliar para la imagen del usuario
fun base64ToBitmap(base64Str: String): Bitmap? {
    return try {
        val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        null
    }
}

@Composable
fun ImagenUsuario(imagenBase64: String?) {
    val isDark = isSystemInDarkTheme()
    val tint = if (isDark) Color.White else Color.Black

    if (!imagenBase64.isNullOrEmpty()) {
        val bitmap = remember(imagenBase64) { base64ToBitmap(imagenBase64) }

        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Usuario",
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.profile),
                contentDescription = "Usuario",
                modifier = Modifier
                    .size(200.dp),
                colorFilter = ColorFilter.tint(tint)
            )
        }
    } else {
        Image(
            painter = painterResource(id = R.drawable.profile),
            contentDescription = "Usuario",
            modifier = Modifier
                .size(200.dp),
            colorFilter = ColorFilter.tint(tint)
        )
    }
}


@Composable
fun BotonAnadirImagenPerfil(onClick: () -> Unit) {
    Button(
        onClick   = onClick,
        modifier = Modifier
            .width(200.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape    = RoundedCornerShape(16.dp),
        colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Text("Añadir foto", color = MaterialTheme.colorScheme.onPrimary)
    }
}
@Composable
fun BotonEliminarImagenPerfil(onClick: () -> Unit) {
    Button(
        onClick   = onClick,
        modifier = Modifier
            .width(200.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape    = RoundedCornerShape(16.dp),
        colors   = ButtonDefaults.buttonColors(containerColor = RojoClaro)
    ) {
        Text("Eliminar foto", color = MaterialTheme.colorScheme.onPrimary)
    }
}



@Composable
fun CampoNombre(value: String, onValueChange: (String) -> Unit) {
    val filtered = value.filter { it.isLetter() || it.isWhitespace() }
    OutlinedTextField(
        value = filtered,
        onValueChange = { onValueChange(it.filter { c -> c.isLetter() || c.isWhitespace() }) },
        label = { Text("Nombre *") },
        modifier = Modifier
            .fillMaxWidth(0.97f)
            .clip(RoundedCornerShape(16.dp)),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = VerdePrincipal,
            unfocusedBorderColor = GrisMedio,
            unfocusedLabelColor  = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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
        label = { Text("Apellidos *") },
        modifier = Modifier
            .fillMaxWidth(0.97f)
            .clip(RoundedCornerShape(16.dp)),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = VerdePrincipal,
            unfocusedBorderColor = GrisMedio,
            unfocusedLabelColor  = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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
        label = { Text("Fecha de nacimiento *") },
        placeholder = { Text("MM/DD/YYYY") },
        modifier = Modifier
            .fillMaxWidth(0.97f)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        readOnly = true,
        trailingIcon = {
            Icon(
                imageVector = Icons.Filled.DateRange,
                contentDescription = "Seleccionar fecha",
                tint = iconTint,
                modifier = Modifier.clickable { onClick() }
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = VerdePrincipal,
            unfocusedBorderColor = GrisMedio,
            unfocusedLabelColor  = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    )
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
fun CampoPeso(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            // Expresión regular: hasta 3 dígitos antes del punto, opcionalmente punto y hasta 2 dígitos después
            val regex = Regex("^\\d{0,3}(\\.\\d{0,2})?$")
            if (newValue.isEmpty() || newValue.matches(regex)) {
                onValueChange(newValue)
            }
        },
        label = { Text("Peso *") },
        modifier = Modifier
            .fillMaxWidth(0.97f)
            .clip(RoundedCornerShape(16.dp)),
        singleLine = true,
        trailingIcon = { Text("kg", color = GrisMedio) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = VerdePrincipal,
            unfocusedBorderColor = GrisMedio,
            unfocusedLabelColor  = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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
        label = { Text("Altura *") },
        modifier = Modifier
            .fillMaxWidth(0.97f)
            .clip(RoundedCornerShape(16.dp)),
        singleLine = true,
        trailingIcon = { Text("cm", color = GrisMedio) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = VerdePrincipal,
            unfocusedBorderColor = GrisMedio,
            unfocusedLabelColor  = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    )
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
fun SelectorGenero(
    opciones: List<String>,
    seleccion: String,
    onSelect: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(0.97f)) {
        Text("Género *", style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            opciones.forEach { opcion ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    RadioButton(
                        selected = opcion == seleccion,
                        onClick  = { onSelect(opcion) },
                        colors    = RadioButtonDefaults.colors(
                            selectedColor   = VerdePrincipal,
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
    Button(
        onClick   = onClick,
        modifier = Modifier
            .width(200.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape    = RoundedCornerShape(16.dp),
        colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Text("Guardar", color = MaterialTheme.colorScheme.onPrimary)
    }
}
