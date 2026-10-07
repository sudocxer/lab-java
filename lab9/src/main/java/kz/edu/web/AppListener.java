package kz.edu.web;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * При старте создаёт EntityManagerFactory («catalogPU»), заполняет БД тестовыми данными
 * и кладёт репозиторий в ServletContext. Если задана DB_URL (jdbc:postgresql://...), используется PostgreSQL.
 */
@WebListener
public class AppListener implements ServletContextListener {

    static final String REPOSITORY = "catalogRepository";

    private EntityManagerFactory emf;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        Map<String, Object> overrides = new HashMap<>();
        String url = System.getenv("DB_URL");
        if (url != null && url.startsWith("jdbc:postgresql:")) {
            overrides.put("jakarta.persistence.jdbc.driver", "org.postgresql.Driver");
            overrides.put("jakarta.persistence.jdbc.url", url);
            overrides.put("jakarta.persistence.jdbc.user", System.getenv("DB_USER"));
            overrides.put("jakarta.persistence.jdbc.password", System.getenv("DB_PASSWORD"));
        }

        emf = Persistence.createEntityManagerFactory("catalogPU", overrides);
        CatalogRepository repository = new CatalogRepository(emf);
        seed(repository);
        event.getServletContext().setAttribute(REPOSITORY, repository);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    private static void seed(CatalogRepository repo) {
        if (repo.countCategories() > 0) {
            return;
        }
        for (String tag : List.of("Новинка", "Хит", "Скидка", "Эко")) {
            repo.addTag(tag);
        }
        repo.addCategory("Электроника", "Смартфоны, ноутбуки и аксессуары");
        repo.addCategory("Одежда", "Одежда и обувь на каждый день");
        repo.addCategory("Книги", "Учебная и художественная литература");
        repo.addCategory("Продукты", "Продукты питания");

        Map<String, Long> cat = new HashMap<>();
        repo.categories().forEach(c -> cat.put(c.getName(), c.getId()));
        Map<String, Long> tag = new HashMap<>();
        repo.tags().forEach(t -> tag.put(t.getName(), t.getId()));

        add(repo, cat.get("Электроника"), "Смартфон Nova X", 289_000, "EL-1001", "6.5\" AMOLED, 128 ГБ", 190, tag, "Новинка", "Хит");
        add(repo, cat.get("Электроника"), "Ноутбук ProBook 15", 650_000, "EL-1002", "16 ГБ ОЗУ, SSD 512 ГБ", 1_800, tag, "Хит");
        add(repo, cat.get("Электроника"), "Наушники Air", 45_000, "EL-1003", "Беспроводные, шумоподавление", 55, tag, "Скидка");
        add(repo, cat.get("Одежда"), "Куртка Urban", 59_000, "CL-2001", "Демисезонная, водоотталкивающая", 900, tag, "Новинка");
        add(repo, cat.get("Одежда"), "Кроссовки Run", 38_000, "CL-2002", "Лёгкие беговые", 300, tag, "Скидка", "Хит");
        add(repo, cat.get("Книги"), "Java: полное руководство", 18_500, "BK-3001", "Шилдт, 12-е издание", 1_200, tag);
        add(repo, cat.get("Книги"), "Чистый код", 14_000, "BK-3002", "Роберт Мартин", 700, tag, "Хит");
        add(repo, cat.get("Продукты"), "Мёд горный", 6_500, "FD-4001", "Натуральный, 500 г", 650, tag, "Эко");
        add(repo, cat.get("Продукты"), "Чай зелёный", 2_900, "FD-4002", "Листовой, 100 г", 100, tag, "Эко", "Новинка");
    }

    private static void add(CatalogRepository repo, Long categoryId, String name, int price,
                            String sku, String description, int weight, Map<String, Long> tags, String... tagNames) {
        Set<Long> ids = new LinkedHashSet<>();
        for (String t : tagNames) {
            ids.add(tags.get(t));
        }
        repo.saveProduct(null, new CatalogRepository.ProductInput(categoryId, name, price, sku, description, weight, ids));
    }
}
