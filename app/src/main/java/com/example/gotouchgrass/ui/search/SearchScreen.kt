package com.example.gotouchgrass.ui.search

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gotouchgrass.ui.theme.GoTouchGrassDimens
import com.example.gotouchgrass.ui.theme.GoTouchGrassTheme
import com.example.gotouchgrass.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: SearchViewModel) {
    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                SearchBar(
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = viewModel.query,
                            onQueryChange = viewModel::onQueryChange,
                            onSearch = { viewModel.onSearch() },
                            expanded = false,
                            onExpandedChange = { },
                            placeholder = {
                                Row() {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = MaterialTheme.colorScheme.primary,
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
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // Display recent searches
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.history_24),
                        contentDescription = "History",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "Recent Searches",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    viewModel.recentSearches.forEach { search ->
                        Card(onClick = {}) {
                            Text(
                                text = search,
                                modifier = Modifier.padding(
                                    horizontal = GoTouchGrassDimens.SpacingSm,
                                    vertical = GoTouchGrassDimens.SpacingXs
                                ),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }

            // Display trending locations
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.trending_up_24),
                        contentDescription = "Trending",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "Trending Now",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                viewModel.trendingLocations.forEach { cardData ->
                    LocationCard(
                        card = cardData,
                        onClick = {})
                }
            }

            // Display nearby locations
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.location_on_24),
                        contentDescription = "Nearby",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "Nearby Zones",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                viewModel.nearbyLocations.forEach { cardData ->
                    LocationCard(
                        card = cardData,
                        onClick = {})
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
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Location",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )

            Spacer(Modifier.width(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = card.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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