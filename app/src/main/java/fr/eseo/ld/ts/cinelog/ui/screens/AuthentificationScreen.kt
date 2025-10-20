package fr.eseo.ld.ts.cinelog.ui.screens

import android.app.Activity
import android.content.ActivityNotFoundException
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import fr.eseo.ld.ts.cinelog.ui.viewmodels.AuthenticationViewModel

@Composable
fun MainAuthenticationScreen(
    onSignUpSelected: () -> Unit,
    authenticationViewModel: AuthenticationViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    // Lire les locals composables UNE FOIS ici (dans la composition)
    val context = LocalContext.current
    val activity: Activity? = LocalActivity.current

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Ce callback n'est pas composable : n'utiliser que des variables capturées (context, activity, ...)
        try {
            val data = result.data
            if (data == null) {
                Log.e("AuthScreen", "Google sign-in intent data is null (resultCode=${result.resultCode})")
                Toast.makeText(context, "Données Google manquantes", Toast.LENGTH_SHORT).show()
                return@rememberLauncherForActivityResult
            }

            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = try {
                task.getResult(ApiException::class.java)
            } catch (e: ApiException) {
                Log.e("AuthScreen", "ApiException getResult", e)
                Toast.makeText(context, "Erreur Google: ${e.statusCode}", Toast.LENGTH_LONG).show()
                return@rememberLauncherForActivityResult
            }

            val idToken: String? = account?.idToken
            if (idToken.isNullOrBlank()) {
                Log.e("AuthScreen", "idToken absent ou vide. account=$account")
                Toast.makeText(context, "Impossible d'obtenir l'idToken Google", Toast.LENGTH_LONG).show()
                return@rememberLauncherForActivityResult
            }

            // Appel au ViewModel — la callback effectue la navigation si succès
            authenticationViewModel.signInWithGoogle(idToken) { success, error ->
                if (success) {
                    Toast.makeText(context, "Connexion réussie", Toast.LENGTH_SHORT).show()
                    onLoginSuccess()
                } else {
                    Log.e("AuthScreen", "signInWithGoogle failed: $error")
                    Toast.makeText(context, error ?: "Erreur de connexion", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (t: Throwable) {
            // Catch général pour éviter que l'app ne tombe sans trace
            Log.e("AuthScreen", "Unexpected crash during Google sign-in", t)
            Toast.makeText(context, "Erreur inattendue lors de la connexion", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(padding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Bienvenue", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Mot de passe") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Button(onClick = {
                authenticationViewModel.loginWithEmail(email, password) { success, error ->
                    if (success) {
                        errorMessage = ""
                        onLoginSuccess()
                    } else {
                        errorMessage = error ?: "Email ou mot de passe incorrect."
                    }
                }
            }) {
                Text("Se connecter")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onSignUpSelected) {
                Text("S'inscrire")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                val googleSignInClient = authenticationViewModel.getGoogleSignInClient(context)
                try {
                    val signInIntent = googleSignInClient.getSignInIntent()
                    if (activity == null) {
                        Log.w("AuthScreen", "Activity introuvable, utilisation du launcher avec Context")
                    }
                    googleSignInLauncher.launch(signInIntent)
                } catch (e: ActivityNotFoundException) {
                    Log.e("AuthScreen", "Activity pour Google Sign-In introuvable", e)
                    Toast.makeText(context, "Impossible de lancer Google Sign-In", Toast.LENGTH_SHORT).show()
                } catch (t: Throwable) {
                    Log.e("AuthScreen", "Erreur au lancement de Google Sign-In", t)
                    Toast.makeText(context, "Erreur inattendue", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Se connecter avec Google")
            }

        }
    }
}

@Composable
fun SignUpScreen(
    authenticationViewModel: AuthenticationViewModel = hiltViewModel(),
    onSignUpSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var pseudo by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(padding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Inscription")

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = pseudo,
                onValueChange = { pseudo = it },
                label = { Text("Pseudo") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Mot de passe") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Button(onClick = {
                authenticationViewModel.signUpWithEmail(email, password, pseudo) { success, error ->
                    if (success) {
                        errorMessage = ""
                        onSignUpSuccess()
                    } else {
                        errorMessage = error ?: "Une erreur est survenue lors de l'inscription."
                    }
                }
            }) {
                Text("S'inscrire")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onBack) {
                Text("Retour")
            }
        }
    }
}

@Composable
fun HomeScreen(
    authenticationViewModel: AuthenticationViewModel = hiltViewModel(),
    onLogout: () -> Unit
) {
    val user by authenticationViewModel.user.collectAsState()
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(padding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Accueil", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = user?.email ?: "Utilisateur inconnu")
            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = onLogout) {
                Text("Se déconnecter")
            }
        }
    }
}

@Composable
fun AuthenticationNavHost(authenticationViewModel: AuthenticationViewModel = hiltViewModel()) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainAuthenticationScreen(
                authenticationViewModel = authenticationViewModel,
                onSignUpSelected = { navController.navigate("signup") },
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }
        composable("signup") {
            SignUpScreen(
                authenticationViewModel = authenticationViewModel,
                onSignUpSuccess = {
                    navController.navigate("home") {
                        popUpTo("signup") { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable("home") {
            HomeScreen(authenticationViewModel = authenticationViewModel, onLogout = {
                authenticationViewModel.logout()
                navController.navigate("main") {
                    popUpTo("home") { inclusive = true }
                }
            })
        }
    }
}
