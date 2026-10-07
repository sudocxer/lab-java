package kz.edu.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Защищённый Servlet с искусственной задержкой: /admin/slow?ms=800.
 * Нужен, чтобы наглядно проверить работу TimingFilter.
 */
@WebServlet("/admin/slow")
public class SlowServlet extends HttpServlet {

    private static final int MAX_MS = 5000;

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        int ms = 500;
        try {
            ms = Math.max(0, Math.min(MAX_MS, Integer.parseInt(request.getParameter("ms"))));
        } catch (NumberFormatException ignored) {
            // параметр не задан или некорректен — берём значение по умолчанию
        }

        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        response.sendRedirect(request.getContextPath() + "/admin/dashboard");
    }
}
