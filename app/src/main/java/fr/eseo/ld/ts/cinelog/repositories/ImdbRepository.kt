package fr.eseo.ld.ts.cinelog.repositories

import android.util.Log
import fr.eseo.ld.ts.cinelog.model.Media
import fr.eseo.ld.ts.cinelog.model.MediaResponse
import fr.eseo.ld.ts.cinelog.model.OmdbMovie
import fr.eseo.ld.ts.cinelog.network.ImdbApiService
import fr.eseo.ld.ts.cinelog.network.OmdbApiService
import javax.inject.Inject

class ImdbRepository @Inject constructor(
    private val imdbApi: ImdbApiService,  // for imdbapi.dev
    private val omdbApi: OmdbApiService   // for omdbapi.com
) {

    suspend fun fetchAllMedia(): MediaResponse {
        val response = imdbApi.fetchAllMedia()
        Log.d("ImdbRepository", "Raw API response: $response")
        return response
    }

    suspend fun fetchOmdbMovie(imdbId: String, apiKey: String): OmdbMovie {
        val response = omdbApi.getMovieById(imdbId,apiKey)
        Log.d("OmdbRepository", "Raw OMDb API response: $response")
        return response
    }

    suspend fun getMediaByType(type: String): MediaResponse {
        val response = imdbApi.fetchMediaByType(type)
        Log.d("ImdbRepository", "Raw API response: $response")
        return response
    }
}
