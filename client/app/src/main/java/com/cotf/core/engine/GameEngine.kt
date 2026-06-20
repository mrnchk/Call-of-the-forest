package com.cotf.core.engine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Game engine — ~60 FPS loop running in a coroutine.
 * Does not start automatically — call start() explicitly.
 */
class GameEngine {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    // Player input (joystick)
    private var inputX = 0f
    private var inputY = 0f

    fun setInput(x: Float, y: Float) {
        inputX = x.coerceIn(-1f, 1f)
        inputY = y.coerceIn(-1f, 1f)
    }

    // Action input
    private var attackRequested = false
    private var harvestRequested = false
    private var consumeBerryRequested = false

    fun requestAttack() { attackRequested = true }
    fun requestHarvest() { harvestRequested = true }
    fun requestConsumeBerry() { consumeBerryRequested = true }

    private var loopJob: Job? = null
    private var isLoopStarted = false

    init {
        val (objects, mobs) = generateMap()
        _state.value = GameState(objects = objects, mobs = mobs)
    }

    fun start() {
        if (isLoopStarted) { resume(); return }
        isLoopStarted = true
        _state.value = _state.value.copy(isRunning = true)
        startLoop()
    }

    fun pause() {
        inputX = 0f; inputY = 0f
        _state.value = _state.value.copy(isRunning = false)
    }

    fun resume() {
        _state.value = _state.value.copy(isRunning = true)
    }

    fun reset() {
        inputX = 0f; inputY = 0f
        attackRequested = false; harvestRequested = false; consumeBerryRequested = false
        val (objects, mobs) = generateMap()
        _state.value = GameState(objects = objects, mobs = mobs, stats = GameStats())
    }

    fun destroy() {
        loopJob?.cancel(); loopJob = null; isLoopStarted = false
    }

    private fun startLoop() {
        loopJob = scope.launch {
            var lastTime = System.nanoTime()
            while (true) {
                if (_state.value.isRunning) {
                    val now = System.nanoTime()
                    val deltaSec = (now - lastTime) / 1_000_000_000f
                    lastTime = now
                    _state.value = update(_state.value, deltaSec)
                    val elapsedMs = (System.nanoTime() - now) / 1_000_000
                    delay(maxOf(1, 16 - elapsedMs))
                } else {
                    lastTime = System.nanoTime()
                    delay(100)
                }
            }
        }
    }

    // ==================== UPDATE PIPELINE ====================

    private fun update(state: GameState, deltaSec: Float): GameState {
        if (state.isGameOver) return state

        var s = state
        val isDying = s.player.deathAnimTime >= 0f
        if (!isDying) {
            s = updatePlayerMovement(s, deltaSec)
            s = updatePlayerAttack(s, deltaSec)
            s = updatePlayerHarvest(s)
            s = updateConsumeBerry(s)
        }
        s = updateMobAI(s, deltaSec)
        if (!isDying) s = resolveCollisions(s, deltaSec)
        if (!isDying) s = updateSurvival(s, deltaSec)
        s = s.copy(camera = Camera(x = s.player.x, y = s.player.y))
        s = advanceTime(s, deltaSec)
        s = updateEffects(s, deltaSec)
        s = tickSurvivedTime(s, deltaSec)
        s = checkGameOver(s)
        return s
    }

    private fun advanceTime(state: GameState, deltaSec: Float): GameState {
        val raw = state.timeOfDay + 0.0001f * deltaSec * 60
        val newTime = raw % 1f
        val dayPassed = raw >= 1f
        val newStats = if (dayPassed) state.stats.copy(daysSurvived = state.stats.daysSurvived + 1) else state.stats
        return state.copy(timeOfDay = newTime, stats = newStats)
    }

    private fun tickSurvivedTime(state: GameState, deltaSec: Float): GameState =
        state.copy(stats = state.stats.copy(survivedSeconds = state.stats.survivedSeconds + deltaSec))

    // ==================== 1. PLAYER MOVEMENT ====================

