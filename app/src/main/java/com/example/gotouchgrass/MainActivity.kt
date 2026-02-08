package com.example.gotouchgrass

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.example.gotouchgrass.ui.screens.AuthScreen
import com.example.gotouchgrass.ui.theme.GoTouchGrassTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GoTouchGrassTheme {
                GoTouchGrassApp()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun GoTouchGrassApp() {
    var isAuthenticated by rememberSaveable { mutableStateOf(false) }
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    if (!isAuthenticated) {
        AuthScreen(
            onSignIn = { email, password ->
                // TODO: Implement actual authentication
                isAuthenticated = true
            },
            onSignUp = { username, email, password ->
                // TODO: Implement actual sign up
                isAuthenticated = true
            },
            onForgotPassword = {
                // TODO: Implement forgot password
            }
        )
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
                Greeting(
                    name = "Android",
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Default.Home),
    FAVORITES("Favorites", Icons.Default.Favorite),
    PROFILE("Profile", Icons.Default.AccountBox),
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GoTouchGrassTheme {
        Greeting("Android")
    }
}