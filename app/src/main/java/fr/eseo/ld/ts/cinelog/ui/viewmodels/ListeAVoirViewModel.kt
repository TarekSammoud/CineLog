package fr.eseo.ld.ts.cinelog.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.eseo.ld.ts.cinelog.data.ListeAVoir
import fr.eseo.ld.ts.cinelog.model.Media
import fr.eseo.ld.ts.cinelog.model.Image
import fr.eseo.ld.ts.cinelog.repositories.ListeAVoirRepository
import fr.eseo.ld.ts.cinelog.repositories.ImdbRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class ListeAVoirViewModel @Inject constructor(
    private val repository: ListeAVoirRepository,
    private val imdbRepository: ImdbRepository
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

                // Charger les détails des films depuis l'API IMDB
                val mediaDetails = mutableListOf<Media>()
                watchlistEntries.forEach { entry ->
                    try {
                        // Récupérer les détails du film depuis l'API
                        val allMedia = imdbRepository.fetchAllMedia()
                        val media = allMedia.titles?.find { it.id == entry.movieId }
                        if (media != null) {
                            mediaDetails.add(media)
                        } else {
                            // Créer un média par défaut si non trouvé
                            mediaDetails.add(
                                Media(
                                    id = entry.movieId,
                                    primaryTitle = entry.movieId,
                                    primaryImage = null
                                )
                            )
                        }
                    } catch (e: Exception) {
                        // En cas d'erreur, ajouter quand même un média par défaut
                        mediaDetails.add(
                            Media(
                                id = entry.movieId,
                                primaryTitle = entry.movieId,
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