    private fun updatePlayerMovement(state: GameState, deltaSec: Float): GameState {
        val player = state.player
        val moving = inputX != 0f || inputY != 0f
        val dx = inputX * player.speed * deltaSec
        val dy = inputY * player.speed * deltaSec
        val newDirection = if (moving) atan2(inputY, inputX) else player.direction
        val newCooldown = maxOf(0f, player.attackCooldown - deltaSec)

        return state.copy(player = player.copy(
            x = player.x + dx,
            y = player.y + dy,
            direction = newDirection,
            attackCooldown = newCooldown,
            isMoving = moving
        ))
    }

    // ==================== 2. PLAYER ATTACK ====================

    private fun updatePlayerAttack(state: GameState, deltaSec: Float): GameState {
        if (!attackRequested || state.player.attackCooldown > 0f) {
            attackRequested = false
            return state
        }
        attackRequested = false

        val player = state.player
        val attackRange = player.radius * 2.5f
        // Attack origin — in front of the player
        val attackX = player.x + cos(player.direction) * player.radius * 1.5f
        val attackY = player.y + sin(player.direction) * player.radius * 1.5f

        // Meat drops probabilistically, max 1 per kill
        var meatGained = 0

        val updatedMobs = state.mobs.map { mob ->
            // Skip already-dying mobs
            if (mob.state == BehaviorState.DYING) return@map mob

            val dx = mob.x - attackX
            val dy = mob.y - attackY
            val dist = sqrt(dx * dx + dy * dy)

            if (dist < attackRange + mob.radius) {
                val knockbackDist = 30f
                val knockbackDx = if (dist > 0f) dx / dist * knockbackDist else 0f
                val knockbackDy = if (dist > 0f) dy / dist * knockbackDist else 0f
                val newHp = mob.hp - 25
                if (newHp <= 0) {
                    // Mob dies — trigger death animation
                    if (Math.random() < mob.type.meatDropChance) meatGained += 1
                    mob.copy(
                        hp = 0,
                        state = BehaviorState.DYING,
                        deathAnimTime = DEATH_ANIM_DURATION,
                        x = mob.x + knockbackDx,
                        y = mob.y + knockbackDy
                    )
                } else {
                    mob.copy(
                        hp = newHp,
                        x = mob.x + knockbackDx,
                        y = mob.y + knockbackDy
                    )
                }
            } else mob
        }
        val aliveMobs = updatedMobs.filter { it.hp > 0 }
        val killedByThisAttack = updatedMobs.size - aliveMobs.size

        val effect = AttackEffect(
            x = attackX, y = attackY,
            radius = attackRange * 0.6f
        )

        // Update inventory with dropped meat
        val newInventory = if (meatGained > 0) {
            val current = player.inventory.getOrDefault(ResourceType.MEAT, 0)
            player.inventory + (ResourceType.MEAT to current + meatGained)
        } else player.inventory

        return state.copy(
            player = player.copy(attackCooldown = 0.4f, inventory = newInventory),
            mobs = aliveMobs,
            attackEffects = state.attackEffects + effect,
            stats = state.stats.copy(mobsKilled = state.stats.mobsKilled + killedByThisAttack)
        )
    }

    // ==================== 3. PLAYER HARVEST ====================

