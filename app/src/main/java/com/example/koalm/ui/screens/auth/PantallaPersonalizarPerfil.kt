        // PantallaPersonalizarPerfil.kt
        package com.example.koalm.ui.screens.auth

        import android.widget.Toast
        import androidx.compose.foundation.Image
        import androidx.compose.foundation.clickable
        import androidx.compose.foundation.isSystemInDarkTheme
        import androidx.compose.foundation.layout.*
        import androidx.compose.foundation.rememberScrollState
        import java.time.LocalDate
        import java.time.format.DateTimeFormatter
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
        import com.example.koalm.ui.components.BarraNavegacionInferior
        import com.example.koalm.ui.components.ExitoDialogoGuardadoAnimado
        import com.example.koalm.ui.components.FalloDialogoGuardadoAnimado
        import com.google.firebase.auth.FirebaseAuth
        import com.google.firebase.firestore.FirebaseFirestore
        import com.google.firebase.firestore.SetOptions
        import java.util.Calendar
        import java.util.Locale
        import android.net.Uri
        import androidx.activity.result.contract.ActivityResultContracts
        import androidx.compose.ui.layout.ContentScale
        import android.util.Base64
        import androidx.activity.compose.rememberLauncherForActivityResult
        import android.graphics.Bitmap
        import android.graphics.BitmapFactory
        import androidx.compose.foundation.shape.CircleShape
        import androidx.compose.ui.graphics.asImageBitmap
        import java.util.TimeZone
        import androidx.compose.foundation.background
        import com.example.koalm.ui.components.ValidacionesDialogoAnimado

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

            // **Nuevo estado: perfilGuardado → para saber si ya guardó sus datos**
            var perfilGuardado by remember { mutableStateOf(false) }

            var mostrarDialogoExito by remember{ mutableStateOf(false) }
            if (mostrarDialogoExito) {
                ExitoDialogoGuardadoAnimado(
                    mensaje = "¡Perfil guardado correctamente!",
                    onDismiss = {
                        mostrarDialogoExito = false
                        // Una vez confirmado, marcamos perfil como guardado y navegamos
                        perfilGuardado = true
                        navController.navigate("menu") {
                            popUpTo("personalizar") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

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

            // Validaciones del formulario (animación)
            var validacionesDialogo by remember{ mutableStateOf(false) }
            if (validacionesDialogo) {
                ValidacionesDialogoAnimado(
                    mensaje = "Por favor, completa todos los campos para continuar",
                    onDismiss = {
                        validacionesDialogo = false
                    }
                )
            }

            // ------------------ A PARTIR DE AQUÍ, MODIFICAMOS SOLO EL SCAFFOLD ------------------

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

                // **REEMPLAZAMOS bottomBar por una condicional: sólo si perfilGuardado == true mostramos la barra.**
                bottomBar = {
                    if (perfilGuardado) {
                        BarraNavegacionInferior(
                            navController = navController,
                            rutaActual = "personalizar" // o la ruta que corresponda
                        )
                    }
                    // Si perfilGuardado == false, no se renderiza nada aquí (la barra está “bloqueada”/oculta).
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
                    ImagenUsuario(
                        imagenBase64 = imagenBase64,
                        onEditClick = { launcher.launch("image/*") },
                        onDeleteClick = {
                            imagenUri = null
                            imagenBase64 = null
                        }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    CampoUsuario(username)         { username = it }
                    CampoNombre(nombre)            { nombre = it }
                    CampoApellidos(apellidos)      { apellidos = it }

                    // Mostrar el campo de fecha de nacimiento
                    CampoFechaNacimiento(fechasec) { showDatePicker = true }
                    // Mostrar el DatePicker y manejar la selección de fecha
                    FechaNacimientoSelector(
                        showDatePicker = showDatePicker,
                        onDismiss = { showDatePicker = false },
                        onValidDateSelected = { fechaSeleccionada ->
                            fechasec = fechaSeleccionada
                            showDatePicker = false
                        }
                    )

                    CampoPeso(peso)                { peso = it }
                    CampoAltura(altura)            { altura = it }
                    SelectorGenero(opcionesGenero, generoSeleccionado) { generoSeleccionado = it }
                    Spacer(modifier = Modifier.weight(1f))

                    // Botón “Guardar”
                    BotonGuardarPerfil {
                        if (uid != null) {
                            if (email == null) {
                                Toast.makeText(context, "No se pudo obtener el correo", Toast.LENGTH_SHORT).show()
                                return@BotonGuardarPerfil
                            }

                            val regex = "^[a-zA-Z0-9_ ]*$".toRegex() // Letras, números, guion bajo y espacios
                            // Validar campos requeridos
                            if (
                                username.isBlank() ||
                                !regex.matches(username) ||
                                username.trim().length < 3 ||
                                nombre.isBlank() ||
                                apellidos.isBlank() ||
                                fechasec.isEmpty() ||
                                peso.isEmpty() ||
                                peso.toFloatOrNull() == null ||
                                altura.isEmpty() ||
                                altura.toIntOrNull() == null ||
                                generoSeleccionado.isEmpty()
                            ) {
                                validacionesDialogo = true
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
                                altura     = altura.toIntOrNull()?.takeIf { it in 1..999 },
                                genero     = generoSeleccionado
                            )

                            // ==== Aquí hacemos la modificación para agregar "fechaCreacion" ====
                            val fechaHoy: String = LocalDate
                                .now()
                                .format(
                                    DateTimeFormatter.ofPattern("d MMMM, yyyy", Locale("es", "MX"))
                                )

                            // (b) Convertimos el Map<String, Any?> que devuelve usuario.toMap() en MutableMap<String, Any>
                            val mapOriginal: Map<String, Any?> = usuario.toMap()

                            val dataUsuario: MutableMap<String, Any> = mapOriginal
                                .filterValues { it != null }             // Eliminamos las entradas con valor null
                                .mapValues { it.value as Any }            // Convertimos Any? → Any
                                .toMutableMap()

        // (c) Agregamos sólo día/mes/año bajo la clave "fechaCreacion"
                            dataUsuario["fechaCreacion"] = fechaHoy

        // (d) Guardamos en Firestore usando merge:
                            FirebaseFirestore.getInstance()
                                .collection("usuarios")
                                .document(email)
                                .set(dataUsuario, SetOptions.merge())
                                .addOnSuccessListener {
                                    // Éxito al guardar usuario + fechaCreacion (solo dd/MM/yyyy)
                                }
                                .addOnFailureListener { e ->
                                    // Error al guardar
                                }
                            // =====================================================================

                            // Guardar (merge) en Firestore
                            db.collection("usuarios")
                                .document(email)
                                .set(dataUsuario, SetOptions.merge())
                                .addOnSuccessListener {
                                    // Al guardar correctamente, mostramos diálogo de éxito
                                    mostrarDialogoExito = true
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Error al guardar: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Función auxiliar para convertir Base64 a Bitmap
        fun base64ToBitmap(base64Str: String): Bitmap? {
            return try {
                val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            } catch (e: Exception) {
                null
            }
        }

        @Composable
        fun ImagenUsuario(imagenBase64: String?, onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
            val isDark = isSystemInDarkTheme()
            val tint = if (isDark) Color.Black else Color.Black

            Box(
                modifier = Modifier
                    .size(200.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                ) {
                    if (!imagenBase64.isNullOrEmpty()) {
                        val bitmap = remember(imagenBase64) { base64ToBitmap(imagenBase64) }

                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Usuario",
                                modifier = Modifier
                                    .fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.profile),
                                contentDescription = "Usuario",
                                modifier = Modifier
                                    .fillMaxSize(),
                                colorFilter = ColorFilter.tint(tint)
                            )
                        }
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.profile),
                            contentDescription = "Usuario",
                            modifier = Modifier
                                .fillMaxSize(),
                                colorFilter = ColorFilter.tint(tint)
                        )
                    }
                }

                // Botones de edición y eliminación en la parte inferior
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 16.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_edit),
                            contentDescription = "Editar foto",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier
                            .size(32.dp)
                            .background(RojoClaro, CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_delete),
                            contentDescription = "Eliminar foto",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }

        @Composable
        fun CampoUsuario(value: String, onValueChange: (String) -> Unit) {
            val regex = "^[a-zA-Z0-9_ ]*$".toRegex()
            val limpio = value.filter { it.code != 8203 }

            val valido = limpio.isNotBlank() &&
                    limpio.trim().length >= 3 &&
                    regex.matches(limpio)

            OutlinedTextField(
                value = value,
                onValueChange = { nuevoTexto ->
                    if (nuevoTexto.matches(regex)) {
                        onValueChange(nuevoTexto)
                    }
                },
                label = { Text("Nombre de usuario *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = if (valido) VerdePrincipal else Color.Red,
                    unfocusedBorderColor = if (valido || value.isEmpty()) GrisMedio else Color.Red,
                    focusedLabelColor    = if (valido) VerdePrincipal else Color.Red,
                    unfocusedLabelColor  = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    errorLabelColor      = Color.Red
                ),
                supportingText = {
                    when {
                        value.isBlank() -> {
                            Text(
                                "El nombre no puede estar vacío o solo contener espacios.",
                                color = Color.Red,
                                fontSize = 12.sp
                            )
                        }
                        value.trim().length < 3 -> {
                            Text(
                                "Debe tener al menos 3 caracteres (excluyendo espacios).",
                                color = Color.Red,
                                fontSize = 12.sp
                            )
                        }
                        !regex.matches(value) -> {
                            Text(
                                "Solo se permiten letras, números, guion bajo y espacios.",
                                color = Color.Red,
                                fontSize = 12.sp
                            )
                        }
                        else -> {
                            Text("Nombre de usuario válido.", color = GrisMedio, fontSize = 12.sp)
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        @Composable
        fun CampoNombre(value: String, onValueChange: (String) -> Unit) {
            val filtered = value.filter { it.isLetter() || it.isWhitespace() }
            OutlinedTextField(
                value = filtered,
                onValueChange = { onValueChange(it.filter { c -> c.isLetter() || c.isWhitespace() }) },
                label = { Text("Nombre *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(12.dp),
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
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = VerdePrincipal,
                    unfocusedBorderColor = GrisMedio,
                    unfocusedLabelColor  = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun FechaNacimientoSelector(
            showDatePicker: Boolean,
            onDismiss: () -> Unit,
            onValidDateSelected: (String) -> Unit
        ) {
            if (showDatePicker) {
                val context = LocalContext.current
                val datePickerState = rememberDatePickerState()

                var mostrarDialogoFallo by remember{ mutableStateOf(false) }
                if (mostrarDialogoFallo) {
                    FalloDialogoGuardadoAnimado(
                        mensaje = "La edad calculada no es válida para continuar. Debes tener más de 12 años.",
                        onDismiss = {
                            mostrarDialogoFallo = false
                        }
                    )
                }

                DatePickerDialog(
                    onDismissRequest = { onDismiss() },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val millisUTC = datePickerState.selectedDateMillis
                                if (millisUTC != null) {
                                    // Ajustar a zona horaria local
                                    val offset = TimeZone.getDefault().getOffset(millisUTC)
                                    val correctedMillis = millisUTC + offset

                                    val cal = Calendar.getInstance().apply { timeInMillis = correctedMillis }
                                    val current = Calendar.getInstance()

                                    val age = current.get(Calendar.YEAR) - cal.get(Calendar.YEAR)
                                    val cumpleEdad = age > 12 || (age == 12 &&
                                            (cal.get(Calendar.MONTH) < current.get(Calendar.MONTH) ||
                                                    (cal.get(Calendar.MONTH) == current.get(Calendar.MONTH) &&
                                                            cal.get(Calendar.DAY_OF_MONTH) <= current.get(Calendar.DAY_OF_MONTH)
                                                            )
                                                    )
                                            )

                                    if (cumpleEdad) {
                                        val fecha = String.format(
                                            Locale("es", "MX"),
                                            "%02d/%02d/%04d",
                                            cal.get(Calendar.DAY_OF_MONTH) + 1,
                                            cal.get(Calendar.MONTH) + 1,
                                            cal.get(Calendar.YEAR)
                                        )
                                        onValidDateSelected(fecha)
                                        onDismiss()
                                    } else {
                                        mostrarDialogoFallo = true
                                    }
                                } else {
                                    onDismiss()
                                }
                            }
                        ) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { onDismiss() }) {
                            Text("Cancelar")
                        }
                    }
                ) {
                    DatePicker(
                        state = datePickerState,
                        showModeToggle = false,
                        colors = DatePickerDefaults.colors(
                            selectedDayContainerColor = VerdePrincipal,
                            todayDateBorderColor = VerdePrincipal
                        )
                    )
                }
            }
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
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(12.dp),
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
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(12.dp),
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
            val filtered = value.filter { it.isDigit() }.take(3)  // Solo dígitos, máximo 3 caracteres
            OutlinedTextField(
                value = filtered,
                onValueChange = {
                    val newValue = it.filter { c -> c.isDigit() }.take(3)
                    onValueChange(newValue)
                },
                label = { Text("Altura *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(12.dp),
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
                onClick = onClick,
                modifier = Modifier
                    .width(200.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Guardar", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
