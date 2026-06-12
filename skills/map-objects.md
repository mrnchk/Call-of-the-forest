# Навык: Карта и объекты

## Суть

Простая процедурная карта с деревьями, камнями и кустами. Без тайтлов — просто фоновый цвет + объекты.

## Генерация карты

Карта генерируется в `GameEngine.generateMap()` (см. [game-loop.md](game-loop.md)):

```kotlin
private fun generateMap(): List<MapObject> {
    val objects = mutableListOf<MapObject>()
    repeat(40) { i ->
        objects.add(MapObject("tree_$i", ObjectType.TREE,
            x = (Math.random() * 2000 - 1000).toFloat(),
            y = (Math.random() * 2000 - 1000).toFloat()
        ))
    }
    repeat(20) { i ->
        objects.add(MapObject("rock_$i", ObjectType.ROCK,
            x = (Math.random() * 2000 - 1000).toFloat(),
            y = (Math.random() * 2000 - 1000).toFloat(),
            radius = 16f
        ))
    }
    repeat(15) { i ->
        objects.add(MapObject("bush_$i", ObjectType.BUSH,
            x = (Math.random() * 2000 - 1000).toFloat(),
            y = (Math.random() * 2000 - 1000).toFloat(),
            radius = 12f
        ))
    }
    return objects
}
```

## Коллизия игрока с объектами

Простая круг-круг проверка прямо в GameLoop:

```kotlin
// В GameEngine.update()
var px = newPlayer.x
var py = newPlayer.y

for (obj in state.objects) {
    val distX = px - obj.x
    val distY = py - obj.y
    val dist = sqrt(distX * distX + distY * distY)
    val minDist = newPlayer.radius + obj.radius
    if (dist < minDist && dist > 0f) {
        // Отталкиваем игрока от объекта
        val overlap = minDist - dist
        px += (distX / dist) * overlap
        py += (distY / dist) * overlap
    }
}
```

## Что добавить потом

- Тайловая карта вместо фонового цвета
- Процедурная генерация (шум Перлина)
- Ресурсы (можно добывать дерево/камень)
- Лут (подбираемые предметы)

## Связанные навыки

- [game-state.md](game-state.md) — MapObject и ObjectType
- [game-loop.md](game-loop.md) — генерация и коллизии
- [canvas-render.md](canvas-render.md) — отрисовка объектов
