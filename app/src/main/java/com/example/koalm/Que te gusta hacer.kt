package com.example.koalm

@Composable
fun PantallaHabitos(navController: NavController) {
    var correr by remember { mutableStateOf(true) }
    var leer by remember { mutableStateOf(true) }
    var meditar by remember { mutableStateOf(true) }
    var nadar by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("¿Qué te gusta hacer?") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigation {
                BottomNavigationItem(
                    selected = true,
                    onClick = { navController.navigate("inicio") },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") }
                )
                BottomNavigationItem(
                    selected = false,
                    onClick = { navController.navigate("habitos") },
                    icon = { Icon(Icons.Default.List, contentDescription = "Hábitos") },
                    label = { Text("Hábitos") }
                )
                BottomNavigationItem(
                    selected = false,
                    onClick = { navController.navigate("perfil") },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.koala_pesas),
                contentDescription = "Koala con pesas",
                modifier = Modifier.size(150.dp)
            )

            Text(
                text = "Marca tus hábitos a mejorar",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Lista de hábitos con checkboxes
            Row {
                Checkbox(checked = correr, onCheckedChange = { correr = it })
                Text(text = "Correr - Tengo el hábito de correr.")
            }
            Row {
                Checkbox(checked = leer, onCheckedChange = { leer = it })
                Text(text = "Leer - Suelo leer constantemente.")
            }
            Row {
                Checkbox(checked = meditar, onCheckedChange = { meditar = it })
                Text(text = "Meditar - Tomo un tiempo para meditar.")
            }
            Row {
                Checkbox(checked = nadar, onCheckedChange = { nadar = it })
                Text(text = "Nadar - Me gusta nadar.")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    navController.navigate("guardar") // Navega después de guardar
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar")
            }
        }
    }
}