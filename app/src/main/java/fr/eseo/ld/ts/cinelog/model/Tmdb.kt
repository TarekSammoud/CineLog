package fr.eseo.ld.ts.cinelog.model

data class TmdbImagesResponse(
    val backdrops: List<TmdbImage>,
    val posters: List<TmdbImage>
)

data class TmdbImage(
    val file_path: String,
    val width: Int,
    val height: Int,
    val iso_639_1: String?,
    val aspect_ratio: Float,
    val vote_average: Float,
    val vote_count: Int
)

data class TmdbFindResponse(
    val movie_results: List<TmdbMovie> = emptyList(),
    val person_results: List<Any> = emptyList(),
    val tv_results: List<Any> = emptyList(),
    val tv_episode_results: List<Any> = emptyList(),
    val tv_season_results: List<Any> = emptyList()
)