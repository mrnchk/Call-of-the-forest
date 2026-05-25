## Архитектура приложения

Проект построен на базе архитектурного паттерна UDF (Unidirectional Data Flow) в связке с адаптированным ECS (Entity-Component-System) для игрового цикла. Данный подход обеспечивает строгую изоляцию бизнес-логики от слоя отрисовки, что минимизирует состояние гонки (race conditions) при мультиплеере и обеспечивает высокую модульность кода.

### 1. Высокоуровневая схема (Game Loop & Render)

Игровой цикл (Game Loop) работает в отдельной корутине, изолированно от UI. Состояние игры иммутабельно, и каждый кадр (тик) генерируется новое состояние на основе действий игроков и работы внутренних систем.


```mermaid
flowchart TD
    subgraph InputLayer["Слой Ввода"]
        UI_Input["Ввод игрока: Джойстик, Кнопки"]
        Net_Input["Сетевые пакеты: Действия 2-го игрока"]
    end

    subgraph GameEngine["Игровой Движок - Корутина ~60 FPS"]
        StartTick(("Начало кадра"))
        CurrentState[("Текущий GameState")]

        Sys_Physics["Physics System: Перемещение, Box2D коллизии"]
        Sys_Combat["Combat System: Расчет урона, хитбоксы, PvP/PvE"]
        Sys_AI["AI System: Стейт-машина мобов"]
        Sys_Event["Environment System: Погода, Время суток, Алтарь"]

        StartTick --> CurrentState
        CurrentState --> Sys_Physics
        Sys_Physics --> Sys_Combat
        Sys_Combat --> Sys_AI
        Sys_AI --> Sys_Event

        Sys_Event --> NewState[("Новый GameState")]
    end

    subgraph RenderLayer["Слой Отрисовки - Compose"]
        StateFlow(("StateFlow"))
        CanvasRenderer["Canvas API: Спрайты, Тайлы, Погода"]
        UIRenderer["Compose UI: HUD, Инвентарь, Меню"]
    end

    UI_Input -->|User Actions| CurrentState
    Net_Input -->|User Actions| CurrentState

    NewState -->|Emit| StateFlow
    StateFlow -->|Рекомпозиция UI| UIRenderer
    StateFlow -->|Отрисовка кадра| CanvasRenderer
    NewState -.->|Следующий тик| StartTick
```
### 2. Управление состоянием (State Management)
Единственным источником истины является GameState. Изменения применяются через чистые функции (pure functions), принимающие предыдущий GameState и Action, и возвращающие новый GameState. Отсутствие мутабельных переменных в бизнес-логике критически важно для предсказуемой интерполяции в мультиплеере и упрощает написание модульных тестов.

### 3. Топология мультиплеера (Client-Server over WebSockets)

Используется клиент-серверная модель на базе Ktor. Один из игроков (Хост) берет на себя вычисление основного игрового цикла и является авторитетным сервером, пресекая рассинхронизацию.


### 3. Топология мультиплеера (Client-Server over WebSockets)

```mermaid
sequenceDiagram
    autonumber

    participant C1 as Клиент 1 / Хост
    participant DB as Room / Локальная БД
    participant Srv as Ktor Server / Слой логики
    participant C2 as Клиент 2
    participant Leader as PostgreSQL API / Лидерборд

    C1->>Srv: InputAction: ходьба, удар мечом
    C2->>Srv: InputAction: поднятие лута

    Note over Srv: Игровой цикл ECS<br/>Валидация действий<br/>Обновление позиции и инвентаря<br/>Спавн погоды

    Srv->>C1: StateDelta: координаты, инвентарь
    Srv->>C2: StateDelta: координаты, инвентарь

    Note over C1,C2: Jetpack Compose рендерит<br/>новое состояние на экранах

    opt Автосохранение / Выход
        C1->>DB: Save GameState
    end

    opt Окончание игры / Смерть
        Srv->>Leader: Отправка результатов: очки, дни выживания
        Leader-->>Srv: 200 OK
    end
```

### 4. Взаимодействие компонентов игры (ECS концепт)

Внутри GameState все объекты представлены в виде сущностей (Entities). Системы не хранят данные, а только фильтруют сущности по нужным компонентам и применяют к ним логику.

### 4. Взаимодействие компонентов игры (ECS концепт)

```mermaid
erDiagram
    GAME_STATE ||--o{ ENTITY : contains
    ENTITY ||--o| POSITION_COMPONENT : has
    ENTITY ||--o| HEALTH_COMPONENT : has
    ENTITY ||--o| AI_COMPONENT : has
    ENTITY ||--o| INVENTORY_COMPONENT : has

    GAME_STATE {
        int timeOfDay
        string weatherCondition
        string entities
    }

    ENTITY {
        string id
        string type
    }

    POSITION_COMPONENT {
        float x
        float y
        float rotation
    }

    HEALTH_COMPONENT {
        int currentHealth
        int maxHealth
    }

    AI_COMPONENT {
        string state
        string targetEntityId
    }

    INVENTORY_COMPONENT {
        string items
        int capacity
    }
```