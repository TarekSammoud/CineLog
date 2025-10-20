package fr.eseo.ld.ts.cinelog.ui

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.eseo.ld.ts.cinelog.repositories.ImdbRepository
import fr.eseo.ld.ts.cinelog.repositories.YoutubeRepository
import fr.eseo.ld.ts.cinelog.ui.navigation.CineLogScreens
import fr.eseo.ld.ts.cinelog.ui.screens.SummaryScreen
import fr.eseo.ld.ts.cinelog.ui.screens.StaticMovieDetailScreen
import fr.eseo.ld.ts.cinelog.viewmodel.ImdbViewModel
import fr.eseo.ld.ts.cinelog.network.ImdbApiServiceImpl
import fr.eseo.ld.ts.cinelog.network.YoutubeApi
import fr.eseo.ld.ts.cinelog.R
import fr.eseo.ld.ts.cinelog.network.OmdbApiService
import fr.eseo.ld.ts.cinelog.network.OmdbApiServiceImpl


@Composable
fun CineLogUi() {
    val application = LocalContext.current.applicationContext as Application
    val navController = rememberNavController()
    // --- Create repositories and view model ---
    val imdbRepository = remember {
        ImdbRepository(
            imdbApi = ImdbApiServiceImpl.imdbApi,
            omdbApi = OmdbApiServiceImpl.omdbApi
        )
    }

    val youtubeRepository = remember {
        YoutubeRepository(
            youtubeApi = YoutubeApi.api
        )
    }

    val viewModel: ImdbViewModel = hiltViewModel()


    // --- Navigation graph ---
    NavHost(
        navController = navController,
        startDestination = CineLogScreens.SUMMARY_SCREEN.name
    ) {
        // SUMMARY SCREEN
        composable(CineLogScreens.SUMMARY_SCREEN.name) {
            SummaryScreen(
                viewModel = viewModel,
                navController = navController
            )
        }

        // DETAILS SCREEN
        composable(
            route = CineLogScreens.DETAILS_SCREEN.name + "/{movieId}",
            arguments = listOf(
                navArgument("movieId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId")

            if (movieId != null) {
                // Load specific movie info
                LaunchedEffect(movieId) {
                    viewModel.fetchOmdbMovie(movieId,application.getString(R.string.omdb_api_key))
                }

                StaticMovieDetailScreen(
                    viewModel = viewModel,
                    navController = navController,
                    movieId = movieId
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Movie not found.")
                }
            }
        }
    }
}
