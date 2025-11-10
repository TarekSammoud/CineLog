package fr.eseo.ld.ts.cinelog.repositories

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import fr.eseo.ld.ts.cinelog.R
import fr.eseo.ld.ts.cinelog.data.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthenticationRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    fun loginAnonymously() =
        firebaseAuth.signInAnonymously()

    fun signUpWithEmail(email: String, password: String) =
        firebaseAuth.createUserWithEmailAndPassword(email, password)

    fun loginWithEmail(email: String, password: String) =
        firebaseAuth.signInWithEmailAndPassword(email, password)

    fun logout() =
        firebaseAuth.signOut()

    fun getCurrentUser() =
        firebaseAuth.currentUser

    fun saveUserData(userId: String, userData: Map<String, Any>) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("users").document(userId).set(userData)
    }

    fun signInWithGoogle(idToken: String) =
        firebaseAuth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))

    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    suspend fun getUserData(uid: String): User? {
        val doc = FirebaseFirestore.getInstance().collection("users").document(uid).get().await()
        return if (doc.exists()) {
            User(
                nom = doc.getString("nom") ?: "",
                prenom = doc.getString("prenom") ?: "",
                email = doc.getString("email") ?: "",
                pseudo = doc.getString("pseudo") ?: "",
                photoUrl = doc.getString("photoUrl")
            )
        } else null
    }

    suspend fun saveUserData(uid: String, userData: Map<String, Any?>) {
        FirebaseFirestore.getInstance().collection("users").document(uid).set(userData).await()
    }

}
