package fr.eseo.ld.ts.cinelog.viewmodel

import android.util.Log
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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImdbViewModel @Inject constructor(private val repository: ImdbRepository,
                                        private val tmdbRepository: TmdbRepository,
    private val youtubeRepository: YoutubeRepository) : ViewModel() {

    private val _mediaList = MutableLiveData<List<Media>>()
    val mediaList: LiveData<List<Media>> = _mediaList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _omdbMovie = MutableLiveData<OmdbMovie?>()
    val omdbMovie: LiveData<OmdbMovie?> = _omdbMovie
    private val _youtubeTrailerId = MutableLiveData<String?>()
    val youtubeTrailerId: LiveData<String?> = _youtubeTrailerId

    fun fetchYoutubeTrailer(title: String, year: String, apiKey: String) {
        viewModelScope.launch {
            try {
                val response = youtubeRepository.fetchYoutubeTrailer(title, year, apiKey)
                val videoId = response.items.firstOrNull()?.id?.videoId
                _youtubeTrailerId.value = videoId
            } catch (e: Exception) {
                _youtubeTrailerId.value = null
            }
        }
    }

    private val _tmdbMovie = MutableLiveData<TmdbMovie?>()

    fun fetchTmdbMovieByImdbId(imdbId: String) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val result = tmdbRepository.getMovieById(imdbId)
                if (result != null) {
                    _tmdbMovie.value = result
                    Log.d("TmdbViewModel", "Fetched TMDb movie: ${result.title}")
                } else {
                    _errorMessage.value = "Movie not found"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error"
                Log.e("TmdbViewModel", "Error fetching TMDb movie: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchOmdbMovie(imdbId: String,apiKey: String) {
        _isLoading.value = true
        _errorMessage.value = null


        viewModelScope.launch {
            try {
                val movie = repository.fetchOmdbMovie(imdbId, apiKey )
                _omdbMovie.value = movie
                Log.d("ImdbViewModel", "Fetched OMDb movie: ${movie.title}")
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error"
                Log.e("ImdbViewModel", "Error fetching OMDb movie: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchFilteredMedia(type: String) {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val response = repository.getMediaByType(type)
                _mediaList.value = response.titles ?: emptyList()
                Log.d("ImdbViewModel", "Fetched media by type: ${response.titles?.size ?: 0} items")
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error"
                Log.e("ImdbViewModel", "Error fetching media by type: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
// In ImdbViewModel.kt

    private val _actorImages = MutableLiveData<Map<String, String?>>()
    val actorImages: LiveData<Map<String, String?>> = _actorImages

    fun fetchActorImages(actorNames: List<String>) {
        viewModelScope.launch {
            val images = mutableMapOf<String, String?>()
            actorNames.forEach { name ->
                try {
                    val url = tmdbRepository.getActorByQuery(name)
                    images[name] = url
                } catch (e: Exception) {
                    images[name] = null
                }
            }
            _actorImages.postValue(images)
        }
    }


    fun fetchAllMedia() {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val response = repository.fetchAllMedia()
                _mediaList.value = response.titles ?: emptyList()
                Log.d("ImdbViewModel", "Fetched media: ${response.titles?.size ?: 0} items")
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error"
                Log.e("ImdbViewModel", "Error fetching media: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}