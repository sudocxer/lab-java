package kz.edu.web;

import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Слой Controller: принимает запрос и выводит список сотрудников.
 * Вся логика — в EmployeeService, который внедряется контейнером (@Inject), а не создаётся через new.
 * /employees?name=...&department=...&minSalary=... — фильтры.
 */
@WebServlet("/employees")
public class EmployeesServlet extends HttpServlet {

    @Inject
    private EmployeeService service;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String ctx = request.getContextPath();

        String name = trim(request.getParameter("name"));
        String department = trim(request.getParameter("department"));
        Integer minSalary = parseInt(request.getParameter("minSalary"));
        boolean filtered = !name.isEmpty() || !department.isEmpty() || minSalary != null;

        List<Employee> employees = service.search(name, department, minSalary);
        EmployeeService.Summary summary = service.summarize(employees);

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        Page.begin(out, request, "Список сотрудников",
                "Servlet → EmployeeService → EmployeeRepository: зависимости внедряет CDI-контейнер.", "list");

        if ("notfound".equals(request.getParameter("msg"))) {
            out.println("<div class=\"flash err\">Сотрудник не найден.</div>");
        }

        out.println("<section class=\"cards\">"
                + card(filtered ? "Найдено" : "Сотрудников", String.valueOf(summary.count()), "blue")
                + card("Средняя зарплата", Page.money(summary.average()), "green")
                + card("Фонд оплаты", Page.money(summary.total()), "orange")
                + "</section>");

        out.println("<section class=\"panel\" id=\"search\"><h2>Поиск</h2>"
                + "<form class=\"search-form\" method=\"get\" action=\"" + ctx + "/employees\">"
                + "<div class=\"field\"><label for=\"name\">По имени</label>"
                + "<input id=\"name\" name=\"name\" placeholder=\"Например: иван\" value=\"" + Html.escape(name) + "\"></div>"
                + "<div class=\"field\"><label for=\"department\">Отдел</label>"
                + "<select id=\"department\" name=\"department\">" + departmentOptions(department) + "</select></div>"
                + "<div class=\"field\"><label for=\"minSalary\">Зарплата от, тг</label>"
                + "<input id=\"minSalary\" name=\"minSalary\" type=\"number\" min=\"0\" placeholder=\"400000\" value=\""
                + (minSalary == null ? "" : minSalary) + "\"></div>"
                + "<div class=\"form-actions\"><button type=\"submit\">Найти</button>"
                + (filtered ? "<a class=\"btn ghost\" href=\"" + ctx + "/employees\">Сбросить</a>" : "")
                + "</div></form></section>");

        out.println("<section class=\"panel\"><h2>Список</h2>");
        out.println("<table><tr><th>ID</th><th>ФИО</th><th>Должность</th><th>Отдел</th><th>Зарплата</th><th>Уровень</th></tr>");
        for (Employee e : employees) {
            String grade = service.getGrade(e.salary());
            out.println("<tr><td>" + e.id() + "</td>"
                    + "<td><a class=\"name\" href=\"" + ctx + "/employees/view?id=" + e.id() + "\">"
                    + Html.escape(e.fullName()) + "</a></td>"
                    + "<td>" + Html.escape(e.position()) + "</td>"
                    + "<td><span class=\"tag\">" + Html.escape(e.department()) + "</span></td>"
                    + "<td>" + Page.money(e.salary()) + "</td>"
                    + "<td><span class=\"tag " + Page.gradeClass(grade) + "\">" + grade + "</span></td></tr>");
        }
        if (employees.isEmpty()) {
            out.println("<tr><td colspan=\"6\" class=\"muted\">По запросу ничего не найдено.</td></tr>");
        }
        out.println("</table></section>");
        Page.end(out);
    }

    private String departmentOptions(String selected) {
        StringBuilder options = new StringBuilder("<option value=\"\">Все отделы</option>");
        for (String department : service.getDepartments()) {
            options.append("<option value=\"").append(Html.escape(department)).append('"')
                    .append(department.equals(selected) ? " selected" : "")
                    .append('>').append(Html.escape(department)).append("</option>");
        }
        return options.toString();
    }

    private static String card(String title, String value, String color) {
        return "<div class=\"card " + color + "\"><span>" + title + "</span><strong>" + value + "</strong></div>";
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private static Integer parseInt(String s) {
        try {
            return s == null || s.isBlank() ? null : Integer.valueOf(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
