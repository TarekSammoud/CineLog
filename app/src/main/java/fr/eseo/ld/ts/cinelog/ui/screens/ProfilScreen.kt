package fr.eseo.ld.ts.cinelog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import fr.eseo.ld.ts.cinelog.ui.viewmodels.AuthenticationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilScreen(
    authenticationViewModel: AuthenticationViewModel = hiltViewModel(),
    navController: NavController
) {
    val user by authenticationViewModel.user.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Retour"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .size(150.dp)
                    .padding(16.dp),
                shape = CircleShape,
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                if (user?.photoUrl != null) {
                    AsyncImage(
                        model = user?.photoUrl.toString(),
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

            Spacer(modifier = Modifier.height(24.dp))

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
                    ProfilInfoRow(
                        label = "Nom complet",
                        value = user?.displayName ?: "Non renseigné"
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    ProfilInfoRow(
                        label = "Pseudo",
                        value = user?.displayName ?: "Non renseigné"
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    ProfilInfoRow(
                        label = "Adresse e-mail",
                        value = user?.email ?: "Non renseigné"
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

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

@Composable
private fun ProfilInfoRow(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp
        )
    }
}
