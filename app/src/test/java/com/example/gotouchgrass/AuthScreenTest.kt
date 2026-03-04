package com.example.gotouchgrass

import com.example.gotouchgrass.ui.screens.AuthResult
import com.example.gotouchgrass.ui.screens.AuthViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the Login/Signup (Auth) screen backed by [AuthViewModel]
 * and [com.example.gotouchgrass.domain.FakeData].
 */
class AuthScreenTest {

    private val viewModel = AuthViewModel()

    // --- Sign In (FakeData-backed) ---

    @Test
    fun signIn_withFakeDataUserEmailAndDemoPassword_returnsSuccess() {
        viewModel.setEmail("you@uwaterloo.ca")
        viewModel.setPassword("password")
        val result = viewModel.signIn()
        assertTrue(result is AuthResult.Success)
    }

    @Test
    fun signIn_withSecondFakeDataUser_returnsSuccess() {
        viewModel.setEmail("world@example.com")
        viewModel.setPassword("password")
        val result = viewModel.signIn()
        assertTrue(result is AuthResult.Success)
    }

    @Test
    fun signIn_withWrongPassword_returnsError() {
        viewModel.setEmail("you@uwaterloo.ca")
        viewModel.setPassword("wrong")
        val result = viewModel.signIn()
        assertTrue(result is AuthResult.Error)
        assertEquals("Incorrect password", (result as AuthResult.Error).message)
    }

    @Test
    fun signIn_withUnknownEmail_returnsError() {
        viewModel.setEmail("unknown@example.com")
        viewModel.setPassword("password")
        val result = viewModel.signIn()
        assertTrue(result is AuthResult.Error)
        assertEquals("No account found for this email", (result as AuthResult.Error).message)
    }

    @Test
    fun signIn_withBlankEmail_returnsError() {
        viewModel.setEmail("")
        viewModel.setPassword("password")
        val result = viewModel.signIn()
        assertTrue(result is AuthResult.Error)
        assertEquals("Enter your email", (result as AuthResult.Error).message)
    }

    @Test
    fun signIn_withBlankPassword_returnsError() {
        viewModel.setEmail("you@uwaterloo.ca")
        viewModel.setPassword("")
        val result = viewModel.signIn()
        assertTrue(result is AuthResult.Error)
        assertEquals("Enter your password", (result as AuthResult.Error).message)
    }

    @Test
    fun signIn_emailCaseInsensitive_returnsSuccess() {
        viewModel.setEmail("YOU@UWATERLOO.CA")
        viewModel.setPassword("password")
        val result = viewModel.signIn()
        assertTrue(result is AuthResult.Success)
    }

    // --- Sign Up ---

    @Test
    fun signUp_withNewEmailAndValidFields_returnsSuccess() {
        viewModel.setSignInTab(false)
        viewModel.setUsername("newuser")
        viewModel.setEmail("new@example.com")
        viewModel.setPassword("validpass")
        val result = viewModel.signUp()
        assertTrue(result is AuthResult.Success)
    }

    @Test
    fun signUp_withExistingEmail_returnsError() {
        viewModel.setSignInTab(false)
        viewModel.setUsername("someone")
        viewModel.setEmail("you@uwaterloo.ca")
        viewModel.setPassword("password123")
        val result = viewModel.signUp()
        assertTrue(result is AuthResult.Error)
        assertEquals("An account with this email already exists", (result as AuthResult.Error).message)
    }

    @Test
    fun signUp_withBlankUsername_returnsError() {
        viewModel.setSignInTab(false)
        viewModel.setUsername("")
        viewModel.setEmail("new@example.com")
        viewModel.setPassword("password123")
        val result = viewModel.signUp()
        assertTrue(result is AuthResult.Error)
        assertEquals("Choose a username", (result as AuthResult.Error).message)
    }

    @Test
    fun signUp_withShortPassword_returnsError() {
        viewModel.setSignInTab(false)
        viewModel.setUsername("newuser")
        viewModel.setEmail("new@example.com")
        viewModel.setPassword("abc")
        val result = viewModel.signUp()
        assertTrue(result is AuthResult.Error)
        assertEquals("Password must be at least 4 characters", (result as AuthResult.Error).message)
    }

    // --- Demo credentials (FakeData integration) ---

    @Test
    fun getDemoCredentials_returnsOneEntryPerFakeDataUser() {
        val credentials = viewModel.getDemoCredentials()
        assertEquals(2, credentials.size)
        assertTrue(credentials.any { it.first == "you@uwaterloo.ca" && it.second == "password" })
        assertTrue(credentials.any { it.first == "world@example.com" && it.second == "password" })
    }
}
