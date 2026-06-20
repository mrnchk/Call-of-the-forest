package com.cotf.render

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import com.cotf.core.engine.BehaviorState
import com.cotf.core.engine.DEATH_ANIM_DURATION
import com.cotf.core.engine.PLAYER_DEATH_ANIM_DURATION
import com.cotf.core.engine.GameEngine
import com.cotf.core.engine.MobType
import com.cotf.core.engine.ObjectType
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Главный рендер игры.
 *
 * @param engine    игровой движок
 * @param sprites   кэш спрайтов (null → рисуем кружки-заглушки)
 * @param gameTime  суммарное время в секундах (для анимации кадров)
 */
@Composable
fun GameRenderer(
    engine: GameEngine,
    sprites: CharSpriteCache? = null,
    gameTime: Float = 0f
) {
    val state by engine.state.collectAsState()

    Canvas(modifier = Modifier.fillMaxSize()) {
        val camX = state.camera.x - size.width  / 2
        val camY = state.camera.y - size.height / 2

        drawRect(Color(0xFF3C6E28))
        drawGrassTexture(camX, camY)

        // ── map objects ────────────────────────────────────────────────
        state.objects.forEach { obj ->
            val sx = obj.x - camX
            val sy = obj.y - camY
            if (sx < -64 || sx > size.width + 64 || sy < -64 || sy > size.height + 64) return@forEach

            when (obj.type) {
                ObjectType.TREE -> {
                    val treeKey = if (obj.id.last().digitToIntOrNull()?.let { it % 2 == 0 } == true) "tree1" else "tree2"
                    val treeSprites = sprites?.get(treeKey)
                    if (treeSprites != null) {
                        val clip = treeSprites.clip("idle")
                        drawCharSprite(treeSprites, clip, 0, sx, sy, scale = obj.radius * 2f / treeSprites.frameWidth, flipX = false)
                    } else {
                        drawTree(sx, sy, obj.radius)
                    }
                }
                ObjectType.ROCK -> {
                    val rockSprites = sprites?.get("rock")
                    if (rockSprites != null) {
                        val clip = rockSprites.clip("idle")
                        drawCharSprite(rockSprites, clip, 0, sx, sy, scale = obj.radius * 2f / rockSprites.frameWidth, flipX = false)
                    } else {
                        drawRock(sx, sy, obj.radius)
                    }
                }
                ObjectType.BUSH -> {
                    val berrySprites = sprites?.get("berry")
                    if (berrySprites != null) {
                        val clip = berrySprites.clip("idle")
                        drawCharSprite(berrySprites, clip, 0, sx, sy, scale = obj.radius * 2f / berrySprites.frameWidth, flipX = false)
                    } else {
                        drawBush(sx, sy, obj.radius)
                    }
                }
            }
        }

        // ── mobs ───────────────────────────────────────────────────────
        state.mobs.forEach { mob ->
            val sx = mob.x - camX
            val sy = mob.y - camY
            if (sx < -64 || sx > size.width + 64 || sy < -64 || sy > size.height + 64) return@forEach

            val charKey = when (mob.type) {
                MobType.WOLF   -> "wolf"
                MobType.BEAR   -> "bear"
                MobType.FOX    -> "fox"
                MobType.DEER   -> "deer"
                MobType.RABBIT -> "rabbit"
                MobType.BIRD   -> "bird"
            }
            val charSprites = sprites?.get(charKey)

            if (charSprites != null) {
                val clipName = when (mob.state) {
                    BehaviorState.DYING  -> "death"
                    BehaviorState.IDLE, BehaviorState.FLEE -> "idle"
                    BehaviorState.PATROL -> "patrol"
                    BehaviorState.CHASE  -> "run"
                    BehaviorState.ATTACK -> "attack"
                }
                val clip = charSprites.clip(clipName)
                val frame = when (mob.state) {
                    BehaviorState.DYING -> {
                        // Death animation — one-shot, plays forward
                        val progress = 1f - (mob.deathAnimTime / DEATH_ANIM_DURATION).coerceIn(0f, 1f)
                        (progress * clip.frameCount).toInt().coerceIn(0, clip.frameCount - 1)
                    }
                    else -> clip.frameAt(gameTime)
                }
                // Dying mobs fade out toward the end
                val alpha = if (mob.state == BehaviorState.DYING)
                    (mob.deathAnimTime / DEATH_ANIM_DURATION).coerceIn(0f, 1f)
                else 1f
                val flipX = cos(mob.direction) < 0f
                if (alpha < 0.99f) {
                    // Draw with transparency via native canvas alpha
                    drawContext.canvas.nativeCanvas.apply {
                        save()
                        android.graphics.Paint().also { p ->
                            p.alpha = (alpha * 255).toInt()
                            // drawCharSprite is called directly — can't apply alpha via paint,
                            // use saveLayerAlpha instead
                        }
                        restore()
                    }
                    // saveLayerAlpha for full-sprite transparency
                    val nativeCanvas = drawContext.canvas.nativeCanvas
                    nativeCanvas.saveLayerAlpha(null, (alpha * 255).toInt())
                    drawCharSprite(charSprites, clip, frame, sx, sy, scale = 4f, flipX = flipX)
                    nativeCanvas.restore()
                } else {
                    drawCharSprite(charSprites, clip, frame, sx, sy, scale = 4f, flipX = flipX)
                }
            } else {
                // Fallbacks
                val fallbackColor = when (mob.type) {
                    MobType.WOLF   -> androidx.compose.ui.graphics.Color(0xFF757575)
                    MobType.BEAR   -> androidx.compose.ui.graphics.Color(0xFF5D4037)
                    MobType.FOX    -> androidx.compose.ui.graphics.Color(0xFFE64A19)
                    MobType.DEER   -> androidx.compose.ui.graphics.Color(0xFF8D6E63)
                    MobType.RABBIT -> androidx.compose.ui.graphics.Color(0xFFCFD8DC)
                    MobType.BIRD   -> androidx.compose.ui.graphics.Color(0xFFFFCC02)
                }
                val alpha = if (mob.state == BehaviorState.DYING)
                    (mob.deathAnimTime / DEATH_ANIM_DURATION).coerceIn(0f, 1f) else 1f
                drawCircle(fallbackColor.copy(alpha = alpha), radius = mob.radius, center = Offset(sx, sy))
                if (mob.state != BehaviorState.DYING) {
                    drawLine(Color.White, Offset(sx, sy),
                        Offset(sx + cos(mob.direction) * mob.radius * 1.5f,
                               sy + sin(mob.direction) * mob.radius * 1.5f), strokeWidth = 2f)
                }
            }

            if (mob.hp < mob.maxHp) drawMobHpBar(sx, sy, mob.radius, mob.hp, mob.maxHp)
        }

        // ── player ─────────────────────────────────────────────────────
        val px = state.player.x - camX
        val py = state.player.y - camY
        val playerSprites = sprites?.get("player")

        if (playerSprites != null) {
            val isDying = state.player.deathAnimTime >= 0f
            val dir         = state.player.direction
            val isAttacking = state.player.attackCooldown > 0f
            val isMoving    = state.player.isMoving

            val clipName: String
            val frame: Int
            val alpha: Float

            if (isDying) {
                clipName = "death"
                val progress = 1f - (state.player.deathAnimTime / PLAYER_DEATH_ANIM_DURATION).coerceIn(0f, 1f)
                val clip = playerSprites.clip(clipName)
                frame = (progress * clip.frameCount).toInt().coerceIn(0, clip.frameCount - 1)
                alpha = (state.player.deathAnimTime / PLAYER_DEATH_ANIM_DURATION).coerceIn(0f, 1f)
            } else {
                val absS = kotlin.math.abs(sin(dir))
                val absC = kotlin.math.abs(cos(dir))
                val dirSuffix = when {
                    absS >= absC && sin(dir) > 0 -> "down"
                    absS >= absC && sin(dir) < 0 -> "up"
                    else                          -> "side"
                }
                clipName = when {
                    isAttacking -> "attack_$dirSuffix"
                    isMoving    -> "walk_$dirSuffix"
                    else        -> "idle_$dirSuffix"
                }
                val clip = playerSprites.clip(clipName)
                frame = when {
                    isAttacking -> {
                        val elapsed = 0.4f - state.player.attackCooldown
                        val t = (elapsed / 0.4f).coerceIn(0f, 1f)
                        (t * clip.frameCount).toInt().coerceIn(0, clip.frameCount - 1)
                    }
                    else -> clip.frameAt(gameTime)
                }
                alpha = 1f
            }

            val clip  = playerSprites.clip(clipName)
            val flipX = cos(dir) < 0f

            if (alpha < 0.99f) {
                val nativeCanvas = drawContext.canvas.nativeCanvas
                nativeCanvas.saveLayerAlpha(null, (alpha * 255).toInt())
                drawCharSprite(playerSprites, clip, frame, px, py, scale = 4f, flipX = flipX)
                nativeCanvas.restore()
            } else {
                drawCharSprite(playerSprites, clip, frame, px, py, scale = 4f, flipX = flipX)
            }
        } else {
            drawPlayer(px, py, state.player.radius, state.player.direction)
        }

        // ── attack effects ─────────────────────────────────────────────
        state.attackEffects.forEach { effect ->
            val ex = effect.x - camX
            val ey = effect.y - camY
            val alpha = (effect.remainingTime / 0.15f).coerceIn(0f, 1f)
            drawCircle(Color.White.copy(alpha = alpha * 0.6f), radius = effect.radius, center = Offset(ex, ey))
        }

        drawDayNight(state.timeOfDay)
    }
}

