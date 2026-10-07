# Лабораторная работа №8. JPA и объектно-реляционное отображение

Maven-проект (`war`), группа `kz.edu`, artifactId `web-service-lab8`. Провайдер JPA — **Hibernate ORM 6**.

## Что реализовано

Индивидуальное задание, **вариант 4**: JPA для сотрудников — приложение «Учёт сотрудников».

- **`Employee`** — Entity (`@Entity`, `@Table(name = "employees")`, `@Id`, `@GeneratedValue(IDENTITY)`, `@Column`); поля `id`, `fullName`, `position`, `department`, `salary`.
- **`EmployeeRepository`** — вся работа с БД через `EntityManager`:
  - `persist()` — добавление; `find()` — просмотр; изменение через managed-сущность (dirty checking, UPDATE при commit); `remove()` — удаление;
  - JPQL: `SELECT e FROM Employee e ORDER BY e.fullName`, поиск `LOWER(e.fullName) LIKE :name`, поиск по баллу (зарплате) `e.salary >= :salary` — параметры только через `setParameter`;
  - изменяющие операции выполняются в транзакции с rollback при ошибке.
- **`META-INF/persistence.xml`** — persistence unit `employeesPU` (RESOURCE_LOCAL, `hbm2ddl.auto=update` — таблицу создаёт Hibernate).
- **`AppListener`** — создаёт `EntityManagerFactory`, заполняет тестовыми данными, закрывает фабрику при остановке.
- **`EmployeesServlet`** (`/employees`) — список, карточки статистики, поиск по имени и по зарплате, форма добавления/редактирования (`?edit=ID`).
- **`EmployeeViewServlet`** (`/employees/view?id=ID`) — карточка сотрудника.
- **`EmployeeSaveServlet`** (`/employees/save`, POST) — добавление и изменение с валидацией.
- **`EmployeeDeleteServlet`** (`/employees/delete`, POST) — удаление с подтверждением.

> В Jetty нет CDI, поэтому вместо `@Inject EntityManager` из методички `EntityManagerFactory` создаётся в `AppListener`, а `EntityManager` открывается на каждую операцию (`RESOURCE_LOCAL`). На Jakarta EE сервере (WildFly/Payara) можно использовать `@PersistenceContext`/`@Inject`.

## База данных

По умолчанию — встроенная **H2** (в памяти), ничего устанавливать не нужно. Для **PostgreSQL**:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/university
export DB_USER=postgres
export DB_PASSWORD=...
mvn jetty:run
```

## Запуск

JDK 17+, Maven 3.9+.

```bash
mvn clean package
mvn jetty:run
```

Открыть: http://localhost:8080/web-service-lab8/employees

## Проверка

1. Список — 6 тестовых сотрудников, карточки со средней зарплатой и фондом оплаты.
2. Поиск: имя `иван` → 1 запись; зарплата от `450000` → 3 записи; оба условия вместе. В консоли Jetty видны SQL-запросы Hibernate.
3. Клик по ФИО — карточка сотрудника (просмотр).
4. Форма «Новый сотрудник» → запись появляется в списке (persist).
5. «Изменить» → «Сохранить» (изменение), «Удалить» → подтвердить (remove).
6. Пустое ФИО или некорректная зарплата → сообщение об ошибке.
