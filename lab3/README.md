# Лабораторная работа №3. JSP и Expression Language

Maven-проект (`war`), группа `kz.edu`, artifactId `web-service-lab3`.

## Что реализовано

- **`Student` + `StudentServlet`** (`/student`) **+ `student.jsp`** — базовый пример из методички: Servlet кладёт объект `Student` в атрибут запроса, `forward()`-ит на JSP, JSP выводит поля через Expression Language (`${student.name}` и т.д.).
- **`Teacher` + `TeacherServlet`** (`/teacher`) **+ `teacher.jsp`** — индивидуальное задание, **вариант 4**: карточка преподавателя (ФИО, кафедра, должность, стаж) по той же схеме Servlet → forward → JSP.

Исходники: [src/main/java/kz/edu/web](src/main/java/kz/edu/web), JSP: [src/main/webapp](src/main/webapp).

## Требования

- JDK 17 или новее
- Apache Maven 3.9+
- Доступ в интернет при первой сборке (дополнительно скачивается модуль JSP-компиляции Jetty)

## Сборка

```bash
mvn clean package
```

Соберётся файл `target/web-service-lab3.war`.

## Запуск — вариант 1: быстрый локальный запуск через Maven-плагин Jetty

Плагин настроен с зависимостью `jetty-ee10-apache-jsp`, поэтому JSP-страницы компилируются и выполняются прямо при `jetty:run`.

```bash
mvn jetty:run
```

После старта откройте в браузере:

- http://localhost:8080/web-service-lab3/student — карточка студента (базовый пример)
- http://localhost:8080/web-service-lab3/teacher — карточка преподавателя (вариант 4)

Остановить сервер — `Ctrl+C` в терминале.

## Запуск — вариант 2: деплой WAR на сервер приложений

1. Собрать `mvn clean package`.
2. Скопировать `target/web-service-lab3.war` в папку деплоя сервера приложений (Tomcat 10+, WildFly, Payara, GlassFish — все поддерживают JSP "из коробки").
3. Запустить сервер.
4. Открыть те же адреса, что и выше.
