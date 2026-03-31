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

    private val _displayName = MutableStateFlow("")
    val displayName: StateFlow<String> = _displayName.asStateFlow()

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

    fun setDisplayName(value: String) {
        _displayName.update { value }
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
        val d = _displayName.value
        val e = _email.value
        val p = _password.value
        if (u.isBlank()) return AuthResult.Error("Choose a username").also { setError(it) }
        if (d.isBlank()) return AuthResult.Error("Enter a display name").also { setError(it) }
        if (d.trim().length < 2) {
            return AuthResult.Error("Display name must be at least 2 characters").also { setError(it) }
        }
        if (d.trim().length > 30) {
            return AuthResult.Error("Display name must be 30 characters or fewer").also { setError(it) }
        }
        val normalized = u.trim().lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')
        if (normalized.length < 3) {
            return AuthResult.Error("Username must be at least 3 valid characters").also { setError(it) }
        }
        if (normalized.length > 20) {
            return AuthResult.Error("Username must be 20 characters or fewer").also { setError(it) }
        }
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
