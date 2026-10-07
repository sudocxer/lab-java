# Лабораторная работа №9. Связи между сущностями JPA

Maven-проект (`war`), группа `kz.edu`, artifactId `web-service-lab9`. Провайдер JPA — **Hibernate ORM 6**.

## Что реализовано

Индивидуальное задание, **вариант 4**: «Категория — товары» (приложение «Каталог товаров»). Для полноты в модель добавлены все четыре типа связей:

```
Category 1 ────── * Product * ────── * Tag        (таблица связи product_tags)
                       │ 1
                       │
                       1 ProductDetails
```

| Связь | Где | Реализация |
|---|---|---|
| **OneToMany** | `Category` → `Product` | `@OneToMany(mappedBy = "category", cascade = ALL, orphanRemoval = true)` |
| **ManyToOne** | `Product` → `Category` | `@ManyToOne(fetch = LAZY)` + `@JoinColumn(name = "category_id")` — owning side |
| **OneToOne** | `Product` → `ProductDetails` | `@OneToOne(cascade = ALL, orphanRemoval = true)` + `@JoinColumn(name = "details_id")` |
| **ManyToMany** | `Product` ↔ `Tag` | `@ManyToMany` + `@JoinTable(name = "product_tags", …)` |

Что демонстрирует приложение:

- **Cascade** — удаление категории удаляет все её товары; сохранение товара сохраняет его детали.
- **orphanRemoval** — товар, убранный из `category.getProducts()`, удаляется из БД; обнуление `details` удаляет строку `product_details`.
- **LAZY + JOIN FETCH** — все связи ленивые, поэтому нужные данные подгружаются одним JPQL-запросом (`LEFT JOIN FETCH`), без проблемы N+1 и `LazyInitializationException`.
- **Синхронизация сторон связи** — `Category.addProduct()/removeProduct()` меняют обе стороны.
- **JPQL по связям** — фильтр товаров по названию, категории и тегу (`p.id IN (SELECT p2.id FROM Product p2 JOIN p2.tags t2 WHERE t2.id = :tagId)`), агрегат `COUNT … GROUP BY` для числа товаров у тега.

## Страницы

- `/categories` — категории со статистикой, добавление и каскадное удаление.
- `/category?id=ID` — товары категории, форма добавления/редактирования товара (категория, цена, артикул, вес, описание, теги); `&edit=PRODUCT_ID` — редактирование.
- `/products` — все товары, фильтр по названию / категории / тегу.
- `/tags` — теги с количеством товаров, добавление и удаление.
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

Открыть: http://localhost:8080/web-service-lab9/categories

## Проверка

1. `/categories` — 4 категории, 9 товаров.
2. Открыть категорию — товары с артикулами (OneToOne) и тегами (ManyToMany).
3. Добавить товар с тегами; изменить его, в том числе перенести в другую категорию.
4. `/products` — отфильтровать по тегу «Хит» (4 товара) и по названию.
5. `/tags` — добавить тег; удалить тег — он снимается со всех товаров.
6. Удалить товар (orphanRemoval) и категорию (cascade — товары удаляются вместе с ней). В консоли Jetty видны SQL-запросы.
