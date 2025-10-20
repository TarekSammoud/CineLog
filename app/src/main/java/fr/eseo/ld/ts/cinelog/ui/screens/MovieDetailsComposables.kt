package fr.eseo.ld.ts.cinelog.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import fr.eseo.ld.ts.cinelog.viewmodel.ImdbViewModel
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import fr.eseo.ld.ts.cinelog.network.ImdbApiServiceImpl
import fr.eseo.ld.ts.cinelog.repositories.ImdbRepository
import fr.eseo.ld.ts.cinelog.R
import fr.eseo.ld.ts.cinelog.network.YoutubeApi
import fr.eseo.ld.ts.cinelog.repositories.YoutubeRepository


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

            // Bottom gradient shadow/blur
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

@Composable
fun StaticMovieDetailScreen(
    viewModel: ImdbViewModel,
    navController: NavController,
    movieId: String,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val movie by viewModel.omdbMovie.observeAsState(null)
    val isLoading by viewModel.isLoading.observeAsState(false)
    val errorMessage by viewModel.errorMessage.observeAsState()
    val trailerId by viewModel.youtubeTrailerId.observeAsState(null)
    val context = LocalContext.current

    // Fetch movie and trailer
    LaunchedEffect(Unit) {
        viewModel.fetchOmdbMovie(movieId,context.getString(R.string.omdb_api_key))
    }

    // Fetch trailer once movie is loaded
    LaunchedEffect(movie) {
        movie?.let {
            viewModel.fetchYoutubeTrailer(it.title, it.year,context.getString(R.string.youtube_api_key)
            )
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: $errorMessage", color = MaterialTheme.colorScheme.error)
                }
            }
            movie != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp)
                ) {
                    // --- Trailer thumbnail ---
                   /* Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(MaterialTheme.shapes.medium),
                        contentAlignment = Alignment.Center
                    ) {
                        if (trailerId != null) {
                            AsyncImage(
                                model = "https://img.youtube.com/vi/$trailerId/hqdefault.jpg",
                                contentDescription = "Trailer thumbnail",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text("Trailer not available", color = Color.Gray)
                        }
                    }*/
                    YoutubeTrailerBox(trailerId = trailerId)


                    Spacer(modifier = Modifier.height(16.dp))

                    // --- Poster and title/description row ---
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

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                        Text("Release Date: ${movie!!.released}", style = MaterialTheme.typography.bodyMedium)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column {
                        Text(text = "Genre: ${movie!!.genre}")
                        Text(text = "Director: ${movie!!.director}")
                        Text(text = "Actors: ${movie!!.actors}")
                        Text(text = "IMDB Rating: ${movie!!.imdbRating}")
                    }
                }
            }
        }
    }
}

/*

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MovieDetailsPreview() {
    val navController = androidx.navigation.compose.rememberNavController()

    // Repository using correct APIs
    val repository = ImdbRepository(
        imdbApi = ImdbApiServiceImpl.imdbApi,
        omdbApi = ImdbApiServiceImpl.omdbApi
    )
    val youtubeRepository= YoutubeRepository(
        youtubeApi = YoutubeApi.api
    )

    val context = LocalContext.current

    // ViewModel
    val viewModel = remember { ImdbViewModel(repository,youtubeRepository) }

    // Trigger static fetch
    LaunchedEffect(Unit) {
        viewModel.fetchOmdbMovie(movieId,context.getString(R.string.omdb_api_key))
    }

    fr.eseo.ld.ts.cinelog.ui.theme.AppTheme {
        StaticMovieDetailScreen(
            viewModel = viewModel,
            navController = navController
        )
    }
}*/
