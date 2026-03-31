package com.example.gotouchgrass.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.gotouchgrass.data.AppNotificationHelper
import com.example.gotouchgrass.data.auth.AuthService
import com.example.gotouchgrass.ui.screens.ProfileViewModel
import kotlinx.coroutines.launch

private fun hasLocationPermission(context: android.content.Context): Boolean {
    val fine = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    return fine || coarse
}

@Composable
fun SettingsFlow(
    viewModel: SettingsViewModel,
    authService: AuthService,
    profileViewModel: ProfileViewModel?,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showEditProfile by remember { mutableStateOf(false) }
    var showPrivacy by remember { mutableStateOf(false) }
    var editEmail by remember { mutableStateOf("") }
    var editSaving by remember { mutableStateOf(false) }
    var editError by remember { mutableStateOf<String?>(null) }

    var pendingNotificationEnable by remember { mutableStateOf(false) }
    var pendingLocationEnable by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (pendingNotificationEnable) {
            pendingNotificationEnable = false
            if (granted) {
                AppNotificationHelper.ensureDefaultChannel(context.applicationContext)
                viewModel.updatePreferences(
                    viewModel.preferences.copy(notificationsEnabled = true)
                )
            } else {
                viewModel.updatePreferences(
                    viewModel.preferences.copy(notificationsEnabled = false)
                )
                scope.launch {
                    snackbarHostState.showSnackbar("Notification permission denied")
                }
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        if (pendingLocationEnable) {
            pendingLocationEnable = false
            val granted = hasLocationPermission(context)
            viewModel.updatePreferences(
                viewModel.preferences.copy(locationServicesEnabled = granted)
            )
            if (!granted) {
                scope.launch {
                    snackbarHostState.showSnackbar("Location permission denied")
                }
            }
        }
    }

    fun onNotificationsToggle(enabled: Boolean) {
        if (!enabled) {
            viewModel.updatePreferences(viewModel.preferences.copy(notificationsEnabled = false))
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    AppNotificationHelper.ensureDefaultChannel(context.applicationContext)
                    viewModel.updatePreferences(
                        viewModel.preferences.copy(notificationsEnabled = true)
                    )
                }

                else -> {
                    pendingNotificationEnable = true
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            AppNotificationHelper.ensureDefaultChannel(context.applicationContext)
            viewModel.updatePreferences(viewModel.preferences.copy(notificationsEnabled = true))
        }
    }

    fun onLocationToggle(enabled: Boolean) {
        if (!enabled) {
            viewModel.updatePreferences(viewModel.preferences.copy(locationServicesEnabled = false))
            return
        }
        if (hasLocationPermission(context)) {
            viewModel.updatePreferences(viewModel.preferences.copy(locationServicesEnabled = true))
        } else {
            pendingLocationEnable = true
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(showEditProfile) {
        if (showEditProfile) {
            editError = null
            authService.getCurrentUser().onSuccess { user ->
                editEmail = user?.email.orEmpty()
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            showPrivacy -> {
                PrivacyPermissionsScreen(onBackClick = { showPrivacy = false })
            }

            else -> {
                SettingsScreen(
                    viewModel = viewModel,
                    onBackClick = onBackClick,
                    onLogoutClick = onLogoutClick,
                    onEditProfileClick = { showEditProfile = true },
                    onPrivacyClick = { showPrivacy = true },
                    onHelpClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Help center coming soon")
                        }
                    },
                    onNotificationsToggle = ::onNotificationsToggle,
                    onLocationToggle = ::onLocationToggle
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (showEditProfile) {
        EditProfileDialog(
            initialUsername = profileViewModel?.username.orEmpty(),
            initialDisplayName = profileViewModel?.displayName.orEmpty(),
            initialAvatarKey = profileViewModel?.avatarKey,
            email = editEmail,
            isSaving = editSaving,
            errorMessage = editError,
            onDismiss = {
                if (!editSaving) {
                    showEditProfile = false
                }
            },
            onSave = { username, displayName, newPassword, avatarKey ->
                scope.launch {
                    val profileUpdater = profileViewModel
                    if (profileUpdater == null) {
                        return@launch
                    }

                    profileUpdater.updateProfile(username, displayName, avatarKey)
                        .onFailure {
                            editError = it.message ?: "Could not update profile"
                            editSaving = false
                            return@launch
                        }
                    if (newPassword != null) {
                        authService.updatePassword(newPassword)
                            .onFailure {
                                editError = it.message ?: "Could not update password"
                                editSaving = false
                                return@launch
                            }
                    }
                    profileViewModel?.refresh()
                    editSaving = false
                    showEditProfile = false
                }
            }
        )
    }
}
