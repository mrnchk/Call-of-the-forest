# Навык: Canvas Render

## Суть

Отрисовка игрового мира через Compose Canvas. Читает GameState — ничего не мутирует.

## Код

```kotlin
// render/GameRenderer.kt
package com.cotf.render

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.cotf.core.engine.GameEngine
import com.cotf.core.engine.GameState
import com.cotf.core.engine.ObjectType

@Composable
fun GameRenderer(engine: GameEngine) {
    val state by engine.state.collectAsState()

    Canvas(modifier = Modifier.fillMaxSize()) {
        val camX = state.camera.x - size.width / 2
        val camY = state.camera.y - size.height / 2

        // Фон — зелёная трава
        drawRect(Color(0xFF4A7C3F))

        // Сетка (опционально — для отладки)
        drawGrid(camX, camY)

        // Объекты карты
        state.objects.forEach { obj ->
            val screenX = obj.x - camX
            val screenY = obj.y - camY

            // Не рисуем за пределами экрана
            if (screenX < -50 || screenX > size.width + 50 ||
                screenY < -50 || screenY > size.height + 50) return@forEach

            when (obj.type) {
                ObjectType.TREE -> drawTree(screenX, screenY, obj.radius)
                ObjectType.ROCK -> drawRock(screenX, screenY, obj.radius)
                ObjectType.BUSH -> drawBush(screenX, screenY, obj.radius)
            }
        }

        // Мобы (стоят на месте — заглушка)
        state.mobs.forEach { mob ->
            val screenX = mob.x - camX
            val screenY = mob.y - camY
            drawCircle(
                color = Color(0xFF8B0000),
                radius = mob.radius,
                center = Offset(screenX, screenY)
            )
        }

        // Игрок
        val playerScreenX = state.player.x - camX
        val playerScreenY = state.player.y - camY
        drawPlayer(playerScreenX, playerScreenY, state.player.radius, state.player.direction)

        // День/ночь оверлей
        drawDayNight(state.timeOfDay)
    }
}

private fun DrawScope.drawTree(x: Float, y: Float, radius: Float) {
    // Ствол
    drawRect(
        color = Color(0xFF6B4226),
        topLeft = Offset(x - 4f, y),
        size = androidx.compose.ui.geometry.Size(8f, radius)
    )
    // Крона
    drawCircle(
        color = Color(0xFF2E7D32),
        radius = radius,
        center = Offset(x, y - radius * 0.3f)
    )
}

private fun DrawScope.drawRock(x: Float, y: Float, radius: Float) {
    drawCircle(
        color = Color(0xFF9E9E9E),
        radius = radius,
        center = Offset(x, y)
    )
    // Блик
    drawCircle(
        color = Color(0xFFBDBDBD),
        radius = radius * 0.5f,
        center = Offset(x - radius * 0.2f, y - radius * 0.2f)
    )
}

private fun DrawScope.drawBush(x: Float, y: Float, radius: Float) {
    drawCircle(
        color = Color(0xFF558B2F),
        radius = radius,
        center = Offset(x, y)
    )
}

private fun DrawScope.drawPlayer(x: Float, y: Float, radius: Float, direction: Float) {
    // Тело
    drawCircle(
        color = Color(0xFF42A5F5),
        radius = radius,
        center = Offset(x, y)
    )
    // Направление взгляда (линия)
    drawLine(
        color = Color.White,
        start = Offset(x, y),
        end = Offset(
            x + kotlin.math.cos(direction) * radius * 1.5f,
            y + kotlin.math.sin(direction) * radius * 1.5f
        ),
        strokeWidth = 3f
    )
}

private fun DrawScope.drawGrid(camX: Float, camY: Float) {
    val gridSize = 100f
    val startX = -(camX % gridSize)
    val startY = -(camY % gridSize)

    for (x in startX..size.width step gridSize) {
        drawLine(
            color = Color(0x154A7C3F),
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 1f
        )
    }
    for (y in startY..size.height step gridSize) {
        drawLine(
            color = Color(0x154A7C3F),
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1f
        )
    }
}

private fun DrawScope.drawDayNight(timeOfDay: Float) {
    // 0=полночь, 0.25=рассвет, 0.5=полдень, 0.75=закат
    val darkness = when {
        timeOfDay < 0.2f -> 0.35f       // ночь
        timeOfDay < 0.3f -> 0.35f - (timeOfDay - 0.2f) * 3.5f  // рассвет
        timeOfDay < 0.7f -> 0f          // день
        timeOfDay < 0.8f -> (timeOfDay - 0.7f) * 3.5f           // закат
        else -> 0.35f                   // ночь
    }

    if (darkness > 0.01f) {
        drawRect(
            color = Color(0, 0, 40, (darkness * 255).toInt()),
            topLeft = Offset.Zero,
            size = size
        )
    }
}
```

## Ключевые моменты

- **Canvas = чистая функция от GameState** — ничего не мутирует
- **Камера** — смещение от позиции игрока к центру экрана
- **Отсечение** — не рисуем объекты за экраном
- **Примитивы** — пока без спрайтов, просто круги/прямоугольники
- **День/ночь** — полупрозрачный overlay, интенсивность зависит от timeOfDay

## Связанные навыки

- [game-state.md](game-state.md) — что отрисовываем
- [game-loop.md](game-loop.md) — откуда приходит GameState
- [player-input.md](player-input.md) — как связать рендер с вводом
