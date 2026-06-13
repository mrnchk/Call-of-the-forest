# Навык: Боевая система

## Суть

Игрок атакует мобов melee-ударом в направлении взгляда. Мобы наносят урон при контакте в состоянии ATTACK.

## Механика игрока

- **Кнопка Attack** на экране → `GameEngine.requestAttack()`
- Удар в направлении `player.direction`, дальность = `player.radius * 2.5`
- Точка атаки: `player.x + cos(direction) * radius * 1.5`
- Урон: 25 HP за удар
- Кулдаун: 0.4 секунды
- Knockback: 30px от игрока к мобу
- Визуальный эффект: белый круг (AttackEffect, 0.15с)

## Механика мобов

- Моб наносит урон при контакте (collision) если `state == ATTACK` и `attackCooldown <= 0`
- Урон: WOLF=12, GOBLIN=10, DEER=5
- Кулдаун моба: 1.0 секунды
- Моб умирает при hp ≤ 0 (удаляется из списка)

## Game Over

- При `player.hp ≤ 0` → `isGameOver = true`, `isRunning = false`
- Показывается GameOverOverlay с "YOU DIED" и кнопкой "Return to Menu"

## Код

```kotlin
// GameEngine.kt
fun requestAttack() { attackRequested = true }

private fun updatePlayerAttack(state, deltaSec): GameState {
    // Проверяем кулдаун и флаг attackRequested
    // Ищем мобов в радиусе атаки перед игроком
    // Наносим урон + knockback
    // Добавляем AttackEffect
}
```

## Связанные навыки

- [ai-mobs.md](ai-mobs.md) — мобы переходят в ATTACK состояние
- [hunger.md](hunger.md) — другой источник урона (голод/холод)
