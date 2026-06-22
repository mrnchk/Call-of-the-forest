## Архитектура приложения

Проект построен на базе архитектурного паттерна UDF (Unidirectional Data Flow) в связке с адаптированным ECS (Entity-Component-System) для игрового цикла. Данный подход отделяет игровую логику от слоя отрисовки и упрощает работу с состоянием игры.

### 1. Высокоуровневая схема (Game Loop & Render)

Игровой цикл (Game Loop) работает в корутине. Состояние игры хранится в `GameState`, обновляется внутри `GameEngine` и передается в UI через `StateFlow`.

```mermaid
flowchart TD
    subgraph InputLayer["Слой ввода"]
        UI_Input["Ввод игрока: джойстик, кнопки"]
    end

    subgraph GameEngine["Игровой движок - корутина ~60 FPS"]
        StartTick(("Начало кадра"))
        CurrentState[("Текущий GameState")]

        Sys_Movement["Movement System: перемещение игрока"]
        Sys_Combat["Combat System: атака, урон, knockback"]
        Sys_Harvest["Harvest System: сбор ресурсов"]
        Sys_AI["AI System: стейт-машина мобов"]
        Sys_Collision["Collision System: базовые круговые коллизии"]
        Sys_Survival["Survival System: здоровье, голод, ночной холод"]
        Sys_Time["Time System: смена дня и ночи"]

        StartTick --> CurrentState
        CurrentState --> Sys_Movement
        Sys_Movement --> Sys_Combat
        Sys_Combat --> Sys_Harvest
        Sys_Harvest --> Sys_AI
        Sys_AI --> Sys_Collision
        Sys_Collision --> Sys_Survival
        Sys_Survival --> Sys_Time

        Sys_Time --> NewState[("Новый GameState")]
    end

    subgraph RenderLayer["Слой отрисовки - Compose"]
        StateFlow(("StateFlow"))
        CanvasRenderer["Canvas API: мир, объекты, мобы, игрок"]
        UIRenderer["Compose UI: HUD, инвентарь, меню"]
    end

    UI_Input -->|PlayerInput| CurrentState

    NewState -->|Emit| StateFlow
    StateFlow -->|Рекомпозиция UI| UIRenderer
    StateFlow -->|Отрисовка кадра| CanvasRenderer
    NewState -.->|Следующий тик| StartTick
```

### 2. Управление состоянием (State Management)

Единственным источником истины является `GameState`. Он содержит данные игрока, мобов, объектов карты, времени, состояния паузы, game over и статистики партии.

`GameViewModel` держит состояние в `StateFlow`, принимает ввод пользователя и вызывает методы `GameEngine`. UI только отображает текущее состояние и отправляет пользовательские действия.

### 3. Сетевая интеграция

Клиент взаимодействует с backend через REST API на Retrofit/OkHttp.

```mermaid
sequenceDiagram
    autonumber

    participant UI as Android UI
    participant VM as ViewModel
    participant API as Retrofit API
    participant Srv as Spring Boot Backend
    participant DB as PostgreSQL

    UI->>VM: register / signin / refresh
    VM->>API: HTTP request
    API->>Srv: Auth request
    Srv->>DB: User data
    DB-->>Srv: Result
    Srv-->>API: JWT tokens
    API-->>VM: Auth response
    VM-->>UI: Session state

    UI->>VM: Game over
    VM->>API: Submit game result
    API->>Srv: Score, survived time, resources, kills
    Srv->>DB: Save result
    DB-->>Srv: OK
    Srv-->>API: Leaderboard entry
```

### 4. Взаимодействие компонентов игры

Игровая логика разделена на независимые системы внутри `GameEngine`:

- перемещение игрока;
- атака и расчет урона;
- сбор ресурсов;
- употребление ягод;
- AI мобов;
- коллизии;
- голод, здоровье и ночной холод;
- смена времени суток;
- подсчет статистики партии.

```mermaid
erDiagram
    GAME_STATE ||--|| PLAYER : contains
    GAME_STATE ||--o{ MOB : contains
    GAME_STATE ||--o{ MAP_OBJECT : contains
    GAME_STATE ||--|| GAME_STATS : contains

    GAME_STATE {
        float timeOfDay
        int dayCount
        boolean isPaused
        boolean isGameOver
    }

    PLAYER {
        float x
        float y
        int health
        float hunger
        string inventory
    }

    MOB {
        string type
        float x
        float y
        int health
        string behaviorState
    }

    MAP_OBJECT {
        string type
        float x
        float y
        int amount
        boolean depleted
    }

    GAME_STATS {
        int score
        int mobsKilled
        int resourcesCollected
        long survivedMillis
    }
```