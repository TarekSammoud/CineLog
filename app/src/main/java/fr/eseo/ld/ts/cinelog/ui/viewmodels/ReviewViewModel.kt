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
import java.util.Date
import javax.inject.Inject
@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository,
    private val authRepo: AuthenticationRepository
) : ViewModel() {

    private val userDataCache = mutableMapOf<String, UserData>()

    private data class UserData(
        val pseudo: String,
        val photoUrl: String?
    )

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews

    private val _averageRating = MutableStateFlow(0.0)
    val averageRating: StateFlow<Double> = _averageRating

    private val _myReviews = MutableStateFlow<List<Review>>(emptyList())
    val myReviews: StateFlow<List<Review>> = _myReviews

    fun loadMyReviews(uid: String) {
        viewModelScope.launch {
            _myReviews.value = reviewRepository.getReviewsByUser(uid)
        }
    }

    fun loadReviews(movieId: String) {
        viewModelScope.launch {
            try {
                val list = reviewRepository.getReviews(movieId)

                val reviewsWithData = list.map { review ->
                    val userData = userDataCache.getOrPut(review.userId) {
                        val user = authRepo.getUserById(review.userId)
                        UserData(
                            pseudo = user?.pseudo ?: "Anonymous",
                            photoUrl = user?.photoUrl
                        )
                    }

                    review.copy(
                        username = userData.pseudo,
                        profilePicUrl = userData.photoUrl
                    )
                }

                _reviews.value = reviewsWithData
                _averageRating.value = if (reviewsWithData.isEmpty()) 0.0
                else reviewsWithData.map { it.rating }.average()

            } catch (e: Exception) {
                e.printStackTrace()
                _reviews.value = emptyList()
                _averageRating.value = 0.0
            }
        }
    }

    fun submitReview(movieId: String, rating: Double, comment: String, posterPath: String? = null) {
        val firebaseUser = authRepo.getCurrentUser() ?: return
        viewModelScope.launch {
            val firestoreUser = authRepo.getUserById(firebaseUser.uid)
            val pseudo = firestoreUser?.pseudo ?: "Anonymous"
            val profilePicUrl = firestoreUser?.photoUrl.orEmpty()

            val review = Review(
                userId = firebaseUser.uid,
                username = pseudo,
                profilePicUrl = profilePicUrl,
                rating = rating,
                comment = comment,
                posterPath = posterPath,
                timestamp = Date(),
                movieId = movieId
            )
            reviewRepository.addReview(movieId, review)
            loadReviews(movieId)
        }
    }
}