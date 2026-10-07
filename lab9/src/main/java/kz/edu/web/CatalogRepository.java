package kz.edu.web;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Работа с каталогом через EntityManager. Связанные коллекции LAZY, поэтому нужные данные
 * загружаются одним запросом через JOIN FETCH (без проблемы N+1) — после закрытия
 * EntityManager обращаться к незагруженным связям нельзя (LazyInitializationException).
 */
public class CatalogRepository {

    /** Данные формы товара. */
    public record ProductInput(Long categoryId, String name, int price,
                               String sku, String description, Integer weightGrams, Set<Long> tagIds) {
    }

    private final EntityManagerFactory emf;

    public CatalogRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    // ---------- чтение ----------

    /** Категории вместе с товарами (один запрос). */
    public List<Category> categories() {
        return read(em -> em.createQuery(
                "SELECT DISTINCT c FROM Category c LEFT JOIN FETCH c.products ORDER BY c.name",
                Category.class).getResultList());
    }

    /** Категория с товарами, их тегами и деталями. */
    public Category category(long id) {
        return read(em -> em.createQuery(
                        "SELECT DISTINCT c FROM Category c "
                                + "LEFT JOIN FETCH c.products p "
                                + "LEFT JOIN FETCH p.tags "
                                + "LEFT JOIN FETCH p.details "
                                + "WHERE c.id = :id", Category.class)
                .setParameter("id", id)
                .getResultStream().findFirst().orElse(null));
    }

    /** Товар с категорией, тегами и деталями. */
    public Product product(long id) {
        return read(em -> em.createQuery(
                        "SELECT DISTINCT p FROM Product p "
                                + "JOIN FETCH p.category "
                                + "LEFT JOIN FETCH p.tags "
                                + "LEFT JOIN FETCH p.details "
                                + "WHERE p.id = :id", Product.class)
                .setParameter("id", id)
                .getResultStream().findFirst().orElse(null));
    }

    /** Все товары с фильтрами (любой может быть null): часть названия, категория, тег. */
    public List<Product> products(String name, Long categoryId, Long tagId) {
        return read(em -> {
            StringBuilder jpql = new StringBuilder("SELECT DISTINCT p FROM Product p "
                    + "JOIN FETCH p.category c LEFT JOIN FETCH p.tags LEFT JOIN FETCH p.details WHERE 1 = 1");
            if (name != null) {
                jpql.append(" AND LOWER(p.name) LIKE :name ESCAPE '\\'");
            }
            if (categoryId != null) {
                jpql.append(" AND c.id = :categoryId");
            }
            if (tagId != null) {
                jpql.append(" AND p.id IN (SELECT p2.id FROM Product p2 JOIN p2.tags t2 WHERE t2.id = :tagId)");
            }
            jpql.append(" ORDER BY p.name");

            TypedQuery<Product> query = em.createQuery(jpql.toString(), Product.class);
            if (name != null) {
                query.setParameter("name", "%" + escapeLike(name.toLowerCase()) + "%");
            }
            if (categoryId != null) {
                query.setParameter("categoryId", categoryId);
            }
            if (tagId != null) {
                query.setParameter("tagId", tagId);
            }
            return query.getResultList();
        });
    }

    public List<Tag> tags() {
        return read(em -> em.createQuery("SELECT t FROM Tag t ORDER BY t.name", Tag.class).getResultList());
    }

    /** Сколько товаров у каждого тега: tagId -> количество. */
    public Map<Long, Long> tagUsage() {
        return read(em -> {
            Map<Long, Long> usage = new HashMap<>();
            for (Object[] row : em.createQuery(
                    "SELECT t.id, COUNT(p) FROM Product p JOIN p.tags t GROUP BY t.id", Object[].class).getResultList()) {
                usage.put((Long) row[0], (Long) row[1]);
            }
            return usage;
        });
    }

    public long countCategories() {
        return read(em -> em.createQuery("SELECT COUNT(c) FROM Category c", Long.class).getSingleResult());
    }

    // ---------- категории ----------

