package com.cotf.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cotf.core.engine.GameEngine
import com.cotf.core.engine.GameStats
import com.cotf.core.engine.ScoreCalculator
import com.cotf.network.LeaderboardApi
import com.cotf.network.dto.SubmitGameResultRequest
import com.cotf.session.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Состояние отправки результата партии на сервер.
 */
sealed interface SubmitState {
    data object Idle : SubmitState
    data object Skipped : SubmitState   // пользователь не залогинен — отправлять некуда
    data object Submitting : SubmitState
    data class Success(val score: Int) : SubmitState
    data class Error(val message: String) : SubmitState
}

class GameViewModel(
    private val leaderboardApi: LeaderboardApi,
    private val userSession: UserSession
) : ViewModel() {

    val engine: GameEngine = GameEngine()

    private val _submitState = MutableStateFlow<SubmitState>(SubmitState.Idle)
    val submitState: StateFlow<SubmitState> = _submitState.asStateFlow()

    private var lastSubmittedStats: GameStats? = null

    fun startGame() { engine.start() }
    fun pauseGame() { engine.pause() }
    fun resumeGame() { engine.resume() }

    fun exitGame() {
        engine.pause()
        engine.reset()
        _submitState.value = SubmitState.Idle
        lastSubmittedStats = null
    }

    fun requestAttack() { engine.requestAttack() }
    fun requestHarvest() { engine.requestHarvest() }
    fun requestConsumeBerry() { engine.requestConsumeBerry() }

    /**
     * Отправляет результат партии. Идемпотентен в рамках одной партии:
     * повторный вызов с теми же stats — no-op (для случая ретрая используется retrySubmit).
     */
    fun submitResultIfNeeded(stats: GameStats) {
        if (lastSubmittedStats == stats && _submitState.value is SubmitState.Success) return
        if (_submitState.value is SubmitState.Submitting) return
        if (!userSession.isLoggedIn()) {
            _submitState.value = SubmitState.Skipped
            return
        }
        sendSubmit(stats)
    }

    fun retrySubmit(stats: GameStats) {
        sendSubmit(stats)
    }

    private fun sendSubmit(stats: GameStats) {
        lastSubmittedStats = stats
        _submitState.value = SubmitState.Submitting
        viewModelScope.launch {
            val request = SubmitGameResultRequest(
                survivedSeconds = stats.survivedSeconds.toInt(),
                mobsKilled = stats.mobsKilled,
                resourcesGathered = stats.resourcesGathered,
                daysSurvived = stats.daysSurvived
            )
            _submitState.value = try {
                val response = leaderboardApi.submit(request)
                if (response.isSuccessful) {
                    val score = response.body()?.score ?: ScoreCalculator.calculate(stats)
                    SubmitState.Success(score)
                } else {
                    SubmitState.Error("Server error (${response.code()})")
                }
            } catch (e: Exception) {
                SubmitState.Error(e.message ?: "Connection error")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        engine.destroy()
    }

    class Factory(
        private val leaderboardApi: LeaderboardApi,
        private val userSession: UserSession
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == GameViewModel::class.java) {
                "Unknown ViewModel class: $modelClass"
            }
            return GameViewModel(leaderboardApi, userSession) as T
        }
    }
}
