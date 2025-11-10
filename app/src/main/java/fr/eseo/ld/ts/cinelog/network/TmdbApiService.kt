package fr.eseo.ld.ts.cinelog.network

import fr.eseo.ld.ts.cinelog.model.MediaResponse
import fr.eseo.ld.ts.cinelog.model.TmdbImagesResponse
import fr.eseo.ld.ts.cinelog.model.TmdbMovie
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApiService {

    // Get movie details by TMDb ID
    @GET("movie/{movie_id}")
    suspend fun getMovieById(
        @Path("movie_id") movieId: Int,
        @Header("Authorization") authToken: String,
        @Query("language") language: String = "en-US"
    ): TmdbMovie
}

object TmdbApiServiceImpl {
    private const val BASE_URL = "https://api.themoviedb.org/3/"

    private val tmdbRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val tmdbApi: TmdbApiService by lazy {
        tmdbRetrofit.create(TmdbApiService::class.java)
    }
}
