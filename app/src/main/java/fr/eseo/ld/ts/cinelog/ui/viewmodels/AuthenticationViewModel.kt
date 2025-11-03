package fr.eseo.ld.ts.cinelog.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.eseo.ld.ts.cinelog.data.AuthState
import fr.eseo.ld.ts.cinelog.data.User
import fr.eseo.ld.ts.cinelog.repositories.AuthenticationRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.collections.toMap

@HiltViewModel
class AuthenticationViewModel @Inject constructor(
    private val authenticationRepository: AuthenticationRepository
) : ViewModel() {

    private val _user = MutableStateFlow<FirebaseUser?>(null)
    val user: StateFlow<FirebaseUser?> = _user.asStateFlow()

    private val _authState = MutableStateFlow(AuthState.LOADING)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Ajout du StateFlow pour l'utilisateur Firestore
    private val _firestoreUser = MutableStateFlow<User?>(null)
    val firestoreUser: StateFlow<User?> = _firestoreUser.asStateFlow()

    init {
        viewModelScope.launch {
            val currentUser = authenticationRepository.getCurrentUser()
            if (currentUser != null) {
                _user.value = currentUser
                _authState.value = AuthState.LOGGED_IN
                loadFirestoreUser(currentUser.uid)
            } else {
                _authState.value = AuthState.LOADING
                loginAnonymously()
            }
        }
    }

    fun loginAnonymously() {
        viewModelScope.launch {
            try {
                authenticationRepository.loginAnonymously().await()
                val currentUser = authenticationRepository.getCurrentUser()
                _user.value = currentUser
                _authState.value = AuthState.LOGGED_IN
                if (currentUser != null) {
                    loadFirestoreUser(currentUser.uid)
                }
            } catch (e: Exception) {
                _user.value = null
                _authState.value = AuthState.LOGGED_OUT
                _firestoreUser.value = null
            }
        }
    }

    fun logout() {
        authenticationRepository.logout()
        _user.value = null
        _authState.value = AuthState.LOGGED_OUT
        _firestoreUser.value = null
        loginAnonymously()
    }

    fun loginWithEmail(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                authenticationRepository.loginWithEmail(email, password).await()
                val currentUser = authenticationRepository.getCurrentUser()
                _user.value = currentUser
                _authState.value = AuthState.LOGGED_IN
                if (currentUser != null) {
                    loadFirestoreUser(currentUser.uid)
                }
                callback(true, null)
            } catch (e: Exception) {
                _user.value = null
                _authState.value = AuthState.LOGGED_OUT
                _firestoreUser.value = null
                val errorMessage = when {
                    e.message?.contains("The supplied auth credential is incorrect") == true ->
                        "Les informations d'identification sont incorrectes"
                    e.message?.contains("email address is badly formatted") == true ->
                        "L'adresse e-mail est mal formatée."
                    else -> "Une erreur est survenue."
                }
                callback(false, errorMessage)
            }
        }
    }

    fun signUpWithEmail(
        nom: String,
        prenom: String,
        email: String,
        pseudo: String,
        password: String,
        photoUrl: String? = null,
        callback: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = authenticationRepository.signUpWithEmail(email, password).await()
                val user = result.user
                if (user != null) {
                    val userData = User(
                        nom = nom,
                        prenom = prenom,
                        email = email,
                        pseudo = pseudo,
                        photoUrl = photoUrl ?: ""
                    )
                    authenticationRepository.saveUserData(user.uid, userData.toMap())
                    _user.value = user
                    _authState.value = AuthState.LOGGED_IN
                    _firestoreUser.value = userData
                    callback(true, null)
                } else {
                    callback(false, "Une erreur est survenue.")
                }
            } catch (e: Exception) {
                _user.value = null
                _authState.value = AuthState.LOGGED_OUT
                _firestoreUser.value = null
                val real = e.cause ?: e
                val errorMessage = when (real) {
                    is FirebaseAuthUserCollisionException ->
                        "Cette adresse e‑mail est déjà utilisée. Connectez‑vous ou réinitialisez votre mot de passe."
                    is FirebaseAuthInvalidCredentialsException ->
                        "L'adresse e‑mail est mal formatée."
                    is FirebaseAuthWeakPasswordException ->
                        "Le mot de passe doit contenir au moins 6 caractères."
                    else -> real.message?.let { msg ->
                        when {
                            msg.contains("email address is badly formatted", ignoreCase = true) ->
                                "L'adresse e‑mail est mal formatée."
                            msg.contains("Password should be at least", ignoreCase = true) ->
                                "Le mot de passe doit contenir au moins 6 caractères."
                            else -> "Une erreur est survenue."
                        }
                    } ?: "Une erreur est survenue."
                }
                callback(false, errorMessage)
            }
        }
    }

    fun signInWithGoogle(idToken: String, callback: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                authenticationRepository.signInWithGoogle(idToken).await()
                val currentUser = authenticationRepository.getCurrentUser()
                _user.value = currentUser
                _authState.value = AuthState.LOGGED_IN
                if (currentUser != null) {
                    loadFirestoreUser(currentUser.uid)
                }
                callback(true, null)
            } catch (e: Exception) {
                _user.value = null
                _authState.value = AuthState.LOGGED_OUT
                _firestoreUser.value = null
                callback(false, "Échec de la connexion avec Google.")
            }
        }
    }

    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        return authenticationRepository.getGoogleSignInClient(context)
    }

    fun updateUser(nom: String, prenom: String, email: String, pseudo: String, photoUrl: String? = null) {
        viewModelScope.launch {
            val uid = _user.value?.uid ?: return@launch
            val userData = User(nom, prenom, email, pseudo, photoUrl)
            authenticationRepository.saveUserData(uid, userData.toMap())
            _firestoreUser.value = userData
        }
    }

    fun loadFirestoreUser(uid: String) {
        viewModelScope.launch {
            val userData = authenticationRepository.getUserData(uid)
            _firestoreUser.value = userData
        }
    }

    private fun User.toMap(): Map<String, Any?> = mapOf(
        "nom" to nom,
        "prenom" to prenom,
        "email" to email,
        "pseudo" to pseudo,
        "photoUrl" to photoUrl
    )
}
