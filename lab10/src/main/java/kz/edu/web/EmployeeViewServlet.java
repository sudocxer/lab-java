package kz.edu.web;

import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

/** Карточка одного сотрудника: /employees/view?id=ID. */
@WebServlet("/employees/view")
public class EmployeeViewServlet extends HttpServlet {

    @Inject
    private EmployeeService service;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String ctx = request.getContextPath();

        Employee employee = null;
        try {
            employee = service.findById(Integer.parseInt(request.getParameter("id"))).orElse(null);
        } catch (NumberFormatException ignored) {
            // некорректный id — сотрудник не найден
        }
        if (employee == null) {
            response.sendRedirect(ctx + "/employees?msg=notfound");
            return;
        }

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        Page.begin(out, request, "Карточка сотрудника",
                "Запись #" + employee.id() + " получена через EmployeeService.findById().", "list");

        String initial = employee.fullName().substring(0, 1).toUpperCase();
        String grade = service.getGrade(employee.salary());

        out.println("<section class=\"panel profile\"><div class=\"avatar big\">" + Html.escape(initial) + "</div>"
                + "<div class=\"profile-main\"><h2>" + Html.escape(employee.fullName()) + "</h2>"
                + "<p class=\"muted\">" + Html.escape(employee.position()) + "</p>"
                + "<dl>"
                + "<dt>ID</dt><dd>" + employee.id() + "</dd>"
                + "<dt>Отдел</dt><dd><span class=\"tag\">" + Html.escape(employee.department()) + "</span></dd>"
                + "<dt>Зарплата</dt><dd>" + Page.money(employee.salary()) + "</dd>"
                + "<dt>Уровень</dt><dd><span class=\"tag " + Page.gradeClass(grade) + "\">" + grade + "</span></dd>"
                + "</dl>"
                + "<div class=\"form-actions\"><a class=\"btn ghost\" href=\"" + ctx + "/employees\">К списку</a></div>"
                + "</div></section>");
        Page.end(out);
    }
}
