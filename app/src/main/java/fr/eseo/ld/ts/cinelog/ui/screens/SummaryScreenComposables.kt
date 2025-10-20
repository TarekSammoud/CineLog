package fr.eseo.ld.ts.cinelog.ui.screens

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import fr.eseo.ld.ts.cinelog.R
import fr.eseo.ld.ts.cinelog.model.Media
import fr.eseo.ld.ts.cinelog.network.ImdbApiServiceImpl
import fr.eseo.ld.ts.cinelog.network.YoutubeApi
import fr.eseo.ld.ts.cinelog.repositories.ImdbRepository
import fr.eseo.ld.ts.cinelog.repositories.YoutubeRepository
import fr.eseo.ld.ts.cinelog.viewmodel.ImdbViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    viewModel: ImdbViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("Trending") }

    val mediaList by viewModel.mediaList.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val errorMessage by viewModel.errorMessage.observeAsState()

    LaunchedEffect(Unit) { viewModel.fetchAllMedia() }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            Text(
                                text = stringResource(R.string.app_name),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    shadow = Shadow(
                                        color = Color.Gray.copy(alpha = 0.5f),
                                        offset = Offset(2f, 2f),
                                        blurRadius = 2f
                                    )
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("Trending", "Movies", "TV Shows").forEach { filter ->
                            Button(
                                onClick = {
                                    selectedFilter = filter
                                    when (filter) {
                                        "Movies" -> viewModel.fetchFilteredMedia("MOVIE")
                                        "TV Shows" -> viewModel.fetchFilteredMedia("TV_SERIES")
                                        "Trending" -> viewModel.fetchAllMedia()
                                    }
                                },
                                colors = if (selectedFilter == filter)
                                    ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
                                else
                                    ButtonDefaults.buttonColors(MaterialTheme.colorScheme.surface)
                            ) {
                                Text(filter)
                            }
                        }
                    }
                }
            },
            content = { innerPadding ->
                if (isLoading) {
                    // Show a loading indicator at the center
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.CircularProgressIndicator()
                    }
                } else if (errorMessage != null) {
                    Text(
                        text = "Error: $errorMessage",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    SummaryScreenMediaList(
                        mediaList = mediaList,
                        navController = navController,
                        onClick = { /* navigate to details */ },
                        onLongClick = { /* optional */ },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        )
    }
}

@Composable
private fun SummaryScreenMediaCard(
    media: Media,
    onClick: (String) -> Unit,
    onLongClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .combinedClickable(
                onClick = { onClick(media.id) },
                onLongClick = { onLongClick(media.id) }
            )
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
            placeholder = painterResource(R.drawable.ic_launcher_foreground), // your local drawable
            error = painterResource(R.drawable.ic_launcher_foreground)        // fallback if loading fails
        )
    }
}

@Composable
private fun SummaryScreenMediaList(
    mediaList: List<Media>,
    navController: NavController,
    onClick: (String) -> Unit,
    onLongClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(4.dp)
    ) {
        items(mediaList.size) { index ->
            val media = mediaList[index]
            SummaryScreenMediaCard(
                media = media,
                onClick = {
                    // Navigate to the details screen with the movieId
                    navController.navigate("DETAILS_SCREEN/${media.id}")
                },
                onLongClick = { /* optional */ }
            )
        }
    }
}
/*
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SummaryScreenPreview() {
    val navController = androidx.navigation.compose.rememberNavController()

    val repository = ImdbRepository(
        imdbApi = ImdbApiServiceImpl.imdbApi,
        omdbApi = ImdbApiServiceImpl.omdbApi
    )

    val youtubeRepository= YoutubeRepository(
        youtubeApi = YoutubeApi.api
    )

    // ViewModel
    val viewModel = remember { ImdbViewModel(repository,youtubeRepository) }



    fr.eseo.ld.ts.cinelog.ui.theme.AppTheme {
        SummaryScreen(
            viewModel = viewModel,
            navController = navController
        )
    }
}
*/