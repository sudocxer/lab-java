# Лабораторные работы по дисциплине Java EE

Пять независимых Maven-проектов (Servlet/JSP, Jakarta EE 10), по одному на каждую лабораторную работу.
Исходные задания — [docs/1-2-3 лаб работа Java EE.docx](docs/1-2-3%20лаб%20работа%20Java%20EE.docx), [docs/4 лаб работа Java EE.docx](docs/4%20лаб%20работа%20Java%20EE.docx), [docs/5 лаб работа Java EE.docx](docs/5%20лаб%20работа%20Java%20EE.docx).

Во всех лабораторных выполнен **вариант 4** индивидуального задания.

| Папка | Лабораторная | Индивидуальное задание (вариант 4) | Документация |
|---|---|---|---|
| [lab1/](lab1) | №1. Создание первого веб-приложения | Servlet `/table` — таблица умножения 1–10 | [lab1/README.md](lab1/README.md) |
| [lab2/](lab2) | №2. Servlet: запросы, ответы, параметры, формы | Конвертер градусов Цельсия в Фаренгейты | [lab2/README.md](lab2/README.md) |
| [lab3/](lab3) | №3. JSP и Expression Language | JSP-карточка преподавателя | [lab3/README.md](lab3/README.md) |
| [lab4/](lab4) | №4. MVC и JSTL | Список студентов | [lab4/README.md](lab4/README.md) |
| [lab5/](lab5) | №5. HTTP-сессии и Cookies | Корзина товаров в Session | [lab5/README.md](lab5/README.md) |

## Быстрый старт

Каждый проект собирается и запускается независимо (JDK 17+, Maven 3.9+):

```bash
cd lab1   # или lab2 / lab3 / lab4 / lab5
mvn clean package
mvn jetty:run
```

Подробные инструкции по запуску, список эндпоинтов и способ деплоя на полноценный сервер приложений (WildFly/Payara/GlassFish/Tomcat) — в README каждой папки.
