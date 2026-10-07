# Лабораторная работа №9 (вариант 1). Связи между сущностями JPA

Maven-проект (`war`), группа `kz.edu`, artifactId `web-service-lab9-v1`. Провайдер JPA — **Hibernate ORM 6**.

## Что реализовано

Индивидуальное задание, **вариант 1**: «Факультет — студенты». Заодно выполнено практическое задание из методички — приложение «Университет»:

```
University 1 ──── * Faculty 1 ──── * Student * ──── * Course     (таблица связи student_courses)
                                       │ 1
                                       │
                                       1 StudentCard
```

| Связь | Где | Реализация |
|---|---|---|
| **OneToMany** | `Faculty` → `Student` (и `University` → `Faculty`) | `@OneToMany(mappedBy = "faculty", cascade = ALL, orphanRemoval = true)` |
| **ManyToOne** | `Student` → `Faculty` (и `Faculty` → `University`) | `@ManyToOne(fetch = LAZY)` + `@JoinColumn(name = "faculty_id")` — owning side |
| **OneToOne** | `Student` → `StudentCard` | `@OneToOne(cascade = ALL, orphanRemoval = true)` + `@JoinColumn(name = "card_id")` |
| **ManyToMany** | `Student` ↔ `Course` | `@ManyToMany` + `@JoinTable(name = "student_courses", …)` |

Что демонстрирует приложение:

- **Cascade** — удаление факультета удаляет всех его студентов; сохранение студента сохраняет его билет.
- **orphanRemoval** — студент, убранный из `faculty.getStudents()`, удаляется из БД; обнуление билета удаляет строку `student_cards`.
- **LAZY + JOIN FETCH** — все связи ленивые, нужные данные загружаются одним JPQL-запросом (без N+1 и `LazyInitializationException`).
- **Синхронизация обеих сторон связи** — `Faculty.addStudent()/removeStudent()`, `University.addFaculty()`.
- **JPQL по связям** — поиск студентов по имени, факультету и курсу (`s.id IN (SELECT s2.id FROM Student s2 JOIN s2.courses c2 WHERE c2.id = :courseId)`).

## Страницы

- `/faculties` — главная: университет, статистика, факультеты в раскрывающихся блоках со студентами, добавление и каскадное удаление факультета.
- `/students` — карточки студентов, поиск по имени, фильтры по факультету и курсу.
- `/student` — форма студента: `?id=ID` — редактирование, `?faculty=ID` — новый студент в факультете. Здесь задаются все связи: факультет, билет, курсы.
- `/courses` — курсы с записанными студентами, добавление и удаление курса.
- `/actions/*` (POST) — все изменяющие операции, затем redirect с сообщением.

## База данных

По умолчанию — встроенная **H2** (в памяти), таблицы создаёт Hibernate (`hbm2ddl.auto=update`). Для **PostgreSQL**:

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

Открыть: http://localhost:8080/web-service-lab9-v1/faculties

## Проверка

1. `/faculties` — 3 факультета, 7 студентов, у студентов билеты и курсы.
2. «+ Добавить студента» в факультете → форма с факультетом, билетом и курсами → запись появляется в списке.
3. `/students` — поиск «дана», фильтр по факультету и по курсу «Java EE» (2 студента).
4. Изменить студента и перенести в другой факультет.
5. `/courses` — добавить курс; удалить курс — он снимается со всех студентов.
6. Удалить студента (orphanRemoval) и факультет (cascade — студенты удаляются вместе с ним). В консоли Jetty видны SQL-запросы.