// ── map objects ────────────────────────────────────────────────────────────

private fun DrawScope.drawTree(x: Float, y: Float, radius: Float) {
    drawRect(Color(0xFF6B4226), topLeft = Offset(x - 4f, y), size = Size(8f, radius))
    drawCircle(Color(0xFF2E7D32), radius = radius, center = Offset(x, y - radius * 0.3f))
}

private fun DrawScope.drawRock(x: Float, y: Float, radius: Float) {
    drawCircle(Color(0xFF9E9E9E), radius = radius, center = Offset(x, y))
    drawCircle(Color(0xFFBDBDBD), radius = radius * 0.5f, center = Offset(x - radius * 0.2f, y - radius * 0.2f))
}

private fun DrawScope.drawBush(x: Float, y: Float, radius: Float) {
    drawCircle(Color(0xFF558B2F), radius = radius, center = Offset(x, y))
}

// ── mob fallbacks ──────────────────────────────────────────────────────────

private fun DrawScope.drawWolf(x: Float, y: Float, radius: Float, direction: Float) {
    drawCircle(Color(0xFF757575), radius = radius, center = Offset(x, y))
    drawLine(Color.White, Offset(x, y),
        Offset(x + cos(direction) * radius * 1.5f, y + sin(direction) * radius * 1.5f), strokeWidth = 2f)
}

