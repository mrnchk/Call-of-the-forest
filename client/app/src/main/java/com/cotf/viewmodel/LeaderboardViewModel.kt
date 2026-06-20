package com.cotf.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cotf.network.LeaderboardApi
import com.cotf.network.dto.LeaderboardEntryDto
import com.cotf.network.dto.MyLeaderboardDto
import com.cotf.session.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface TopState {
    data object Loading : TopState
    data class Success(val entries: List<LeaderboardEntryDto>) : TopState
    data class Error(val message: String) : TopState
}

sealed interface MeState {
    data object Loading : MeState
    data object Anonymous : MeState
    data class Success(val payload: MyLeaderboardDto) : MeState
    data class Error(val message: String) : MeState
}

class LeaderboardViewModel(
    private val api: LeaderboardApi,
    private val userSession: UserSession
) : ViewModel() {

    private val _topState = MutableStateFlow<TopState>(TopState.Loading)
    val topState: StateFlow<TopState> = _topState.asStateFlow()

    private val _meState = MutableStateFlow<MeState>(MeState.Loading)
    val meState: StateFlow<MeState> = _meState.asStateFlow()

    init {
        loadTop()
        loadMe()
    }

    fun loadTop() {
        _topState.value = TopState.Loading
        viewModelScope.launch {
            _topState.value = try {
                val response = api.top()
                if (response.isSuccessful) {
                    TopState.Success(response.body().orEmpty())
                } else {
                    TopState.Error("Server error (${response.code()})")
                }
            } catch (e: Exception) {
                TopState.Error(e.message ?: "Connection error")
            }
        }
    }

    fun loadMe() {
        if (!userSession.isLoggedIn()) {
            _meState.value = MeState.Anonymous
            return
        }
        _meState.value = MeState.Loading
        viewModelScope.launch {
            _meState.value = try {
                val response = api.me()
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    MeState.Success(body)
                } else {
                    MeState.Error("Server error (${response.code()})")
                }
            } catch (e: Exception) {
                MeState.Error(e.message ?: "Connection error")
            }
        }
    }

    class Factory(
        private val api: LeaderboardApi,
        private val userSession: UserSession
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == LeaderboardViewModel::class.java) {
                "Unknown ViewModel class: $modelClass"
            }
            return LeaderboardViewModel(api, userSession) as T
        }
    }
}
