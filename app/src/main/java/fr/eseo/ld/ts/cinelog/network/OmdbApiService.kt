package fr.eseo.ld.ts.cinelog.network

import fr.eseo.ld.ts.cinelog.model.Media
import fr.eseo.ld.ts.cinelog.model.MediaResponse
import fr.eseo.ld.ts.cinelog.model.OmdbMovie
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import androidx.compose.ui.res.stringResource
import fr.eseo.ld.ts.cinelog.R


interface OmdbApiService {


    @GET("/")
    suspend fun getMovieById(
        @Query("i") imdbId: String,
        @Query("apikey") apiKey: String
    ): OmdbMovie


}
object OmdbApiServiceImpl {

    private const val BASE_URL = "https://www.omdbapi.com/"

    // Retrofit instance for imdbapi.dev


    private val omdbRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // API for omdbapi.com
    val omdbApi: OmdbApiService by lazy { omdbRetrofit.create(OmdbApiService::class.java) }
}