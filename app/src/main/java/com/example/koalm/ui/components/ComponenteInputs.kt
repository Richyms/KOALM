// src/main/java/com/example/koalm/ui/components/ComponenteInputs.kt
package com.example.koalm.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier

@Composable
fun ComponenteInputs(
    label: String,
    estado: MutableState<Float>,
    fecha: String
) {
    OutlinedTextField(
        value = if (estado.value == 0f) "" else estado.value.toString(),
        onValueChange = { nuevoTexto ->
            // Sólo actualizamos el estado si el texto es un Float válido
            nuevoTexto.toFloatOrNull()?.let { estado.value = it }
        },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        trailingIcon = { Text(text = "kg el $fecha") }
    )
}
