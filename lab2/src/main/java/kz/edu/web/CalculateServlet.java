package kz.edu.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/calculate")
public class CalculateServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws IOException {

        response.setContentType("text/html; charset=UTF-8");

        String aValue = request.getParameter("a");
        String bValue = request.getParameter("b");

        if (aValue == null || bValue == null ||
                aValue.isBlank() || bValue.isBlank()) {

            response.getWriter().println(
                    "<h2>Необходимо заполнить оба поля</h2>"
            );
            return;
        }

        try {
            double a = Double.parseDouble(aValue);
            double b = Double.parseDouble(bValue);

            double result = a + b;

            response.getWriter().println(
                    "<h1>Результат: " + result + "</h1>"
            );
        } catch (NumberFormatException e) {
            response.getWriter().println(
                    "<h2>Введите корректные числа</h2>"
            );
        }
    }
}
