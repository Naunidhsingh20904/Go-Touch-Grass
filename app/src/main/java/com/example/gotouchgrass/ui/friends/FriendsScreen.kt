package com.example.gotouchgrass.ui.friends

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gotouchgrass.domain.User

private enum class FriendsTab(val title: String) {
    SEARCH("Search"),
    REQUESTS("Requests"),
    FRIENDS("Friends")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    viewModel: FriendsViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = FriendsTab.entries

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Friends",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        if (viewModel.isLoading) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        }

        viewModel.errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(tab.title) }
                )
            }
        }

        when (tabs[selectedTabIndex]) {
            FriendsTab.SEARCH -> SearchTab(viewModel)
            FriendsTab.REQUESTS -> RequestsTab(viewModel)
            FriendsTab.FRIENDS -> FriendsTabContent(viewModel)
        }
    }
}

@Composable
private fun SearchTab(viewModel: FriendsViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = viewModel.searchQuery,
            onValueChange = viewModel::updateSearchQuery,
            label = { Text("Search by username or display name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Text(
            text = "Results",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(viewModel.searchResults, key = { it.id }) { user ->
                val isFriend = viewModel.isAlreadyFriend(user.id)
                val isOutgoingPending = viewModel.isOutgoingRequestPending(user.id)
                val isIncomingPending = viewModel.isIncomingRequestPending(user.id)

                val actionLabel = when {
                    isFriend -> "Friends"
                    isOutgoingPending || isIncomingPending -> "Pending"
                    else -> "Add"
                }

                val actionEnabled = !(isFriend || isOutgoingPending || isIncomingPending)

                UserCard(
                    user = user,
                    actionLabel = actionLabel,
                    actionEnabled = actionEnabled,
                    onAction = { if (actionEnabled) viewModel.sendFriendRequest(user.id) }
                )
            }
        }
    }
}

@Composable
private fun RequestsTab(viewModel: FriendsViewModel) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text(
                text = "Incoming Requests",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (viewModel.incomingRequests.isEmpty()) {
            item { Text("No incoming requests") }
        } else {
            items(viewModel.incomingRequests, key = { it.first.id }) { pair ->
                val request = pair.first
                val user = pair.second
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = user.displayName, style = MaterialTheme.typography.titleSmall)
                        Text(text = "@${user.username}", style = MaterialTheme.typography.bodySmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { viewModel.acceptFriendRequest(request.id) }) {
                                Text("Accept")
                            }
                            OutlinedButton(onClick = { viewModel.declineFriendRequest(request.id) }) {
                                Text("Decline")
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            Text(
                text = "Outgoing Requests",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (viewModel.outgoingRequests.isEmpty()) {
            item { Text("No outgoing requests") }
        } else {
            items(viewModel.outgoingRequests, key = { it.first.id }) { pair ->
                val request = pair.first
                val user = pair.second
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = user.displayName, style = MaterialTheme.typography.titleSmall)
                        Text(text = "@${user.username}", style = MaterialTheme.typography.bodySmall)
                        OutlinedButton(onClick = { viewModel.cancelFriendRequest(request.id) }) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendsTabContent(viewModel: FriendsViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Current Friends",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        if (viewModel.friends.isEmpty()) {
            Text("No friends yet")
            return
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(viewModel.friends, key = { it.id }) { user ->
                UserCard(
                    user = user,
                    actionLabel = "Remove",
                    actionEnabled = true,
                    onAction = { viewModel.removeFriend(user.id) }
                )
            }
        }
    }
}

@Composable
private fun UserCard(
    user: User,
    actionLabel: String,
    actionEnabled: Boolean,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = user.displayName, style = MaterialTheme.typography.titleSmall)
                Text(text = "@${user.username}", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "Level ${user.level} • ${user.xpTotal} XP",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            OutlinedButton(onClick = onAction, enabled = actionEnabled) {
                Text(actionLabel)
            }
        }
    }
}
