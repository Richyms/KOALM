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
import android.util.Log
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
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import androidx.credentials.*
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider

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

        /* ---------- Diagnóstico opcional ---------- */
        val sm = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sm.getSensorList(Sensor.TYPE_ALL).forEach {
            Log.i("SENSORS", "${it.name} – ${it.type}")
        }

        /* ---------- Pedimos permiso y –si lo hay– arrancamos el service ---------- */
        requestActivityRecognitionIfNeeded()

        val startDestination =
            if (firebaseAuth.currentUser?.isEmailVerified == true) "menu" else "iniciar"

        setContent { MainApp(::handleGoogleSignIn, startDestination) }
    }

    /* ---------- Serv. de pasos ---------- */
    private fun launchStepService() {
        val intent = Intent(this, com.example.koalm.services.MovimientoService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else startService(intent)
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

    /* ---------- Permiso ACTIVITY_RECOGNITION ---------- */
    private fun requestActivityRecognitionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED

            if (granted) {
                launchStepService()          // ya está concedido → arrancamos
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                    ACTIVITY_RECOGNITION_REQ
                )
            }
        } else {
            launchStepService()              // < Android 10 no requiere permiso
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
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

            if (granted) launchStepService()   // sólo arrancamos si ahora se concedió
        }
    }

    /* ---------- GOOGLE SIGN‑IN ---------- */
    private fun handleGoogleSignIn() { /* ... sin cambios ... */ }

    private fun handleGoogleSignUp() { /* ... sin cambios ... */ }

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
                /* Arrancamos el servicio sólo si se tiene permiso */
                startStepServiceIfPermitted()

                Toast.makeText(
                    this,
                    "Bienvenido ${firebaseAuth.currentUser?.displayName}",
                    Toast.LENGTH_SHORT
                ).show()

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
    onGoogleSignInClick: () -> Unit,
    startDestination: String
) {
    KoalmTheme {
        val navController = rememberNavController()
        val systemUi      = rememberSystemUiController()
        val isDark        = isSystemInDarkTheme()

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
