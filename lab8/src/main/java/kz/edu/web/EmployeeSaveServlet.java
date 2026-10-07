package kz.edu.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Добавление (persist) и изменение (через managed entity): если передан id — обновление. */
@WebServlet("/employees/save")
public class EmployeeSaveServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        String base = request.getContextPath() + "/employees";

        String fullName = trim(request.getParameter("fullName"));
        String position = limit(trim(request.getParameter("position")), 60);
        String department = limit(trim(request.getParameter("department")), 60);
        int salary;
        try {
            salary = Integer.parseInt(trim(request.getParameter("salary")));
        } catch (NumberFormatException e) {
            salary = -1;
        }
        if (fullName.isEmpty() || fullName.length() > 100 || salary < 0) {
            response.sendRedirect(base + "?msg=invalid");
            return;
        }

        EmployeeRepository repository = (EmployeeRepository) getServletContext().getAttribute(AppListener.REPOSITORY);
        String idParam = request.getParameter("id");

        if (idParam == null || idParam.isBlank()) {
            repository.add(new Employee(fullName, position, department, salary));
            response.sendRedirect(base + "?msg=added");
            return;
        }
        try {
            boolean found = repository.update(Long.parseLong(idParam), fullName, position, department, salary);
            response.sendRedirect(base + "?msg=" + (found ? "updated" : "notfound"));
        } catch (NumberFormatException e) {
            response.sendRedirect(base + "?msg=invalid");
        }
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private static String limit(String s, int max) {
        return s.length() > max ? s.substring(0, max) : s;
    }
}
