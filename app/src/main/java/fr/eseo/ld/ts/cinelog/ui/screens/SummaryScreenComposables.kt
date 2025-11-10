package fr.eseo.ld.ts.cinelog.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import fr.eseo.ld.ts.cinelog.R
import fr.eseo.ld.ts.cinelog.model.TmdbMovie
import fr.eseo.ld.ts.cinelog.ui.navigation.CineLogScreens
import fr.eseo.ld.ts.cinelog.viewmodel.ImdbViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    viewModel: ImdbViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("Trending") }

    val movieList by viewModel.movieList.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val errorMessage by viewModel.errorMessage.observeAsState()

    val isLoadingMore = isLoading && movieList.isNotEmpty() // true only during pagination

    // Load first page when filter changes
    LaunchedEffect(selectedFilter) {
        viewModel.loadFirstPage(selectedFilter)
    }


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
                        listOf("Trending", "Popular").forEach { filter ->
                            Button(
                                onClick = { selectedFilter = filter },
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
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    errorMessage != null -> {
                        Text(
                            text = "Error: $errorMessage",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    else -> {
                        SummaryScreenMediaList(
                            movieList = movieList,
                            isLoadingMore = isLoadingMore,
                            navController = navController,
                            onLoadMore = { viewModel.loadNextPage() },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        )
    }
}

// ── Card (TMDB version) ─────────────────────────────────────
@Composable
private fun SummaryScreenMediaCard(
    movie: TmdbMovie,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(2.dp)
    ) {
        AsyncImage(
            model = movie.poster_path?.let { "https://image.tmdb.org/t/p/w342$it" }
                ?: R.drawable.ic_launcher_foreground,
            contentDescription = movie.title,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.ic_launcher_foreground),
            error = painterResource(R.drawable.ic_launcher_foreground),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(MaterialTheme.shapes.medium)
        )
    }
}

// ── Grid ─────────────────────────────────────────────────────
@Composable
private fun SummaryScreenMediaList(
    movieList: List<TmdbMovie>,
    isLoadingMore: Boolean,
    navController: NavController,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyGridState()

    // Trigger load more when near bottom
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo }
            .collect { layoutInfo ->
                val totalItems = layoutInfo.totalItemsCount
                val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                if (lastVisible >= totalItems - 3 && !isLoadingMore) {
                    onLoadMore()
                }
            }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(4.dp)
    ) {
        items(movieList.size,
            key = { index -> movieList[index].id }) { index ->
            val movie = movieList[index]
            SummaryScreenMediaCard(
                movie = movie,
                onClick = {
                    navController.navigate("${CineLogScreens.DETAILS_SCREEN.name}/tmdb/${movie.id}")
                }
            )
        }

        if (isLoadingMore) {
            item(span = { GridItemSpan(3) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun CineLogWithBottomBar(
    navController: NavController,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        bottomBar = {
            BottomAppBar {
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = {
                    navController.navigate("PROFIL_SCREEN")
                }) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profil"
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            content(padding)
        }
    }
}