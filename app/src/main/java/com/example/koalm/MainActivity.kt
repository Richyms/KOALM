package com.example.koalm
import androidx.credentials.CustomCredential
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    private lateinit var credentialManager: CredentialManager
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa FirebaseAuth y CredentialManager
        firebaseAuth = FirebaseAuth.getInstance()
        credentialManager = CredentialManager.create(this)

        // Puede venir “habitos” o “iniciar” desde un Intent previo
        val navigateTo = intent.getStringExtra("navigateTo")

        // Lanza la UI Compose
        setContent {
            MainApp(
                onGoogleSignInClick = { handleGoogleSignIn() },
                startDestination = navigateTo ?: "iniciar"
            )
        }
    }

    private fun handleGoogleSignIn() {
        // Construye la opción para cuentas ya autorizadas
        val option = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(getString(R.string.default_web_client_id))
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        // Importante: el orden es (context, request), no al revés :contentReference[oaicite:0]{index=0}
        lifecycleScope.launch {
            try {
                val response: GetCredentialResponse =
                    credentialManager.getCredential(
                        /* context = */ this@MainActivity,
                        /* request = */ request
                    )
                processCredential(response)
            } catch (e: GetCredentialException) {
                // Si no había credenciales, lanzamos flujo de signup
                handleGoogleSignUp()
            }
        }
    }

    private fun handleGoogleSignUp() {
        val option = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.default_web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        lifecycleScope.launch {
            try {
                val response = credentialManager.getCredential(
                    /* context = */ this@MainActivity,
                    /* request = */ request
                )
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
        val cred = response.credential
        if (cred is CustomCredential &&
            cred.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val idToken = GoogleIdTokenCredential.createFrom(cred.data).idToken
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
                        startActivity(
                            Intent(this, MainActivity::class.java).apply {
                                putExtra("navigateTo", "habitos")
                            }
                        )
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
        } else {
            Toast.makeText(
                this,
                "No se obtuvo credencial válida de Google",
                Toast.LENGTH_LONG
            ).show()
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
