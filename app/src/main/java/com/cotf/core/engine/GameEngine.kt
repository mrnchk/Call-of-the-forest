package com.cotf.core.engine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Игровой движок — цикл ~60 FPS в корутине.
 * Каждый кадр: ввод → обновление → emit.
 */
class GameEngine {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    // Ввод от игрока (джойстик)
    private var inputX = 0f  // -1..1
    private var inputY = 0f  // -1..1

    fun setInput(x: Float, y: Float) {
        inputX = x.coerceIn(-1f, 1f)
        inputY = y.coerceIn(-1f, 1f)
    }

    // Карта объектов (создаём один раз)
    private val mapObjects = generateMap()

    init {
        _state.value = GameState(objects = mapObjects)
        startLoop()
    }

    private fun startLoop() {
        scope.launch {
            var lastTime = System.nanoTime()

            while (true) {
                val now = System.nanoTime()
                val deltaSec = (now - lastTime) / 1_000_000_000f
                lastTime = now

                // Обновляем состояние
                val newState = update(_state.value, deltaSec)
                _state.value = newState

                // ~60 FPS
                val elapsedMs = (System.nanoTime() - now) / 1_000_000
                delay(maxOf(1, 16 - elapsedMs))
            }
        }
    }

    private fun update(state: GameState, deltaSec: Float): GameState {
        // 1. Двигаем игрока
        val dx = inputX * state.player.speed * deltaSec
        val dy = inputY * state.player.speed * deltaSec

        val newPlayer = state.player.copy(
            x = state.player.x + dx,
            y = state.player.y + dy,
            direction = if (inputX != 0f || inputY != 0f)
                atan2(inputY, inputX)
            else state.player.direction
        )

        // 2. Простая коллизия — отталкиваем от объектов
        var px = newPlayer.x
        var py = newPlayer.y
        for (obj in state.objects) {
            val distX = px - obj.x
            val distY = py - obj.y
            val dist = sqrt(distX * distX + distY * distY)
            val minDist = newPlayer.radius + obj.radius
            if (dist < minDist && dist > 0f) {
                val overlap = minDist - dist
                px += (distX / dist) * overlap
                py += (distY / dist) * overlap
            }
        }

        // 3. Камера следует за игроком
        val newCamera = Camera(x = px, y = py)

        // 4. Время суток (медленно меняется)
        val newTime = (state.timeOfDay + 0.0001f * deltaSec * 60) % 1f

        return state.copy(
            player = newPlayer.copy(x = px, y = py),
            camera = newCamera,
            timeOfDay = newTime
        )
    }

    private fun generateMap(): List<MapObject> {
        val objects = mutableListOf<MapObject>()
        // Разбрасываем деревья и камни
        repeat(40) { i ->
            objects.add(MapObject("tree_$i", ObjectType.TREE,
                x = (Math.random() * 2000 - 1000).toFloat(),
                y = (Math.random() * 2000 - 1000).toFloat()
            ))
        }
        repeat(20) { i ->
            objects.add(MapObject("rock_$i", ObjectType.ROCK,
                x = (Math.random() * 2000 - 1000).toFloat(),
                y = (Math.random() * 2000 - 1000).toFloat(),
                radius = 16f
            ))
        }
        repeat(15) { i ->
            objects.add(MapObject("bush_$i", ObjectType.BUSH,
                x = (Math.random() * 2000 - 1000).toFloat(),
                y = (Math.random() * 2000 - 1000).toFloat(),
                radius = 12f
            ))
        }
        return objects
    }
}
