package com.cotf.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.cotf.core.engine.GameEngine
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Виртуальный джойстик для управления игроком.
 * Тач-ввод → нормализованный вектор → GameEngine.setInput().
 */
@Composable
fun VirtualJoystick(
    engine: GameEngine,
    modifier: Modifier = Modifier
) {
    var stickX by remember { mutableFloatStateOf(0f) }
    var stickY by remember { mutableFloatStateOf(0f) }

    val outerRadius = 70f
    val innerRadius = 25f

    Canvas(
        modifier = modifier
            .size(150.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        stickX = 0f
                        stickY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        stickX += dragAmount.x
                        stickY += dragAmount.y

                        // Ограничиваем радиус
                        val dist = sqrt(stickX * stickX + stickY * stickY)
                        if (dist > outerRadius) {
                            stickX = stickX / dist * outerRadius
                            stickY = stickY / dist * outerRadius
                        }

                        // Нормализуем -1..1 и отправляем в движок
                        engine.setInput(
                            x = stickX / outerRadius,
                            y = stickY / outerRadius
                        )
                    },
                    onDragEnd = {
                        stickX = 0f
                        stickY = 0f
                        engine.setInput(0f, 0f)
                    },
                    onDragCancel = {
                        stickX = 0f
                        stickY = 0f
                        engine.setInput(0f, 0f)
                    }
                )
            }
    ) {
        // Внешний круг
        drawCircle(
            color = Color(0x40FFFFFF),
            radius = outerRadius,
            center = center
        )
        // Внутренний круг (стик)
        drawCircle(
            color = Color(0x80FFFFFF),
            radius = innerRadius,
            center = Offset(center.x + stickX, center.y + stickY)
        )
    }
}
