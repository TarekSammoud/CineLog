package fr.eseo.ld.ts.cinelog.repositories

import com.google.firebase.firestore.FirebaseFirestore
import fr.eseo.ld.ts.cinelog.data.ListeAVoir
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ListeAVoirRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    private val collection = db.collection("watchlists")

    suspend fun addToWatchlist(userId: String, movieId: String) {
        val entry = ListeAVoir(userId, movieId)
        collection.add(entry).await()
    }

    suspend fun getWatchlistForUser(userId: String): List<ListeAVoir> {
        val snapshot = collection.whereEqualTo("userId", userId).get().await()
        return snapshot.toObjects(ListeAVoir::class.java)
    }

    // Nouvelle fonction : supprime toutes les entrées correspondant à (userId, movieId)
    suspend fun removeFromWatchlist(userId: String, movieId: String) {
        val snapshot = collection
            .whereEqualTo("userId", userId)
            .whereEqualTo("movieId", movieId)
            .get()
            .await()

        val deletions = snapshot.documents.map { doc ->
            doc.reference.delete()
        }

        // attendre toutes les suppressions
        deletions.forEach { it.await() }
    }
}
