package fr.eseo.ld.ts.cinelog.repositories
import com.google.firebase.firestore.FirebaseFirestore
import fr.eseo.ld.ts.cinelog.model.Review
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepository  @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun getReviews(movieId: String): List<Review> {
        val snapshot = firestore.collection("movies")
            .document(movieId)
            .collection("reviews")
            .orderBy("createdAt")
            .get()
            .await()
        return snapshot.toObjects(Review::class.java)
    }

    suspend fun addReview(movieId: String, review: Review) {
        firestore.collection("movies")
            .document(movieId)
            .collection("reviews")
            .add(review)
            .await()
    }

    suspend fun getAverageRating(movieId: String): Double {
        val reviews = getReviews(movieId)
        if (reviews.isEmpty()) return 0.0
        return reviews.map { it.rating }.average()
    }
}