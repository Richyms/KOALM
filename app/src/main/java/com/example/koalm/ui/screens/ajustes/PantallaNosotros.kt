/*PantallaNosotros.kt*/
package com.example.koalm.ui.screens.ajustes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.koalm.R
import com.example.koalm.model.Usuario
import com.example.koalm.ui.theme.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.navigation.NavController
import com.example.koalm.ui.components.BarraNavegacionInferior

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaNosotros(navController: NavHostController){
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {Text("Acerca de Nosotros")},
                navigationIcon = {
                    IconButton(onClick =  { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            BarraNavegacionInferior(
                navController = navController,
                rutaActual = "tipos_habitos"
            )
        }
    )
    { innerPadding ->

        Column(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape   = RoundedCornerShape(16.dp),
                border  = BorderStroke(1.dp, VerdeBorde),
                colors  = CardDefaults.cardColors(containerColor = VerdeContenedor)
            ){
                Column(
                    Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Text("Descripción de todo el equipo")
                }
            }

            Spacer(Modifier.weight(1f))

            ListaIntegrantes()

            Image(
                painter = painterResource(id = R.drawable.koala_comiendo),//Remplazar foto por una del logo o equipo
                contentDescription = "Equipo Koalm",
                modifier = Modifier.size(300.dp)
            )

        }

    }
}

@Composable
fun ListaIntegrantes() {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Integrantes",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text("• Karime Calero")
                Text("• Kein Carrillo")
                Text("• Eduardo Morgado")
                Text("• Damian Franco")
                Text("• Miguel Gomez")
                Text("• Nailea Hernandez")
                Text("• Maximiliano Leon")
                Text("• Jesus Melo")
                Text("• Ricardo Mora")
                Text("• Rigel Ocaña")
                Text("• Michel Vazquez")
                Text("• Veronica Villegas")
            }
        }
    }
}