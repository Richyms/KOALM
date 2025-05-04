package com.example.koalm

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.koalm.navigation.AppNavigation
import com.example.koalm.ui.theme.KoalmTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException

class MainActivity : ComponentActivity() {


//Constante para los pasos
    private val ACTIVITY_RECOGNITION_REQ = 101   // requestCode para el permiso de pasos

    /* ----------  ATRIBUTOS  ---------- */
    private lateinit var credentialManager: CredentialManager
    private lateinit var firebaseAuth: FirebaseAuth

    /* ----------  CICLO DE VIDA  ---------- */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Firebase & CredentialManager
        firebaseAuth = FirebaseAuth.getInstance()
        credentialManager = CredentialManager.create(this)

        // Solicitar permiso de actividad física si hace falta
        requestActivityRecognitionIfNeeded()

        // Pantalla inicial según usuario verificado o no
        val startDestination = if (firebaseAuth.currentUser?.isEmailVerified == true) {
            "menu"
        } else {
            "iniciar"
        }

        // UI Compose
        setContent {
            val navController = rememberNavController()

            /* --- Navegación desde notificaciones / intents --- */
            LaunchedEffect(intent?.action, intent?.getStringExtra("route")) {
                when {
                    intent?.action == "com.example.koalm.START_TIMER" -> {
                        navController.navigate("notas")
                    }
                    intent?.getStringExtra("route") == "notas" -> {
                        navController.navigate("notas") {
                            popUpTo("menu") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                    intent?.getStringExtra("route") == "libros" -> {
                        navController.navigate("libros") {
                            popUpTo("menu") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            }

        //Navegacion principal
            KoalmTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        navController = navController,
                        onGoogleSignInClick = { handleGoogleSignIn() },
                        startDestination = startDestination
                    )
                }
            }
        }
    }

//Permisos para el podometro
private fun requestActivityRecognitionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                    ACTIVITY_RECOGNITION_REQ
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ACTIVITY_RECOGNITION_REQ) {
            if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso activado ✔️", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    this,
                    "Permino no activado",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun handleGoogleSignIn() {
        val option = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(getString(com.example.koalm.R.string.default_web_client_id))
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        lifecycleScope.launch {
            try {
                val response: GetCredentialResponse =
                    credentialManager.getCredential(this@MainActivity, request)
                processCredential(response)
            } catch (e: GetCredentialException) {
                // Si no existen credenciales, se lanza el flujo de registro
                handleGoogleSignUp()
            }
        }
    }

    private fun handleGoogleSignUp() {
        val option = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(com.example.koalm.R.string.default_web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        lifecycleScope.launch {
            try {
                val response = credentialManager.getCredential(this@MainActivity, request)
                processCredential(response)
            } catch (e: GetCredentialException) {
                Toast.makeText(
                    this@MainActivity,
                    "Error al registrar con Google: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun processCredential(response: GetCredentialResponse) {
        val idToken = GoogleIdTokenCredential.createFrom(response.credential.data).idToken
        if (idToken != null) {
            firebaseAuth.signInWithCredential(
                GoogleAuthProvider.getCredential(idToken, null)
            ).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Bienvenido ${firebaseAuth.currentUser?.displayName}",
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(this, MainActivity::class.java).apply {
                        putExtra("navigateTo", "habitos")
                    })
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        "Error al iniciar sesión: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } else {
            Toast.makeText(this, "No se obtuvo credencial válida de Google", Toast.LENGTH_LONG)
                .show()
        }
    }
}


@Composable
fun MainApp(
    onGoogleSignInClick: () -> Unit,
    startDestination: String
) {
    KoalmTheme {
        val navController = rememberNavController()
        val systemUi = rememberSystemUiController()
        val isDark = isSystemInDarkTheme()

        SideEffect {
            systemUi.setSystemBarsColor(
                color = Color.Transparent,
                darkIcons = !isDark
            )
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AppNavigation(
                navController = navController,
                onGoogleSignInClick = onGoogleSignInClick,
                startDestination = startDestination
            )
        }
    }
}
