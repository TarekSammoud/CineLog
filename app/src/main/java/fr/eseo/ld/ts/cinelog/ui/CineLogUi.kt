package fr.eseo.ld.ts.cinelog.ui

import android.app.Application
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
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
import fr.eseo.ld.ts.cinelog.R
import fr.eseo.ld.ts.cinelog.data.AuthState
import fr.eseo.ld.ts.cinelog.ui.navigation.CineLogScreens
import fr.eseo.ld.ts.cinelog.ui.screens.*
import fr.eseo.ld.ts.cinelog.ui.viewmodels.AuthenticationViewModel
import fr.eseo.ld.ts.cinelog.viewmodel.ImdbViewModel

@Composable
fun CineLogUi() {
    val navController = rememberNavController()
    val authViewModel: AuthenticationViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()

    val context = LocalContext.current
    val application = context.applicationContext as Application

    // Reactive navigation based on auth state
    LaunchedEffect(authState) {
        when (authState) {
            AuthState.LOADING -> {
                // stay on a splash/loading screen if needed
            }
            AuthState.LOGGED_IN -> {
                navController.navigate(CineLogScreens.SUMMARY_SCREEN.name) {
                    popUpTo(0) // clear backstack
                }
            }
            AuthState.LOGGED_OUT -> {
                navController.navigate("auth") {
                    popUpTo(0)
                }
            }
        }
    }

    NavHost(navController = navController, startDestination = "auth") {

        // --- AUTH FLOW ---
        composable("auth") {
            MainAuthenticationScreen(
                authenticationViewModel = authViewModel,
                onSignUpSelected = { navController.navigate("signup") },
                onLoginSuccess = {
                    navController.navigate(CineLogScreens.SUMMARY_SCREEN.name) {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }

        composable("signup") {
            SignUpScreen(
                authenticationViewModel = authViewModel,
                onSignUpSuccess = {
                    navController.navigate(CineLogScreens.SUMMARY_SCREEN.name) {
                        popUpTo("signup") { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // --- MAIN APP FLOW ---
        composable(CineLogScreens.SUMMARY_SCREEN.name) {
            val imdbViewModel: ImdbViewModel = hiltViewModel()
            CineLogWithBottomBar(navController = navController) { padding ->
                SummaryScreen(
                    viewModel = imdbViewModel,
                    navController = navController
                )
            }
        }

        composable(
            route = CineLogScreens.DETAILS_SCREEN.name + "/{movieId}",
            arguments = listOf(navArgument("movieId") { type = NavType.StringType })
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId")
            val imdbViewModel: ImdbViewModel = hiltViewModel()
            if (movieId != null) {
                LaunchedEffect(movieId) {
                    imdbViewModel.fetchOmdbMovie(
                        movieId,
                        application.getString(R.string.omdb_api_key)
                    )
                }
                StaticMovieDetailScreen(
                    viewModel = imdbViewModel,
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
        composable(CineLogScreens.PROFIL_SCREEN.name) {
            CineLogWithBottomBar(navController = navController) { padding ->
                ProfilScreen(
                    authenticationViewModel = authViewModel,
                    navController = navController
                )
            }
        }
    }
}