    /** false — если категория с таким именем уже есть. */
    public boolean addCategory(String name, String description) {
        return inTransaction(em -> {
            boolean exists = !em.createQuery("SELECT c.id FROM Category c WHERE LOWER(c.name) = :n", Long.class)
                    .setParameter("n", name.toLowerCase()).getResultList().isEmpty();
            if (exists) {
                return false;
            }
            em.persist(new Category(name, description));
            return true;
        });
    }

    /** Каскадно удаляет категорию вместе с её товарами (cascade = ALL). */
    public boolean deleteCategory(long id) {
        return inTransaction(em -> {
            Category category = em.find(Category.class, id);
            if (category == null) {
                return false;
            }
            em.remove(category);
            return true;
        });
    }

    // ---------- товары ----------

    /** Создаёт товар (productId == null) или обновляет существующий. Возвращает null, если не найдена категория/товар. */
    public Product saveProduct(Long productId, ProductInput in) {
        return inTransaction(em -> {
            Category category = em.find(Category.class, in.categoryId());
            if (category == null) {
                return null;
            }
            Product product;
            if (productId == null) {
                product = new Product();
                category.addProduct(product);
            } else {
                product = em.find(Product.class, productId);
                if (product == null) {
                    return null;
                }
                if (!product.getCategory().getId().equals(category.getId())) {
                    // Перенос меняет только owning side (category_id). Нельзя убирать товар из старой
                    // коллекции category.products — orphanRemoval сочтёт его «сиротой» и удалит из БД.
                    product.setCategory(category);
                }
            }
            product.setName(in.name());
            product.setPrice(in.price());

            boolean hasDetails = !in.sku().isEmpty() || !in.description().isEmpty() || in.weightGrams() != null;
            if (hasDetails) {
                ProductDetails details = product.getDetails() == null ? new ProductDetails() : product.getDetails();
                details.setSku(in.sku());
                details.setDescription(in.description());
                details.setWeightGrams(in.weightGrams());
                product.setDetails(details);
            } else {
                product.setDetails(null); // orphanRemoval удалит строку из product_details
            }

            product.getTags().clear();
            for (Long tagId : in.tagIds()) {
                Tag tag = em.find(Tag.class, tagId);
                if (tag != null) {
                    product.getTags().add(tag);
                }
            }
            if (productId == null) {
                em.persist(product);
            }
            return product;
        });
    }

    /** Удаление через коллекцию категории: сработает orphanRemoval. */
    public boolean deleteProduct(long id) {
        return inTransaction(em -> {
            Product product = em.find(Product.class, id);
            if (product == null) {
                return false;
            }
            product.getCategory().removeProduct(product);
            return true;
        });
    }

    // ---------- теги ----------

    public boolean addTag(String name) {
        return inTransaction(em -> {
            boolean exists = !em.createQuery("SELECT t.id FROM Tag t WHERE LOWER(t.name) = :n", Long.class)
                    .setParameter("n", name.toLowerCase()).getResultList().isEmpty();
            if (exists) {
                return false;
            }
            em.persist(new Tag(name));
            return true;
        });
    }

    /** Тег удаляется после того, как его уберут у всех товаров (Product — владелец связи). */
    public boolean deleteTag(long id) {
        return inTransaction(em -> {
            Tag tag = em.find(Tag.class, id);
            if (tag == null) {
                return false;
            }
            List<Product> users = em.createQuery(
                            "SELECT p FROM Product p JOIN p.tags t WHERE t.id = :id", Product.class)
                    .setParameter("id", id).getResultList();
            for (Product p : users) {
                p.getTags().remove(tag);
            }
            em.remove(tag);
            return true;
        });
    }

    // ---------- инфраструктура ----------

    private <T> T read(Function<EntityManager, T> action) {
        try (EntityManager em = emf.createEntityManager()) {
            return action.apply(em);
        }
    }

    private <T> T inTransaction(Function<EntityManager, T> action) {
        try (EntityManager em = emf.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            try {
                T result = action.apply(em);
                tx.commit();
                return result;
            } catch (RuntimeException e) {
                if (tx.isActive()) {
                    tx.rollback();
                }
                throw e;
            }
        }
    }

    private static String escapeLike(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
