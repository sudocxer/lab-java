package kz.edu.web;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.HashMap;
import java.util.Map;

/**
 * При старте создаёт EntityManagerFactory по persistence unit «employeesPU»,
 * заполняет таблицу тестовыми данными и кладёт репозиторий в ServletContext.
 * Если задана переменная окружения DB_URL (jdbc:postgresql://...), используется PostgreSQL.
 */
@WebListener
public class AppListener implements ServletContextListener {

    static final String REPOSITORY = "employeeRepository";

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

        emf = Persistence.createEntityManagerFactory("employeesPU", overrides);
        EmployeeRepository repository = new EmployeeRepository(emf);
        seed(repository);
        event.getServletContext().setAttribute(REPOSITORY, repository);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    private static void seed(EmployeeRepository repository) {
        if (repository.count() > 0) {
            return;
        }
        repository.add(new Employee("Иван Иванов", "Инженер-программист", "Разработка", 450_000));
        repository.add(new Employee("Алия Садыкова", "Бухгалтер", "Финансы", 380_000));
        repository.add(new Employee("Петр Петров", "Менеджер проектов", "Разработка", 520_000));
        repository.add(new Employee("Динара Ержанова", "HR-специалист", "Персонал", 340_000));
        repository.add(new Employee("Арман Нурлан", "Аналитик данных", "Аналитика", 480_000));
        repository.add(new Employee("Мария Ким", "Дизайнер интерфейсов", "Разработка", 410_000));
    }
}
