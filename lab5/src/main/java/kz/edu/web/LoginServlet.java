package kz.edu.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Базовый пример из методички: авторизация через HttpSession.
 * Проверка пароля не выполняется — это учебная демонстрация механизма Session,
 * а не полноценная система аутентификации.
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws IOException {

        String username = request.getParameter("username");

        if (username == null || username.isBlank()) {
            response.setContentType("text/html; charset=UTF-8");
            response.getWriter().println("<h2>Необходимо указать логин</h2>");
            return;
        }

        HttpSession session = request.getSession();
        session.setAttribute("username", username);

        response.sendRedirect("profile");
    }
}
