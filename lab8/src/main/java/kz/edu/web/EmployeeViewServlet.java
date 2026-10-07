package kz.edu.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

/** Просмотр одного сотрудника: /employees/view?id=ID (em.find). */
@WebServlet("/employees/view")
public class EmployeeViewServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        EmployeeRepository repository = (EmployeeRepository) getServletContext().getAttribute(AppListener.REPOSITORY);
        String ctx = request.getContextPath();

        Employee employee = null;
        try {
            employee = repository.findById(Long.parseLong(request.getParameter("id")));
        } catch (NumberFormatException ignored) {
            // некорректный id — сотрудник не найден
        }
        if (employee == null) {
            response.sendRedirect(ctx + "/employees?msg=notfound");
            return;
        }

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        Page.begin(out, request, "Карточка сотрудника", "Запись #" + employee.getId() + " загружена через EntityManager.find().", "list");

        String name = employee.getFullName().trim();
        String initial = name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase();

        out.println("<section class=\"panel profile\"><div class=\"avatar big\">" + Html.escape(initial) + "</div>"
                + "<div class=\"profile-main\"><h2>" + Html.escape(name) + "</h2>"
                + "<p class=\"muted\">" + Html.escape(Page.nvl(employee.getPosition())) + "</p>"
                + "<dl>"
                + "<dt>ID</dt><dd>" + employee.getId() + "</dd>"
                + "<dt>Отдел</dt><dd><span class=\"tag\">" + Html.escape(Page.nvl(employee.getDepartment())) + "</span></dd>"
                + "<dt>Зарплата</dt><dd>" + Page.money(employee.getSalary() == null ? 0 : employee.getSalary()) + "</dd>"
                + "</dl>"
                + "<div class=\"form-actions\">"
                + "<a class=\"btn\" href=\"" + ctx + "/employees?edit=" + employee.getId() + "#form\">Изменить</a>"
                + "<a class=\"btn ghost\" href=\"" + ctx + "/employees\">К списку</a></div>"
                + "</div></section>");
        Page.end(out);
    }
}
