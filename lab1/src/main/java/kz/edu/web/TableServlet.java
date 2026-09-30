package kz.edu.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Индивидуальное задание, вариант 4:
 * Servlet /table, выводящий таблицу умножения от 1 до 10.
 */
@WebServlet("/table")
public class TableServlet extends HttpServlet {

    private static final int SIZE = 10;

    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException {

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head><meta charset=\"UTF-8\"><title>Таблица умножения</title>"
                + "<link rel=\"stylesheet\" href=\"style.css\"></head>");
        out.println("<body>");
        out.println("<h1>Таблица умножения от 1 до " + SIZE + "</h1>");
        out.println("<table>");

        out.print("<tr><th>&times;</th>");
        for (int col = 1; col <= SIZE; col++) {
            out.print("<th>" + col + "</th>");
        }
        out.println("</tr>");

        for (int row = 1; row <= SIZE; row++) {
            out.print("<tr><th>" + row + "</th>");
            for (int col = 1; col <= SIZE; col++) {
                out.print("<td>" + (row * col) + "</td>");
            }
            out.println("</tr>");
        }

        out.println("</table>");
        out.println("</body>");
        out.println("</html>");
    }
}
