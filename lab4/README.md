# Лабораторная работа №4. MVC и JSTL

Maven-проект (`war`), группа `kz.edu`, artifactId `web-service-lab4`.

## Что реализовано

- **`Course` + `CourseServlet`** (`/courses`) **+ `courses.jsp`** — базовый пример из методички «Учебные курсы»: Controller (Model — список курсов) `forward()`-ит на View, JSP выводит таблицу через JSTL `<c:forEach>` и помечает курсы с `hours > 50` как «Расширенный курс» через `<c:if>`.
- **`Student` + `StudentsServlet`** (`/students`) **+ `students.jsp`** — индивидуальное задание, **вариант 4**: список студентов. Та же схема MVC + JSTL: студенты с баллом ≥ 90 помечаются как «Отличник».

Исходники: [src/main/java/kz/edu/web](src/main/java/kz/edu/web), JSP: [src/main/webapp](src/main/webapp).

## Требования

- JDK 17 или новее
- Apache Maven 3.9+
- Доступ в интернет при первой сборке (дополнительно скачиваются JSTL и модуль JSP-компиляции Jetty)

## Сборка

```bash
mvn clean package
```

Соберётся файл `target/web-service-lab4.war`. В `WEB-INF/lib` этого WAR-файла будут упакованы JSTL-библиотеки (`jakarta.servlet.jsp.jstl-api` + реализация `org.glassfish.web:jakarta.servlet.jsp.jstl`) — это нужно для деплоя на сервер приложений, который сам JSTL не предоставляет.

## Запуск — вариант 1: быстрый локальный запуск через Maven-плагин Jetty

```bash
mvn jetty:run
```

После старта откройте в браузере:

- http://localhost:8080/web-service-lab4/courses — учебные курсы (базовый пример)
- http://localhost:8080/web-service-lab4/students — список студентов (вариант 4)

Остановить сервер — `Ctrl+C` в терминале.

## Запуск — вариант 2: деплой WAR на сервер приложений

1. Собрать `mvn clean package`.
2. Скопировать `target/web-service-lab4.war` в папку деплоя сервера приложений (Tomcat 10+, WildFly, Payara, GlassFish).
3. Запустить сервер.
4. Открыть те же адреса, что и выше.
