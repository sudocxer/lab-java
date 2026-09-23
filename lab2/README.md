# Лабораторная работа №2. Servlet: запросы, ответы, параметры и формы

Maven-проект (`war`), группа `kz.edu`, artifactId `web-service-lab2`.

## Что реализовано

- **`calculator.html` + `CalculateServlet`** (`/calculate`) — базовый пример из методички: HTML-форма с двумя числами, сложение через `doPost`, с проверкой пустых/некорректных полей.
- **`temperature.html` + `TemperatureServlet`** (`/convert`) — индивидуальное задание, **вариант 4**: конвертер градусов Цельсия в Фаренгейты.

Исходники: [src/main/java/kz/edu/web](src/main/java/kz/edu/web), формы: [src/main/webapp](src/main/webapp).

## Требования

- JDK 17 или новее
- Apache Maven 3.9+
- Доступ в интернет при первой сборке

## Сборка

```bash
mvn clean package
```

Соберётся файл `target/web-service-lab2.war`.

## Запуск — вариант 1: быстрый локальный запуск через Maven-плагин Jetty

```bash
mvn jetty:run
```

После старта откройте в браузере:

- http://localhost:8080/web-service-lab2/calculator.html — калькулятор суммы двух чисел
- http://localhost:8080/web-service-lab2/temperature.html — конвертер °C → °F (вариант 4)

Остановить сервер — `Ctrl+C` в терминале.

## Запуск — вариант 2: деплой WAR на сервер приложений

1. Собрать `mvn clean package`.
2. Скопировать `target/web-service-lab2.war` в папку деплоя сервера приложений (Tomcat 10+, WildFly, Payara, GlassFish).
3. Запустить сервер.
4. Открыть те же адреса, что и выше.

## Проверка обработки ошибок

- Пустое поле в форме → сообщение "Необходимо заполнить оба поля" / "Необходимо указать температуру...".
- Нечисловое значение → сообщение "Введите корректные числа" / "Введите корректное число".