    private fun updatePlayerHarvest(state: GameState): GameState {
        if (!harvestRequested) return state
        harvestRequested = false

        val player = state.player
        val harvestRange = player.radius + 32f + 8f

        // Find nearest object in the direction the player is facing
        var bestObj: MapObject? = null
        var bestDist = Float.MAX_VALUE

        for (obj in state.objects) {
            val dx = obj.x - player.x
            val dy = obj.y - player.y
            val dist = sqrt(dx * dx + dy * dy)
            if (dist > harvestRange + obj.radius) continue

            // Check that the object is roughly ahead of the player (±90°)
            val angleToObj = atan2(dy, dx)
            var angleDiff = angleToObj - player.direction
            while (angleDiff > Math.PI) angleDiff -= (2 * Math.PI).toFloat()
            while (angleDiff < -Math.PI) angleDiff += (2 * Math.PI).toFloat()
            if (kotlin.math.abs(angleDiff) > Math.PI / 2) continue

            if (dist < bestDist) {
                bestDist = dist
                bestObj = obj
            }
        }

        if (bestObj == null) return state

        val obj = bestObj
        val newHp = obj.hp - 1
        val currentCount = player.inventory.getOrDefault(obj.resourceType, 0)
        val newInventory = player.inventory + (obj.resourceType to currentCount + 1)

        val newObjects = if (newHp <= 0) {
            state.objects.filter { it.id != obj.id }
        } else {
            state.objects.map { if (it.id == obj.id) it.copy(hp = newHp) else it }
        }

        return state.copy(
            objects = newObjects,
            player = player.copy(inventory = newInventory),
            stats = state.stats.copy(resourcesGathered = state.stats.resourcesGathered + 1)
        )
    }

    // ==================== 4. CONSUME BERRY ====================

    private fun updateConsumeBerry(state: GameState): GameState {
        if (!consumeBerryRequested) return state
        consumeBerryRequested = false

        val player = state.player
        val berryCount = player.inventory.getOrDefault(ResourceType.BERRY, 0)
        if (berryCount <= 0) return state

        val newHunger = minOf(100f, player.hunger + 10f)
        val newInventory = player.inventory + (ResourceType.BERRY to berryCount - 1)

        return state.copy(player = player.copy(
            hunger = newHunger,
            inventory = newInventory
        ))
    }

    // ==================== 5. MOB AI ====================

    private fun updateMobAI(state: GameState, deltaSec: Float): GameState {
        val player = state.player
        val newMobs = state.mobs.map { mob ->
            val dx = player.x - mob.x
            val dy = player.y - mob.y
            val distToPlayer = sqrt(dx * dx + dy * dy)
            val dirToPlayer = atan2(dy, dx)
            val dirAwayPlayer = atan2(-dy, -dx)

            var newState = mob.state
            var newX = mob.x
            var newY = mob.y
            var newDirection = mob.direction
            var newPatrolX = mob.patrolTargetX
            var newPatrolY = mob.patrolTargetY
            var newCooldown = maxOf(0f, mob.attackCooldown - deltaSec)

            // Skip dying mobs — let the timer tick down
            if (mob.state == BehaviorState.DYING) {
                return@map mob.copy(deathAnimTime = maxOf(0f, mob.deathAnimTime - deltaSec))
            }

            // State transitions
            if (mob.type.isPredator) {
                when {
                    mob.hp < mob.maxHp * 0.2f          -> newState = BehaviorState.FLEE
                    distToPlayer < mob.type.attackRange -> newState = BehaviorState.ATTACK
                    distToPlayer < mob.type.aggroRange  -> newState = BehaviorState.CHASE
                    else                                -> newState = BehaviorState.IDLE
                }
            } else {
                // Prey (including birds): patrol, flee when threatened
                if (distToPlayer < mob.type.aggroRange) {
                    newState = BehaviorState.FLEE
                } else {
                    newState = BehaviorState.PATROL
                }
            }

            // Movement based on state
            val moveSpeed = when (newState) {
                BehaviorState.FLEE -> {
                    if (mob.type == MobType.DEER) mob.speed * 1.3f else mob.speed * 1.2f
                }
                else -> mob.speed
            }

            when (newState) {
                BehaviorState.IDLE -> { /* stand still */ }
                BehaviorState.CHASE -> {
                    newX += cos(dirToPlayer) * moveSpeed * deltaSec
                    newY += sin(dirToPlayer) * moveSpeed * deltaSec
                    newDirection = dirToPlayer
                }
                BehaviorState.FLEE -> {
                    newX += cos(dirAwayPlayer) * moveSpeed * deltaSec
                    newY += sin(dirAwayPlayer) * moveSpeed * deltaSec
                    newDirection = dirAwayPlayer
                }
                BehaviorState.ATTACK -> {
                    newDirection = dirToPlayer
                    // Damage is dealt in resolveCollisions on contact
                }
                BehaviorState.PATROL -> {
                    // If no patrol target or reached it — pick a new one
                    val pdx = newPatrolX - mob.x
                    val pdy = newPatrolY - mob.y
                    val pDist = sqrt(pdx * pdx + pdy * pdy)
                    if (pDist < 10f || newPatrolX == 0f && newPatrolY == 0f) {
                        newPatrolX = mob.x + (Math.random() * 400 - 200).toFloat()
                        newPatrolY = mob.y + (Math.random() * 400 - 200).toFloat()
                    } else {
                        val dirToTarget = atan2(pdy, pdx)
                        newX += cos(dirToTarget) * moveSpeed * 0.5f * deltaSec
                        newY += sin(dirToTarget) * moveSpeed * 0.5f * deltaSec
                        newDirection = dirToTarget
                    }
                }
                BehaviorState.DYING -> { /* handled above — early return */ }
            }

            mob.copy(
                x = newX, y = newY, direction = newDirection,
                state = newState, attackCooldown = newCooldown,
                patrolTargetX = newPatrolX, patrolTargetY = newPatrolY
            )
        }
        return state.copy(mobs = newMobs)
    }

