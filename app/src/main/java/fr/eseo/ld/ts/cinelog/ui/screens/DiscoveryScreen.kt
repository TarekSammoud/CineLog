package fr.eseo.ld.ts.cinelog.ui.screens


import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
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
import fr.eseo.ld.ts.cinelog.ui.viewmodels.ReviewViewModel
import fr.eseo.ld.ts.cinelog.viewmodel.ImdbViewModel
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.filled.ArrowBack
import kotlin.math.abs

@Composable
fun DiscoveryScreen(
    navController: NavController,
    imdbViewModel: ImdbViewModel = hiltViewModel(),
    reviewViewModel: ReviewViewModel = hiltViewModel()
) {
    // --------------------------------------------------------------
    // State
    // --------------------------------------------------------------
    val movies by imdbViewModel.discoveryMovies.collectAsState()
    val isLoading by imdbViewModel.isLoading.collectAsState()
    var currentIndex by remember { mutableStateOf(0) }
    var showReviewDialog by remember { mutableStateOf(false) }
    val currentMovie = movies.getOrNull(currentIndex)

    // --------------------------------------------------------------
    // Load movies
    // --------------------------------------------------------------
    LaunchedEffect(Unit) { imdbViewModel.loadDiscoveryMovies() }

    LaunchedEffect(movies.size - currentIndex) {
        if (movies.size - currentIndex < 5) {
            imdbViewModel.loadDiscoveryMovies((movies.size / 20) + 1)
        }
    }

    // --------------------------------------------------------------
    // UI
    // --------------------------------------------------------------
    Surface(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize()) {
            // ------------------- Loading / Empty -------------------
            when {
                isLoading && movies.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                currentMovie == null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No more movies!")
                    }
                }
                else -> {
                    // ------------------- Stack of cards -------------------
                    Box(Modifier.fillMaxSize()) {
                        // Next card (scaled & dimmed)
                        if (currentIndex + 1 < movies.size) {
                            MovieSwipeCard(
                                movie = movies[currentIndex + 1],
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .scale(0.9f)
                                    .alpha(0.7f)
                            )
                        }

                        // Current swipeable card
                        SwipeableCard(
                            movie = currentMovie,
                            onSwipeLeft = { currentIndex++ },
                            onSwipeRight = { showReviewDialog = true },
                            onDrag = { /* optional visual feedback */ }
                        )

                        // ------------------- Bottom buttons -------------------
                        Row(
                            Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            Button(onClick = { currentIndex++ }) { Text("Skip") }
                            Button(onClick = { showReviewDialog = true }) { Text("Rate") }
                        }

                        // ------------------- Review dialog -------------------
                        if (showReviewDialog) {
                            QuickReviewDialog(
                                movieId = currentMovie.id.toString(),
                                reviewViewModel = reviewViewModel,
                                onDismiss = {
                                    showReviewDialog = false
                                    currentIndex++   // go to next after rating
                                },
                                "https://image.tmdb.org/t/p/w500${currentMovie.poster_path}"
                            )
                        }
                    }
                }
            }

            // ------------------- Top‑left back button -------------------
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun MovieSwipeCard(movie: TmdbMovie, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .size(width = 300.dp, height = 500.dp)
            .clip(RoundedCornerShape(16.dp))
            .shadow(8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w500${movie.poster_path}",
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
            Column(Modifier.padding(16.dp)) {
                Text(movie.title, style = MaterialTheme.typography.titleLarge)
                Text(movie.overview?.take(100) + "...", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun SwipeableCard(
    movie: TmdbMovie,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onDrag: (Float) -> Unit = {}
) {
    var offsetX by remember { mutableStateOf(0f) }
    var rotation by remember { mutableStateOf(0f) }
    val dragState = rememberDraggableState { delta ->
        offsetX += delta
        rotation = (offsetX / 20f).coerceIn(-15f, 15f)   // subtle tilt
        onDrag(offsetX)
    }

    // Auto‑finish when velocity is high
    LaunchedEffect(offsetX) {
        if (kotlin.math.abs(offsetX) > 300f) {
            val direction = if (offsetX > 0) onSwipeRight else onSwipeLeft
            direction()
            offsetX = 0f
            rotation = 0f
        }
    }

    // Reset when movie changes
    LaunchedEffect(movie) {
        offsetX = 0f
        rotation = 0f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        // Velocity‑based decision
                        // (you can also use `velocity` from `detectDragGestures`)
                        if (kotlin.math.abs(offsetX) > 120f) {
                            if (offsetX > 0) onSwipeRight() else onSwipeLeft()
                        }
                        offsetX = 0f
                        rotation = 0f
                    },
                    onDrag = { _, dragAmount ->
                        offsetX += dragAmount.x
                        rotation = (offsetX / 20f).coerceIn(-15f, 15f)
                    }
                )
            }
    ) {
        MovieSwipeCard(
            movie = movie,
            modifier = Modifier
                .align(Alignment.Center)
                .graphicsLayer {
                    translationX = offsetX
                    rotationZ = rotation
                    alpha = 1f - (kotlin.math.abs(offsetX) / 800f).coerceAtMost(1f)
                }
                .draggable(
                    state = dragState,
                    orientation = Orientation.Horizontal,
                    onDragStopped = { velocity ->
                        if (velocity > 800f) onSwipeRight()
                        else if (velocity < -800f) onSwipeLeft()
                        else {
                            // snap back
                            offsetX = 0f
                            rotation = 0f
                        }
                    }
                )
        )
    }
}
@Composable
fun QuickReviewDialog(
    movieId: String,
    reviewViewModel: ReviewViewModel,
    onDismiss: () -> Unit,
    posterPath: String
) {
    var rating by remember { mutableStateOf(0f) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Quick Review") },
        text = {
            Column {
                RatingStar(rating = rating, onRatingChanged = { rating = it })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    placeholder = { Text("Optional comment") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                reviewViewModel.submitReview(movieId, rating.toDouble(), comment,posterPath)
                onDismiss()
            }) { Text("Submit") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}


