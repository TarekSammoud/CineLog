package fr.eseo.ld.ts.cinelog.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.eseo.ld.ts.cinelog.model.Review
import fr.eseo.ld.ts.cinelog.repositories.AuthenticationRepository
import fr.eseo.ld.ts.cinelog.repositories.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository,
    private val authRepo: AuthenticationRepository
) : ViewModel() {

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews

    private val _averageRating = MutableStateFlow(0.0)
    val averageRating: StateFlow<Double> = _averageRating

    fun loadReviews(movieId: String) {
        viewModelScope.launch {
            val list = reviewRepository.getReviews(movieId)
            _reviews.value = list
            _averageRating.value = if (list.isEmpty()) 0.0 else list.map { it.rating }.average()
        }
    }

    fun submitReview(movieId: String, rating: Double, comment: String) {
        val user = authRepo.getCurrentUser() ?: return
        val review = Review(
            userId = user.uid,
            username = user.displayName ?: "Anonymous",
            rating = rating,
            comment = comment
        )
        viewModelScope.launch {
            reviewRepository.addReview(movieId, review)
            loadReviews(movieId) // refresh
        }
    }
}
