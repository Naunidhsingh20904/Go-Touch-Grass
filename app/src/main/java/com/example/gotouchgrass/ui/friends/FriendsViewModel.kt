package com.example.gotouchgrass.ui.friends

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gotouchgrass.data.GoTouchGrassRepository
import com.example.gotouchgrass.domain.User
import com.example.gotouchgrass.data.supabase.FriendRequestRow
import kotlinx.coroutines.launch


class FriendsViewModel(
    private val repository: GoTouchGrassRepository,
    private val currentAuthUserId: String
) : ViewModel() {

    var searchQuery by mutableStateOf("")
        private set

    var searchResults by mutableStateOf<List<User>>(emptyList())
        private set

    var incomingRequests by mutableStateOf<List<Pair<FriendRequestRow, User>>>(emptyList())
        private set

    var outgoingRequests by mutableStateOf<List<Pair<FriendRequestRow, User>>>(emptyList())
        private set

    var friends by mutableStateOf<List<User>>(emptyList())
        private set

    var friendsLeaderboard by mutableStateOf<List<User>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadAllData()
        performSearch("")
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query
        performSearch(query)
    }

    fun performSearch(query: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val result = repository.searchUsers(query.trim(), limit = 20)
            result.onSuccess { users ->
                searchResults = users.filter { it.id != currentAuthUserId } // exclude self
            }.onFailure { error ->
                errorMessage = error.message ?: "Search failed"
                searchResults = emptyList()
            }

            isLoading = false
        }
    }

    fun sendFriendRequest(recipientAuthUserId: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val result = repository.sendFriendRequest(
                currentAuthUserId,
                recipientAuthUserId
            )
            result.onSuccess {
                loadOutgoingRequests()
                performSearch(searchQuery)
            }.onFailure { error ->
                errorMessage = error.message ?: "Failed to send request"
            }

            isLoading = false
        }
    }

    fun acceptFriendRequest(requestId: Long) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val result = repository.acceptFriendRequest(requestId)
            result.onSuccess {
                loadIncomingRequests()
                loadFriends()
            }.onFailure { error ->
                errorMessage = error.message ?: "Failed to accept request"
            }

            isLoading = false
        }
    }

    fun declineFriendRequest(requestId: Long) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val result = repository.declineFriendRequest(requestId)
            result.onSuccess {
                loadIncomingRequests()
            }.onFailure { error ->
                errorMessage = error.message ?: "Failed to decline request"
            }

            isLoading = false
        }
    }

    fun cancelFriendRequest(requestId: Long) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val result = repository.cancelFriendRequest(requestId)
            result.onSuccess {
                loadOutgoingRequests()
            }.onFailure { error ->
                errorMessage = error.message ?: "Failed to cancel request"
            }

            isLoading = false
        }
    }

    fun removeFriend(friendAuthUserId: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val result = repository.removeFriend(currentAuthUserId, friendAuthUserId)
            result.onSuccess {
                loadFriends()
                loadFriendsLeaderboard()
            }.onFailure { error ->
                errorMessage = error.message ?: "Failed to remove friend"
            }

            isLoading = false
        }
    }

    fun isOutgoingRequestPending(userAuthId: String): Boolean {
        return outgoingRequests.any { (_, user) -> user.id == userAuthId }
    }

    fun isIncomingRequestPending(userAuthId: String): Boolean {
        return incomingRequests.any { (_, user) -> user.id == userAuthId }
    }

    fun isAlreadyFriend(userAuthId: String): Boolean {
        return friends.any { it.id == userAuthId }
    }

    private fun loadAllData() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            runCatching {
                loadIncomingRequests()
                loadOutgoingRequests()
                loadFriends()
                loadFriendsLeaderboard()
            }.onFailure { error ->
                errorMessage = error.message ?: "Failed to load friends data"
            }

            isLoading = false
        }
    }

    private suspend fun loadIncomingRequests() {
        val result = repository.getIncomingFriendRequests(currentAuthUserId)
        result.onSuccess { requests ->
            incomingRequests = requests
        }.onFailure { error ->
            errorMessage = error.message ?: "Failed to load incoming requests"
        }
    }

    private suspend fun loadOutgoingRequests() {
        val result = repository.getOutgoingFriendRequests(currentAuthUserId)
        result.onSuccess { requests ->
            outgoingRequests = requests
        }.onFailure { error ->
            errorMessage = error.message ?: "Failed to load outgoing requests"
        }
    }

    private suspend fun loadFriends() {
        val result = repository.getFriends(currentAuthUserId)
        result.onSuccess { friendsList ->
            friends = friendsList
        }.onFailure { error ->
            errorMessage = error.message ?: "Failed to load friends"
        }
    }

    private suspend fun loadFriendsLeaderboard() {
        val result = repository.getFriendsLeaderboard(currentAuthUserId)
        result.onSuccess { leaderboard ->
            friendsLeaderboard = leaderboard
        }.onFailure { error ->
            errorMessage = error.message ?: "Failed to load friends leaderboard"
        }
    }
}
