package fr.eseo.ld.ts.cinelog.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import fr.eseo.ld.ts.cinelog.viewmodel.ImdbViewModel
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import fr.eseo.ld.ts.cinelog.network.ImdbApiServiceImpl
import fr.eseo.ld.ts.cinelog.repositories.ImdbRepository
import fr.eseo.ld.ts.cinelog.R
import fr.eseo.ld.ts.cinelog.network.YoutubeApi
import fr.eseo.ld.ts.cinelog.repositories.YoutubeRepository
import fr.eseo.ld.ts.cinelog.ui.viewmodels.ReviewViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow

import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.runtime.produceState
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import fr.eseo.ld.ts.cinelog.model.TmdbMovie
import fr.eseo.ld.ts.cinelog.repositories.TmdbRepository


// --- YouTube Trailer Box ---
@Composable
fun YoutubeTrailerBox(trailerId: String?) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = trailerId != null) {
                trailerId?.let {
                    val intent =
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$it"))
                    context.startActivity(intent)
                }
            }
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (trailerId != null) {
            AsyncImage(
                model = "https://img.youtube.com/vi/$trailerId/hqdefault.jpg",
                contentDescription = "Trailer Thumbnail",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Bottom gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.3f)),
                            startY = 100f
                        )
                    )
            )
            // Play button overlay
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play Trailer",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .shadow(4.dp)
                    .background(Color.Black.copy(alpha = 0.4f), shape = RoundedCornerShape(50))
            )
        } else {
            Text("Trailer not available", color = Color.Gray)
        }
    }
}

// --- Movie Detail Screen ---
@Composable
fun StaticMovieDetailScreen(
    viewModel: ImdbViewModel,
    navController: NavController,
    movieId: String,
    modifier: Modifier = Modifier
) {

// In fetchTmdbMovieByImdbId():
    val scrollState = rememberScrollState()
    val movie by viewModel.omdbMovie.observeAsState(null)
    val isLoading by viewModel.isLoading.observeAsState(false)
    val errorMessage by viewModel.errorMessage.observeAsState()
    val trailerId by viewModel.youtubeTrailerId.observeAsState(null)
    val context = LocalContext.current
    val actorImages by viewModel.actorImages.observeAsState(emptyMap())

    val tmdbMovie by viewModel.tmdbMovie.observeAsState(null) // Add this LiveData

    // Fetch movie
    LaunchedEffect(Unit) {
        viewModel.fetchTmdbMovieByImdbId(movieId)
    }

    // Fetch trailer once movie is loaded
    LaunchedEffect(movie) {
        movie?.let {
            viewModel.fetchYoutubeTrailer(it.title, it.year, context.getString(R.string.youtube_api_key))
            // Add this:
            val actors = it.actors.split(",").map { actor -> actor.trim() }
            viewModel.fetchActorImages(actors)
        }
    }

    LaunchedEffect(tmdbMovie) {
        tmdbMovie?.let { tmdb ->
            viewModel.fetchSimilarMovies(tmdb.id.toString())
        }
    }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column {
            // --- Top Bar with Back Button ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft, // make sure you have a back arrow drawable
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = movie?.title ?: "Movie Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            when {
                isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
                errorMessage != null -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Error: $errorMessage", color = MaterialTheme.colorScheme.error)
                }
                movie != null -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp)
                ) {
                    YoutubeTrailerBox(trailerId = trailerId)
                    Spacer(modifier = Modifier.height(16.dp))
                    // Poster & details
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                        AsyncImage(
                            model = movie!!.poster,
                            contentDescription = movie!!.title,
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(R.drawable.ic_launcher_foreground),
                            error = painterResource(R.drawable.ic_launcher_foreground),
                            modifier = Modifier
                                .width(150.dp)
                                .height(225.dp)
                                .clip(MaterialTheme.shapes.medium)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = movie!!.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = movie!!.plot,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 6,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Release Date: ${movie!!.released}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Column {
                        Text("Genre: ${movie!!.genre}")
                        Text("Director: ${movie!!.director}")
                        ActorsGrid(
                            actors = movie!!.actors,
                            actorImages = actorImages
                        )
                        Text("IMDB Rating: ${movie!!.imdbRating}")
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    MovieReviewSection(movieId = movieId)

                    Spacer(modifier = Modifier.height(24.dp))
                    SimilarMoviesSection(
                        similarMovies = viewModel.similarMovies.observeAsState(emptyList()).value,
                        navController = navController
                    )
                }
            }
        }
    }
}




