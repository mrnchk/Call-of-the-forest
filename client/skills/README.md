# Навыки проекта Call of the Forest — Phase 2

Минимально рабочий прототип: игрок двигается по карте, видит мир через Canvas, есть заглушки для будущего.

## Что работает в MVP

- ✅ Игрок двигается джойстиком / тачем
- ✅ Рендер 2D мира через Canvas
- ✅ Immutable GameState + StateFlow
- ✅ GameLoop ~60 FPS
- ✅ Простые статичные объекты на карте (деревья, камни, кусты)
- ✅ Главное меню с кнопками Play / Login / Exit
- ✅ Экран входа (Username + Password, локальное сохранение)
- ✅ Пауза (кнопка + Back) с overlay Resume / Exit to Menu
- ✅ Навигация между экранами (NavHost)
- ✅ Лесная цветовая тема (Forest palette)
- ✅ AI мобов — стейт-машина (WOLF chase, DEER flee, GOBLIN patrol)
- ✅ Боевая система — атака в направлении взгляда, урон, knockback, кулдаун
- ✅ Мобы атакуют игрока при контакте
- ✅ Game Over overlay при смерти
- ✅ Инвентарь — сбор ресурсов (Wood/Stone/Berry) с объектов
- ✅ Механика голода — убывает со временем, ночью быстрее
- ✅ Ночной холод — урон HP ночью
- ✅ Ягоды восстанавливают голод
- ✅ HUD: HP бар, Hunger бар, Quick Inventory

## Что заглушено (на будущее)

- ⬜ Крафт система
- ⬜ Мультиплеер → Фаза 3 (Ktor WebSockets)
- ⬜ Глобальные события (гроза, вулкан) → Фаза 3
- ⬜ Алтарь → Фаза 3
- ⬜ Room сохранение → Фаза 4
- ⬜ Лидерборд → Фаза 4
- ⬜ Hilt DI → отложен до совместимости KSP с Kotlin 2.2.10

## Навыки

| # | Навык | Файл | Статус | Реализация |
|---|---|---|---|---|
| 1 | GameState | [game-state.md](game-state.md) | ✅ Готово | `core/engine/GameState.kt` |
| 2 | GameLoop | [game-loop.md](game-loop.md) | ✅ Готово | `core/engine/GameEngine.kt` — 10-step pipeline |
| 3 | Canvas Render | [canvas-render.md](canvas-render.md) | ✅ Готово | `render/GameRenderer.kt` |
| 4 | Player Input | [player-input.md](player-input.md) | ✅ Готово | `ui/Joystick.kt` + action buttons |
| 5 | Map & Objects | [map-objects.md](map-objects.md) | ✅ Готово | Встроено в `GameEngine.kt` (harvestable) |
| 6 | Stubs | [stubs.md](stubs.md) | ✅ Готово | Заглушки убраны — AI/Combat/Inventory/Hunger реализованы |
| 7 | Main Menu | — | ✅ Готово | `ui/screens/MainMenuScreen.kt` |
| 8 | Login Screen | — | ✅ Готово | `ui/screens/LoginScreen.kt` |
| 9 | Pause Menu | — | ✅ Готово | `ui/components/PauseOverlay.kt` |
| 10 | Navigation | — | ✅ Готово | `navigation/Routes.kt` + NavHost |
| 11 | AI Mobs | [ai-mobs.md](ai-mobs.md) | ✅ Готово | `GameEngine.updateMobAI()` — WOLF/DEER/GOBLIN state machine |
| 12 | Combat | [combat.md](combat.md) | ✅ Готово | `GameEngine.updatePlayerAttack()` + mob damage in collisions |
| 13 | Inventory | [inventory.md](inventory.md) | ✅ Готово | `GameEngine.updatePlayerHarvest()` + `ResourceType` enum |
| 14 | Hunger/Survival | [hunger.md](hunger.md) | ✅ Готово | `GameEngine.updateSurvival()` — hunger drain + cold damage |

## Структура проекта

```
app/src/main/java/com/cotf/
├── CotfApp.kt                  — Application + UserSession
├── MainActivity.kt             — NavHost: Menu → Login → Game
├── core/engine/
│   ├── GameState.kt            — Player, Mob, MapObject, BehaviorState, ResourceType, AttackEffect
│   └── GameEngine.kt           — 10-step update pipeline: move→attack→harvest→AI→collisions→survival→camera→time→effects→gameover
├── navigation/Routes.kt
├── render/GameRenderer.kt      — Mob type rendering, HP bars, attack effects, harvest highlights
├── session/UserSession.kt
├── ui/
│   ├── Joystick.kt
│   ├── components/
│   │   ├── ForestButton.kt
│   │   ├── GameOverOverlay.kt
│   │   ├── HudBars.kt          — HP bar, Hunger bar, Quick inventory
│   │   └── PauseOverlay.kt
│   ├── screens/
│   │   ├── GameScreen.kt       — HUD + Attack/Harvest/Berry/Pause buttons
│   │   ├── LoginScreen.kt
│   │   └── MainMenuScreen.kt
│   └── theme/
└── viewmodel/GameViewModel.kt  — + requestAttack/Harvest/ConsumeBerry
```
