## Архитектура приложения

Проект построен на базе архитектурного паттерна UDF (Unidirectional Data Flow) в связке с адаптированным ECS (Entity-Component-System) для игрового цикла. Данный подход обеспечивает строгую изоляцию бизнес-логики от слоя отрисовки, что минимизирует состояние гонки (race conditions) при мультиплеере и обеспечивает высокую модульность кода.

### 1. Высокоуровневая схема (Game Loop & Render)

Игровой цикл (Game Loop) работает в отдельной корутине, изолированно от UI. Состояние игры иммутабельно, и каждый кадр (тик) генерируется новое состояние на основе действий игроков и работы внутренних систем.


graph TD
subgraph InputLayer [Слой Ввода]
UI_Input[Ввод игрока: Джойстик, Кнопки]
Net_Input[Сетевые пакеты: Действия 2-го игрока]
end

    subgraph GameEngine [Игровой Движок - Корутина ~60 FPS]
        StartTick((Начало кадра))
        CurrentState[(Текущий GameState)]
        
        Sys_Physics[Physics System: Перемещение, Box2D коллизии]
        Sys_Combat[Combat System: Расчет урона, хитбоксы, PvP/PvE]
        Sys_AI[AI System: Стейт-машина мобов]
        Sys_Event[Environment System: Погода, Время суток, Алтарь]
        
        StartTick --> CurrentState
        CurrentState --> Sys_Physics
        Sys_Physics --> Sys_Combat
        Sys_Combat --> Sys_AI
        Sys_AI --> Sys_Event
        
        Sys_Event --> NewState[(Новый GameState)]
    end

    subgraph RenderLayer [Слой Отрисовки - Compose]
        StateFlow((StateFlow))
        CanvasRenderer[Canvas API: Спрайты, Тайлы, Погода]
        UIRenderer[Compose UI: HUD, Инвентарь, Меню]
    end

    UI_Input & Net_Input -->|User Actions| CurrentState
    NewState -->|Emit| StateFlow
    StateFlow -->|Рекомпозиция UI| UIRenderer
    StateFlow -->|Отрисовка кадра| CanvasRenderer
    NewState -.->|Следующий тик| StartTick

### 2. Управление состоянием (State Management)
Единственным источником истины является GameState. Изменения применяются через чистые функции (pure functions), принимающие предыдущий GameState и Action, и возвращающие новый GameState. Отсутствие мутабельных переменных в бизнес-логике критически важно для предсказуемой интерполяции в мультиплеере и упрощает написание модульных тестов.

### 3. Топология мультиплеера (Client-Server over WebSockets)

Используется клиент-серверная модель на базе Ktor. Один из игроков (Хост) берет на себя вычисление основного игрового цикла и является авторитетным сервером, пресекая рассинхронизацию.


sequenceDiagram
autonumber
participant C1 as Клиент 1 (Хост)
participant DB as Room (Локальная БД)
participant Srv as Ktor Server (Слой логики)
participant C2 as Клиент 2
participant Leader as PostgreSQL API (Лидерборд)

    C1->>Srv: InputAction (Ходьба, Удар мечом)
    C2->>Srv: InputAction (Поднятие лута)
    
    note over Srv: Игровой цикл (ECS):<br/>- Валидация действий<br/>- Обновление позиции/Инвентаря<br/>- Спавн погоды
    
    Srv->>C1: StateDelta (Новые координаты, Изменения инвентаря)
    Srv->>C2: StateDelta (Новые координаты, Изменения инвентаря)
    
    note over C1, C2: Jetpack Compose рендерит<br/>новое состояние на экранах
    
    opt Автосохранение / Выход
        C1->>DB: Save GameState (Сохранение в Room)
    end
    
    opt Окончание игры (Смерть)
        Srv->>Leader: Отправка результатов (Очки, Дни выживания)
        Leader-->>Srv: 200 OK
    end
### 4. Взаимодействие компонентов игры (ECS концепт)

Внутри GameState все объекты представлены в виде сущностей (Entities). Системы не хранят данные, а только фильтруют сущности по нужным компонентам и применяют к ним логику.

erDiagram
GAME-STATE ||--o{ ENTITY : contains
GAME-STATE {
int timeOfDay
enum weatherCondition
list entities
}
ENTITY ||--o| POSITION-COMPONENT : has
ENTITY ||--o| HEALTH-COMPONENT : has
ENTITY ||--o| AI-COMPONENT : has
ENTITY ||--o| INVENTORY-COMPONENT : has

    ENTITY {
        string id
        enum type
    }
    POSITION-COMPONENT {
        float x
        float y
        float rotation
    }