private fun DrawScope.drawDeer(x: Float, y: Float, radius: Float, direction: Float) {
    drawCircle(Color(0xFF8D6E63), radius = radius, center = Offset(x, y))
    drawLine(Color.White, Offset(x, y),
        Offset(x + cos(direction) * radius * 1.5f, y + sin(direction) * radius * 1.5f), strokeWidth = 2f)
}

private fun DrawScope.drawGoblin(x: Float, y: Float, radius: Float, direction: Float) {
    drawCircle(Color(0xFF4CAF50), radius = radius, center = Offset(x, y))
    drawLine(Color.White, Offset(x, y),
        Offset(x + cos(direction) * radius * 1.5f, y + sin(direction) * radius * 1.5f), strokeWidth = 2f)
}

private fun DrawScope.drawMobHpBar(x: Float, y: Float, radius: Float, hp: Int, maxHp: Int) {
    val bw = 24f; val bh = 3f
    val bx = x - bw / 2; val by = y - radius - 8f
    val fr = (hp.coerceAtLeast(0).toFloat() / maxHp).coerceIn(0f, 1f)
    drawRect(Color(0xFF1A1A1A), topLeft = Offset(bx, by), size = Size(bw, bh))
    drawRect(Color(0xFFCF6679), topLeft = Offset(bx, by), size = Size(bw * fr, bh))
}

// ── player fallback ────────────────────────────────────────────────────────

private fun DrawScope.drawPlayer(x: Float, y: Float, radius: Float, direction: Float) {
    drawCircle(Color(0xFF42A5F5), radius = radius, center = Offset(x, y))
    drawLine(Color.White, Offset(x, y),
        Offset(x + cos(direction) * radius * 1.5f, y + sin(direction) * radius * 1.5f), strokeWidth = 3f)
}

// ── grass texture ──────────────────────────────────────────────────────────

private fun DrawScope.drawGrassTexture(camX: Float, camY: Float) {
    val tileSize = 10f
    val startX = kotlin.math.floor(camX / tileSize).toInt()
    val startY = kotlin.math.floor(camY / tileSize).toInt()
    val cols = (size.width  / tileSize).toInt() + 2
    val rows = (size.height / tileSize).toInt() + 2

    for (row in startY..startY + rows) {
        for (col in startX..startX + cols) {
            val wx = col * tileSize - camX
            val wy = row * tileSize - camY
            // Better hash with XOR mixing for per-tile variation
            var hash = col * 374761393 xor row * 668265263
            hash = hash xor (hash ushr 13)
            hash *= -1640531535
            hash = hash xor (hash ushr 16)
            hash = hash and 0xFFFFFF
            val r = (0x3C + (hash and 0x0F)) / 255f         // 60..75  (red)
            val g = (0x6E + ((hash shr 4) and 0x1A)) / 255f // 110..136 (green)
            val b = (0x28 + ((hash shr 8) and 0x0A)) / 255f // 40..50  (blue)
            val tileColor  = Color(red = r, green = g, blue = b, alpha = 1f)
            drawRect(
                color    = tileColor,
                topLeft  = Offset(wx, wy),
                size     = Size(tileSize - 0.5f, tileSize - 0.5f)
            )
        }
    }
}

