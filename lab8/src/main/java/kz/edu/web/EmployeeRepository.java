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
     * Поиск по имени (часть имени, без учёта регистра) и/или по минимальной зарплате.
     * Любой из параметров может быть null — тогда условие не применяется.
     */
    public List<Employee> search(String name, Integer minSalary) {
        return read(em -> {
            StringBuilder jpql = new StringBuilder("SELECT e FROM Employee e WHERE 1 = 1");
            if (name != null) {
                jpql.append(" AND LOWER(e.fullName) LIKE :name ESCAPE '\\'");
            }
            if (minSalary != null) {
                jpql.append(" AND e.salary >= :salary");
            }
            jpql.append(" ORDER BY e.fullName");

            TypedQuery<Employee> query = em.createQuery(jpql.toString(), Employee.class);
            if (name != null) {
                query.setParameter("name", "%" + escapeLike(name.toLowerCase()) + "%");
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

    private static String escapeLike(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
