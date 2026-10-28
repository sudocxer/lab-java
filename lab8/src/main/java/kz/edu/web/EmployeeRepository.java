package kz.edu.web;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.function.Function;

/**
 * Работа с Entity через EntityManager (JPA вместо ручного JDBC).
 * Для каждой операции создаётся свой EntityManager (и persistence context),
 * изменения выполняются в транзакции.
 */
public class EmployeeRepository {

    private final EntityManagerFactory emf;

    public EmployeeRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    /** Список с сортировкой по имени. */
    public List<Employee> findAll() {
        return search(null, null);
    }

    /** Просмотр одного сотрудника: em.find(). */
    public Employee findById(long id) {
        return read(em -> em.find(Employee.class, id));
    }

    /**
     * Поиск по имени (часть имени, регистр не важен) и/или по минимальной зарплате.
     * Любой из параметров может быть null — тогда условие не применяется.
     */
    public List<Employee> search(String name, Integer minSalary) {
        return read(em -> {
            StringBuilder jpql = new StringBuilder("SELECT e FROM Employee e WHERE 1 = 1");
            List<String> variants = name == null ? List.of() : caseVariants(name);
            if (!variants.isEmpty()) {
                jpql.append(" AND (");
                for (int i = 0; i < variants.size(); i++) {
                    jpql.append(i == 0 ? "" : " OR ").append("e.fullName LIKE :name").append(i).append(" ESCAPE '\\'");
                }
                jpql.append(")");
            }
            if (minSalary != null) {
                jpql.append(" AND e.salary >= :salary");
            }
            jpql.append(" ORDER BY e.fullName");

            TypedQuery<Employee> query = em.createQuery(jpql.toString(), Employee.class);
            for (int i = 0; i < variants.size(); i++) {
                query.setParameter("name" + i, "%" + escapeLike(variants.get(i)) + "%");
            }
            if (minSalary != null) {
                query.setParameter("salary", minSalary);
            }
            return query.getResultList();
        });
    }

    public long count() {
        return read(em -> em.createQuery("SELECT COUNT(e) FROM Employee e", Long.class).getSingleResult());
    }

    /** Добавление: em.persist(). */
    public void add(Employee employee) {
        inTransaction(em -> {
            em.persist(employee);
            return null;
        });
    }

    /**
     * Изменение: сущность, полученная через find(), находится в persistence context,
     * поэтому достаточно вызвать сеттеры — Hibernate сам выполнит UPDATE при commit.
     */
    public boolean update(long id, String fullName, String position, String department, Integer salary) {
        return inTransaction(em -> {
            Employee employee = em.find(Employee.class, id);
            if (employee == null) {
                return false;
            }
            employee.setFullName(fullName);
            employee.setPosition(position);
            employee.setDepartment(department);
            employee.setSalary(salary);
            return true;
        });
    }

    /** Удаление: em.remove(). */
    public boolean delete(long id) {
        return inTransaction(em -> {
            Employee employee = em.find(Employee.class, id);
            if (employee == null) {
                return false;
            }
            em.remove(employee);
            return true;
        });
    }

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

    /**
     * SQLite приводит регистр (LOWER, LIKE) только для латиницы, поэтому для кириллицы
     * ищем сразу по нескольким вариантам написания: как введено, строчные, с заглавной, заглавные.
     */
    private static List<String> caseVariants(String text) {
        String lower = text.toLowerCase();
        String capitalized = lower.isEmpty() ? lower : lower.substring(0, 1).toUpperCase() + lower.substring(1);
        return new java.util.ArrayList<>(new java.util.LinkedHashSet<>(
                List.of(text, lower, capitalized, text.toUpperCase())));
    }

    private static String escapeLike(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
