# Лабораторная работа №5. HTTP-сессии и Cookies

Maven-проект (`war`), группа `kz.edu`, artifactId `web-service-lab5`.

## Что реализовано

- **`login.html` + `LoginServlet` (`/login`) + `ProfileServlet` (`/profile`) + `LogoutServlet` (`/logout`)** — базовый пример из методички: авторизация через `HttpSession` (`session.setAttribute` / `getAttribute` / `invalidate`). Пароль не проверяется — это учебная демонстрация механизма Session, а не полноценная аутентификация. Логин выводится на странице профиля с HTML-экранированием (защита от XSS).
- **`Product`, `Catalog` + `CatalogServlet` (`/catalog`), `CartServlet` (`/cart`), `CartAddServlet` (`/cart/add`), `CartRemoveServlet` (`/cart/remove`), `CartClearServlet` (`/cart/clear`)** — индивидуальное задание, **вариант 4**: корзина товаров в Session. Корзина (карта «id товара → количество») хранится в `HttpSession`; можно добавлять товары из каталога, увеличивать количество повторным добавлением, убирать позицию или очищать корзину целиком, итоговая сумма считается на странице `/cart`.

Исходники: [src/main/java/kz/edu/web](src/main/java/kz/edu/web).

## Требования

- JDK 17 или новее
- Apache Maven 3.9+
- Доступ в интернет при первой сборке

## Сборка

```bash
mvn clean package
```

Соберётся файл `target/web-service-lab5.war`.

## Запуск — вариант 1: быстрый локальный запуск через Maven-плагин Jetty

```bash
mvn jetty:run
```

После старта откройте в браузере:

- http://localhost:8080/web-service-lab5/login.html — вход в систему (базовый пример, логин/пароль любые, пароль не проверяется)
- http://localhost:8080/web-service-lab5/profile — профиль (доступен только после входа)
- http://localhost:8080/web-service-lab5/logout — выход (сбрасывает сессию и возвращает на страницу входа)
- http://localhost:8080/web-service-lab5/catalog — каталог товаров (вариант 4)
- http://localhost:8080/web-service-lab5/cart — корзина

Остановить сервер — `Ctrl+C` в терминале.

## Запуск — вариант 2: деплой WAR на сервер приложений

1. Собрать `mvn clean package`.
2. Скопировать `target/web-service-lab5.war` в папку деплоя сервера приложений (Tomcat 10+, WildFly, Payara, GlassFish).
3. Запустить сервер.
4. Открыть те же адреса, что и выше.
