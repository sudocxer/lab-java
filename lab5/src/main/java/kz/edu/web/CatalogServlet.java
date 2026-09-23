package kz.edu.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Каталог товаров: точка входа для варианта 4 (корзина товаров в Session).
 */
@WebServlet("/catalog")
public class CatalogServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException {

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        String ctx = request.getContextPath();

        out.println("<!DOCTYPE html><html><head><meta charset=\"UTF-8\">"
                + "<title>Каталог товаров</title></head><body>");
        out.println("<h1>Каталог товаров</h1>");
        out.println("<table border=\"1\" cellpadding=\"6\" cellspacing=\"0\">");
        out.println("<tr><th>Товар</th><th>Цена</th><th></th></tr>");

        for (Product product : Catalog.PRODUCTS) {
            out.println("<tr><td>" + Html.escape(product.getName()) + "</td>"
                    + "<td>" + product.getPrice() + " тг</td>"
                    + "<td><a href=\"" + ctx + "/cart/add?id=" + product.getId()
                    + "\">Добавить в корзину</a></td></tr>");
        }

        out.println("</table>");
        out.println("<p><a href=\"" + ctx + "/cart\">Перейти в корзину</a></p>");
        out.println("</body></html>");
    }
}
