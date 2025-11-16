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

    private val userCache = mutableMapOf<String, String>()

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews

    private val _averageRating = MutableStateFlow(0.0)
    val averageRating: StateFlow<Double> = _averageRating

    private val _myReviews = MutableStateFlow<List<Review>>(emptyList())
    val myReviews: StateFlow<List<Review>> = _myReviews

    fun loadMyReviews(uid: String) {
        viewModelScope.launch {
            val list = reviewRepository.getReviewsByUser(uid)
            _myReviews.value = list
        }
    }

    fun loadReviews(movieId: String) {
        viewModelScope.launch {
            try {
                val list = reviewRepository.getReviews(movieId)

                // Fetch pseudo for each review
                val reviewsWithPseudo = list.mapNotNull { review ->
                    val pseudo = userCache.getOrPut(review.userId) {
                        authRepo.getUserById(review.userId)?.pseudo ?: "Anonymous"
                    }
                    review.copy(username = pseudo)
                }

                _reviews.value = reviewsWithPseudo
                _averageRating.value = if (reviewsWithPseudo.isEmpty()) 0.0
                else reviewsWithPseudo.map { it.rating }.average()
            } catch (e: Exception) {
                // Handle error
                _reviews.value = emptyList()
                _averageRating.value = 0.0
            }
        }
    }

    fun submitReview(movieId: String, rating: Double, comment: String,posterPath: String?=null) {
        val firebaseUser = authRepo.getCurrentUser() ?: return
        viewModelScope.launch {
            val firestoreUser = authRepo.getUserById(firebaseUser.uid)
            val pseudo = firestoreUser?.pseudo ?: "Anonymous"

            val review = Review(
                userId = firebaseUser.uid,
                username = pseudo,  // ← use pseudo
                rating = rating,
                comment = comment,
                posterPath = posterPath,
                timestamp = java.util.Date(),
                movieId = movieId
            )
            reviewRepository.addReview(movieId, review)
            loadReviews(movieId)
        }
    }
}
