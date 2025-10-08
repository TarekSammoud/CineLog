package fr.eseo.ld.ts.cinelog.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthenticationRepository @Inject constructor (
    private val firebaseAuth : FirebaseAuth
) {
    fun loginAnonymously() =
        firebaseAuth.signInAnonymously()

    fun signUpWithEmail(email : String, password : String) =
        firebaseAuth.createUserWithEmailAndPassword(email, password)

    fun loginWithEmail(email : String, password : String) =
        firebaseAuth.signInWithEmailAndPassword(email, password)

    fun logout() =
        firebaseAuth.signOut()

    fun getCurrentUser() =
        firebaseAuth.currentUser
}