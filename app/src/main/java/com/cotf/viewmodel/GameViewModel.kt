package com.cotf.viewmodel

import androidx.lifecycle.ViewModel
import com.cotf.core.engine.GameEngine

class GameViewModel : ViewModel() {
    val engine: GameEngine = GameEngine()

    fun startGame() { engine.start() }
    fun pauseGame() { engine.pause() }
    fun resumeGame() { engine.resume() }
    fun exitGame() { engine.pause(); engine.reset() }

    fun requestAttack() { engine.requestAttack() }
    fun requestHarvest() { engine.requestHarvest() }
    fun requestConsumeBerry() { engine.requestConsumeBerry() }

    override fun onCleared() {
        super.onCleared()
        engine.destroy()
    }
}
