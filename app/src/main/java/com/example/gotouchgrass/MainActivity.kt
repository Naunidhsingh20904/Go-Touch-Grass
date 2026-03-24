package com.example.gotouchgrass

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gotouchgrass.ui.explore.ExploreScreen
import com.example.gotouchgrass.ui.explore.ExploreViewModel
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.example.gotouchgrass.ui.screens.AuthScreen
import com.example.gotouchgrass.ui.screens.AuthViewModel
import com.example.gotouchgrass.ui.screens.ProfileScreen
import com.example.gotouchgrass.ui.map.MapScreen
import com.example.gotouchgrass.ui.theme.GoTouchGrassTheme

import com.example.gotouchgrass.ui.search.SearchScreen
import com.example.gotouchgrass.ui.search.SearchViewModel
import com.example.gotouchgrass.data.preferences.AppPreferencesStore
import com.example.gotouchgrass.ui.settings.SettingsFlow
import com.example.gotouchgrass.ui.screens.ProfileViewModel
import com.example.gotouchgrass.ui.settings.SettingsViewModel
import com.example.gotouchgrass.data.ProfileRepository
import com.example.gotouchgrass.data.SupabaseProfileRepository
import com.example.gotouchgrass.domain.ProfileModel
import com.example.gotouchgrass.ui.stats.StatsScreen
import com.example.gotouchgrass.ui.stats.StatsViewModel
import com.example.gotouchgrass.data.GoTouchGrassRepository
import com.example.gotouchgrass.data.MapRepository
import com.example.gotouchgrass.data.SupabaseMapRepository
import com.example.gotouchgrass.data.auth.AuthService
import com.example.gotouchgrass.data.supabase.SupabaseDataSource
import com.example.gotouchgrass.domain.MapModel
import com.example.gotouchgrass.location.AppLocationTracker
import com.example.gotouchgrass.ui.map.MapViewModel
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.Auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

val supabase = createSupabaseClient(
    supabaseUrl = "https://pfpjfczpztwqcicxryoy.supabase.co",
    supabaseKey = "sb_publishable_ch0S89n_RpY33Y6KIUxPSg_Ox2mSuyd"
) {
    install(Postgrest)
    install(Auth)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY)
        }
        enableEdgeToEdge()
        val initialDarkMode = runBlocking {
            AppPreferencesStore(applicationContext).readDarkMode()
        }
        setContent {
            GoTouchGrassApp(initialDarkMode = initialDarkMode)
        }
    }
}

@Preview(
    name = "Phone",
    showBackground = true,
    widthDp = 360,
    heightDp = 800
)
@Composable
fun GoTouchGrassAppPreview() {
    GoTouchGrassApp()
}

