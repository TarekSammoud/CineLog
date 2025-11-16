package fr.eseo.ld.ts.cinelog.network

import fr.eseo.ld.ts.cinelog.model.MediaResponse
import fr.eseo.ld.ts.cinelog.model.TmdbImagesResponse
import fr.eseo.ld.ts.cinelog.model.TmdbMovie
import fr.eseo.ld.ts.cinelog.model.TmdbPageResponse
import fr.eseo.ld.ts.cinelog.model.TmdbSimilarMoviesResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApiService {

    @GET("search/person")
    suspend fun searchPerson(
        @Query("query") query: String,
        @Query("include_adult") includeAdult: Boolean = false,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1,
        @Header("Authorization") authorization: String
    ): TmdbImagesResponse
    @GET("trending/movie/week")
    suspend fun getTrendingMovies(
        @Header("Authorization") auth: String,
        @Query("page") page: Int = 1
    ): TmdbPageResponse


    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Header("Authorization") auth: String,
        @Query("page") page: Int = 1
    ): TmdbPageResponse
    @GET("movie/{movie_id}/similar")
    suspend fun getSimilarMovies(
        @Path("movie_id") movieId: String,
        @Header("Authorization") authorization: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): TmdbSimilarMoviesResponse

    @GET("discover/movie")
    suspend fun getDiscoverMovies(
        @Query("page") page: Int = 1,
        @Header("Authorization") authToken: String,
        @Query("language") language: String = "en-US",
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("include_adult") includeAdult: Boolean = true,
        @Query("include_video") includeVideo: Boolean = false
    ): TmdbPageResponse

    // Get movie details by TMDb ID
    @GET("movie/{movie_id}")
    suspend fun getMovieById(
        @Path("movie_id") movieId: String,
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
