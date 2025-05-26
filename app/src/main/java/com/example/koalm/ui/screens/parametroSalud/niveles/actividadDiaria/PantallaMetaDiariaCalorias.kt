package com.example.koalm.ui.screens.parametroSalud.niveles.actividadDiaria

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.theme.*
import kotlin.math.roundToInt
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.CenterAlignedTopAppBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaMetaCalorias(
    navController: NavHostController = rememberNavController(),
    onCaloriasSeleccionadas: (Int) -> Unit = {}
) {
    val caloriasList = (400..1000 step 10).toList()
    val itemHeight = 56.dp
    val visibleItems = 3
    val density = LocalDensity.current

    val correo = FirebaseAuth.getInstance().currentUser?.email
    var metaCalorias by remember { mutableStateOf(500) }

    // 🔹 Leer valor desde Firestore
    LaunchedEffect(correo) {
        if (correo != null) {
            val snapshot = Firebase.firestore.collection("usuarios")
                .document(correo)
                .collection("metasSalud")
                .document("valores")
                .get()
                .await()

            metaCalorias = snapshot.getLong("metaCalorias")?.toInt() ?: 500
        }
    }

    // 🔹 Scroll basado en la meta leída
    val listState = remember(metaCalorias) {
        LazyListState(
            firstVisibleItemIndex = caloriasList.indexOf(metaCalorias).coerceAtLeast(0)
        )
    }

    val centeredItemIndex by remember(
        listState.firstVisibleItemIndex,
        listState.firstVisibleItemScrollOffset
    ) {
        derivedStateOf {
            val offset = listState.firstVisibleItemScrollOffset
            val index = listState.firstVisibleItemIndex +
                    ((offset / with(density) { itemHeight.toPx() }).roundToInt())
            caloriasList.getOrElse(index) { metaCalorias }
        }
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            listState.animateScrollToItem(caloriasList.indexOf(centeredItemIndex))
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Meta diaria de calorías",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        correo?.let {
                            Firebase.firestore.collection("usuarios")
                                .document(it)
                                .collection("metasSalud")
                                .document("valores")
                                .update("metaCalorias", centeredItemIndex)
                        }
                        navController.navigateUp()
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Confirmar")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
            )
        },
        bottomBar = {
            BarraNavegacionInferior(navController, rutaActual = "inicio")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Text("Calorías", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .height(itemHeight * visibleItems)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PaddingValues(vertical = itemHeight)
                ) {
                    items(caloriasList.size) { i ->
                        val valor = caloriasList[i]
                        val color = if (valor == centeredItemIndex) VerdePrincipal else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        val scale = if (valor == centeredItemIndex) 1.4f else 1f

                        Text(
                            text = "$valor",
                            fontSize = 24.sp * scale,
                            color = color,
                            modifier = Modifier
                                .height(itemHeight)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Solo se contabilizan las calorías quemadas en las actividades, no las quemadas de manera pasiva.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Justify
            )
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun VistaPreviaPantallaMetaCalorias() {
    PantallaMetaCalorias()
}
