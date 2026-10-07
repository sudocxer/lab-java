package kz.edu.web;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * При старте создаёт EntityManagerFactory («universityPU»), заполняет БД тестовыми данными
 * и кладёт репозиторий в ServletContext. Если задана DB_URL (jdbc:postgresql://...), используется PostgreSQL.
 */
@WebListener
public class AppListener implements ServletContextListener {

    static final String REPOSITORY = "universityRepository";

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

        emf = Persistence.createEntityManagerFactory("universityPU", overrides);
        UniversityRepository repository = new UniversityRepository(emf);
        seed(repository);
        event.getServletContext().setAttribute(REPOSITORY, repository);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    private static void seed(UniversityRepository repo) {
        repo.ensureUniversity("Университет «Алатау»", "Алматы");
        if (repo.countFaculties() > 0) {
            return;
        }
        repo.addFaculty("Информационные технологии", "Серик Касымов");
        repo.addFaculty("Экономика и бизнес", "Гульнара Абенова");
        repo.addFaculty("Право", "Данияр Омаров");
        for (Object[] c : new Object[][]{{"Java EE", 5}, {"Базы данных", 4}, {"Алгоритмы", 5},
                {"Высшая математика", 6}, {"Английский язык", 3}, {"Экономическая теория", 4}}) {
            repo.addCourse((String) c[0], (Integer) c[1]);
        }

        Map<String, Long> fac = new HashMap<>();
        repo.faculties().forEach(f -> fac.put(f.getName(), f.getId()));
        Map<String, Long> course = new HashMap<>();
        repo.courses().forEach(c -> course.put(c.getTitle(), c.getId()));

        String it = "Информационные технологии";
        String eco = "Экономика и бизнес";
        String law = "Право";
        add(repo, fac.get(it), "Айдар Нурланов", "ИС-21", 92, "S-2101", 2021, course, "Java EE", "Базы данных", "Алгоритмы");
        add(repo, fac.get(it), "Меруерт Сатпаева", "ИС-21", 88, "S-2102", 2021, course, "Java EE", "Английский язык");
        add(repo, fac.get(it), "Тимур Ли", "ИС-22", 76, "S-2205", 2022, course, "Алгоритмы", "Высшая математика");
        add(repo, fac.get(eco), "Дана Ержанова", "ЭК-21", 95, "S-2110", 2021, course, "Экономическая теория", "Английский язык");
        add(repo, fac.get(eco), "Ермек Жумабаев", "ЭК-22", 69, "S-2211", 2022, course, "Экономическая теория", "Высшая математика");
        add(repo, fac.get(law), "Алия Бекова", "ЮР-21", 84, "S-2120", 2021, course, "Английский язык");
        add(repo, fac.get(law), "Руслан Ахметов", "ЮР-23", 73, "S-2321", 2023, course);
    }

    private static void add(UniversityRepository repo, Long facultyId, String name, String group, int score,
                            String card, int year, Map<String, Long> courses, String... titles) {
        Set<Long> ids = new LinkedHashSet<>();
        for (String t : titles) {
            ids.add(courses.get(t));
        }
        repo.saveStudent(null, new UniversityRepository.StudentInput(facultyId, name, group, score, card, year, ids));
    }
}
