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
import fr.eseo.ld.ts.cinelog.repositories.AuthenticationRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


@HiltViewModel
class AuthenticationViewModel @Inject constructor(
    private val authenticationRepository: AuthenticationRepository
) : ViewModel() {

    // Current Firebase user
    private val _user = MutableStateFlow<FirebaseUser?>(null)
    val user: StateFlow<FirebaseUser?> = _user.asStateFlow()

    // Authentication state for UI
    private val _authState = MutableStateFlow(AuthState.LOADING)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        viewModelScope.launch {
            val currentUser = authenticationRepository.getCurrentUser()
            if (currentUser != null) {
                _user.value = currentUser
                _authState.value = AuthState.LOGGED_IN
            } else {
                _authState.value = AuthState.LOADING
                loginAnonymously()
            }
        }
    }

    /**
     * Anonymous login
     */
    fun loginAnonymously() {
        viewModelScope.launch {
            try {
                authenticationRepository.loginAnonymously().await()
                _user.value = authenticationRepository.getCurrentUser()
                _authState.value = AuthState.LOGGED_IN
            } catch (e: Exception) {
                _user.value = null
                _authState.value = AuthState.LOGGED_OUT
            }
        }
    }

    /**
     * Logout
     */
    fun logout() {
        authenticationRepository.logout()
        _user.value = null
        _authState.value = AuthState.LOGGED_OUT
        loginAnonymously()
    }

    /**
     * Email & password login
     */
    fun loginWithEmail(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                authenticationRepository.loginWithEmail(email, password).await()
                _user.value = authenticationRepository.getCurrentUser()
                _authState.value = AuthState.LOGGED_IN
                callback(true, null)
            } catch (e: Exception) {
                _user.value = null
                _authState.value = AuthState.LOGGED_OUT
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

    /**
     * Email & password signup
     */
    fun signUpWithEmail(
        email: String,
        password: String,
        pseudo: String,
        callback: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = authenticationRepository.signUpWithEmail(email, password).await()
                val user = result.user
                if (user != null) {
                    val userData = mapOf(
                        "pseudo" to pseudo,
                        "email" to email
                    )
                    authenticationRepository.saveUserData(user.uid, userData)
                    _user.value = user
                    _authState.value = AuthState.LOGGED_IN
                    callback(true, null)
                } else {
                    callback(false, "Une erreur est survenue.")
                }
            } catch (e: Exception) {
                _user.value = null
                _authState.value = AuthState.LOGGED_OUT
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

    /**
     * Google Sign-In
     */
    fun signInWithGoogle(idToken: String, callback: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                authenticationRepository.signInWithGoogle(idToken).await()
                _user.value = authenticationRepository.getCurrentUser()
                _authState.value = AuthState.LOGGED_IN
                callback(true, null)
            } catch (e: Exception) {
                _user.value = null
                _authState.value = AuthState.LOGGED_OUT
                callback(false, "Échec de la connexion avec Google.")
            }
        }
    }

    /**
     * Get GoogleSignInClient
     */
    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        return authenticationRepository.getGoogleSignInClient(context)
    }
}