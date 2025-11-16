package fr.eseo.ld.ts.cinelog.viewmodel

import android.util.Log
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.eseo.ld.ts.cinelog.R
import fr.eseo.ld.ts.cinelog.model.Media
import fr.eseo.ld.ts.cinelog.model.OmdbMovie
import fr.eseo.ld.ts.cinelog.model.TmdbMovie
import fr.eseo.ld.ts.cinelog.repositories.ImdbRepository
import fr.eseo.ld.ts.cinelog.repositories.TmdbRepository
import fr.eseo.ld.ts.cinelog.repositories.YoutubeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class ImdbViewModel @Inject constructor(
    private val tmdbRepository: TmdbRepository,
    private val youtubeRepository: YoutubeRepository
) : ViewModel() {

    private val _lazyGridState = mutableStateOf(LazyGridState())
    val lazyGridState: State<LazyGridState> = _lazyGridState

    // ── UI state ─────────────────────────────────────
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    // Movie detail
    private val _tmdbMovie = MutableLiveData<TmdbMovie?>()
    val tmdbMovie: LiveData<TmdbMovie?> = _tmdbMovie

    // Trailer
    private val _youtubeTrailerId = MutableLiveData<String?>()
    val youtubeTrailerId: LiveData<String?> = _youtubeTrailerId

    // Similar movies
    private val _similarMovies = MutableLiveData<List<TmdbMovie>>()
    val similarMovies: LiveData<List<TmdbMovie>> = _similarMovies

    // Actor images
    private val _actorImages = MutableLiveData<Map<String, String?>>()
    val actorImages: LiveData<Map<String, String?>> = _actorImages

    // List for SummaryScreen
    private val _movieList = MutableStateFlow<List<TmdbMovie>>(emptyList())
    val movieList: StateFlow<List<TmdbMovie>> = _movieList

    // --- Pagination state ---
    private var currentPage = 1
    private var totalPages = Int.MAX_VALUE
    private var isLoadingPage = false


    private var currentFilter = "Trending"


    private val _discoveryMovies = MutableStateFlow<List<TmdbMovie>>(emptyList())
    val discoveryMovies: StateFlow<List<TmdbMovie>> = _discoveryMovies

    private val TAG = "DiscoveryVM"   // <-- add this at the top of the class

    fun loadDiscoveryMovies(page: Int = 1) {
        Log.d(TAG, "loadDiscoveryMovies(page = $page) – START")
        viewModelScope.launch {
            try {
                Log.d(TAG, "Calling tmdbRepository.discoverMovies(page = $page)…")
                val newMovies = tmdbRepository.discoverMovies(page)

                // 1. Log raw response size
                Log.d(TAG, "Repository returned ${newMovies.size} movies for page $page")

                // 2. (Optional) Log a few movie titles so you can see real data
                newMovies.take(3).forEachIndexed { i, m ->
                    Log.d(TAG, "   [$i] ${m.title} (id=${m.id})")
                }

                // 3. Append to existing list
                val current = _discoveryMovies.value
                Log.d(TAG, "Current list size BEFORE append: ${current.size}")

                _discoveryMovies.value = current + newMovies

                Log.d(TAG,
                    "Discovery list UPDATED – total movies now: ${_discoveryMovies.value.size}"
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load discovery movies (page $page)", e)
                _errorMessage.value = e.message
            }
        }
    }
    // --- Reset & fetch first page ---
    fun loadFirstPage(filter: String) {
        currentFilter = filter
        currentPage = 1
        totalPages = Int.MAX_VALUE
        _movieList.value = emptyList()
        fetchPage(currentPage)
    }

    fun loadLastPage() {
        if (currentPage != 1 ||isLoadingPage || currentPage >= totalPages) return
        fetchPage(currentPage + 1)
    }

    // --- Load next page when scrolled ---
    fun loadNextPage() {
        if (isLoadingPage || currentPage >= totalPages) return
        fetchPage(currentPage + 1)
    }

    private fun fetchPage(page: Int) {
        if (isLoadingPage) return
        isLoadingPage = true
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val newMovies = when (currentFilter) {
                    "Trending" -> tmdbRepository.getTrendingMovies(page)
                    "Popular"  -> tmdbRepository.getPopularMovies(page)
                    else -> emptyList()
                }

                totalPages = 500 // TMDB max ~500 pages
                currentPage = page

                val current = _movieList.value.orEmpty()
                _movieList.value = current + newMovies

                Log.d("ImdbViewModel", "Loaded page $page → ${newMovies.size} movies (total: ${current.size + newMovies.size})")
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load page $page: ${e.message}"
                Log.e("ImdbViewModel", "Pagination error", e)
            } finally {
                isLoadingPage = false
                _isLoading.value = false
            }
        }
    }

    // ── TMDB fetch by TMDB ID ───────────────────────
    fun fetchTmdbMovieByTmdbId(tmdbId: String) {
        Log.d("ImdbViewModel", "Starting fetchTmdbMovieByTmdbId for ID: $tmdbId")
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val movie = tmdbRepository.getMovieById(tmdbId)
                Log.d("ImdbViewModel", "Successfully fetched TMDB movie: ${movie.title}")
                _tmdbMovie.postValue(movie)
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Unknown error"
                Log.e("ImdbViewModel", "ERROR in fetchTrendingMovies: $errorMsg", e)
                _errorMessage.value = errorMsg
            } finally {
                _isLoading.value = false
                Log.d("ImdbViewModel", "Finished fetchTmdbMovieByTmdbId for ID: $tmdbId")
            }
        }
    }

    // ── Trailer (title + year) ───────────────────────
    fun fetchYoutubeTrailer(title: String, year: String, apiKey: String) {
        Log.d("ImdbViewModel", "Starting fetchYoutubeTrailer for title: $title, year: $year")
        viewModelScope.launch {
            try {
                val resp = youtubeRepository.fetchYoutubeTrailer(title, year, apiKey)
                val videoId = resp.items.firstOrNull()?.id?.videoId
                Log.d("ImdbViewModel", "Successfully fetched trailer ID: $videoId")
                _youtubeTrailerId.value = videoId
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Unknown error"
                Log.e("ImdbViewModel", "ERROR in fetchTrendingMovies: $errorMsg", e)
                _errorMessage.value = errorMsg
            }finally {
                Log.d("ImdbViewModel", "Finished fetchYoutubeTrailer for title: $title, year: $year")
            }
        }
    }

    // ── Similar movies ───────────────────────────────
    fun fetchSimilarMovies(tmdbId: String) {
        Log.d("ImdbViewModel", "Starting fetchSimilarMovies for TMDB ID: $tmdbId")
        viewModelScope.launch {
            try {
                val list = tmdbRepository.getSimilarMovies(tmdbId)
                Log.d("ImdbViewModel", "Successfully fetched ${list.size} similar movies")
                _similarMovies.postValue(list)
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Unknown error"
                Log.e("ImdbViewModel", "ERROR in fetchTrendingMovies: $errorMsg", e)
                _errorMessage.value = errorMsg
            } finally {
                Log.d("ImdbViewModel", "Finished fetchSimilarMovies for TMDB ID: $tmdbId")
            }
        }
    }

    // ── Actor images (optional – you can later fetch credits) ─────
    fun fetchActorImages(names: List<String>) {
        Log.d("ImdbViewModel", "Starting fetchActorImages for actors: ${names.joinToString()}")
        viewModelScope.launch {
            val map = mutableMapOf<String, String?>()
            names.forEach { name ->
                try {
                    val url = tmdbRepository.getActorByQuery(name)
                    Log.d("ImdbViewModel", "Fetched image for actor $name: $url")
                    map[name] = url
                } catch (e: Exception) {
                    val errorMsg = e.message ?: "Unknown error"
                    Log.e("ImdbViewModel", "ERROR in fetchTrendingMovies: $errorMsg", e)
                    _errorMessage.value = errorMsg
                }
            }
            _actorImages.postValue(map)
            Log.d("ImdbViewModel", "Finished fetchActorImages, fetched ${map.size} images")
        }
    }

    // ── SummaryScreen lists ─────────────────────────────
    fun fetchTrendingMovies() = viewModelScope.launch {
        Log.d("ImdbViewModel", "Starting fetchTrendingMovies")
        _isLoading.value = true
        try {
            val list = tmdbRepository.getTrendingMovies(currentPage)
            Log.d("ImdbViewModel", "Successfully fetched ${list.size} trending movies")
            _movieList.value = list
        } catch (e: Exception) {
            val errorMsg = e.message ?: "Unknown error"
            Log.e("ImdbViewModel", "ERROR in fetchTrendingMovies: $errorMsg", e)
            _errorMessage.value = errorMsg
        } finally {
            _isLoading.value = false
            Log.d("ImdbViewModel", "Finished fetchTrendingMovies")
        }
    }

    fun fetchPopularMovies() = viewModelScope.launch {
        Log.d("ImdbViewModel", "Starting fetchPopularMovies")
        _isLoading.value = true
        try {
            val list = tmdbRepository.getPopularMovies(currentPage)
            Log.d("ImdbViewModel", "Successfully fetched ${list.size} popular movies")
            _movieList.value = list
        } catch (e: Exception) {
            val errorMsg = e.message ?: "Unknown error"
            Log.e("ImdbViewModel", "ERROR in fetchTrendingMovies: $errorMsg", e)
            _errorMessage.value = errorMsg
        } finally {
            _isLoading.value = false
            Log.d("ImdbViewModel", "Finished fetchPopularMovies")
        }
    }
}