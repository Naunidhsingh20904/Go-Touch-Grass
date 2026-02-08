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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.example.gotouchgrass.ui.theme.GoTouchGrassTheme

import com.example.gotouchgrass.ui.search.SearchScreen
import com.example.gotouchgrass.ui.search.SearchViewModel

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

@Preview(
    name = "Phone",
    showBackground = true,
    widthDp = 360,
    heightDp = 800
)
@Composable
fun GoTouchGrassAppPreview() {
    GoTouchGrassTheme {
        GoTouchGrassApp()
    }
}

@Composable
fun GoTouchGrassApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.SEARCH) }

    val searchViewModel = remember { SearchViewModel() }

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
                    AppDestinations.SEARCH -> SearchScreen(viewModel = searchViewModel)
                    AppDestinations.EXPLORE -> Text("Explore TODO")
                    AppDestinations.STATS -> Text("Stats TODO")
                    AppDestinations.PROFILE -> Text("Profile TODO")
                    AppDestinations.MAP -> Text("Map TODO")
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
    PROFILE("Profile", Icons.Default.Face),
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}