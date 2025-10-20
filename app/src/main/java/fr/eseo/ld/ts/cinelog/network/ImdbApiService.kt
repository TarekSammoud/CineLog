package fr.eseo.ld.ts.cinelog.network

import fr.eseo.ld.ts.cinelog.model.Media
import fr.eseo.ld.ts.cinelog.model.MediaResponse
import fr.eseo.ld.ts.cinelog.model.OmdbMovie
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface ImdbApiService {

    @GET("titles")
    suspend fun fetchAllMedia(): MediaResponse

    @GET("/")
    suspend fun getMovieById(
        @Query("i") imdbId: String,
        @Query("apikey") apiKey: String = "<OMDB_API_KEY>"
    ): OmdbMovie

    @GET("titles")
    suspend fun fetchMediaByType(@Query("types") type: String): MediaResponse
}
object ImdbApiServiceImpl {

    private const val BASE_URL_IMDB = "https://api.imdbapi.dev/"
    private const val BASE_URL_OMDB = "https://www.omdbapi.com/"

    // Retrofit instance for imdbapi.dev
    private val imdbRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_IMDB)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val omdbRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_OMDB)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    // API for imdbapi.dev
    val imdbApi: ImdbApiService by lazy { imdbRetrofit.create(ImdbApiService::class.java) }

    // API for omdbapi.com
    val omdbApi: ImdbApiService by lazy { omdbRetrofit.create(ImdbApiService::class.java) }
}