    // ==================== 6. COLLISIONS ====================

    private fun resolveCollisions(state: GameState, deltaSec: Float): GameState {
        var player = state.player
        var px = player.x
        var py = player.y
        var playerHp = player.hp

        // Player vs objects — 3 passes to prevent tunneling through adjacent trees
        repeat(3) {
            for (obj in state.objects) {
                val distX = px - obj.x
                val distY = py - obj.y
                val dist = sqrt(distX * distX + distY * distY)
                val minDist = player.radius + obj.radius
                if (dist < minDist && dist > 0f) {
                    val overlap = minDist - dist
                    px += (distX / dist) * overlap
                    py += (distY / dist) * overlap
                }
            }
        }

        // Player vs mobs + mob damage (dying mobs don't block or deal damage)
        val newMobs = state.mobs.toMutableList()
        for (i in newMobs.indices) {
            val mob = newMobs[i]
            if (mob.state == BehaviorState.DYING) continue

            val distX = px - mob.x
            val distY = py - mob.y
            val dist = sqrt(distX * distX + distY * distY)
            val minDist = player.radius + mob.radius
            if (dist < minDist && dist > 0f) {
                val overlap = minDist - dist
                px += (distX / dist) * overlap
                py += (distY / dist) * overlap

                if (mob.state == BehaviorState.ATTACK && mob.attackCooldown <= 0f) {
                    playerHp -= mob.type.attackDamage
                    newMobs[i] = mob.copy(attackCooldown = 1.0f)
                }
            }
        }

        // Mob vs mob (separation)
        for (i in newMobs.indices) {
            for (j in i + 1 until newMobs.size) {
                val a = newMobs[i]
                val b = newMobs[j]
                val dx = a.x - b.x
                val dy = a.y - b.y
                val dist = sqrt(dx * dx + dy * dy)
                val minDist = a.radius + b.radius
                if (dist < minDist && dist > 0f) {
                    val overlap = (minDist - dist) / 2f
                    val nx = dx / dist * overlap
                    val ny = dy / dist * overlap
                    newMobs[i] = a.copy(x = a.x + nx, y = a.y + ny)
                    newMobs[j] = b.copy(x = b.x - nx, y = b.y - ny)
                }
            }
        }

        // Mob vs objects
        for (i in newMobs.indices) {
            var mx = newMobs[i].x
            var my = newMobs[i].y
            for (obj in state.objects) {
                val dx = mx - obj.x
                val dy = my - obj.y
                val dist = sqrt(dx * dx + dy * dy)
                val minDist = newMobs[i].radius + obj.radius
                if (dist < minDist && dist > 0f) {
                    val overlap = minDist - dist
                    mx += (dx / dist) * overlap
                    my += (dy / dist) * overlap
                }
            }
            newMobs[i] = newMobs[i].copy(x = mx, y = my)
        }

        return state.copy(
            player = player.copy(x = px, y = py, hp = playerHp),
            mobs = newMobs
        )
    }

