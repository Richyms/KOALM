package com.example.koalm
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import kotlinx.coroutines.launch
import androidx.credentials.exceptions.GetCredentialException

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import androidx.credentials.*
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

class MainActivity : ComponentActivity() {

    companion object {
        const val ACTIVITY_RECOGNITION_REQ_CODE = 101
    }

    private lateinit var credentialManager: CredentialManager
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        credentialManager = CredentialManager.create(this)

        requestActivityRecognitionIfNeeded()

        val startDestination = if (firebaseAuth.currentUser?.isEmailVerified == true) "menu" else "iniciar"

        // Lanzar servicio desde inicio
        launchStepService()

        setContent {
            val navController = rememberNavController()
            MainApp(navController, ::handleGoogleSignIn, startDestination)
        }
    }

    private fun launchStepService() {
        val intent = Intent(this, com.example.koalm.services.MovimientoService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent)
        else startService(intent)
    }

    private fun requestActivityRecognitionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED

            if (granted) launchStepService()
            else ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                ACTIVITY_RECOGNITION_REQ_CODE
            )
        } else launchStepService()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == ACTIVITY_RECOGNITION_REQ_CODE) {
            if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                launchStepService()
            }
        }
    }

    private fun handleGoogleSignIn() {
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
            } catch (_: GetCredentialException) {
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
                Toast.makeText(this@MainActivity, "Error al registrar con Google", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun processCredential(response: GetCredentialResponse) {
        val idToken = GoogleIdTokenCredential.createFrom(response.credential.data).idToken ?: return

        firebaseAuth.signInWithCredential(
            GoogleAuthProvider.getCredential(idToken, null)
        ).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = firebaseAuth.currentUser
                val uid = user?.uid
                val correo = user?.email

                // Guardar en SharedPreferences
                getSharedPreferences("koalm_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putString("uid", uid)
                    .putString("correo", correo)
                    .apply()

                // 🔹 Crear metasSalud por defecto si no existen
                if (correo != null) {
                    val metasRef = Firebase.firestore
                        .collection("usuarios")
                        .document(correo)
                        .collection("metasSalud")
                        .document("valores")

                    metasRef.get().addOnSuccessListener { doc: com.google.firebase.firestore.DocumentSnapshot ->
                        if (!doc.exists()) {
                            metasRef.set(
                                mapOf(
                                    "metaPasos" to 6000,
                                    "metaMinutos" to 60,
                                    "metaCalorias" to 300
                                )
                            )
                        }
                    }


                }

                launchStepService()
                Toast.makeText(this, "Bienvenido ${user?.displayName}", Toast.LENGTH_SHORT).show()

                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }


    @Composable
    fun MainApp(
        navController: androidx.navigation.NavHostController,
        onGoogleSignInClick: () -> Unit,
        startDestination: String
    ) {
        KoalmTheme {
            val systemUi = rememberSystemUiController()
            val isDark = isSystemInDarkTheme()

            SideEffect {
                systemUi.setSystemBarsColor(Color.Transparent, darkIcons = !isDark)
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
}