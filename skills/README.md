# Навыки проекта Call of the Forest — MVP v0.1

Минимально рабочий прототип: игрок двигается по карте, видит мир через Canvas, есть заглушки для будущего.

## Что работает в MVP

- ✅ Игрок двигается джойстиком / тачем
- ✅ Рендер 2D мира через Canvas
- ✅ Immutable GameState + StateFlow
- ✅ GameLoop ~60 FPS
- ✅ Простые статичные объекты на карте (деревья, камни)

## Что заглушено (на будущее)

- ⬜ AI мобов → заглушка (мобы стоят на месте)
- ⬜ Боевая система → заглушка (нет урона)
- ⬜ Мультиплеер → заглушка (singleplayer only)
- ⬜ Room сохранение → заглушка (в памяти)
- ⬜ Инвентарь → заглушка
- ⬜ Hilt DI → отложен до совместимости KSP с Kotlin 2.2.10

## Навыки

| # | Навык | Файл | Статус | Реализация |
|---|---|---|---|---|
| 1 | GameState | [game-state.md](game-state.md) | ✅ Готово | `core/engine/GameState.kt` |
| 2 | GameLoop | [game-loop.md](game-loop.md) | ✅ Готово | `core/engine/GameEngine.kt` |
| 3 | Canvas Render | [canvas-render.md](canvas-render.md) | ✅ Готово | `render/GameRenderer.kt` |
| 4 | Player Input | [player-input.md](player-input.md) | ✅ Готово | `ui/Joystick.kt` + `MainActivity.kt` |
| 5 | Map & Objects | [map-objects.md](map-objects.md) | ✅ Готово | Встроено в `GameEngine.kt` |
| 6 | Stubs | [stubs.md](stubs.md) | ✅ Готово | Заглушки в GameState (Mob, MobType.IDLE) |

## Структура проекта (реализовано)

```
app/src/main/java/com/cotf/
├── CotfApp.kt                  — Application (Hilt убран до совместимости KSP)
├── MainActivity.kt             — ComponentActivity, Compose UI
├── core/engine/
│   ├── GameState.kt            — data classes: GameState, Player, Camera, MapObject, Mob
│   └── GameEngine.kt           — Coroutine loop ~60 FPS, setInput(), collision, map gen
├── render/
│   └── GameRenderer.kt         — Compose Canvas: draw trees/rocks/bushes/player, day/night
├── ui/
│   ├── Joystick.kt             — VirtualJoystick composable
│   └── theme/                  — Color, Theme, Typography
└── stub/                       — (пока пусто, заглушки в GameState)
```

## Зависимости

| Библиотека | Версия | Статус |
|---|---|---|
| Jetpack Compose | BOM 2026.02.01 | ✅ Используется |
| JBox2D | 2.2.1.1 | ✅ Подключена |
| Hilt | 2.51.1 | ⬸ Отложена (KSP несовместим с Kotlin 2.2.10) |
| Room | 2.6.1 | ⬸ Отложена (требует KSP) |
| Ktor Client | 2.3.12 | ⬸ Отложена (для Фазы 3 мультиплеера) |

> **Примечание:** Hilt, Room, Ktor закомментированы в libs.versions.toml и build.gradle.kts.
> Когда KSP выпустит версию для Kotlin 2.2.10 — раскомментировать и добавить ksp()-зависимости.
