# Навык: Голода и выживание

## Суть

Механика голода и холода — игрок должен есть и переживать ночи.

## Голод

- `Player.hunger: Float` — 0..100, начинается на 100
- Днём убывает: **-2 в минуту**
- Ночью убывает быстрее: **-5 в минуту**
- При hunger = 0: игрок теряет **1 HP в секунду**
- Урон от голода аккумулируется через `hungerDmgAccum: Float` (конвертируется в Int HP)

## Ночной холод

- Ночь: `timeOfDay < 0.2 || timeOfDay > 0.8`
- Ночью: **-1 HP в секунду** от холода
- Урон аккумулируется через `coldDmgAccum: Float`
- Днём аккумулятор сбрасывается

## Ягоды

- **Кнопка Eat Berry** → `GameEngine.requestConsumeBerry()`
- Видна только если `inventory[BERRY] > 0`
- Эффект: hunger +25 (capped at 100), berry count -1

## HUD

- **HP Bar** — зелёная > 50%, жёлтая > 25%, красная ниже
- **Hunger Bar** — жёлто-оранжевая > 40%, красная ниже

## Код

```kotlin
// GameEngine.kt
private fun updateSurvival(state, deltaSec): GameState {
    val isNight = state.timeOfDay < 0.2f || state.timeOfDay > 0.8f
    val hungerRate = if (isNight) 5f / 60f else 2f / 60f
    var newHunger = max(0f, player.hunger - hungerRate * deltaSec)
    // accumulator pattern для дробного урона → Int HP
}
```

## Связанные навыки

- [inventory.md](inventory.md) — ягоды из инвентаря
- [combat.md](combat.md) — другой источник урона
