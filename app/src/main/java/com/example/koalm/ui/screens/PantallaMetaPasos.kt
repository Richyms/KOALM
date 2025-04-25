package com.example.koalm.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.example.koalm.ui.theme.VerdePrincipal
import kotlin.math.roundToInt
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.CenterAlignedTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaMetaPasos(
    navController: NavHostController = rememberNavController(),
    metaPasos: Int = 10000,
    onPasosSeleccionados: (Int) -> Unit = {}
) {
    val pasosList = (5000..15000 step 500).toList()
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = pasosList.indexOf(metaPasos).coerceAtLeast(0)
    )
    val itemHeight = 56.dp
    val visibleItems = 3

    val density = LocalDensity.current
    val centeredItemIndex by remember(
        listState.firstVisibleItemIndex,
        listState.firstVisibleItemScrollOffset
    ) {
        derivedStateOf {
            val offset = listState.firstVisibleItemScrollOffset
            val index = listState.firstVisibleItemIndex +
                    ((offset / with(density) { itemHeight.toPx() }).roundToInt())
            pasosList.getOrElse(index) { metaPasos }
        }
    }

    // Scroll automático al ítem centrado
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            listState.animateScrollToItem(pasosList.indexOf(centeredItemIndex))
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Meta diaria de pasos") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = { onPasosSeleccionados(centeredItemIndex) }) {
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
            Text("Pasos", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)

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
                    items(pasosList.size) { i ->
                        val valor = pasosList[i]
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
                text = "La meta de pasos configurada por defecto es de 10000 pasos, la cual puede ser alcanzada en 1 hora  caminata rápida o 1:30 hr de caminata lenta.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Justify
            )
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun VistaPreviaPantallaMetaPasos() {
    PantallaMetaPasos()
}
