# Навык: AI мобов

## Суть

Стейт-машина для мобов: каждый тип моба имеет своё поведение. Мобы спавнятся при генерации карты.

## Типы мобов

| Моб | HP | Скорость | Агро-радиус | Поведение |
|---|---|---|---|---|
| WOLF | 60 | 140 | 200px | Chase → Attack, Flee при hp<20% |
| DEER | 30 | 120 | 150px | Flee от игрока, Patrol в покое |
| GOBLIN | 50 | 100 | 180px | Patrol → Chase → Attack |

## Стейт-машина

```
WOLF:   IDLE → CHASE (<200px) → ATTACK (<40px) → FLEE (hp<20%)
DEER:   IDLE/PATROL → FLEE (<150px, speed×1.3)
GOBLIN: PATROL → CHASE (<180px) → ATTACK (<35px)
```

## Реализация

- `BehaviorState` enum: IDLE, PATROL, CHASE, ATTACK, FLEE
- `GameEngine.updateMobAI()` — обрабатывает каждого моба каждый кадр
- Мобы двигаются к/от игрока в зависимости от состояния
- PATROL: моб бродит к случайным точкам (patrolTargetX/Y)
- Статы мобов: extension свойства на `MobType` (baseSpeed, baseMaxHp, aggroRange, attackRange, attackDamage)

## Спавн

В `generateMap()`: 8 волков, 6 оленей, 5 гоблинов. Позиции случайные в области -800..800.

## Связанные навыки

- [game-state.md](game-state.md) — Mob data class, BehaviorState, MobType
- [combat.md](combat.md) — мобы наносят урон при ATTACK контакте
