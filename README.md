# Лабораторные работы по дисциплине Java EE

Независимые Maven-проектов (Servlet/JSP, Jakarta EE 10), по одному на каждую лабораторную работу.
Исходные задания — [docs/1-2-3 лаб работа Java EE.docx](docs/1-2-3%20лаб%20работа%20Java%20EE.docx), [docs/4 лаб работа Java EE.docx](docs/4%20лаб%20работа%20Java%20EE.docx), [docs/5 лаб работа Java EE.docx](docs/5%20лаб%20работа%20Java%20EE.docx), [docs/6 лаб работа Java EE.docx](docs/6%20лаб%20работа%20Java%20EE.docx).

Во всех лабораторных выполнен **вариант 4** индивидуального задания.

| Папка | Лабораторная | Индивидуальное задание (вариант 4) | Документация |
|---|---|---|---|
| [lab1/](lab1) | №1. Создание первого веб-приложения | Servlet `/table` — таблица умножения 1–10 | [lab1/README.md](lab1/README.md) |
| [lab2/](lab2) | №2. Servlet: запросы, ответы, параметры, формы | Конвертер градусов Цельсия в Фаренгейты | [lab2/README.md](lab2/README.md) |
| [lab3/](lab3) | №3. JSP и Expression Language | JSP-карточка преподавателя | [lab3/README.md](lab3/README.md) |
| [lab4/](lab4) | №4. MVC и JSTL | Список студентов | [lab4/README.md](lab4/README.md) |
| [lab5/](lab5) | №5. HTTP-сессии и Cookies | Корзина товаров в Session | [lab5/README.md](lab5/README.md) |
| [lab6/](lab6) | №6. Фильтры и обработка HTTP-запросов | Фильтр измерения времени выполнения Servlet | [lab6/README.md](lab6/README.md) |
| [lab6-v9/](lab6-v9) | №6 (вариант 9). Фильтры и обработка HTTP-запросов | Фильтр аудита действий пользователя | [lab6-v9/README.md](lab6-v9/README.md) |
| [lab7/](lab7) | №7. JDBC и работа с базой данных | CRUD сотрудников | [lab7/README.md](lab7/README.md) |
| [lab7-v9/](lab7-v9) | №7 (вариант 9). JDBC и работа с базой данных | CRUD заказов | [lab7-v9/README.md](lab7-v9/README.md) |
| [lab8/](lab8) | №8. JPA и объектно-реляционное отображение | JPA для сотрудников (Hibernate ORM) | [lab8/README.md](lab8/README.md) |
| [lab9/](lab9) | №9. Связи между сущностями JPA | Категория — товары (OneToMany, ManyToOne, OneToOne, ManyToMany) | [lab9/README.md](lab9/README.md) |
| [lab9-v1/](lab9-v1) | №9 (вариант 1). Связи между сущностями JPA | Факультет — студенты (University → Faculty → Student, Student ↔ Course) | [lab9-v1/README.md](lab9-v1/README.md) |

## Быстрый старт

Каждый проект собирается и запускается независимо (JDK 17+, Maven 3.9+):

```bash
cd lab1   # или lab2 / lab3 / lab4 / lab5 / lab6 / lab7 / lab7-v9 / lab8 / lab9 / lab9-v1
mvn clean package
mvn jetty:run
```

Подробные инструкции по запуску, список эндпоинтов и способ деплоя на полноценный сервер приложений (WildFly/Payara/GlassFish/Tomcat) — в README каждой папки.
