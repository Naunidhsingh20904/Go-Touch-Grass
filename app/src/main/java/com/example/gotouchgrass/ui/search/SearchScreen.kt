package com.example.gotouchgrass.ui.search

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextOverflow
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
fun SearchScreen(
    viewModel: SearchViewModel,
    locationServicesEnabled: Boolean = true
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val isQueryActive = viewModel.query.isNotBlank()

    LaunchedEffect(viewModel, context, locationServicesEnabled) {
        if (!locationServicesEnabled) return@LaunchedEffect
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
                    horizontalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingSm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isQueryActive) {
                        IconButton(onClick = { viewModel.onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }

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
                itemsIndexed(
                    items = viewModel.searchResults,
                    key = { index, item -> "search_${item.id}_$index" }
                ) { _, cardData ->
                    LocationCard(
                        card = cardData,
                        onClick = { viewModel.onSearchResultSelected(cardData) }
                    )
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
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingSm)) {
                            items(
                                items = viewModel.recentSearches,
                                key = { search -> search }
                            ) { search ->
                                Surface(
                                    onClick = { viewModel.onRecentSearchSelected(search) },
                                    shape = RoundedCornerShape(GoTouchGrassDimens.RadiusFull),
                                    color = SandLight
                                ) {
                                    Text(
                                        text = search,
                                        modifier = Modifier
                                            .widthIn(max = 140.dp)
                                            .padding(
                                                horizontal = GoTouchGrassDimens.SpacingSm,
                                                vertical = GoTouchGrassDimens.SpacingXs
                                            ),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
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
                itemsIndexed(
                    items = viewModel.trendingLocations,
                    key = { index, item -> "trending_${item.id}_$index" }
                ) { _, cardData ->
                    LocationCard(
                        card = cardData,
                        onClick = { viewModel.onTrendingSelected(cardData) }
                    )
                }

            }
        }
    }
}

@Composable
fun LocationCard(
    modifier: Modifier = Modifier,
    card: LocationCardData,
    onClick: (() -> Unit)? = null
) {
    ElevatedCard(
        onClick = { onClick?.invoke() },
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

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingXs)
            ) {
                Text(
                    text = card.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(GoTouchGrassDimens.SpacingSm),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = card.description,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = card.rarity.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = card.rarity.color(),
                        maxLines = 1
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