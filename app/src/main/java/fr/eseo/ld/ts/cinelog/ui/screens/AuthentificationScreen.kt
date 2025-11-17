package fr.eseo.ld.ts.cinelog.ui.screens

import android.app.Activity
import android.content.ActivityNotFoundException
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import fr.eseo.ld.ts.cinelog.ui.viewmodels.AuthenticationViewModel
import  fr.eseo.ld.ts.cinelog.R
import kotlin.compareTo
import kotlin.text.matches


@Composable
fun MainAuthenticationScreen(
    onSignUpSelected: () -> Unit,
    authenticationViewModel: AuthenticationViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

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
    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.login_background), // your drawable
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize() // fill whole screen,

        )

        // Foreground: Scaffold content
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent, // important: make scaffold background transparent
            contentColor = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxSize()
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Bienvenue",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

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
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(
                        onClick = {
                            authenticationViewModel.loginWithEmail(email, password) { success, error ->
                                if (success) {
                                    errorMessage = ""
                                    onLoginSuccess()
                                } else {
                                    errorMessage = error ?: "Email ou mot de passe incorrect."
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors =  ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)

                    ) {
                        Text("Se connecter")
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onSignUpSelected,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("S'inscrire")
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val googleSignInClient = authenticationViewModel.getGoogleSignInClient(context)
                            val signInIntent = googleSignInClient.getSignInIntent()
                            googleSignInLauncher.launch(signInIntent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Se connecter avec Google")
                    }
                }
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
    var nom by remember { mutableStateOf("") }
    var prenom by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.login_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Scaffold(
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxSize()
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Inscription", color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = pseudo,
                    onValueChange = { pseudo = it },
                    label = { Text("User Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = nom,
                    onValueChange = { nom = it },
                    label = { Text("Last Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = prenom,
                    onValueChange = { prenom = it },
                    label = { Text("First Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
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
                    label = { Text("Password") },
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
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val emailTrim = email.trim()
                        val pseudoTrim = pseudo.trim()
                        val nomTrim = nom.trim()
                        val prenomTrim = prenom.trim()
                        val passwordTrim = password
                        if (emailTrim.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(emailTrim).matches()) {
                            errorMessage = "Adresse e‑mail invalide"
                            return@Button
                        }
                        if (passwordTrim.length < 6) {
                            errorMessage = "Le mot de passe doit contenir au moins 6 caractères."
                            return@Button
                        }
                        if (nomTrim.isEmpty()) {
                            errorMessage = "Le nom est obligatoire."
                            return@Button
                        }
                        if (prenomTrim.isEmpty()) {
                            errorMessage = "Le prénom est obligatoire."
                            return@Button
                        }
                        authenticationViewModel.signUpWithEmail(
                            nomTrim,
                            prenomTrim,
                            emailTrim,
                            pseudoTrim,
                            passwordTrim
                        ) { success, error ->
                            if (success) {
                                errorMessage = ""
                                onSignUpSuccess()
                            } else {
                                errorMessage = error ?: "Une erreur est survenue lors de l'inscription."
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("S'inscrire")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Retour")
                }
            }
        }
    }
}

