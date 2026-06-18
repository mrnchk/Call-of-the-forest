# COTF Server

Spring Boot 3.3.1 бэкенд для авторизации по JWT.

## Технологии

- **Kotlin** 1.9.24 / **Java** 17
- **Spring Boot** 3.3.1 (Web, Data JPA, Validation)
- **PostgreSQL** 16
- **JJWT** 0.12.6 (access + refresh токены)
- **jBCrypt** для хеширования паролей

## API Endpoints

| Метод  | Путь                 | Авторизация | Описание                        |
|--------|----------------------|-------------|---------------------------------|
| POST   | `/api/auth/register` | Нет         | Регистрация, возвращает токены  |
| POST   | `/api/auth/signin`   | Нет         | Вход, возвращает токены         |
| POST   | `/api/auth/refresh`  | Нет         | Обновление access-токена        |
| GET    | `/api/auth/me`       | Да (JWT)    | Информация о текущем пользователе |

## Переменные окружения

| Переменная               | По умолчанию | Описание                              |
|--------------------------|--------------|---------------------------------------|
| `DB_HOST`                | `localhost`  | Хост PostgreSQL                       |
| `DB_PORT`                | `5432`       | Порт PostgreSQL                       |
| `DB_NAME`                | `cotf`       | Имя базы данных                       |
| `DB_USERNAME`            | `cotf`       | Пользователь БД                       |
| `DB_PASSWORD`            | —            | Пароль БД (обязательно)               |
| `JWT_SECRET`             | —            | Секрет для подписи JWT (обязательно)  |
| `JWT_ACCESS_EXPIRATION`  | `900000`     | Время жизни access-токена (мс, 15 мин) |
| `JWT_REFRESH_EXPIRATION` | `604800000`  | Время жизни refresh-токена (мс, 7 дней) |

## Запуск

### 1. Настроить .env

```bash
cp .env.example .env
```

Заполните `.env` реальными значениями:

```env
# Database
DB_NAME=cotf
DB_USERNAME=cotf
DB_PASSWORD=
DB_PORT=5432
JWT_SECRET=changeme-to-a-secure-random-string-at-least-32-chars
```

### 2. Запустить PostgreSQL через Docker

```bash
docker compose up -d
```

Поднимется PostgreSQL 16 на порту 5432. Настройки берутся из `.env`.

### 3. Запустить сервер локально

**Требования**: JDK 17

```bash
./gradlew bootRun
```

Сервер будет доступен на `http://localhost:8080`.

## Остановка PostgreSQL

```bash
docker compose down
docker compose down -v # очистить бд
```