    // ==================== 7. SURVIVAL ====================

    private fun updateSurvival(state: GameState, deltaSec: Float): GameState {
        val player = state.player
        val isNight = state.timeOfDay < 0.2f || state.timeOfDay > 0.8f

        // Hunger drains faster: 10/min during day, 20/min at night
        val hungerRate = if (isNight) 20f / 60f else 10f / 60f
        var newHunger = maxOf(0f, player.hunger - hungerRate * deltaSec)
        var newHp = player.hp
        var hungerAccum = player.hungerDmgAccum
        var coldAccum = player.coldDmgAccum

        // Hunger damage:
        //   hunger == 0  → 1 HP/sec
        //   hunger <= 20 → 1 HP every 3 sec
        val hungerDmgInterval = when {
            newHunger <= 0f  -> 1f   // 1 HP/sec
            newHunger <= 20f -> 3f   // 1 HP per 3 sec
            else             -> -1f  // no damage
        }
        if (hungerDmgInterval > 0f) {
            hungerAccum += deltaSec
            while (hungerAccum >= hungerDmgInterval) {
                newHp -= 1
                hungerAccum -= hungerDmgInterval
            }
        } else {
            hungerAccum = 0f
        }

        // Cold damage at night: 1 HP/sec
        if (isNight) {
            coldAccum += deltaSec
            while (coldAccum >= 1f) {
                newHp -= 1
                coldAccum -= 1f
            }
        } else {
            coldAccum = 0f
        }

        return state.copy(player = player.copy(
            hunger = newHunger,
            hp = newHp,
            hungerDmgAccum = hungerAccum,
            coldDmgAccum = coldAccum
        ))
    }

    // ==================== 8. EFFECTS ====================

    private fun updateEffects(state: GameState, deltaSec: Float): GameState {
        val newEffects = state.attackEffects
            .map { it.copy(remainingTime = it.remainingTime - deltaSec) }
            .filter { it.remainingTime > 0f }
        // Remove mobs whose death animation has finished
        val aliveMobs = state.mobs.filter { it.state != BehaviorState.DYING || it.deathAnimTime > 0f }
        // Tick player death timer
        val newPlayer = if (state.player.deathAnimTime > 0f) {
            state.player.copy(deathAnimTime = maxOf(0f, state.player.deathAnimTime - deltaSec))
        } else state.player
        return state.copy(attackEffects = newEffects, mobs = aliveMobs, player = newPlayer)
    }

    // ==================== 9. GAME OVER ====================

    private fun checkGameOver(state: GameState): GameState {
        // Player still alive — nothing to do
        if (state.player.hp > 0) return state
        // Death animation already running — check if finished
        if (state.player.deathAnimTime >= 0f) {
            return if (state.player.deathAnimTime <= 0f)
                state.copy(isGameOver = true, isRunning = false)
            else state
        }
        // hp <= 0, animation not started yet — trigger it
        return state.copy(player = state.player.copy(deathAnimTime = PLAYER_DEATH_ANIM_DURATION))
    }

    // ==================== MAP GENERATION ====================

