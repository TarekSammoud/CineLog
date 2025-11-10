package fr.eseo.ld.ts.cinelog.repositories

import android.util.Log
import fr.eseo.ld.ts.cinelog.model.MediaResponse
import fr.eseo.ld.ts.cinelog.model.TmdbImagesResponse
import fr.eseo.ld.ts.cinelog.model.TmdbMovie
import fr.eseo.ld.ts.cinelog.network.TmdbApiServiceImpl
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TmdbRepository @Inject constructor(
) {
    val hardcodedBearer = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJjMTczYzMzYjE1Mzg4Mjk1MmU3NzI0YTQ1NDE0OTI3YSIsIm5iZiI6MTc2MDk0ODk4OS44NjcsInN1YiI6IjY4ZjVmMmZkMmY5ZTc5N2E3NTk3ZjgwYiIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.3xfai1n40kx_JjtIGxStTyjeF--i7Ydfa5d13izAKqY"
    suspend fun getMovieById(movieId: String): TmdbMovie {
        return TmdbApiServiceImpl.tmdbApi.getMovieById(movieId, hardcodedBearer)
    }

    suspend fun getTrendingMovies(page: Int): List<TmdbMovie> {
        Log.d("TmdbRepo", "Fetching trending page $page")
        return TmdbApiServiceImpl.tmdbApi.getTrendingMovies(hardcodedBearer, page).results
    }

    suspend fun getPopularMovies(page: Int): List<TmdbMovie> {
        Log.d("TmdbRepo", "Fetching popular page $page")
        return TmdbApiServiceImpl.tmdbApi.getPopularMovies(hardcodedBearer, page).results
    }
    suspend fun getSimilarMovies(movieId: String): List<TmdbMovie> {
        return TmdbApiServiceImpl.tmdbApi.getSimilarMovies(movieId, hardcodedBearer)
            .results
            .take(10) // limit to 10
    }

    suspend fun getActorByQuery(query: String): String? {
        val response = TmdbApiServiceImpl.tmdbApi.searchPerson(
            query = query,
            includeAdult = false,
            language = "en-US",
            page = 1,
            authorization = hardcodedBearer
        )
        return response.results.firstOrNull()?.profilePath?.let { "https://image.tmdb.org/t/p/w500$it" }
    }

}
