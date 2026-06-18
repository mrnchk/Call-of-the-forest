# Навык: Player Input

## Суть

Виртуальный джойстик для управления игроком. Тач-ввод → нормализованный вектор → GameEngine.setInput().

## Код

```kotlin
// ui/Joystick.kt
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
                    onDragStart = { offset ->
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
```

## MainActivity — собираем всё вместе

```kotlin
// MainActivity.kt
package com.cotf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cotf.core.engine.GameEngine
import com.cotf.render.GameRenderer
import com.cotf.ui.VirtualJoystick

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val engine = remember { GameEngine() }

            Box(modifier = Modifier.fillMaxSize()) {
                // Рендер мира (на весь экран)
                GameRenderer(engine = engine)

                // Джойстик (внизу слева)
                VirtualJoystick(
                    engine = engine,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(32.dp)
                )
            }
        }
    }
}
```

## Ключевые моменты

- **detectDragGestures** — палец вниз → двигается → вверх
- **Нормализация -1..1** — GameEngine не знает про пиксели джойстика
- **onDragEnd** — отпустил палец = скорость 0
- **remember { GameEngine() }** — один экземпляр на всё время жизни Compose

## Связанные навыки

- [game-loop.md](game-loop.md) — куда приходит setInput
- [canvas-render.md](canvas-render.md) — рендер результата движения
- [game-state.md](game-state.md) — Player.x/y обновляются