@Composable
fun ActorsGrid(
    actors: String,
    actorImages: Map<String, String?>,
    modifier: Modifier = Modifier
) {
    val actorList = actors.split(",").map { it.trim() }

    LazyRow (
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp), // Increased slightly to accommodate content
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp) // Optional: padding at start/end
    ) {
        items(actorList) { actor ->
            val imageUrl = actorImages[actor]

            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .width(120.dp) // Fixed width for each card
                    .padding(vertical = 4.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(8.dp)
                ) {
                    AsyncImage(
                        model = imageUrl ?: R.drawable.ic_launcher_foreground,
                        contentDescription = actor,
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.ic_launcher_foreground),
                        error = painterResource(R.drawable.ic_launcher_foreground),
                        modifier = Modifier
                            .size(width = 104.dp, height = 156.dp) // 2:3 aspect ratio (104:156)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = actor,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(104.dp)
                    )
                }
            }
        }
    }
}



// --- Reviews Section ---
@Composable
fun MovieReviewSection(
    movieId: String,
    viewModel: ReviewViewModel = hiltViewModel()
) {
    val reviews by viewModel.reviews.collectAsState()
    val averageRating by viewModel.averageRating.collectAsState()
    var newComment by remember { mutableStateOf("") }
    var newRating by remember { mutableStateOf(0f) }

    LaunchedEffect(movieId) {
        viewModel.loadReviews(movieId)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Average Rating: ${"%.1f".format(averageRating)} / 5", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        reviews.forEach { review ->
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Text("${review.username} (${review.rating}/5)", fontWeight = FontWeight.Bold)
                Text(review.comment)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Add a Review", style = MaterialTheme.typography.titleMedium)
        Text("Your Rating:", style = MaterialTheme.typography.bodyMedium)
        RatingStar(
            rating = newRating,
            onRatingChanged = { newRating = it }
        )
        TextField(
            value = newComment,
            onValueChange = { newComment = it },
            placeholder = { Text("Write your comment") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                viewModel.submitReview(movieId, newRating.toDouble(), newComment)
                newComment = ""
                newRating = 0f
            },
            modifier = Modifier.padding(top = 8.dp)
        ) { Text("Submit Review") }
    }
}

// --- Rating Star Composable (full + half stars) ---
@Composable
fun RatingStar(
    rating: Float = 5f,
    maxRating: Int = 5,
    onRatingChanged: (Float) -> Unit,
    isIndicator: Boolean = false
) {
    Row {
        for (i in 1..maxRating) {
            val starValue = i.toFloat()
            val iconTint = when {
                rating >= starValue -> MaterialTheme.colorScheme.secondary // full star
                rating >= starValue - 0.5f -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f) // half star
                else -> Color.Gray
            }
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier
                    .size(24.dp)
                    .clickable(!isIndicator) {
                        val newRating = if (rating >= starValue - 0.5f) starValue - 0.5f else starValue
                        onRatingChanged(newRating)
                    }
            )
        }
    }
}

@Composable
fun SimilarMoviesSection(
    similarMovies: List<TmdbMovie>,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    if (similarMovies.isEmpty()) return

    Column(modifier = modifier) {
        Text(
            text = "Similar Movies",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(similarMovies) { movie ->
                SimilarMovieCard(movie = movie, navController = navController)
            }
        }
    }
}
@Composable
fun SimilarMovieCard(
    movie: TmdbMovie,
    navController: NavController
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable(
                onClick = {
                    Log.d("TMDB","${movie.imdb_id}")
                    Log.d("TMDB ID","${movie.id}")

                    navController.navigate("DETAILS_SCREEN/${movie.imdb_id ?: movie.id}")
                }
            ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            AsyncImage(
                model = movie.poster_path?.let { "https://image.tmdb.org/t/p/w342$it" }
                    ?: R.drawable.ic_launcher_foreground,
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.ic_launcher_foreground),
                error = painterResource(R.drawable.ic_launcher_foreground),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = movie.title,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .padding(bottom = 8.dp)
            )
        }
    }
}