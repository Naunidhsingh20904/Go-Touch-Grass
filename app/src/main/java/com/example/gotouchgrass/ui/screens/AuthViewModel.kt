package com.example.gotouchgrass.ui.screens

import androidx.lifecycle.ViewModel
import com.example.gotouchgrass.domain.FakeData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Demo password for all users in FakeData (prototyping only). */
private const val DEMO_PASSWORD = "password"

/**
 * Result of a sign-in or sign-up attempt.
 */
sealed class AuthResult {
    data object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
}

/**
 * ViewModel for the Login/Signup (Auth) screen. Validates credentials against
 * [FakeData].users for sign-in; sign-up is allowed for new emails (no persistence).
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

    private val knownEmails: Set<String> = FakeData.users.map { it.email }.toSet()

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
     * Validates credentials against FakeData.users. Returns [AuthResult.Success]
     * if a user exists with this email and password equals [DEMO_PASSWORD].
     */
    fun signIn(): AuthResult {
        val e = _email.value
        val p = _password.value
        if (e.isBlank()) return AuthResult.Error("Enter your email").also { setError(it) }
        if (p.isBlank()) return AuthResult.Error("Enter your password").also { setError(it) }
        val user = FakeData.users.find { it.email.equals(e.trim(), ignoreCase = true) }
            ?: return AuthResult.Error("No account found for this email").also { setError(it) }
        return if (p == DEMO_PASSWORD) {
            _errorMessage.update { null }
            AuthResult.Success
        } else {
            AuthResult.Error("Incorrect password").also { setError(it) }
        }
    }

    /**
     * Validates sign-up fields. Returns [AuthResult.Success] if username, email, and password
     * are non-blank and email is not already in FakeData (no persistence for new users).
     */
    fun signUp(): AuthResult {
        val u = _username.value
        val e = _email.value
        val p = _password.value
        if (u.isBlank()) return AuthResult.Error("Choose a username").also { setError(it) }
        if (e.isBlank()) return AuthResult.Error("Enter your email").also { setError(it) }
        if (p.isBlank()) return AuthResult.Error("Enter a password").also { setError(it) }
        if (p.length < 4) return AuthResult.Error("Password must be at least 4 characters").also { setError(it) }
        if (knownEmails.any { it.equals(e.trim(), ignoreCase = true) }) {
            return AuthResult.Error("An account with this email already exists").also { setError(it) }
        }
        _errorMessage.update { null }
        return AuthResult.Success
    }

    private fun setError(result: AuthResult.Error) {
        _errorMessage.update { result.message }
    }

    /** Demo credentials for testing / prefill: list of (email, password) for FakeData users. */
    fun getDemoCredentials(): List<Pair<String, String>> =
        FakeData.users.map { it.email to DEMO_PASSWORD }
}
