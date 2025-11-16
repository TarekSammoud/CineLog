package fr.eseo.ld.ts.cinelog.repositories
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import fr.eseo.ld.ts.cinelog.model.Review
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepository  @Inject constructor(
    private val firestore: FirebaseFirestore
) {


    suspend fun getReviewsByUser(uid: String): List<Review> {
        return try {
            val snapshot = firestore
                .collection("user_reviews")
                .document(uid)
                .collection("reviews")
                .orderBy("timestamp", Query.Direction.DESCENDING)  // newest first
                .get()
                .await()

            snapshot.toObjects(Review::class.java)
        } catch (e: Exception) {
            Log.e("ReviewRepo", "Failed to load user reviews", e)
            emptyList()
        }
    }

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

        firestore.collection("user_reviews")
            .document(review.userId)
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