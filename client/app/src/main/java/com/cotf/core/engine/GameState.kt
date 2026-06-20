package com.cotf.core.engine

/**
 * Единое immutable состояние игры.
 * Все данные — здесь. Никаких отдельных переменных.
 */

data class GameState(
    val player: Player = Player(),
    val objects: List<MapObject> = emptyList(),
    val mobs: List<Mob> = emptyList(),
    val camera: Camera = Camera(),
    val timeOfDay: Float = 0.3f,       // 0=полночь, 0.5=полдень
    val isRunning: Boolean = true,
    val isGameOver: Boolean = false,
    val attackEffects: List<AttackEffect> = emptyList(),
    val stats: GameStats = GameStats()
)

data class Player(
    val x: Float = 400f,
    val y: Float = 400f,
    val speed: Float = 200f,           // пикселей в секунду
    val radius: Float = 16f,
    val hp: Int = 100,
    val maxHp: Int = 100,
    val direction: Float = 0f,         // радианы
    val hunger: Float = 100f,          // 0..100
    val attackCooldown: Float = 0f,    // секунды до следующей атаки
    val inventory: Map<ResourceType, Int> = emptyMap(),
    val hungerDmgAccum: Float = 0f,    // аккумулятор урона от голода
    val coldDmgAccum: Float = 0f       // аккумулятор урона от холода
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
    val radius: Float = 24f,
    val hp: Int = 3,
    val resourceType: ResourceType = ResourceType.WOOD
)

enum class ObjectType { TREE, ROCK, BUSH }

data class Mob(
    val id: String,
    val x: Float,
    val y: Float,
    val radius: Float = 14f,
    val hp: Int = 50,
    val maxHp: Int = 50,
    val type: MobType = MobType.WOLF,
    val state: BehaviorState = BehaviorState.IDLE,
    val direction: Float = 0f,
    val patrolTargetX: Float = 0f,
    val patrolTargetY: Float = 0f,
    val attackCooldown: Float = 0f,
    val speed: Float = 100f
)

enum class MobType { WOLF, DEER, GOBLIN }

enum class BehaviorState { IDLE, PATROL, CHASE, ATTACK, FLEE }

enum class ResourceType { WOOD, STONE, BERRY }

data class AttackEffect(
    val x: Float,
    val y: Float,
    val radius: Float,
    val remainingTime: Float = 0.15f
)

// Статы мобов по типу
val MobType.baseSpeed: Float
    get() = when (this) {
        MobType.WOLF -> 140f
        MobType.DEER -> 120f
        MobType.GOBLIN -> 100f
    }

val MobType.baseMaxHp: Int
    get() = when (this) {
        MobType.WOLF -> 60
        MobType.DEER -> 30
        MobType.GOBLIN -> 50
    }

val MobType.aggroRange: Float
    get() = when (this) {
        MobType.WOLF -> 200f
        MobType.DEER -> 150f
        MobType.GOBLIN -> 180f
    }

val MobType.attackRange: Float
    get() = when (this) {
        MobType.WOLF -> 40f
        MobType.DEER -> 30f
        MobType.GOBLIN -> 35f
    }

val MobType.attackDamage: Int
    get() = when (this) {
        MobType.WOLF -> 12
        MobType.DEER -> 5
        MobType.GOBLIN -> 10
    }
