package com.example.koalm

/* ---------- IMPORTS ---------- */
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
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
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import androidx.credentials.*

class MainActivity : ComponentActivity() {

    /* ---------- CONSTANTES ---------- */
    private val ACTIVITY_RECOGNITION_REQ = 101

    /* ---------- ATRIBUTOS ---------- */
    private lateinit var credentialManager: CredentialManager
    private lateinit var firebaseAuth: FirebaseAuth

    /* ---------- CICLO DE VIDA ---------- */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAuth      = FirebaseAuth.getInstance()
        credentialManager = CredentialManager.create(this)

        // Diagnóstico opcional: lista de sensores disponibles
        (getSystemService(Context.SENSOR_SERVICE) as SensorManager)
            .getSensorList(Sensor.TYPE_ALL).forEach {
                android.util.Log.i("SENSORS", "${it.name} – ${it.type}")
            }

        // Permiso ACTIVITY_RECOGNITION y, si procede, arranque del servicio
        requestActivityRecognitionIfNeeded()

        val startDestination =
            if (firebaseAuth.currentUser?.isEmailVerified == true) "menu" else "iniciar"

        /* ---------------- UI COMPOSE ---------------- */
        setContent {
            val navController = rememberNavController()

            // Responder a intents/notificaciones externas
            LaunchedEffect(intent?.action, intent?.getStringExtra("route")) {
                when {
                    intent?.action == "com.example.koalm.START_TIMER" ->
                        navController.navigate("notas")
                    intent?.getStringExtra("route") == "notas"   ->
                        navController.navigate("notas")  { popUpTo("menu") { saveState = true }; launchSingleTop = true; restoreState = true }
                    intent?.getStringExtra("route") == "libros"  ->
                        navController.navigate("libros") { popUpTo("menu") { saveState = true }; launchSingleTop = true; restoreState = true }
                }
            }

            MainApp(
                navController        = navController,
                onGoogleSignInClick  = ::handleGoogleSignIn,
                startDestination     = startDestination
            )
        }
    }

    /* ---------- SERVICIO DE PASOS ---------- */
    private fun launchStepService() {
        val intent = Intent(this, com.example.koalm.services.MovimientoService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent)
        else startService(intent)
    }

    private fun startStepServiceIfPermitted() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            launchStepService()
        }
    }

    /* ---------- PERMISO ACTIVITY_RECOGNITION ---------- */
    private fun requestActivityRecognitionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED

            if (granted) {
                launchStepService()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                    ACTIVITY_RECOGNITION_REQ
                )
            }
        } else {
            launchStepService() // < Android 10 no requiere permiso
        }
    }

    @Suppress("DEPRECATION")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,       // firma exacta
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == ACTIVITY_RECOGNITION_REQ) {
            val granted = grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED
            val msg = if (granted)
                "Permiso de actividad física otorgado ✔️"
            else
                "Permiso de actividad física denegado — el podómetro estará desactivado"
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()

            if (granted) launchStepService()
        }
    }
    /* ---------- GOOGLE SIGN‑IN / SIGN‑UP ---------- */
    private fun handleGoogleSignIn() {
        // 1. Intentamos con cuentas ya autorizadas
        val option = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(getString(R.string.default_web_client_id))
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()

        lifecycleScope.launch {
            try {
                val response = credentialManager.getCredential(this@MainActivity, request)
                processCredential(response)
            } catch (e: GetCredentialException) {
                // 2. Si falla, lanzamos flujo de alta
                handleGoogleSignUp()
            }
        }
    }

    private fun handleGoogleSignUp() {
        val option = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.default_web_client_id))
            .build()

        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()

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

    /* ---------- AUTH CON FIREBASE ---------- */
    private fun processCredential(response: GetCredentialResponse) {
        val idToken = GoogleIdTokenCredential
            .createFrom(response.credential.data).idToken ?: run {
            Toast.makeText(this, "No se obtuvo token válido de Google", Toast.LENGTH_LONG).show()
            return
        }

        firebaseAuth.signInWithCredential(
            GoogleAuthProvider.getCredential(idToken, null)
        ).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                startStepServiceIfPermitted()

                Toast.makeText(
                    this,
                    "Bienvenido ${firebaseAuth.currentUser?.displayName}",
                    Toast.LENGTH_SHORT
                ).show()

                // Reiniciamos para refrescar navegación según usuario logueado
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(
                    this,
                    "Error al iniciar sesión: ${task.exception?.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}

/* ---------- COMPOSABLE PRINCIPAL ---------- */
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
            systemUi.setSystemBarsColor(Color.Transparent, darkIcons = !isDark)
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color    = MaterialTheme.colorScheme.background
        ) {
            AppNavigation(
                navController        = navController,
                onGoogleSignInClick  = onGoogleSignInClick,
                startDestination     = startDestination
            )
        }
    }
}
