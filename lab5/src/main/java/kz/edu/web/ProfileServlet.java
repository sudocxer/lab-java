package kz.edu.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);

        response.setContentType("text/html; charset=UTF-8");

        if (session == null || session.getAttribute("username") == null) {
            response.getWriter().println("<h2>Необходимо войти в систему</h2>"
                    + "<p><a href=\"login.html\">Войти</a></p>");
            return;
        }

        String username = (String) session.getAttribute("username");

        response.getWriter().println(
                "<h1>Добро пожаловать, " + Html.escape(username) + "!</h1>"
                        + "<p><a href=\"logout\">Выйти</a></p>"
        );
    }
}
