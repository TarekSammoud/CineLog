package fr.eseo.ld.ts.cinelog.repositories

import fr.eseo.ld.ts.cinelog.model.YoutubeSearchResponse
import fr.eseo.ld.ts.cinelog.network.YoutubeApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YoutubeRepository @Inject constructor(
    private val youtubeApi: YoutubeApiService
) {

    suspend fun fetchYoutubeTrailer(title: String, year: String, apiKey: String): YoutubeSearchResponse {
        val query = "$title $year trailer"
        return youtubeApi.searchVideo(
            query = query,
            apiKey = apiKey,
            part = "snippet",
            type = "video",
            maxResults = 1
        )
    }
}
