package com.example.gotouchgrass.ui.screens

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Result of a sign-in or sign-up attempt.
 */
sealed class AuthResult {
    data object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
}

/**
 * ViewModel for the Login/Signup (Auth) screen.
 * Performs lightweight input validation only; actual authentication is handled by Supabase.
 */
class AuthViewModel : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _isSignInTab = MutableStateFlow(true)
    val isSignInTab: StateFlow<Boolean> = _isSignInTab.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun setEmail(value: String) {
        _email.update { value }
        _errorMessage.update { null }
    }

    fun setUsername(value: String) {
        _username.update { value }
        _errorMessage.update { null }
    }

    fun setPassword(value: String) {
        _password.update { value }
        _errorMessage.update { null }
    }

    fun setSignInTab(isSignIn: Boolean) {
        _isSignInTab.update { isSignIn }
        _errorMessage.update { null }
    }

    fun clearError() {
        _errorMessage.update { null }
    }

    /**
     * Validates that sign-in fields are present before network auth.
     */
    fun signIn(): AuthResult {
        val e = _email.value
        val p = _password.value
        if (e.isBlank()) return AuthResult.Error("Enter your email").also { setError(it) }
        if (p.isBlank()) return AuthResult.Error("Enter your password").also { setError(it) }
        _errorMessage.update { null }
        return AuthResult.Success
    }

    /**
     * Validates sign-up fields before network auth.
     */
    fun signUp(): AuthResult {
        val u = _username.value
        val e = _email.value
        val p = _password.value
        if (u.isBlank()) return AuthResult.Error("Choose a username").also { setError(it) }
        if (e.isBlank()) return AuthResult.Error("Enter your email").also { setError(it) }
        if (p.isBlank()) return AuthResult.Error("Enter a password").also { setError(it) }
        if (p.length < 4) return AuthResult.Error("Password must be at least 4 characters").also { setError(it) }
        _errorMessage.update { null }
        return AuthResult.Success
    }

    private fun setError(result: AuthResult.Error) {
        _errorMessage.update { result.message }
    }
}