// ── day/night overlay ──────────────────────────────────────────────────────

/**
 * Draws a layered day/night overlay.
 *
 * Time-of-day phases (timeOfDay ∈ [0, 1), 0 = midnight, 0.5 = noon):
 *   0.00–0.18  deep night        — dark blue, stars visible
 *   0.18–0.28  dawn              — night fades, orange/gold tint peaks at 0.23
 *   0.28–0.72  full day          — no overlay
 *   0.72–0.82  dusk              — orange/gold rises then night fades in
 *   0.82–1.00  deep night        — dark blue, stars visible
 */
private fun DrawScope.drawDayNight(timeOfDay: Float) {
    // Night darkness alpha (0 = transparent, 0.55 = deepest)
    val nightAlpha = when {
        timeOfDay < 0.18f -> 0.55f
        timeOfDay < 0.28f -> 0.55f * (1f - (timeOfDay - 0.18f) / 0.10f)
        timeOfDay < 0.72f -> 0f
        timeOfDay < 0.82f -> 0.55f * ((timeOfDay - 0.72f) / 0.10f)
        else              -> 0.55f
    }

    // Orange/golden tint — peaks mid-dawn and mid-dusk
    val orangeAlpha = when {
        timeOfDay >= 0.18f && timeOfDay < 0.28f -> {
            val t = (timeOfDay - 0.18f) / 0.10f          // 0→1 through dawn
            (1f - kotlin.math.abs(t - 0.5f) * 2f).coerceAtLeast(0f) * 0.32f
        }
        timeOfDay >= 0.72f && timeOfDay < 0.82f -> {
            val t = (timeOfDay - 0.72f) / 0.10f          // 0→1 through dusk
            (1f - kotlin.math.abs(t - 0.5f) * 2f).coerceAtLeast(0f) * 0.32f
        }
        else -> 0f
    }

    // Stars (render below the night overlay so they are dimmed by it)
    if (nightAlpha > 0.08f) drawStars(nightAlpha)

    // Dark-blue night layer
    if (nightAlpha > 0.01f)
        drawRect(Color(red = 0f, green = 0.02f, blue = 0.20f, alpha = nightAlpha),
            topLeft = Offset.Zero, size = size)

    // Warm orange/gold dawn-dusk layer
    if (orangeAlpha > 0.01f)
        drawRect(Color(red = 1f, green = 0.42f, blue = 0.08f, alpha = orangeAlpha),
            topLeft = Offset.Zero, size = size)
}

/**
 * Renders pseudo-random stars. Positions are screen-fixed (not world-fixed),
 * which matches the visual expectation of stars on a static sky.
 * Visibility fades in/out proportionally to [nightAlpha].
 */
private fun DrawScope.drawStars(nightAlpha: Float) {
    val starAlpha = ((nightAlpha - 0.08f) / 0.47f).coerceIn(0f, 1f)
    if (starAlpha < 0.02f) return

    val w = size.width
    val h = size.height
    val nativeCanvas = drawContext.canvas.nativeCanvas
    val paint = android.graphics.Paint().apply { isAntiAlias = true }

    val starCount = 90
    for (i in 0 until starCount) {
        // Two independent Wang-hash passes for X and Y
        var hx = i * 2531011 xor (i * 1376312589 + 0xDEADBEEF.toInt())
        hx = hx xor (hx ushr 15); hx *= -1640531527; hx = hx xor (hx ushr 13)

        var hy = i * 374761393 xor (i * 668265263 + 0xCAFEBABE.toInt())
        hy = hy xor (hy ushr 13); hy *= -1640531535; hy = hy xor (hy ushr 16)

        val sx = ((hx and 0x7FFFFFFF).toLong().toFloat() / 0x7FFFFFFF.toLong().toFloat()) * w
        val sy = ((hy and 0x7FFFFFFF).toLong().toFloat() / 0x7FFFFFFF.toLong().toFloat()) * h * 0.72f

        // Larger star every 7th
        val radius = if (i % 7 == 0) 2.8f else 1.6f
        // Individual twinkle brightness variation
        val brightness = 0.55f + ((hx ushr 8) and 0xFF) / 255f * 0.45f
        val a = (starAlpha * brightness).coerceIn(0f, 1f)

        paint.color = android.graphics.Color.argb((a * 255).toInt(), 220, 230, 255)
        nativeCanvas.drawCircle(sx, sy, radius, paint)
    }
}
