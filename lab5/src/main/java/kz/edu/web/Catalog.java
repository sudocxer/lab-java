package kz.edu.web;

import java.util.List;
import java.util.Optional;

/**
 * Статичный каталог товаров для демонстрации корзины в Session (вариант 4).
 */
final class Catalog {

    static final List<Product> PRODUCTS = List.of(
            new Product(1, "Клавиатура", 12000),
            new Product(2, "Мышь", 5000),
            new Product(3, "Монитор", 85000),
            new Product(4, "Наушники", 15000)
    );

    private Catalog() {
    }

    static Optional<Product> findById(long id) {
        return PRODUCTS.stream()
                .filter(p -> p.getId() == id)
                .findFirst();
    }
}
