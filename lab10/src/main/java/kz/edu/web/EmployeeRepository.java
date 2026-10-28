package kz.edu.web;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

/**
 * Слой Repository: работает с данными.
 * CDI Bean с областью @ApplicationScoped — один экземпляр на всё приложение.
 * Источник данных — список в памяти; о бизнес-логике и HTTP репозиторий ничего не знает.
 */
@ApplicationScoped
public class EmployeeRepository {

    private final List<Employee> employees = List.of(
            new Employee(1, "Иван Иванов", "Инженер-программист", "Разработка", 450_000),
            new Employee(2, "Алия Садыкова", "Бухгалтер", "Финансы", 380_000),
            new Employee(3, "Петр Петров", "Менеджер проектов", "Разработка", 520_000),
            new Employee(4, "Динара Ержанова", "HR-специалист", "Персонал", 340_000),
            new Employee(5, "Арман Нурлан", "Аналитик данных", "Аналитика", 480_000),
            new Employee(6, "Мария Ким", "Дизайнер интерфейсов", "Разработка", 410_000));

    public List<Employee> findAll() {
        return employees;
    }

    public Optional<Employee> findById(int id) {
        return employees.stream().filter(e -> e.id() == id).findFirst();
    }
}
