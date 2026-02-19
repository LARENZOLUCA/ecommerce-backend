
# bbig project

Backend-сервис интернет-магазина на Ktor с JWT авторизацией, PostgreSQL, Redis кэшированием и RabbitMQ.

Данный проект был создан с использованием [Ktor Project Generator](https://start.ktor.io).

## Полезные ссылки

- [Ktor Documentation](https://ktor.io/docs/home.html)
- [Ktor GitHub page](https://github.com/ktorio/ktor)
- [Ktor Slack chat](https://app.slack.com/client/T09229ZC6/C0A974TJ9) - [запросить приглашение](https://surveys.jetbrains.com/s3/kotlin-slack-sign-up)


## Технологии

- **Ktor** - web-фреймворк
- **PostgreSQL** - база данных
- **Exposed** - ORM
- **Flyway** - миграции БД
- **JWT** - авторизация
- **Redis** - кэширование
- **RabbitMQ** - очередь сообщений
- **Swagger/OpenAPI** - документация API
- **Docker** - контейнеризация
- **TestContainers** - интеграционное тестирование


## Функциональность

### Пользователи

- Регистрация и JWT авторизация
- Разделение прав (USER/ADMIN)


### Товары

- Просмотр списка товаров (все пользователи)
- Просмотр товара по ID (все пользователи)
- Создание товара (только ADMIN)
- Обновление товара (только ADMIN)
- Удаление товара (только ADMIN)


### Заказы

- Создание заказа с проверкой наличия товара
- Просмотр истории заказов
- Отмена заказа (только для владельца)
- Автоматическое уменьшение stock при заказе
- Возврат товаров при отмене


### Дополнительно

- Кэширование товаров в Redis (TTL 5 минут)
- Отправка событий в RabbitMQ при создании заказа
- Фоновая обработка сообщений (email-уведомления)
- Swagger UI документация
- Unit и Integration тесты


## Запуск проекта

### Предварительные требования

- JDK 17 или выше
- Docker Desktop (для Windows/Mac) или Docker Engine (для Linux)
- Gradle (или используйте `./gradlew`)


### 1. Клонирование репозитория

```bash
git clone [https://github.com/LARENZOLUCA/ecommerce-backend.git](https://github.com/LARENZOLUCA/ecommerce-backend.git)
cd ecommerce-backend
```


### 2. Запуск инфраструктуры (PostgreSQL, Redis, RabbitMQ)

#### 2.1 Проверьте, что Docker запущен

```bash
docker --version
docker-compose --version
```


#### 2.2 Запустите все сервисы

```bash
docker-compose up -d
```

Данная команда запустит в фоновом режиме:

- **PostgreSQL** на порту 5432
- **Redis** на порту 6379
- **RabbitMQ** на портах 5672 (приложение) и 15672 (веб-интерфейс)


#### 2.3 Проверьте, что все контейнеры запущены

```bash
docker ps
```

Вы должны увидеть три запущенных контейнера:

- `ecommerce-postgres`
- `ecommerce-redis`
- `ecommerce-rabbitmq`


#### 2.4 Проверьте доступность сервисов

**PostgreSQL:**

```bash
docker exec -it ecommerce-postgres psql -U postgres -d ecommerce -c "SELECT 1;"
```

**Redis:**

```bash
docker exec -it ecommerce-redis redis-cli ping
# Должен ответить: PONG
```

**RabbitMQ:**

```bash
docker exec -it ecommerce-rabbitmq rabbitmqctl ping
# Должен ответить: Ping succeeded
```


#### 2.5 Доступ к веб-интерфейсам

- **RabbitMQ Management UI:** http://localhost:15672 (guest/guest)
- **PostgreSQL (через любой клиент):** `localhost:5432`, database `ecommerce`, user `postgres`, password `postgres`


### 3. Запуск приложения

#### 3.1 Сборка проекта

```bash
./gradlew clean build
```


#### 3.2 Запуск приложения

```bash
./gradlew run
```


#### 3.3 Проверка работы

- **API:** http://localhost:8080
- **Swagger UI:** http://localhost:8080/swagger-ui
- **OpenAPI:** http://localhost:8080/openapi

При успешном запуске вы увидите:

```
2026-02-19 11:23:59.379 [main] INFO  Application - Application started in 1.707 seconds.
2026-02-19 11:23:59.420 [DefaultDispatcher-worker-1] INFO  Application - Responding at [http://127.0.0.1:8080](http://127.0.0.1:8080)
```


### 4. Остановка проекта

```bash
# Остановка приложения (Ctrl+C в терминале с приложением)

# Остановка инфраструктуры
docker-compose down

# Полная очистка (включая данные БД)
docker-compose down -v
```


## API Endpoints

### Аутентификация

| Метод | Endpoint | Описание |
| :-- | :-- | :-- |
| POST | /auth/register | Регистрация нового пользователя |
| POST | /auth/login | Вход в систему |

### Пользователи

| Метод | Endpoint | Описание |
| :-- | :-- | :-- |
| GET | /user/profile | Профиль текущего пользователя |

### Товары (публичные)

| Метод | Endpoint | Описание |
| :-- | :-- | :-- |
| GET | /products | Список всех товаров |
| GET | /products/{id} | Информация о товаре |

### Товары (админские)

| Метод | Endpoint | Описание |
| :-- | :-- | :-- |
| POST | /products | Создание товара |
| PUT | /products/{id} | Обновление товара |
| DELETE | /products/{id} | Удаление товара |

### Заказы

| Метод | Endpoint | Описание |
| :-- | :-- | :-- |
| POST | /orders | Создание заказа |
| GET | /orders | История заказов |
| DELETE | /orders/{id} | Отмена заказа |

### Статистика (админские)

| Метод | Endpoint | Описание |
| :-- | :-- | :-- |
| GET | /stats/orders | Статистика заказов |

## Тестирование

| Task | Description |
| :-- | :-- |
| `./gradlew test` | Запуск всех тестов |
| `./gradlew test --tests "com.example.unit.*"` | Запуск unit-тестов |
| `./gradlew test --tests "com.example.integration.*"` | Запуск integration-тестов |
| `./gradlew build` | Сборка проекта |
| `./gradlew buildFatJar` | Сборка исполняемого JAR со всеми зависимостями |
| `./gradlew run` | Запуск сервера |

### Отчет о тестировании

После выполнения тестов откройте:

```
build/reports/tests/test/index.html
```


## Docker

| Task | Description |
| :-- | :-- |
| `./gradlew buildImage` | Сборка Docker образа |
| `./gradlew publishImageToLocalRegistry` | Публикация образа в локальный реестр |
| `./gradlew runDocker` | Запуск в Docker контейнере |
| `docker-compose up -d` | Запуск инфраструктуры (PostgreSQL, Redis, RabbitMQ) |
| `docker-compose down` | Остановка инфраструктуры |

### Просмотр логов

```bash
docker-compose logs -f [service]
```


## Структура проекта

```
src/
├── main/
│   └── kotlin/
│       └── com/example/
│           ├── config/          # Конфигурации (Database, Redis, RabbitMQ)
│           ├── dto/              # Data Transfer Objects
│           ├── model/            # Domain модели
│           ├── plugins/          # Ktor плагины
│           ├── repository/        # Репозитории для работы с БД
│           ├── service/           # Бизнес-логика
│           ├── worker/            # Фоновые задачи (RabbitMQ consumer)
│           └── Application.kt     # Точка входа
└── test/
    └── kotlin/
        └── com/example/
            ├── unit/              # Unit-тесты
            └── integration/       # Integration-тесты с TestContainers
```


## База данных

### Таблицы

- **users** - пользователи
- **products** - товары
- **orders** - заказы
- **order_items** - позиции заказов
- **audit_logs** - логи аудита


### Миграции Flyway

Все миграции находятся в `src/main/resources/db/migration/`

## Конфигурация

Основные настройки в `application.yaml`:

```yaml
database:
  url: "jdbc:postgresql://localhost:5432/ecommerce"
  user: "postgres"
  password: "postgres"
  maxPoolSize: 10

redis:
  host: "localhost"
  port: 6379

rabbitmq:
  host: "localhost"
  port: 5672
  username: "guest"
  password: "guest"
  queueName: "order-events"

jwt:
  secret: "your-secret-key"
  issuer: "ecommerce-app"
  audience: "ecommerce-users"
  expiresIn: 86400000
```


## Примеры запросов

### Регистрация

```bash
$body = @{
    email = "user@example.com"
    password = "123456"
    firstName = "John"
    lastName = "Doe"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/auth/register" `
  -Method Post `
  -Body $body `
  -ContentType "application/json"
```

### Назначание роли Admin 

```bash
docker exec -it ecommerce-postgres psql -U postgres -d ecommerce -c "UPDATE users SET role = 'ADMIN' WHERE email = 'admin@example.com';"
docker exec -it ecommerce-postgres psql -U postgres -d ecommerce -c "SELECT id, email, role FROM users WHERE email = 'admin@example.com';"
```


### Логин

```bash
$loginBody = @{
    email = "user@example.com"
    password = "123456"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8080/auth/login" `
  -Method Post `
  -Body $loginBody `
  -ContentType "application/json"

$token = $response.token
Write-Host "Токен: $token"
```

### Логин как Admin
```bash
$adminLoginBody = @{
    email = "admin@example.com"
    password = "admin123"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8080/auth/login" `
  -Method Post `
  -Body $adminLoginBody `
  -ContentType "application/json"

$adminToken = $response.token
$adminHeaders = @{ Authorization = "Bearer $adminToken" }

Write-Host "Токен админа: $adminToken"
Write-Host "Роль: $($response.role)"
```
### Создание заказа

```bash
$orderBody = @{
    items = @(
        @{ productId = 1; quantity = 2 }
    )
} | ConvertTo-Json

$headers = @{ Authorization = "Bearer $token" }

Invoke-RestMethod -Uri "http://localhost:8080/orders" `
  -Method Post `
  -Headers $headers `
  -Body $orderBody `
  -ContentType "application/json"
```


### Получение списка товаров
```bash
Invoke-RestMethod -Uri "http://localhost:8080/products" | ConvertTo-Json -Depth 3
```


### Создание товара (только ADMIN)
```bash
$productBody = @{
    name = "Новый товар"
    description = "Описание товара"
    price = 999.99
    stock = 10
} | ConvertTo-Json

$headers = @{ Authorization = "Bearer $adminToken" }

Invoke-RestMethod -Uri "http://localhost:8080/products" `
  -Method Post `
  -Headers $headers `
  -Body $productBody `
  -ContentType "application/json"
```


### История заказов
```bash
$headers = @{ Authorization = "Bearer $token" }

Invoke-RestMethod -Uri "http://localhost:8080/orders" `
  -Headers $headers | ConvertTo-Json -Depth 3
```


### Отмена заказа
```bash
$orderId = 1
$headers = @{ Authorization = "Bearer $token" }

Invoke-RestMethod -Uri "http://localhost:8080/orders/$orderId" `
  -Method Delete `
  -Headers $headers
```
