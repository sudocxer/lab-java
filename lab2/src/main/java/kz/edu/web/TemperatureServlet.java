package kz.edu.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Locale;

/**
 * Индивидуальное задание, вариант 4:
 * Конвертер градусов Цельсия в Фаренгейты.
 */
@WebServlet("/convert")
public class TemperatureServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws IOException {

        response.setContentType("text/html; charset=UTF-8");

        String celsiusValue = request.getParameter("celsius");

        if (celsiusValue == null || celsiusValue.isBlank()) {
            response.getWriter().println(
                    page("<h2>Необходимо указать температуру в градусах Цельсия</h2>"));
            return;
        }

        try {
            double celsius = Double.parseDouble(celsiusValue);
            double fahrenheit = celsius * 9.0 / 5.0 + 32.0;

            response.getWriter().println(page(String.format(Locale.US,
                    "<h1>%.1f &deg;C = %.1f &deg;F</h1>", celsius, fahrenheit)));
        } catch (NumberFormatException e) {
            response.getWriter().println(page("<h2>Введите корректное число</h2>"));
        }
    }

    private String page(String body) {
        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\">"
                + "<title>Конвертер температуры</title><link rel=\"stylesheet\" href=\"style.css\"></head>"
                + "<body>" + body + "</body></html>";
    }
}