    private fun generateMap(): Pair<List<MapObject>, List<Mob>> {
        val objects = mutableListOf<MapObject>()
        val mobs = mutableListOf<Mob>()

        // Trees — drop Wood (radius x2, map 6000×6000)
        repeat(120) { i ->
            val x = (Math.random() * 6000 - 3000).toFloat()
            val y = (Math.random() * 6000 - 3000).toFloat()
            objects.add(MapObject("tree_$i", ObjectType.TREE, x, y, 48f, 3, ResourceType.WOOD))
        }
        // Rocks — drop Stone (radius x2)
        repeat(60) { i ->
            val x = (Math.random() * 6000 - 3000).toFloat()
            val y = (Math.random() * 6000 - 3000).toFloat()
            objects.add(MapObject("rock_$i", ObjectType.ROCK, x, y, 32f, 4, ResourceType.STONE))
        }
        // Bushes — drop Berry (radius x2)
        repeat(50) { i ->
            val x = (Math.random() * 6000 - 3000).toFloat()
            val y = (Math.random() * 6000 - 3000).toFloat()
            objects.add(MapObject("bush_$i", ObjectType.BUSH, x, y, 24f, 1, ResourceType.BERRY))
        }

        // Wolves — predators
        repeat(15) { i ->
            mobs.add(Mob(
                id = "wolf_$i", type = MobType.WOLF,
                x = (Math.random() * 5000 - 2500).toFloat(),
                y = (Math.random() * 5000 - 2500).toFloat(),
                speed = MobType.WOLF.baseSpeed,
                hp = MobType.WOLF.baseMaxHp, maxHp = MobType.WOLF.baseMaxHp,
                radius = 16f
            ))
        }
        // Bears — predators, slow and powerful
        repeat(10) { i ->
            mobs.add(Mob(
                id = "bear_$i", type = MobType.BEAR,
                x = (Math.random() * 5000 - 2500).toFloat(),
                y = (Math.random() * 5000 - 2500).toFloat(),
                speed = MobType.BEAR.baseSpeed,
                hp = MobType.BEAR.baseMaxHp, maxHp = MobType.BEAR.baseMaxHp,
                radius = 22f
            ))
        }
        // Foxes — predators, fast
        repeat(12) { i ->
            mobs.add(Mob(
                id = "fox_$i", type = MobType.FOX,
                x = (Math.random() * 5000 - 2500).toFloat(),
                y = (Math.random() * 5000 - 2500).toFloat(),
                speed = MobType.FOX.baseSpeed,
                hp = MobType.FOX.baseMaxHp, maxHp = MobType.FOX.baseMaxHp,
                radius = 12f
            ))
        }
        // Deer — prey
        repeat(18) { i ->
            mobs.add(Mob(
                id = "deer_$i", type = MobType.DEER,
                x = (Math.random() * 5000 - 2500).toFloat(),
                y = (Math.random() * 5000 - 2500).toFloat(),
                speed = MobType.DEER.baseSpeed,
                hp = MobType.DEER.baseMaxHp, maxHp = MobType.DEER.baseMaxHp,
                radius = 14f,
                patrolTargetX = (Math.random() * 5000 - 2500).toFloat(),
                patrolTargetY = (Math.random() * 5000 - 2500).toFloat()
            ))
        }
        // Rabbits — prey, fastest
        repeat(20) { i ->
            mobs.add(Mob(
                id = "rabbit_$i", type = MobType.RABBIT,
                x = (Math.random() * 5000 - 2500).toFloat(),
                y = (Math.random() * 5000 - 2500).toFloat(),
                speed = MobType.RABBIT.baseSpeed,
                hp = MobType.RABBIT.baseMaxHp, maxHp = MobType.RABBIT.baseMaxHp,
                radius = 8f,
                patrolTargetX = (Math.random() * 5000 - 2500).toFloat(),
                patrolTargetY = (Math.random() * 5000 - 2500).toFloat()
            ))
        }
        // Birds — just wander, don't react to the player
        repeat(15) { i ->
            mobs.add(Mob(
                id = "bird_$i", type = MobType.BIRD,
                x = (Math.random() * 5000 - 2500).toFloat(),
                y = (Math.random() * 5000 - 2500).toFloat(),
                speed = MobType.BIRD.baseSpeed,
                hp = MobType.BIRD.baseMaxHp, maxHp = MobType.BIRD.baseMaxHp,
                radius = 6f,
                patrolTargetX = (Math.random() * 5000 - 2500).toFloat(),
                patrolTargetY = (Math.random() * 5000 - 2500).toFloat()
            ))
        }

        return Pair(objects, mobs)
    }

    private fun isNight(timeOfDay: Float): Boolean = timeOfDay < 0.2f || timeOfDay > 0.8f
}
