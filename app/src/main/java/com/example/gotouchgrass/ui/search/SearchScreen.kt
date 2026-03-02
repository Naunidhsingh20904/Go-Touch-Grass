package com.example.gotouchgrass.ui.search

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gotouchgrass.ui.theme.GoTouchGrassDimens
import com.example.gotouchgrass.ui.theme.GoTouchGrassTheme
import com.example.gotouchgrass.R
import com.google.android.gms.location.LocationServices
import androidx.core.content.ContextCompat
import com.example.gotouchgrass.ui.theme.ForestGreen
import com.example.gotouchgrass.ui.theme.SandLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: SearchViewModel) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    LaunchedEffect(viewModel, context) {
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFineLocationPermission) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    viewModel.updateCurrentLocation(location.latitude, location.longitude)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = GoTouchGrassDimens.SpacingMd)
                    .padding(
                        top = GoTouchGrassDimens.SpacingMd,
                        bottom = GoTouchGrassDimens.SpacingSm
                    )
            ) {

                // Header (Search + XP)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Search",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(GoTouchGrassDimens.SpacingSm))

                SearchBar(
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = viewModel.query,
                            onQueryChange = viewModel::onQueryChange,
                            onSearch = { viewModel.onSearch() },
                            expanded = false,
                            onExpandedChange = { },
                            placeholder = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = ForestGreen
                                    )
                                    Text("Search zones, landmarks, areas…")
                                }
                            }
                        )
                    },
                    expanded = false,
                    onExpandedChange = { },
                    modifier = Modifier.fillMaxWidth()
                ) { }
            }
        }

    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = GoTouchGrassDimens.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingMd),
            contentPadding = PaddingValues(bottom = GoTouchGrassDimens.SpacingMd)
        ) {
            val isQueryActive = viewModel.query.isNotBlank()

            if (viewModel.isSearching) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            viewModel.searchError?.let { error ->
                item {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (viewModel.searchResults.isNotEmpty()) {
                item {
                    Text(
                        text = "Search Results",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                items(
                    items = viewModel.searchResults,
                    key = { it.id }
                ) { cardData ->
                    LocationCard(card = cardData, onClick = {})
                }
            }

            if (!isQueryActive) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingSm)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingSm)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.history_24),
                                contentDescription = "History",
                                tint = ForestGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Recent Searches",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingSm)) {
                            viewModel.recentSearches.forEach { search ->
                                Surface(
                                    onClick = { viewModel.onQueryChange(search) },
                                    shape = RoundedCornerShape(GoTouchGrassDimens.RadiusFull),
                                    color = SandLight
                                ) {
                                    Text(
                                        text = search,
                                        modifier = Modifier.padding(
                                            horizontal = GoTouchGrassDimens.SpacingSm,
                                            vertical = GoTouchGrassDimens.SpacingXs
                                        ),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingSm)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.trending_up_24),
                            contentDescription = "Trending",
                            tint = ForestGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Trending Now",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                items(viewModel.trendingLocations, key = { it.id }) { cardData ->
                    LocationCard(card = cardData, onClick = {})
                }

                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingSm)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.location_on_24),
                            contentDescription = "Nearby",
                            tint = ForestGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Nearby Zones",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                items(viewModel.nearbyLocations, key = { it.id }) { cardData ->
                    LocationCard(card = cardData, onClick = {})
                }
            }
        }
    }
}

@Composable
fun LocationCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    card: LocationCardData
) {
    ElevatedCard(
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(GoTouchGrassDimens.SpacingMd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Location",
                tint = ForestGreen,
                modifier = Modifier.size(32.dp)
            )

            Spacer(Modifier.width(GoTouchGrassDimens.SpacingMd))

            Column(verticalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingXs)) {
                Text(
                    text = card.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingSm)) {
                    Text(
                        text = card.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = card.rarity.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = card.rarity.color()
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview() {
    GoTouchGrassTheme {
        SearchScreen(viewModel = SearchViewModel())
    }
}