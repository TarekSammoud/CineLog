package fr.eseo.ld.ts.cinelog.ui.screens

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import fr.eseo.ld.ts.cinelog.R
import fr.eseo.ld.ts.cinelog.network.FreeImageHostUploader
import fr.eseo.ld.ts.cinelog.ui.viewmodels.AuthenticationViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    authenticationViewModel: AuthenticationViewModel = hiltViewModel(),
    onSignUpSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var pseudo by remember { mutableStateOf("") }
    var nom by remember { mutableStateOf("") }
    var prenom by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }
    var showPhotoPicker by remember { mutableStateOf(false) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedPhotoUrl by remember { mutableStateOf<String?>(null) }

    // Photo upload helper
    suspend fun uploadPhotoAndGetUrl(uri: Uri, onUrlReady: (String) -> Unit) {
        isUploading = true
        val result = FreeImageHostUploader.uploadImage(context, uri)
        isUploading = false
        result.onSuccess { url ->
            selectedPhotoUrl = url
            onUrlReady(url)
            Toast.makeText(context, "Photo uploaded!", Toast.LENGTH_SHORT).show()
        }.onFailure {
            Toast.makeText(context, "Upload failed", Toast.LENGTH_LONG).show()
        }
    }

    // Launchers
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) { /* ignored */ }
            scope.launch { uploadPhotoAndGetUrl(it) { } }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempImageUri != null) {
            scope.launch { uploadPhotoAndGetUrl(tempImageUri!!) { } }
        }
    }

    fun createCameraUri(): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "profile_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.login_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Scaffold(containerColor = Color.Transparent) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Inscription",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Profile picture
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .clickable { showPhotoPicker = true }
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedPhotoUrl != null) {
                        AsyncImage(
                            model = selectedPhotoUrl,
                            contentDescription = "Profile picture",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(120.dp)
                        )
                    }
                    if (isUploading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Change photo",
                            tint = Color.White,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Tap to add a profile picture", color = Color.White.copy(alpha = 0.8f))

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(value = pseudo, onValueChange = { pseudo = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = nom, onValueChange = { nom = it }, label = { Text("Last Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = prenom, onValueChange = { prenom = it }, label = { Text("First Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage, color = MaterialTheme.colorScheme.error)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val e = email.trim()
                        val p = pseudo.trim()
                        val n = nom.trim()
                        val pr = prenom.trim()

                        when {
                            e.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(e).matches() -> errorMessage = "Invalid email address"
                            password.length < 6 -> errorMessage = "Password must be at least 6 characters"
                            n.isEmpty() -> errorMessage = "Last name is required"
                            pr.isEmpty() -> errorMessage = "First name is required"
                            else -> {
                                authenticationViewModel.signUpWithEmail(
                                    nom = n,
                                    prenom = pr,
                                    email = e,
                                    pseudo = p,
                                    password = password,
                                    photoUrl = selectedPhotoUrl
                                ) { success, err ->
                                    if (success) {
                                        errorMessage = ""
                                        onSignUpSuccess()
                                    } else {
                                        errorMessage = err ?: "Sign-up failed"
                                    }
                                }
                            }
                        }
                    },
                    enabled = !isUploading,
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Creating account...")
                    } else {
                        Text("Sign Up")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onBack, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                    Text("Back")
                }
            }

            // Photo picker bottom sheet
            if (showPhotoPicker) {
                ModalBottomSheet(onDismissRequest = { showPhotoPicker = false }) {
                    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                        Text("Choose a profile picture", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                showPhotoPicker = false
                                val uri = createCameraUri()
                                if (uri != null) {
                                    tempImageUri = uri
                                    cameraLauncher.launch(uri)
                                }
                            }.padding(vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(28.dp))
                            Spacer(Modifier.width(16.dp))
                            Text("Take a photo", style = MaterialTheme.typography.bodyLarge)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                showPhotoPicker = false
                                galleryLauncher.launch("image/*")
                            }.padding(vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Collections, contentDescription = null, modifier = Modifier.size(28.dp))
                            Spacer(Modifier.width(16.dp))
                            Text("Choose from gallery", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}

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
                    Toast.makeText(context, "Connexion Successful", Toast.LENGTH_SHORT).show()
                    onLoginSuccess()
                } else {
                    Log.e("AuthScreen", "signInWithGoogle failed: $error")
                    Toast.makeText(context, error ?: "Connexion error", Toast.LENGTH_SHORT).show()
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
                    text = "Welcome",
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
                    label = { Text("Password") },
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
                                    errorMessage = error ?: "Email or password incorrect."
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors =  ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)

                    ) {
                        Text("Login")
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onSignUpSelected,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Sign up")
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
                        Text("Login with Google")
                    }
                }
            }
        }
    }
}