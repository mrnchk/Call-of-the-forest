package com.cotf.core.engine

/**
 * Single immutable game state.
 * All data lives here. No separate variables.
 */

data class GameState(
    val player: Player = Player(),
    val objects: List<MapObject> = emptyList(),
    val mobs: List<Mob> = emptyList(),
    val camera: Camera = Camera(),
    val timeOfDay: Float = 0.3f,       // 0=midnight, 0.5=noon
    val isRunning: Boolean = true,
    val isGameOver: Boolean = false,
    val attackEffects: List<AttackEffect> = emptyList(),
    val stats: GameStats = GameStats()
)

data class Player(
    val x: Float = 400f,
    val y: Float = 400f,
    val speed: Float = 200f,           // pixels per second
    val radius: Float = 16f,
    val hp: Int = 100,
    val maxHp: Int = 100,
    val direction: Float = 0f,         // radians
    val hunger: Float = 100f,          // 0..100
    val attackCooldown: Float = 0f,    // seconds until next attack
    val inventory: Map<ResourceType, Int> = emptyMap(),
    val hungerDmgAccum: Float = 0f,    // hunger damage accumulator
    val coldDmgAccum: Float = 0f,      // cold damage accumulator
    val isMoving: Boolean = false,     // true when player is moving (for animation)
    /** Death animation timer (sec). -1 = alive, >0 = dying, 0 = game over. */
    val deathAnimTime: Float = -1f
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
    val speed: Float = 100f,
    /** Death animation timer (sec). >0 = dying, 0 = remove, -1 = alive. */
    val deathAnimTime: Float = -1f
)

/** Predators (attack): WOLF, BEAR, FOX. Prey (flee): DEER, RABBIT, BIRD. */
enum class MobType { WOLF, BEAR, FOX, DEER, RABBIT, BIRD }

val MobType.isPredator: Boolean
    get() = this == MobType.WOLF || this == MobType.BEAR || this == MobType.FOX

enum class BehaviorState { IDLE, PATROL, CHASE, ATTACK, FLEE, DYING }

enum class ResourceType { WOOD, STONE, BERRY, MEAT }

data class AttackEffect(
    val x: Float,
    val y: Float,
    val radius: Float,
    val remainingTime: Float = 0.15f
)

// ── Mob stats ──────────────────────────────────────────────────────────────

/** Duration of mob death animation in seconds. */
const val DEATH_ANIM_DURATION = 0.8f

/** Duration of player death animation in seconds. */
const val PLAYER_DEATH_ANIM_DURATION = 1.2f

val MobType.baseSpeed: Float
    get() = when (this) {
        MobType.WOLF   -> 140f
        MobType.BEAR   -> 110f
        MobType.FOX    -> 160f
        MobType.DEER   -> 130f
        MobType.RABBIT -> 170f
        MobType.BIRD   -> 90f   // drifts slowly
    }

val MobType.baseMaxHp: Int
    get() = when (this) {
        MobType.WOLF   -> 60
        MobType.BEAR   -> 120
        MobType.FOX    -> 35
        MobType.DEER   -> 30
        MobType.RABBIT -> 15
        MobType.BIRD   -> 10
    }

/** Aggro range (for predators — detection range; for prey — panic range). */
val MobType.aggroRange: Float
    get() = when (this) {
        MobType.WOLF   -> 200f
        MobType.BEAR   -> 150f
        MobType.FOX    -> 220f
        MobType.DEER   -> 180f
        MobType.RABBIT -> 200f
        MobType.BIRD   -> 250f
    }

val MobType.attackRange: Float
    get() = when (this) {
        MobType.WOLF   -> 40f
        MobType.BEAR   -> 50f
        MobType.FOX    -> 35f
        MobType.DEER   -> 0f
        MobType.RABBIT -> 0f
        MobType.BIRD   -> 0f
    }

val MobType.attackDamage: Int
    get() = when (this) {
        MobType.WOLF   -> 12
        MobType.BEAR   -> 20
        MobType.FOX    -> 8
        MobType.DEER   -> 0
        MobType.RABBIT -> 0
        MobType.BIRD   -> 0
    }

/** Probability of meat drop on kill (0.0..1.0). */
val MobType.meatDropChance: Float
    get() = when (this) {
        MobType.WOLF   -> 0.6f
        MobType.BEAR   -> 0.8f
        MobType.FOX    -> 0.5f
        MobType.DEER   -> 0.9f
        MobType.RABBIT -> 0.7f
        MobType.BIRD   -> 0.4f
    }
