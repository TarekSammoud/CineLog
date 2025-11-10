package fr.eseo.ld.ts.cinelog.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.eseo.ld.ts.cinelog.data.ListeAVoir
import fr.eseo.ld.ts.cinelog.model.Media
import fr.eseo.ld.ts.cinelog.model.Image
import fr.eseo.ld.ts.cinelog.repositories.ListeAVoirRepository
import fr.eseo.ld.ts.cinelog.repositories.TmdbRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListeAVoirViewModel @Inject constructor(
    private val repository: ListeAVoirRepository,
    private val tmdbRepository: TmdbRepository
) : ViewModel() {

    private val _watchlist = MutableStateFlow<List<ListeAVoir>>(emptyList())
    val watchlist: StateFlow<List<ListeAVoir>> = _watchlist

    private val _mediaList = MutableStateFlow<List<Media>>(emptyList())
    val mediaList: StateFlow<List<Media>> = _mediaList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadWatchlist(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val watchlistEntries = repository.getWatchlistForUser(userId)
                _watchlist.value = watchlistEntries

                val mediaDetails = mutableListOf<Media>()

                for (entry in watchlistEntries) {
                    try {
                        val movie = tmdbRepository.getMovieById(entry.movieId)
                        val media = Media(
                            id = movie.id.toString(),

                            primaryTitle = movie.title ?: "Untitled",
                            primaryImage = movie.poster_path?.let {
                                Image(url = "https://image.tmdb.org/t/p/w500$it",
                                    width = 500,
                                    height = 0) // or 0 if not nullable)
                            }
                        )
                        mediaDetails.add(media)
                    } catch (e: Exception) {
                        // fallback media if TMDB fetch fails
                        mediaDetails.add(
                            Media(
                                id = entry.movieId,
                                primaryTitle = "Unknown Movie (${entry.movieId})",
                                primaryImage = null
                            )
                        )
                    }
                }

                _mediaList.value = mediaDetails
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addToWatchlist(userId: String, movieId: String) {
        viewModelScope.launch {
            try {
                repository.addToWatchlist(userId, movieId)
            } catch (e: Exception) {
                // log or handle error
            }
            loadWatchlist(userId)
        }
    }

    fun removeFromWatchlist(userId: String, movieId: String) {
        viewModelScope.launch {
            try {
                repository.removeFromWatchlist(userId, movieId)
            } catch (e: Exception) {
                // log or handle error
            }
            loadWatchlist(userId)
        }
    }
}
