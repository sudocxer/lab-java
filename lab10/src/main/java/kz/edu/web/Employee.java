package kz.edu.web;

/** Предметная область: сотрудник. Неизменяемые данные, которые Repository отдаёт Service. */
public record Employee(int id, String fullName, String position, String department, int salary) {
}
