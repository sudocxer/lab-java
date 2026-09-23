# Лабораторная работа №1. Создание первого веб-приложения Java EE/Jakarta EE

Maven-проект (`war`), группа `kz.edu`, artifactId `web-service-lab1`.

## Что реализовано

- **`HelloServlet`** (`/hello`) — базовый пример из методички: выводит приветственную HTML-страницу.
- **`TableServlet`** (`/table`) — индивидуальное задание, **вариант 4**: выводит таблицу умножения от 1 до 10.

Исходники: [src/main/java/kz/edu/web](src/main/java/kz/edu/web).

## Требования

- JDK 17 или новее
- Apache Maven 3.9+
- Доступ в интернет при первой сборке (Maven скачает зависимости и, при запуске через плагин, дистрибутив Jetty)

## Сборка

```bash
mvn clean package
```

Соберётся файл `target/web-service-lab1.war`.

## Запуск — вариант 1: быстрый локальный запуск через Maven-плагин Jetty

Не требует установки отдельного сервера приложений — плагин `jetty-ee10-maven-plugin` поднимает встроенный Jetty (поддерживает Jakarta Servlet 6.0).

```bash
mvn jetty:run
```

После старта откройте в браузере:

- http://localhost:8080/web-service-lab1/hello
- http://localhost:8080/web-service-lab1/table

Остановить сервер — `Ctrl+C` в терминале.

## Запуск — вариант 2: деплой WAR на сервер приложений

Как описано в методичке (WildFly, Payara, GlassFish, Tomcat 10+):

1. Собрать `mvn clean package`.
2. Скопировать `target/web-service-lab1.war` в папку деплоя сервера (`webapps/` для Tomcat, `standalone/deployments/` для WildFly и т.д.).
3. Запустить сервер приложений.
4. Открыть те же адреса, что и выше.
