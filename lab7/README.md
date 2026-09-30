# Лабораторная работа №7. JDBC и работа с базой данных

Maven-проект (`war`), группа `kz.edu`, artifactId `web-service-lab7`.

## Что реализовано

Индивидуальное задание, **вариант 4**: CRUD сотрудников (таблица `employees`: `id`, `full_name`, `position`, `department`, `salary`).

- **`Employee`** — модель.
- **`EmployeeDao`** — DAO на `DataSource`: `findAll`, `findById` (Read), `insert` (Create), `update` (Update), `delete` (Delete). Все запросы — через `PreparedStatement` с параметрами `?` (защита от SQL injection), ресурсы закрываются через try-with-resources.
- **`Database`** — создаёт `DataSource`, таблицу и тестовые данные при первом запуске.
- **`AppListener`** — при старте приложения инициализирует БД и кладёт DAO в `ServletContext`.
- **`EmployeesServlet`** (`/employees`) — список, сводные карточки и форма; `?edit=ID` открывает форму редактирования.
- **`EmployeeSaveServlet`** (`/employees/save`, POST) — добавление и обновление с валидацией (ФИО обязательно, зарплата — неотрицательное целое).
- **`EmployeeDeleteServlet`** (`/employees/delete`, POST) — удаление с подтверждением.

## База данных

По умолчанию используется встроенная **H2** (в памяти) — ничего устанавливать не нужно, данные сбрасываются при перезапуске.

Для **PostgreSQL** (как в методичке) создайте базу и задайте переменные окружения перед запуском:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/university
export DB_USER=postgres
export DB_PASSWORD=...
mvn jetty:run
```

Таблица создаётся автоматически (`BIGSERIAL PRIMARY KEY` для PostgreSQL).

## Запуск

JDK 17+, Maven 3.9+.

```bash
mvn clean package
mvn jetty:run
```

Открыть: http://localhost:8080/web-service-lab7/employees

## Проверка

1. Открыть список — 4 тестовых сотрудника, карточки со средней зарплатой и фондом оплаты.
2. Заполнить форму «Новый сотрудник» → запись появляется в списке (Create).
3. «Изменить» → поправить поля → «Сохранить» (Update).
4. «Удалить» → подтвердить (Delete).
5. Пустое ФИО или некорректная зарплата → сообщение об ошибке.
