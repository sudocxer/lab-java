package kz.edu.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Set;

/**
 * Имитация действий пользователя (/admin/action?type=...), чтобы было что журналировать.
 * Сам Servlet ничего не пишет в журнал — это делает AuditFilter.
 */
@WebServlet("/admin/action")
public class ActionServlet extends HttpServlet {

    private static final Set<String> TYPES = Set.of("create", "edit", "delete");

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        String type = request.getParameter("type");
        if (type == null || !TYPES.contains(type)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Неизвестное действие");
            return;
        }
        response.sendRedirect(request.getContextPath() + "/admin/audit");
    }
}
