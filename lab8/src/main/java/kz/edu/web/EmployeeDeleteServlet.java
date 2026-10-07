package kz.edu.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Удаление (em.remove). Только POST, чтобы удаление нельзя было вызвать простой ссылкой. */
@WebServlet("/employees/delete")
public class EmployeeDeleteServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String base = request.getContextPath() + "/employees";
        EmployeeRepository repository = (EmployeeRepository) getServletContext().getAttribute(AppListener.REPOSITORY);

        try {
            boolean deleted = repository.delete(Long.parseLong(request.getParameter("id")));
            response.sendRedirect(base + "?msg=" + (deleted ? "deleted" : "notfound"));
        } catch (NumberFormatException e) {
            response.sendRedirect(base + "?msg=notfound");
        }
    }
}
