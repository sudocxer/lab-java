package kz.edu.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * Список сотрудников, поиск по имени и по зарплате, форма добавления/редактирования.
 * /employees?name=...&minSalary=...  — поиск, /employees?edit=ID — форма редактирования.
 */
@WebServlet("/employees")
public class EmployeesServlet extends HttpServlet {

    private static final Map<String, String[]> MESSAGES = Map.of(
            "added", new String[]{"ok", "Сотрудник добавлен."},
            "updated", new String[]{"ok", "Данные сотрудника обновлены."},
            "deleted", new String[]{"ok", "Сотрудник удалён."},
            "invalid", new String[]{"err", "Проверьте поля: ФИО обязательно, зарплата — неотрицательное число."},
            "notfound", new String[]{"err", "Сотрудник не найден."});

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        EmployeeRepository repository = (EmployeeRepository) getServletContext().getAttribute(AppListener.REPOSITORY);
        String ctx = request.getContextPath();

        String name = trim(request.getParameter("name"));
        String minSalaryParam = trim(request.getParameter("minSalary"));
        Integer minSalary = parseInt(minSalaryParam);
        boolean filtered = !name.isEmpty() || minSalary != null;

        List<Employee> employees = repository.search(name.isEmpty() ? null : name, minSalary);

        Employee editing = null;
        Integer editId = parseInt(request.getParameter("edit"));
        if (editId != null) {
            editing = repository.findById(editId);
        }

        long total = employees.stream().mapToLong(e -> e.getSalary() == null ? 0 : e.getSalary()).sum();
        long average = employees.isEmpty() ? 0 : total / employees.size();

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        Page.begin(out, request, "Учёт сотрудников",
                "JPA и ORM: Entity, EntityManager, JPQL (Hibernate).", "list");

        String msg = request.getParameter("msg");
        String[] message = msg == null ? null : MESSAGES.get(msg);
        if (message != null) {
            out.println("<div class=\"flash " + message[0] + "\">" + message[1] + "</div>");
        }

        out.println("<section class=\"cards\">"
                + card(filtered ? "Найдено" : "Сотрудников", String.valueOf(employees.size()), "blue")
                + card("Средняя зарплата", Page.money(average), "green")
                + card("Фонд оплаты", Page.money(total), "orange")
                + "</section>");

        out.println("<section class=\"panel\" id=\"search\"><h2>Поиск</h2>"
                + "<form class=\"search-form\" method=\"get\" action=\"" + ctx + "/employees\">"
                + "<div class=\"field\"><label for=\"name\">По имени</label>"
                + "<input id=\"name\" name=\"name\" placeholder=\"Например: иван\" value=\"" + Html.escape(name) + "\"></div>"
                + "<div class=\"field\"><label for=\"minSalary\">Зарплата от, тг</label>"
                + "<input id=\"minSalary\" name=\"minSalary\" type=\"number\" min=\"0\" placeholder=\"400000\" value=\""
                + (minSalary == null ? "" : minSalary) + "\"></div>"
                + "<div class=\"form-actions\"><button type=\"submit\">Найти</button>"
                + (filtered ? "<a class=\"btn ghost\" href=\"" + ctx + "/employees\">Сбросить</a>" : "")
                + "</div></form>"
                + (filtered ? "<p class=\"query\">JPQL: <code>SELECT e FROM Employee e WHERE "
                + (name.isEmpty() ? "" : "LOWER(e.fullName) LIKE :name")
                + (!name.isEmpty() && minSalary != null ? " AND " : "")
                + (minSalary == null ? "" : "e.salary &gt;= :salary")
                + " ORDER BY e.fullName</code></p>" : "")
                + "</section>");

        out.println("<section class=\"panel\"><h2>Список</h2>");
        out.println("<table><tr><th>ID</th><th>ФИО</th><th>Должность</th><th>Отдел</th><th>Зарплата</th><th></th></tr>");
        for (Employee e : employees) {
            out.println("<tr><td>" + e.getId() + "</td>"
                    + "<td><a class=\"name\" href=\"" + ctx + "/employees/view?id=" + e.getId() + "\">"
                    + Html.escape(e.getFullName()) + "</a></td>"
                    + "<td>" + Html.escape(Page.nvl(e.getPosition())) + "</td>"
                    + "<td><span class=\"tag\">" + Html.escape(Page.nvl(e.getDepartment())) + "</span></td>"
                    + "<td>" + Page.money(e.getSalary() == null ? 0 : e.getSalary()) + "</td>"
                    + "<td class=\"actions\"><a class=\"btn small\" href=\"" + ctx + "/employees?edit=" + e.getId()
                    + "#form\">Изменить</a>"
                    + "<form method=\"post\" action=\"" + ctx + "/employees/delete\" "
                    + "onsubmit=\"return confirm('Удалить сотрудника?')\">"
                    + "<input type=\"hidden\" name=\"id\" value=\"" + e.getId() + "\">"
                    + "<button class=\"small danger\" type=\"submit\">Удалить</button></form></td></tr>");
        }
        if (employees.isEmpty()) {
            out.println("<tr><td colspan=\"6\" class=\"muted\">"
                    + (filtered ? "По запросу ничего не найдено." : "Список пуст — добавьте первого сотрудника.")
                    + "</td></tr>");
        }
        out.println("</table></section>");

        boolean isEdit = editing != null;
        out.println("<section class=\"panel\" id=\"form\"><h2>"
                + (isEdit ? "Редактирование #" + editing.getId() : "Новый сотрудник") + "</h2>");
        out.println("<form class=\"emp-form\" method=\"post\" action=\"" + ctx + "/employees/save\">");
        if (isEdit) {
            out.println("<input type=\"hidden\" name=\"id\" value=\"" + editing.getId() + "\">");
        }
        out.println(field("fullName", "ФИО", isEdit ? editing.getFullName() : "", true)
                + field("position", "Должность", isEdit ? Page.nvl(editing.getPosition()) : "", false)
                + field("department", "Отдел", isEdit ? Page.nvl(editing.getDepartment()) : "", false)
                + field("salary", "Зарплата, тг", isEdit && editing.getSalary() != null ? String.valueOf(editing.getSalary()) : "", true));
        out.println("<div class=\"form-actions\"><button type=\"submit\">" + (isEdit ? "Сохранить" : "Добавить") + "</button>"
                + (isEdit ? "<a class=\"btn ghost\" href=\"" + ctx + "/employees\">Отмена</a>" : "") + "</div>");
        out.println("</form></section>");
        Page.end(out);
    }

    private static String field(String name, String label, String value, boolean required) {
        return "<div class=\"field\"><label for=\"" + name + "\">" + label + "</label>"
                + "<input id=\"" + name + "\" name=\"" + name + "\" value=\"" + Html.escape(value) + "\""
                + (required ? " required" : "") + (name.equals("salary") ? " type=\"number\" min=\"0\"" : "")
                + "></div>";
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
