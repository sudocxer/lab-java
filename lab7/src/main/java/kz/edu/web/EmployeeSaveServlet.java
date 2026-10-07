package kz.edu.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/** Create и Update: если передан id — обновление, иначе добавление. */
@WebServlet("/employees/save")
public class EmployeeSaveServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException, ServletException {

        request.setCharacterEncoding("UTF-8");
        String base = request.getContextPath() + "/employees";

        String fullName = trim(request.getParameter("fullName"));
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

        Employee employee = new Employee();
        employee.setFullName(fullName);
        employee.setPosition(limit(trim(request.getParameter("position")), 60));
        employee.setDepartment(limit(trim(request.getParameter("department")), 60));
        employee.setSalary(salary);

        EmployeeDao dao = (EmployeeDao) getServletContext().getAttribute(AppListener.DAO);
        String idParam = request.getParameter("id");

        try {
            if (idParam == null || idParam.isBlank()) {
                dao.insert(employee);
                response.sendRedirect(base + "?msg=added");
            } else {
                employee.setId(Long.parseLong(idParam));
                boolean found = dao.update(employee);
                response.sendRedirect(base + "?msg=" + (found ? "updated" : "notfound"));
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(base + "?msg=invalid");
        } catch (SQLException e) {
            throw new ServletException("Ошибка записи в базу данных", e);
        }
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private static String limit(String s, int max) {
        return s.length() > max ? s.substring(0, max) : s;
    }
}
