# Лабораторная работа №10. CDI и внедрение зависимостей

Maven-проект (`war`), группа `kz.edu`, artifactId `web-service-lab10`. Реализация CDI — **Weld 5.1** (Jakarta CDI 4.0).

## Что реализовано

Индивидуальное задание, **вариант 4**: `EmployeeService` — приложение «Сотрудники» из трёх слоёв, зависимости между которыми внедряются через `@Inject`:

```
EmployeesServlet  ──@Inject──▶  EmployeeService  ──@Inject──▶  EmployeeRepository
   (Controller)                    (Service)                       (Repository)
   принимает запрос,               бизнес-логика:                  данные: список
   рисует HTML                     поиск, итоги, уровень оплаты    сотрудников
```

- **`EmployeeRepository`** (`@ApplicationScoped`) — слой данных: `findAll()`, `findById()`. Данные — список в памяти.
- **`EmployeeService`** (`@ApplicationScoped`) — слой бизнес-логики. Репозиторий получает через `@Inject`, а не `new`:
  - `search(name, department, minSalary)` — поиск по имени (без учёта регистра), отделу и минимальной зарплате;
  - `summarize()` — количество, фонд оплаты, средняя зарплата;
  - `getGrade(salary)` — уровень оплаты: Высокий / Выше среднего / Средний / Базовый (аналог `getStatus(score)` из методички).
- **`EmployeesServlet`** (`/employees`) — контроллер: `@Inject EmployeeService`, список сотрудников, карточки со статистикой, фильтры.
- **`EmployeeViewServlet`** (`/employees/view?id=ID`) — карточка сотрудника.
- **`ScopesServlet`** (`/scopes`) — демонстрация CDI: какие объекты внедрил контейнер и как живут бины разных областей.

### Области видимости (CDI Scopes)

| Бин | Область | Поведение (видно на `/scopes`) |
|---|---|---|
| `EmployeeRepository`, `EmployeeService`, `ApplicationInfo` | `@ApplicationScoped` | один экземпляр на приложение |
| `SessionInfo` | `@SessionScoped` | свой экземпляр у каждой HTTP-сессии (`Serializable`) |
| `RequestInfo` | `@RequestScoped` | новый экземпляр на каждый запрос |
| `DependentInfo` | `@Dependent` | своя копия в каждой точке внедрения, живёт как компонент-владелец |

Для бинов с областью контейнер внедряет не сам объект, а **client proxy** (класс `…$Proxy$_$$_WeldClientProxy`) — это видно в таблице «Цепочка зависимостей» на `/scopes`.

## Как CDI подключён

В Jetty нет собственного CDI (в lab8 из-за этого пришлось создавать `EntityManagerFactory` вручную в `AppListener`). Здесь CDI подключён полноценно:

- `weld-servlet-shaded` в зависимостях проекта — реализация CDI внутри WAR; Weld сам стартует через `ServletContainerInitializer`;
- `jetty-ee10-cdi` в зависимостях `jetty-ee10-maven-plugin` — интеграция Jetty с CDI, благодаря которой `@Inject` работает и в **сервлетах**, которые создаёт не CDI, а контейнер сервлетов (в логе: `WELD-ENV-001213: Jetty CDI SPI support detected`);
- [`WEB-INF/beans.xml`](src/main/webapp/WEB-INF/beans.xml) с `bean-discovery-mode="annotated"` — бинами считаются классы с аннотацией области.

На сервере приложений с готовым CDI (WildFly, Payara, GlassFish, TomEE) `beans.xml` и код остаются теми же, а зависимость `weld-servlet-shaded` не нужна (деплой на такие серверы здесь не проверялся).

## Запуск

JDK 17+, Maven 3.9+.

```bash
mvn clean package
mvn jetty:run
```

Открыть: http://localhost:8080/web-service-lab10/employees

## Проверка

1. `/employees` — 6 сотрудников, карточки: средняя зарплата 430 000 тг, фонд оплаты 2 580 000 тг; у каждого сотрудника метка уровня оплаты (из `EmployeeService.getGrade`).
2. Фильтры: имя `иван` → 1 запись; отдел «Разработка» → 3; отдел «Разработка» + зарплата от `420000` → 2; некорректная зарплата игнорируется.
3. Клик по ФИО — карточка; несуществующий `id` → возврат к списку с сообщением.
4. `/scopes` — в таблице «Цепочка зависимостей» видны proxy-классы `EmployeeService` и `EmployeeRepository`, внедрённые контейнером.
5. Несколько раз обновить `/scopes`: у `@ApplicationScoped` и `@SessionScoped` идентификатор экземпляра не меняется, счётчики растут; у `@RequestScoped` идентификатор каждый раз новый; две точки `@Dependent` — два разных экземпляра. В другом браузере (или в режиме инкогнито) счётчик сессии начнётся заново, счётчик приложения продолжится.
