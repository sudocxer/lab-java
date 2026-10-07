package kz.edu.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Вход в систему: логин сохраняется в Session. Пароль не проверяется —
 * это учебная демонстрация работы фильтров, а не полноценная аутентификация.
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException {

        request.setCharacterEncoding("UTF-8");
        String username = request.getParameter("username");

        if (username == null || username.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/login.html?error=1");
            return;
        }

        // новая сессия при входе — защита от фиксации сессии
        HttpSession old = request.getSession(false);
        String returnTo = old == null ? null : (String) old.getAttribute("returnTo");
        if (old != null) {
            old.invalidate();
        }

        HttpSession session = request.getSession();
        session.setAttribute("username", username.trim());
        session.setAttribute("loginTime", System.currentTimeMillis());

        String target = returnTo != null && returnTo.startsWith("/admin/") ? returnTo : "/admin/dashboard";
        response.sendRedirect(request.getContextPath() + target);
    }
}
