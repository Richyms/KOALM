package com.example.koalm.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dotlottie.dlplayer.Mode
import com.lottiefiles.dotlottie.core.compose.ui.DotLottieAnimation
import com.lottiefiles.dotlottie.core.util.DotLottieSource

@Composable
fun ExitoDialogoGuardadoAnimado(
    mensaje: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = false, dismissOnBackPress = false)
    ){
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .padding(16.dp)
                .wrapContentSize()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // DotLottieAnimation
                DotLottieAnimation(
                    source = DotLottieSource.Url("https://lottie.host/d4eb5337-6ae9-4e63-a6c2-62d4b0ccdb42/WXTotsqYYQ.lottie"),
                    autoplay = true,
                    loop = false,
                    speed = 1.5f,
                    useFrameInterpolation = true,
                    playMode = Mode.FORWARD,
                    modifier = Modifier
                        .size(120.dp)
                        .background(MaterialTheme.colorScheme.surface, shape = CircleShape)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = mensaje,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = onDismiss) {
                    Text("¡Genial!")
                }
            }
        }
    }
}

@Composable
fun FalloDialogoGuardadoAnimado(
    mensaje: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = false, dismissOnBackPress = false)
    ){
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .padding(16.dp)
                .wrapContentSize()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // DotLottieAnimation
                DotLottieAnimation(
                    source = DotLottieSource.Url("https://lottie.host/294b7a8b-750d-469f-9bbe-b34e1fc458f8/Ge24RqRaKI.lottie"),
                    autoplay = true,
                    loop = true,
                    speed = 1.5f,
                    useFrameInterpolation = false,
                    playMode = Mode.FORWARD,
                    modifier = Modifier
                        .size(120.dp)
                        .background(MaterialTheme.colorScheme.surface, shape = CircleShape)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = mensaje,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = onDismiss) {
                    Text("¡Intenta nuevamente!")
                }
            }
        }
    }
}

@Composable
fun LogroDialogoAnimado(
    mensaje: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = false, dismissOnBackPress = false)
    ){
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .padding(16.dp)
                .wrapContentSize()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // DotLottieAnimation
                DotLottieAnimation(
                    source = DotLottieSource.Url("https://lottie.host/6595274c-487a-486a-bd88-faf798070040/EsZBcKokZ3.lottie"),
                    autoplay = true,
                    loop = false,
                    speed = 1.5f,
                    useFrameInterpolation = false,
                    playMode = Mode.FORWARD,
                    modifier = Modifier
                        .size(120.dp)
                        .background(MaterialTheme.colorScheme.surface, shape = CircleShape)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = mensaje,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = onDismiss) {
                    Text("¡Genial!")
                }
            }
        }
    }
}

@Composable
fun ValidacionesDialogoAnimado(
    mensaje: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = false, dismissOnBackPress = false)
    ){
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .padding(16.dp)
                .wrapContentSize()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // DotLottieAnimation
                DotLottieAnimation(
                    source = DotLottieSource.Url("https://lottie.host/5e424783-9eb3-4aca-ab79-3e632976aae1/c3WN6MQ5VS.lottie"),
                    autoplay = true,
                    loop = true,
                    speed = 1.5f,
                    useFrameInterpolation = false,
                    playMode = Mode.FORWARD,
                    modifier = Modifier
                        .size(120.dp)
                        .background(MaterialTheme.colorScheme.surface, shape = CircleShape)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = mensaje,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = onDismiss) {
                    Text("¡OK!")
                }
            }
        }
    }
}

@Composable
fun BienvenidoDialogoAnimado(
    mensaje: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = false, dismissOnBackPress = false)
    ){
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .padding(16.dp)
                .wrapContentSize()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // DotLottieAnimation
                DotLottieAnimation(
                    source = DotLottieSource.Url("https://lottie.host/9a1808be-0436-42cd-918c-55963c8e71a5/zCh5lTbX1N.lottie"),
                    autoplay = true,
                    loop = false,
                    speed = 1.5f,
                    useFrameInterpolation = false,
                    playMode = Mode.FORWARD,
                    modifier = Modifier
                        .size(200.dp)
                        .background(MaterialTheme.colorScheme.surface, shape = CircleShape)
                        .pointerInput(Unit) {}
                )


                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = mensaje,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = onDismiss) {
                    Text("¡Comenzar!")
                }
            }
        }
    }
}