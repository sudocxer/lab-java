package kz.edu.web;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Слой Service: бизнес-логика.
 * Репозиторий не создаётся через new — его внедряет CDI-контейнер (@Inject).
 */
@ApplicationScoped
public class EmployeeService {

    /** Итоги по выборке сотрудников. */
    public record Summary(int count, long total, long average) {
    }

    @Inject
    private EmployeeRepository repository;

    public List<Employee> getEmployees() {
        return repository.findAll();
    }

    public Optional<Employee> findById(int id) {
        return repository.findById(id);
    }

    /** Поиск: каждое условие необязательно (null / пустая строка — не учитывается). */
    public List<Employee> search(String name, String department, Integer minSalary) {
        String needle = name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
        return repository.findAll().stream()
                .filter(e -> needle.isEmpty() || e.fullName().toLowerCase(Locale.ROOT).contains(needle))
                .filter(e -> department == null || department.isBlank() || e.department().equals(department))
                .filter(e -> minSalary == null || e.salary() >= minSalary)
                .toList();
    }

    public List<String> getDepartments() {
        return repository.findAll().stream().map(Employee::department).distinct().sorted().toList();
    }

    public Summary summarize(List<Employee> employees) {
        long total = employees.stream().mapToLong(Employee::salary).sum();
        return new Summary(employees.size(), total, employees.isEmpty() ? 0 : total / employees.size());
    }

    /** Уровень оплаты по зарплате (аналог getStatus(score) из методички). */
    public String getGrade(int salary) {
        if (salary >= 500_000) {
            return "Высокий";
        }
        if (salary >= 450_000) {
            return "Выше среднего";
        }
        if (salary >= 380_000) {
            return "Средний";
        }
        return "Базовый";
    }

    /** Класс внедрённого репозитория — для страницы /scopes (видно, что это proxy контейнера). */
    public String repositoryClass() {
        return repository.getClass().getName();
    }
}
