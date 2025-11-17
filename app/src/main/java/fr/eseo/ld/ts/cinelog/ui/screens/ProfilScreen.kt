package fr.eseo.ld.ts.cinelog.ui.screens

import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import fr.eseo.ld.ts.cinelog.ui.viewmodels.AuthenticationViewModel
import fr.eseo.ld.ts.cinelog.ui.viewmodels.ReviewViewModel
import fr.eseo.ld.ts.cinelog.data.User
import fr.eseo.ld.ts.cinelog.model.Review
import fr.eseo.ld.ts.cinelog.ui.navigation.CineLogScreens
import java.text.SimpleDateFormat
import java.util.*

// ---------------------------------------------------------------------
//  ProfilScreen – main entry point
// ---------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilScreen(
    authenticationViewModel: AuthenticationViewModel = hiltViewModel(),
    reviewViewModel: ReviewViewModel = hiltViewModel(),
    navController: NavController
) {
    // ----- Firestore & Firebase user -----
    val firestoreUser by authenticationViewModel.firestoreUser.collectAsState<User?>()
    val firebaseUser by authenticationViewModel.user.collectAsState()

    // ----- Edit form state (hidden until gear tapped) -----
    var editMode by rememberSaveable { mutableStateOf(false) }
    var nom by rememberSaveable { mutableStateOf(firestoreUser?.nom ?: "") }
    var prenom by rememberSaveable { mutableStateOf(firestoreUser?.prenom ?: "") }
    var email by rememberSaveable { mutableStateOf(firestoreUser?.email ?: "") }
    var pseudo by rememberSaveable { mutableStateOf(firestoreUser?.pseudo ?: "") }

    // ----- Photo picker -----
    val context = LocalContext.current
    var showSheet by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { updatePhotoUrl(it, authenticationViewModel) } }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && imageUri != null) updatePhotoUrl(imageUri!!, authenticationViewModel)
    }

    fun createImageUri(): Uri? {
        val contentResolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    }

    // ----- Load my reviews (by current UID) -----
    val currentUid = firebaseUser?.uid.orEmpty()
    LaunchedEffect(currentUid) {
        if (currentUid.isNotBlank()) reviewViewModel.loadMyReviews(currentUid)
    }
    val myReviews by reviewViewModel.myReviews.collectAsState(emptyList())

    // -----------------------------------------------------------------
    //  UI
    // -----------------------------------------------------------------
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Profile",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { editMode = !editMode }) {
                        Icon(
                            imageVector = if (editMode) Icons.Default.Close else Icons.Default.Settings,
                            contentDescription = "Edit profile"
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // -------------------- Avatar --------------------
            item {
                Card(
                    modifier = Modifier
                        .size(150.dp)
                        .padding(top = 24.dp, bottom = 16.dp)
                        .clickable { showSheet = true },
                    shape = CircleShape,
                    elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
                ) {
                    if (firestoreUser?.photoUrl?.isNotEmpty() == true) {
                        AsyncImage(
                            model = firestoreUser?.photoUrl,
                            contentDescription = "Photo de profil",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Default avatar",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            tint = Color.White
                        )
                    }
                }
            }

            // -------------------- Static info (always visible) --------------------
            item {
                ProfileInfoRow(label = "Username", value = firestoreUser?.pseudo ?: "–", icon = Icons.Default.Person)
                ProfileInfoRow(label = "E‑mail", value = firestoreUser?.email ?: "–", icon = Icons.Default.Email)
            }

            // -------------------- Edit form (shown only in editMode) --------------------
            if (editMode) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(Modifier.padding(20.dp)) {
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
                                label = "Adresse e‑mail",
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

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            authenticationViewModel.updateUser(
                                nom = nom,
                                prenom = prenom,
                                email = email,
                                pseudo = pseudo
                            )
                            editMode = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Save") }
                }
            }

            // -------------------- My Reviews --------------------
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "My reviews",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (myReviews.isEmpty()) {
                item {
                    Text(
                        "No reviews yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            } else {
                items(myReviews) { review ->
                    ReviewCard(review = review,navController)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // -------------------- Logout --------------------
            item {
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedButton(
                    onClick = {
                        authenticationViewModel.logout()
                        navController.navigate("auth") { popUpTo(0) }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Logout",color = Color.White) }
            }
        }
    }

    // -------------------- Photo picker bottom sheet --------------------
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
                    "Choose a picture",
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
                    Icon(Icons.Default.CameraAlt, tint = Color.White, contentDescription = null, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(16.dp))
                    Text("Take a picture", style = MaterialTheme.typography.bodyLarge)
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
                    Icon(Icons.Default.Collections,tint = Color.White,  contentDescription = null, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(16.dp))
                    Text("Choose from gallery", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------
//  Helper composables
// ---------------------------------------------------------------------

@Composable
private fun ProfileInfoRow(label: String, value: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.White)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
    }
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
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
        Spacer(Modifier.height(4.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(icon,tint = Color.White, contentDescription = label) }
        )
    }
}

@Composable
private fun ReviewCard(review: Review,navController: NavController) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = {
            navController.navigate("${CineLogScreens.DETAILS_SCREEN.name}/tmdb/${review.movieId}")
        }
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            AsyncImage(
                model = review.posterPath,
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(android.R.drawable.ic_menu_gallery)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                // Rating stars
                Row {
                    repeat(5) { idx ->
                        Icon(
                            imageVector = if (idx < review.rating.toInt()) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(review.comment, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(review.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ---------------------------------------------------------------------
//  Photo URL update (kept unchanged)
// ---------------------------------------------------------------------
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