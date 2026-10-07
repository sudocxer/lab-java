# Лабораторная работа №7 (вариант 9). JDBC и работа с базой данных

Отдельный Maven-проект (`war`), группа `kz.edu`, artifactId `web-service-lab7-v9`.

## Что реализовано

Индивидуальное задание, **вариант 9**: CRUD заказов (таблица `orders`: `id`, `customer`, `item`, `quantity`, `amount`, `status`; статусы — новый, оплачен, отправлен, отменён).

- **`Order`** — модель.
- **`OrderDao`** — DAO на `DataSource`: `findAll`, `findById` (Read), `insert` (Create), `update` (Update), `delete` (Delete). Все запросы — через `PreparedStatement` с параметрами `?` (защита от SQL injection), ресурсы закрываются через try-with-resources.
- **`Database`** — создаёт `DataSource`, таблицу и тестовые данные при первом запуске.
- **`AppListener`** — при старте инициализирует БД и кладёт DAO в `ServletContext`.
- **`OrdersServlet`** (`/orders`) — список, сводные карточки (всего, в работе, выручка без отменённых) и форма; `?edit=ID` открывает редактирование.
- **`OrderSaveServlet`** (`/orders/save`, POST) — добавление и обновление с валидацией (клиент и товар обязательны, количество ≥ 1, сумма ≥ 0, статус из списка).
- **`OrderDeleteServlet`** (`/orders/delete`, POST) — удаление с подтверждением.

## База данных

По умолчанию — встроенная **H2** (в памяти): ничего устанавливать не нужно, данные сбрасываются при перезапуске.

Для **PostgreSQL** создайте базу и задайте переменные окружения перед запуском:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/shop
export DB_USER=postgres
export DB_PASSWORD=...
mvn jetty:run
```

Таблица создаётся автоматически.

## Запуск

JDK 17+, Maven 3.9+.

```bash
mvn clean package
mvn jetty:run
```

Открыть: http://localhost:8080/web-service-lab7-v9/orders

## Проверка

1. Открыть список — 4 тестовых заказа и сводные карточки.
2. Форма «Новый заказ» → заказ появляется в списке (Create).
3. «Изменить» → поправить поля/статус → «Сохранить» (Update).
4. «Удалить» → подтвердить (Delete).
5. Пустой клиент, количество 0 или отрицательная сумма → сообщение об ошибке.
