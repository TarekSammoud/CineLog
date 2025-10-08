package fr.eseo.ld.ts.cinelog.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.eseo.ld.ts.cinelog.repositories.AuthenticationRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@HiltViewModel
class AuthenticationViewModel @Inject constructor(
    private val authenticationRepository: AuthenticationRepository
) : ViewModel() {

    // 4. Private MutableStateFlow to hold the FirebaseUser state
    private val _user = MutableStateFlow<FirebaseUser?>(null)

    // 5. Public immutable StateFlow to expose the user state
    val user: StateFlow<FirebaseUser?>
        get() = _user.asStateFlow()

    // 6. Initialize the ViewModel with current user or perform anonymous login
    init {
        _user.value = authenticationRepository.getCurrentUser()
        if (_user.value == null) {
            loginAnonymously()
        }
    }

    fun loginAnonymously() {
        viewModelScope.launch {
            try {
                authenticationRepository.loginAnonymously().await()
                _user.value = authenticationRepository.getCurrentUser()
                Log.d("AuthVM", "Anonymous login successful: ${_user.value?.uid}")
            } catch (e: Exception) {
                _user.value = null
                Log.e("AuthVM", "Anonymous login failed", e)
            }
        }
    }


    fun logout() {
        authenticationRepository.logout()
        loginAnonymously()
    }

    fun login(email : String, password : String) {
        authenticationRepository.loginWithEmail(email, password)
            .addOnCompleteListener {
                    task ->
                if(task.isSuccessful) {
                    _user.value = authenticationRepository.getCurrentUser()
                }
                else {
                    _user.value = null
                }
            }
    }

    fun signUp(email : String, password : String) {
        authenticationRepository.signUpWithEmail(email, password)
            .addOnCompleteListener {
                    task ->
                if(task.isSuccessful) {
                    _user.value = authenticationRepository.getCurrentUser()
                }
                else {
                    _user.value = null
                }
            }
    }



}