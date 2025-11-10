package fr.eseo.ld.ts.cinelog.ui.screens

import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import fr.eseo.ld.ts.cinelog.ui.viewmodels.AuthenticationViewModel
import fr.eseo.ld.ts.cinelog.data.User


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilScreen(
    authenticationViewModel: AuthenticationViewModel = hiltViewModel(),
    navController: NavController
) {
    val firestoreUser by authenticationViewModel.firestoreUser.collectAsState<User?>()
    val firebaseUser by authenticationViewModel.user.collectAsState()

    var nom by rememberSaveable { mutableStateOf(firestoreUser?.nom ?: "") }
    var prenom by rememberSaveable { mutableStateOf(firestoreUser?.prenom ?: "") }
    var email by rememberSaveable { mutableStateOf(firestoreUser?.email ?: "") }
    var pseudo by rememberSaveable { mutableStateOf(firestoreUser?.pseudo ?: "") }

    val profilUser = firestoreUser

    val context = LocalContext.current
    var showSheet by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher pour la galerie
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { updatePhotoUrl(it, authenticationViewModel) }
    }

    // Launcher pour la caméra
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && imageUri != null) {
            updatePhotoUrl(imageUri!!, authenticationViewModel)
        }
    }

    // Crée un Uri pour la caméra
    fun createImageUri(): Uri? {
        val contentResolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 24.dp)
            ) {
                Text(
                    text = "Choisir une photo",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showSheet = false
                            val uri = createImageUri()
                            if (uri != null) {
                                imageUri = uri
                                cameraLauncher.launch(uri)
                            }
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Prendre une photo",
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Prendre une photo", style = MaterialTheme.typography.bodyLarge)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showSheet = false
                            galleryLauncher.launch("image/*")
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Collections,
                        contentDescription = "Choisir dans la galerie",
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Choisir dans la galerie", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )}
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .size(150.dp)
                    .padding(20.dp)
                    .clickable { showSheet = true },
                shape = CircleShape,
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
            ) {
                if (profilUser != null && !profilUser.photoUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = profilUser.photoUrl,
                        contentDescription = "Photo de profil",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Photo de profil par défaut",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    EditableProfilInfoRow(
                        label = "Nom",
                        value = nom,
                        onValueChange = { nom = it },
                        icon = Icons.Default.Face
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                    EditableProfilInfoRow(
                        label = "Prénom",
                        value = prenom,
                        onValueChange = { prenom = it },
                        icon = Icons.Default.Face
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                    EditableProfilInfoRow(
                        label = "Adresse e-mail",
                        value = email,
                        onValueChange = { email = it },
                        icon = Icons.Default.Email
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                    EditableProfilInfoRow(
                        label = "Pseudo",
                        value = pseudo,
                        onValueChange = { pseudo = it },
                        icon = Icons.Default.Person
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    authenticationViewModel.updateUser(
                        nom = nom,
                        prenom = prenom,
                        email = email,
                        pseudo = pseudo
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enregistrer")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    authenticationViewModel.logout()
                    navController.navigate("auth") {
                        popUpTo(0)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Se déconnecter")
            }
        }
    }
}

// Met à jour le champ photoUrl dans Firestore avec l'URI locale
private fun updatePhotoUrl(
    uri: Uri,
    authenticationViewModel: AuthenticationViewModel
) {
    authenticationViewModel.updateUser(
        nom = authenticationViewModel.firestoreUser.value?.nom ?: "",
        prenom = authenticationViewModel.firestoreUser.value?.prenom ?: "",
        email = authenticationViewModel.firestoreUser.value?.email ?: "",
        pseudo = authenticationViewModel.firestoreUser.value?.pseudo ?: "",
        photoUrl = uri.toString()
    )
}

@Composable
private fun EditableProfilInfoRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = label
                )
            }
        )
    }
}