@Composable
fun GoTouchGrassApp(initialDarkMode: Boolean = false) {
    val appContext = LocalContext.current.applicationContext
    val appPrefs = remember { AppPreferencesStore(appContext) }
    val locationTracker = remember { AppLocationTracker(appContext) }
    var darkTheme by remember { mutableStateOf(initialDarkMode) }
    LaunchedEffect(appPrefs) {
        appPrefs.darkModeFlow.collect { darkTheme = it }
    }

    GoTouchGrassTheme(darkTheme = darkTheme) {
        var isAuthenticated by rememberSaveable { mutableStateOf(false) }
        var currentUserId by rememberSaveable { mutableStateOf<String?>(null) }
        var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.MAP) }
        var showSettings by rememberSaveable { mutableStateOf(false) }
        var authError by rememberSaveable { mutableStateOf<String?>(null) }
        var selectedMapPlaceId by rememberSaveable { mutableStateOf<String?>(null) }

        val context = LocalContext.current.applicationContext
        val coroutineScope = rememberCoroutineScope()
        val authService = remember { AuthService(supabase) }
        val dataSource = remember { SupabaseDataSource(supabase) }
        val repository = remember { GoTouchGrassRepository(dataSource) }
        val profileRepository: ProfileRepository =
            remember { SupabaseProfileRepository(repository) }
        val mapRepository: MapRepository = remember { SupabaseMapRepository(repository) }
        val searchViewModel = remember(currentUserId) {
            SearchViewModel(
                currentUserId = currentUserId,
                repository = repository,
                onSelectPlace = { placeId ->
                    selectedMapPlaceId = placeId
                    currentDestination = AppDestinations.MAP
                }
            )
        }
        val exploreViewModel = remember(currentUserId) {
            currentUserId?.let { ExploreViewModel(currentUserId = it, repository = repository) }
        }
        val statsViewModel = remember(currentUserId) {
            StatsViewModel(userId = currentUserId, repository = repository)
        }
        val profileViewModel = remember(currentUserId) {
            currentUserId?.let { userId ->
                val model = ProfileModel(
                    currentUserId = userId,
                    repository = profileRepository
                )
                ProfileViewModel(model = model)
            }
        }
        val mapViewModel = remember(currentUserId) {
            currentUserId?.let { userId ->
                val model = MapModel(
                    currentUserId = userId,
                    profileRepository = profileRepository,
                    mapRepository = mapRepository
                )
                MapViewModel(model = model)
            }
        }
        val authViewModel = remember { AuthViewModel() }
        val settingsViewModel = remember(currentUserId, appPrefs) {
            SettingsViewModel(
                userId = currentUserId,
                repository = repository,
                appPreferencesStore = appPrefs
            )
        }
        var placesClient by remember { mutableStateOf<PlacesClient?>(null) }

        LaunchedEffect(settingsViewModel.preferences.locationServicesEnabled) {
            if (settingsViewModel.preferences.locationServicesEnabled) {
                locationTracker.startTracking()
            } else {
                locationTracker.stopTracking()
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                locationTracker.stopTracking()
            }
        }

        LaunchedEffect(searchViewModel, context) {
            if (Places.isInitialized()) {
                val client = Places.createClient(context)
                placesClient = client
                searchViewModel.initPlaces(client)
            }
        }

        LaunchedEffect(authService) {
            authService.getCurrentUser().onSuccess { user ->
                isAuthenticated = user != null
                currentUserId = user?.id
            }
        }

        if (!isAuthenticated) {
            AuthScreen(
                viewModel = authViewModel,
                onSignIn = { email, password ->
                    coroutineScope.launch {
                        authService.signIn(email, password)
                            .onSuccess { user ->
                                authError = null
                                currentUserId = user.id
                                isAuthenticated = true
                            }
                            .onFailure { error ->
                                authError = error.message
                            }
                    }
                },
                onSignUp = { username, email, password ->
                    coroutineScope.launch {
                        authService.signUp(email, password, username)
                            .onSuccess { user ->
                                authError = null
                                currentUserId = user.id
                                isAuthenticated = true
                            }
                            .onFailure { error ->
                                authError = error.message
                            }
                    }
                },
                onForgotPassword = {
                    // TODO: Implement forgot password
                }
            )
            if (!authError.isNullOrBlank()) {
                Text(
                    text = authError ?: "Authentication failed",
                    color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        } else {
            if (showSettings) {
                // Settings screen is accessed from Profile, not from bottom navigation
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(Modifier.padding(innerPadding)) {
                        val uid = currentUserId
                        if (uid != null) {
                            SettingsFlow(
                                viewModel = settingsViewModel,
                                authService = authService,
                                profileViewModel = profileViewModel,
                                currentUserId = uid,
                                onBackClick = { showSettings = false },
                                onLogoutClick = {
                                    coroutineScope.launch {
                                        authService.signOut()
                                        currentUserId = null
                                        isAuthenticated = false
                                        showSettings = false
                                        currentDestination = AppDestinations.MAP
                                        authError = null
                                    }
                                }
                            )
                        } else {
                            Text("Loading account…")
                        }
                    }
                }
            } else {
                NavigationSuiteScaffold(
                    navigationSuiteItems = {
                        AppDestinations.entries.forEach {
                            item(
                                icon = {
                                    Icon(
                                        it.icon,
                                        contentDescription = it.label
                                    )
                                },
                                label = { Text(it.label) },
                                selected = it == currentDestination,
                                onClick = { currentDestination = it }
                            )
                        }
                    }
                ) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        Box(Modifier.padding(innerPadding)) {
                            when (currentDestination) {
                                AppDestinations.SEARCH -> SearchScreen(
                                    viewModel = searchViewModel,
                                    locationServicesEnabled = settingsViewModel.preferences.locationServicesEnabled,
                                    locationTracker = locationTracker
                                )

                                AppDestinations.EXPLORE -> {
                                    if (exploreViewModel != null) {
                                        ExploreScreen(viewModel = exploreViewModel)
                                    } else {
                                        Text("Loading user data...")
                                    }
                                }

                                AppDestinations.STATS -> StatsScreen(viewModel = statsViewModel)
                                AppDestinations.PROFILE -> {
                                    if (profileViewModel != null) {
                                        ProfileScreen(
                                            viewModel = profileViewModel,
                                            onSettingsClick = { showSettings = true }
                                        )
                                    } else {
                                        Text("Loading profile...")
                                    }
                                }

                                AppDestinations.MAP -> MapScreen(
                                    selectedPlaceId = selectedMapPlaceId,
                                    placesClient = placesClient,
                                    viewModel = mapViewModel,
                                    locationServicesEnabled = settingsViewModel.preferences.locationServicesEnabled,
                                    locationTracker = locationTracker
                                ) {
                                    selectedMapPlaceId = null
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// TODO: Update placeholder icons
enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    MAP("Map", Icons.Default.Home),
    EXPLORE("Explore", Icons.Default.LocationOn),
    SEARCH("Search", Icons.Default.Search),
    STATS("Stats", Icons.Default.Share),
    PROFILE("Profile", Icons.Default.Face)
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Gotouchgrass",
        modifier = modifier
    )
}
