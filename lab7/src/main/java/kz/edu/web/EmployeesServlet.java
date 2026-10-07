package kz.edu.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Список сотрудников (Read) и форма добавления/редактирования.
 * /employees — список и пустая форма, /employees?edit=ID — форма с данными сотрудника.
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
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException, ServletException {

        EmployeeDao dao = (EmployeeDao) getServletContext().getAttribute(AppListener.DAO);
        String ctx = request.getContextPath();

        List<Employee> employees;
        Employee editing = null;
        try {
            employees = dao.findAll();
            String edit = request.getParameter("edit");
            if (edit != null) {
                try {
                    editing = dao.findById(Long.parseLong(edit));
                } catch (NumberFormatException ignored) {
                    // некорректный id — покажем пустую форму
                }
            }
        } catch (SQLException e) {
            throw new ServletException("Ошибка чтения из базы данных", e);
        }

        long total = employees.stream().mapToLong(Employee::getSalary).sum();
        long average = employees.isEmpty() ? 0 : total / employees.size();

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html><html lang=\"ru\"><head><meta charset=\"UTF-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
                + "<title>Сотрудники</title>"
                + "<link rel=\"stylesheet\" href=\"" + ctx + "/style.css\"></head>"
                + "<body class=\"app\">");

        out.println("<aside class=\"sidebar\"><div class=\"brand\">👥 Staff<span>DB</span></div>"
                + "<nav><a class=\"active\" href=\"" + ctx + "/employees\">Сотрудники</a>"
                + "<a href=\"" + ctx + "/employees#form\">Добавить сотрудника</a></nav></aside>");

        out.println("<main class=\"content\">");
        out.println("<header class=\"topbar\"><div><h1>Сотрудники</h1>"
                + "<p class=\"muted\">CRUD через JDBC: DAO, PreparedStatement, DataSource.</p></div></header>");

        String msg = request.getParameter("msg");
        String[] message = msg == null ? null : MESSAGES.get(msg);
        if (message != null) {
            out.println("<div class=\"flash " + message[0] + "\">" + message[1] + "</div>");
        }

        out.println("<section class=\"cards\">"
                + card("Сотрудников", String.valueOf(employees.size()), "blue")
                + card("Средняя зарплата", money(average), "green")
                + card("Фонд оплаты", money(total), "orange")
                + "</section>");

        out.println("<section class=\"panel\"><h2>Список</h2>");
        out.println("<table><tr><th>ID</th><th>ФИО</th><th>Должность</th><th>Отдел</th><th>Зарплата</th><th></th></tr>");
        for (Employee e : employees) {
            out.println("<tr><td>" + e.getId() + "</td><td>" + Html.escape(e.getFullName()) + "</td>"
                    + "<td>" + Html.escape(nvl(e.getPosition())) + "</td>"
                    + "<td><span class=\"tag\">" + Html.escape(nvl(e.getDepartment())) + "</span></td>"
                    + "<td>" + money(e.getSalary()) + "</td>"
                    + "<td class=\"actions\"><a class=\"btn small\" href=\"" + ctx + "/employees?edit=" + e.getId()
                    + "#form\">Изменить</a>"
                    + "<form method=\"post\" action=\"" + ctx + "/employees/delete\" "
                    + "onsubmit=\"return confirm('Удалить сотрудника?')\">"
                    + "<input type=\"hidden\" name=\"id\" value=\"" + e.getId() + "\">"
                    + "<button class=\"small danger\" type=\"submit\">Удалить</button></form></td></tr>");
        }
        if (employees.isEmpty()) {
            out.println("<tr><td colspan=\"6\" class=\"muted\">Список пуст — добавьте первого сотрудника.</td></tr>");
        }
        out.println("</table></section>");

        boolean isEdit = editing != null;
        out.println("<section class=\"panel\" id=\"form\"><h2>" + (isEdit ? "Редактирование #" + editing.getId() : "Новый сотрудник") + "</h2>");
        out.println("<form class=\"emp-form\" method=\"post\" action=\"" + ctx + "/employees/save\">");
        if (isEdit) {
            out.println("<input type=\"hidden\" name=\"id\" value=\"" + editing.getId() + "\">");
        }
        out.println(field("fullName", "ФИО", isEdit ? editing.getFullName() : "", true)
                + field("position", "Должность", isEdit ? nvl(editing.getPosition()) : "", false)
                + field("department", "Отдел", isEdit ? nvl(editing.getDepartment()) : "", false)
                + field("salary", "Зарплата, тг", isEdit ? String.valueOf(editing.getSalary()) : "", true));
        out.println("<div class=\"form-actions\"><button type=\"submit\">" + (isEdit ? "Сохранить" : "Добавить") + "</button>"
                + (isEdit ? "<a class=\"btn ghost\" href=\"" + ctx + "/employees\">Отмена</a>" : "") + "</div>");
        out.println("</form></section>");
        out.println("</main></body></html>");
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

    private static String money(long value) {
        return String.format(Locale.ROOT, "%,d", value).replace(',', ' ') + " тг";
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }
}
