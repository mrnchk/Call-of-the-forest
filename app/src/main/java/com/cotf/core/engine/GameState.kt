package com.cotf.core.engine

/**
 * Единое immutable состояние игры.
 * Все данные — здесь. Никаких отдельных переменных.
 *
 * Правила:
 * 1. Только data class — никаких var, только val
 * 2. Изменение через .copy()
 * 3. Один источник истины
 * 4. Никакой логики — только данные
 */
data class GameState(
    val player: Player = Player(),
    val objects: List<MapObject> = emptyList(),
    val mobs: List<Mob> = emptyList(),
    val camera: Camera = Camera(),
    val timeOfDay: Float = 0.3f,       // 0=полночь, 0.5=полдень
    val isRunning: Boolean = true
)

data class Player(
    val x: Float = 400f,
    val y: Float = 400f,
    val speed: Float = 200f,           // пикселей в секунду
    val radius: Float = 16f,
    val hp: Int = 100,
    val direction: Float = 0f          // радианы
)

data class Camera(
    val x: Float = 0f,
    val y: Float = 0f
)

data class MapObject(
    val id: String,
    val type: ObjectType,
    val x: Float,
    val y: Float,
    val radius: Float = 24f
)

enum class ObjectType { TREE, ROCK, BUSH }

data class Mob(
    val id: String,
    val x: Float,
    val y: Float,
    val radius: Float = 14f,
    val hp: Int = 50,
    val type: MobType = MobType.IDLE
)

enum class MobType { IDLE }  // пока только один тип — стоит на месте
