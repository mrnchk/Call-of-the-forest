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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.cotf.core.engine.BehaviorState
import com.cotf.core.engine.GameEngine
import com.cotf.core.engine.GameState
import com.cotf.core.engine.MobType
import com.cotf.core.engine.ObjectType
import com.cotf.core.engine.ResourceType
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun GameRenderer(engine: GameEngine) {
    val state by engine.state.collectAsState()

    Canvas(modifier = Modifier.fillMaxSize()) {
        val camX = state.camera.x - size.width / 2
        val camY = state.camera.y - size.height / 2

        drawRect(Color(0xFF4A7C3F)) // трава
        drawGrid(camX, camY)

        // Объекты
        state.objects.forEach { obj ->
            val sx = obj.x - camX
            val sy = obj.y - camY
            if (sx < -50 || sx > size.width + 50 || sy < -50 || sy > size.height + 50) return@forEach

            // Подсветка если рядом с игроком и он смотрит на объект
            val dx = obj.x - state.player.x
            val dy = obj.y - state.player.y
            val dist = sqrt(dx * dx + dy * dy)
            val harvestRange = state.player.radius + 32f + 8f + obj.radius
            if (dist < harvestRange) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.15f),
                    radius = obj.radius + 6f,
                    center = Offset(sx, sy)
                )
            }

            when (obj.type) {
                ObjectType.TREE -> drawTree(sx, sy, obj.radius)
                ObjectType.ROCK -> drawRock(sx, sy, obj.radius)
                ObjectType.BUSH -> drawBush(sx, sy, obj.radius)
            }
        }

        // Мобы
        state.mobs.forEach { mob ->
            val sx = mob.x - camX
            val sy = mob.y - camY
            if (sx < -50 || sx > size.width + 50 || sy < -50 || sy > size.height + 50) return@forEach

            when (mob.type) {
                MobType.WOLF -> drawWolf(sx, sy, mob.radius, mob.direction)
                MobType.DEER -> drawDeer(sx, sy, mob.radius, mob.direction)
                MobType.GOBLIN -> drawGoblin(sx, sy, mob.radius, mob.direction)
            }

            // HP бар над мобом если повреждён
            if (mob.hp < mob.maxHp) {
                drawMobHpBar(sx, sy, mob.radius, mob.hp, mob.maxHp)
            }
        }

        // Игрок
        val px = state.player.x - camX
        val py = state.player.y - camY
        drawPlayer(px, py, state.player.radius, state.player.direction)

        // Эффекты атаки
        state.attackEffects.forEach { effect ->
            val ex = effect.x - camX
            val ey = effect.y - camY
            val alpha = (effect.remainingTime / 0.15f).coerceIn(0f, 1f)
            drawCircle(
                color = Color.White.copy(alpha = alpha * 0.6f),
                radius = effect.radius,
                center = Offset(ex, ey)
            )
        }

        drawDayNight(state.timeOfDay)
    }
}

// ==================== OBJECT DRAWING ====================

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

// ==================== MOB DRAWING ====================

private fun DrawScope.drawWolf(x: Float, y: Float, radius: Float, direction: Float) {
    drawCircle(Color(0xFF757575), radius = radius, center = Offset(x, y))
    drawLine(Color.White, Offset(x, y), Offset(
        x + cos(direction) * radius * 1.5f,
        y + sin(direction) * radius * 1.5f
    ), strokeWidth = 2f)
}

private fun DrawScope.drawDeer(x: Float, y: Float, radius: Float, direction: Float) {
    drawCircle(Color(0xFF8D6E63), radius = radius, center = Offset(x, y))
    drawLine(Color.White, Offset(x, y), Offset(
        x + cos(direction) * radius * 1.5f,
        y + sin(direction) * radius * 1.5f
    ), strokeWidth = 2f)
}

private fun DrawScope.drawGoblin(x: Float, y: Float, radius: Float, direction: Float) {
    drawCircle(Color(0xFF4CAF50), radius = radius, center = Offset(x, y))
    drawLine(Color.White, Offset(x, y), Offset(
        x + cos(direction) * radius * 1.5f,
        y + sin(direction) * radius * 1.5f
    ), strokeWidth = 2f)
}

private fun DrawScope.drawMobHpBar(x: Float, y: Float, radius: Float, hp: Int, maxHp: Int) {
    val barWidth = 24f
    val barHeight = 3f
    val barX = x - barWidth / 2
    val barY = y - radius - 8f
    val fraction = (hp.coerceAtLeast(0).toFloat() / maxHp.toFloat()).coerceIn(0f, 1f)

    drawRect(Color(0xFF1A1A1A), topLeft = Offset(barX, barY), size = Size(barWidth, barHeight))
    drawRect(Color(0xFFCF6679), topLeft = Offset(barX, barY), size = Size(barWidth * fraction, barHeight))
}

// ==================== PLAYER DRAWING ====================

private fun DrawScope.drawPlayer(x: Float, y: Float, radius: Float, direction: Float) {
    drawCircle(Color(0xFF42A5F5), radius = radius, center = Offset(x, y))
    drawLine(Color.White, Offset(x, y), Offset(
        x + cos(direction) * radius * 1.5f,
        y + sin(direction) * radius * 1.5f
    ), strokeWidth = 3f)
}

// ==================== GRID ====================

private fun DrawScope.drawGrid(camX: Float, camY: Float) {
    val gridSize = 100f
    var x = -(camX % gridSize)
    while (x < size.width) {
        drawLine(Color(0x154A7C3F), Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
        x += gridSize
    }
    var y = -(camY % gridSize)
    while (y < size.height) {
        drawLine(Color(0x154A7C3F), Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
        y += gridSize
    }
}

// ==================== DAY/NIGHT ====================

private fun DrawScope.drawDayNight(timeOfDay: Float) {
    val darkness = when {
        timeOfDay < 0.2f -> 0.4f
        timeOfDay < 0.3f -> 0.4f - (timeOfDay - 0.2f) * 4f
        timeOfDay < 0.7f -> 0f
        timeOfDay < 0.8f -> (timeOfDay - 0.7f) * 4f
        else -> 0.4f
    }
    if (darkness > 0.01f) {
        val nightTint = if (timeOfDay < 0.2f || timeOfDay > 0.8f)
            Color(0, 0, 60, (darkness * 255).toInt()) else
            Color(0, 0, 40, (darkness * 255).toInt())
        drawRect(nightTint, topLeft = Offset.Zero, size = size)
    }
}
