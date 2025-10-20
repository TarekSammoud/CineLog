package fr.eseo.ld.ts.cinelog.repositories

import fr.eseo.ld.ts.cinelog.model.YoutubeSearchResponse
import fr.eseo.ld.ts.cinelog.network.YoutubeApi
import fr.eseo.ld.ts.cinelog.network.YoutubeApiService

class YoutubeRepository(
    private val youtubeApi: YoutubeApiService = YoutubeApi.api
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
