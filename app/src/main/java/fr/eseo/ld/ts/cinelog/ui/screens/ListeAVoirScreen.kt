package fr.eseo.ld.ts.cinelog.ui.screens

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import fr.eseo.ld.ts.cinelog.R
import fr.eseo.ld.ts.cinelog.model.Media
import fr.eseo.ld.ts.cinelog.ui.navigation.CineLogScreens
import fr.eseo.ld.ts.cinelog.ui.viewmodels.AuthenticationViewModel
import fr.eseo.ld.ts.cinelog.ui.viewmodels.ListeAVoirViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListeAVoirScreen(
    authenticationViewModel: AuthenticationViewModel = hiltViewModel(),
    watchlistViewModel: ListeAVoirViewModel = hiltViewModel(),
    navController: NavController
) {
    val user by authenticationViewModel.user.collectAsState()
    val userId = user?.uid ?: ""
    val mediaList by watchlistViewModel.mediaList.collectAsState()
    val isLoading by watchlistViewModel.isLoading.collectAsState()

    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            watchlistViewModel.loadWatchlist(userId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Ma liste de films à voir",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                ) }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (mediaList.isEmpty()) {
                Text(
                    text = "Votre liste de films à voir est vide",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                WatchlistMediaGrid(
                    mediaList = mediaList,
                    navController = navController,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun WatchlistMediaGrid(
    mediaList: List<Media>,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.padding(4.dp)
    ) {
        items(mediaList.size) { index ->
            val media = mediaList[index]
            WatchlistMediaCard(
                media = media,
                onClick = {
                    navController.navigate("${CineLogScreens.DETAILS_SCREEN.name}/tmdb/${media.id}")
                }
            )
        }
    }
}

@Composable
private fun WatchlistMediaCard(
    media: Media,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .combinedClickable(onClick = onClick)
            .padding(2.dp)
    ) {
        AsyncImage(
            model = media.primaryImage?.url,
            contentDescription = media.primaryTitle,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(MaterialTheme.shapes.medium),
            placeholder = painterResource(R.drawable.ic_launcher_foreground),
            error = painterResource(R.drawable.ic_launcher_foreground)
        )
    }
}
