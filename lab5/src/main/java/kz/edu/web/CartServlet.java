package kz.edu.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Индивидуальное задание, вариант 4: корзина товаров в Session.
 */
@WebServlet("/cart")
public class CartServlet extends HttpServlet {

    @Override
    @SuppressWarnings("unchecked")
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException {

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        String ctx = request.getContextPath();

        HttpSession session = request.getSession(false);
        Map<Long, Integer> cart = session == null
                ? null
                : (Map<Long, Integer>) session.getAttribute("cart");

        out.println("<!DOCTYPE html><html><head><meta charset=\"UTF-8\">"
                + "<title>Корзина</title><link rel=\"stylesheet\" href=\"style.css\"></head><body>");
        out.println("<h1>Корзина товаров</h1>");

        if (cart == null || cart.isEmpty()) {
            out.println("<p>Корзина пуста.</p>");
        } else {
            out.println("<table>");
            out.println("<tr><th>Товар</th><th>Цена</th><th>Кол-во</th><th>Сумма</th><th></th></tr>");

            double total = 0;
            for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
                Product product = Catalog.findById(entry.getKey()).orElse(null);
                if (product == null) {
                    continue;
                }
                int quantity = entry.getValue();
                double lineTotal = product.getPrice() * quantity;
                total += lineTotal;

                out.println("<tr><td>" + Html.escape(product.getName()) + "</td>"
                        + "<td>" + product.getPrice() + " тг</td>"
                        + "<td>" + quantity + "</td>"
                        + "<td>" + lineTotal + " тг</td>"
                        + "<td><a href=\"" + ctx + "/cart/remove?id=" + product.getId()
                        + "\">Убрать</a></td></tr>");
            }

            out.println("</table>");
            out.println("<p><strong>Итого: " + total + " тг</strong></p>");
            out.println("<p><a href=\"" + ctx + "/cart/clear\">Очистить корзину</a></p>");
        }

        out.println("<p><a href=\"" + ctx + "/catalog\">Вернуться в каталог</a></p>");
        out.println("</body></html>");
    }
}
