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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import kotlinx.coroutines.launch

// Importa tu tema y tu NavGraph personalizados:
import com.example.koalm.ui.theme.KoalmTheme
import com.example.koalm.navigation.AppNavigation

class MainActivity : ComponentActivity() {

    companion object {
        const val ACTIVITY_RECOGNITION_REQ_CODE = 101
    }

    private lateinit var credentialManager: CredentialManager
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Instanciamos FirebaseAuth y CredentialManager
        firebaseAuth = FirebaseAuth.getInstance()
        credentialManager = CredentialManager.create(this)

        // Pedimos permiso de Activity Recognition si es necesario
        requestActivityRecognitionIfNeeded()

        // —————— LEEMOS EXTRA “startDestination” EN CASO DE QUE HAYA VENIDO DE processCredential() ——————
        val fromIntent: String? = intent.getStringExtra("startDestination")
        // Si no vino por Google Sign-In, usamos la lógica por defecto:
        val startDestination: String = fromIntent
            ?: if (firebaseAuth.currentUser?.isEmailVerified == true) {
                // Si el usuario ya tenía sesión iniciada y verificada por email
                "menu"
            } else {
                // Si el usuario no está autenticado o no ha verificado su correo
                "iniciar"
            }

        // Iniciamos el servicio de conteo de pasos (solo si ya tenemos permisos)
        launchStepService()

        // —————— SETEAMOS EL CONTENIDO COMPOSABLE ——————
        setContent {
            // NavController para navegación Compose
            val navController = rememberNavController()

            // Esta función es COMPOSABLE; llama a KoalmTheme y a AppNavigation
            MainApp(
                navController     = navController,
                onGoogleSignInClick = ::handleGoogleSignIn,
                startDestination  = startDestination
            )
        }
    }

    //  Este método arranca tu MovimientoService (foreground service para contar pasos)
    private fun launchStepService() {
        val intent = Intent(this, com.example.koalm.services.MovimientoService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    //  Este método verifica/solicita permiso de Activity Recognition en Android Q+
    private fun requestActivityRecognitionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED

            if (granted) {
                launchStepService()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                    ACTIVITY_RECOGNITION_REQ_CODE
                )
            }
        } else {
            // En versiones anteriores no se necesita este permiso explícito
            launchStepService()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ACTIVITY_RECOGNITION_REQ_CODE) {
            if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                launchStepService()
            }
        }
    }

    // —————— INICIO DE SESIÓN CON GOOGLE ——————

    private fun handleGoogleSignIn() {
        // 1) Configuramos la petición para obtener el ID Token
        val option = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(getString(R.string.default_web_client_id))
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        // 2) Abrimos el flujo de coroutines para invocar getCredential()
        lifecycleScope.launch {
            try {
                val response: GetCredentialResponse =
                    credentialManager.getCredential(this@MainActivity, request)
                processCredential(response)
            } catch (_: GetCredentialException) {
                // Si falla porque no existe credencial preautorizada, abrimos la pantalla de signup de Google
                handleGoogleSignUp()
            }
        }
    }

    private fun handleGoogleSignUp() {
        // 1) Misma lógica que handleGoogleSignIn, pero sin filtrar cuentas autorizadas
        val option = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.default_web_client_id))
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
                Toast.makeText(this@MainActivity,
                    "Error al registrar con Google",
                    Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun processCredential(response: GetCredentialResponse) {
        // 1) Extraemos el ID Token de la respuesta
        val idToken: String =
            GoogleIdTokenCredential.createFrom(response.credential.data).idToken
                ?: return

        // 2) Autenticamos con Firebase usando ese ID Token
        firebaseAuth.signInWithCredential(
            GoogleAuthProvider.getCredential(idToken, null)
        ).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Usuario autenticado en Firebase
                val user = firebaseAuth.currentUser
                val correo = user?.email ?: return@addOnCompleteListener

                // 3) Creamos metasSalud por defecto si no existen
                val metasRef = Firebase.firestore
                    .collection("usuarios")
                    .document(correo)
                    .collection("metasSalud")
                    .document("valores")

                metasRef.get().addOnSuccessListener { metasDoc ->
                    if (!metasDoc.exists()) {
                        metasRef.set(
                            mapOf(
                                "metaPasos"    to 6000,
                                "metaMinutos"  to 60,
                                "metaCalorias" to 300
                            )
                        )
                    }
                }

                // 4) Checamos si el perfil del usuario está completo o no
                val userDocRef = Firebase.firestore
                    .collection("usuarios")
                    .document(correo)

                userDocRef.get().addOnSuccessListener { doc ->
                    // Si no existe, forzamos a personalizar
                    val destino: String = if (doc.exists()) {
                        val nombre     = doc.getString("nombre")     ?: ""
                        val apellido   = doc.getString("apellido")   ?: ""
                        val nacimiento = doc.getString("nacimiento") ?: ""
                        val genero     = doc.getString("genero")     ?: ""
                        val peso       = doc.getDouble("peso")?.toFloat()
                        val altura     = doc.getLong("altura")?.toInt()

                        val completo = listOf(
                            nombre.isNotBlank(),
                            apellido.isNotBlank(),
                            nacimiento.isNotBlank(),
                            genero.isNotBlank(),
                            peso != null,
                            altura != null
                        ).all { it }

                        if (completo) "menu" else "personalizar"
                    } else {
                        "personalizar"
                    }

                    // 5) Lanzamos MainActivity de nuevo, enviando el extra "startDestination"
                    val intent = Intent(this, MainActivity::class.java).apply {
                        putExtra("startDestination", destino)
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(intent)
                    finish()
                }
            } else {
                Toast.makeText(this,
                    "Error al iniciar con Google",
                    Toast.LENGTH_LONG).show()
            }
        }
    }

    // —————— FUNCIÓN COMPOSABLE PRINCIPAL ——————

    @Composable
    fun MainApp(
        navController: androidx.navigation.NavHostController,
        onGoogleSignInClick: () -> Unit,
        startDestination: String
    ) {
        KoalmTheme {
            val systemUi = rememberSystemUiController()
            val isDark   = isSystemInDarkTheme()

            SideEffect {
                // Cambiamos color de barra de estado según tema
                systemUi.setSystemBarsColor(Color.Transparent, darkIcons = !isDark)
            }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                // Aquí definimos el NavHost y las pantallas, empezando en startDestination
                AppNavigation(
                    navController       = navController,
                    onGoogleSignInClick = onGoogleSignInClick,
                    startDestination    = startDestination
                )
            }
        }
    }
}
