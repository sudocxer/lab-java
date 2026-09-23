package kz.edu.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet("/cart/add")
public class CartAddServlet extends HttpServlet {

    @Override
    @SuppressWarnings("unchecked")
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException {

        String ctx = request.getContextPath();
        long id = parseIdOrDefault(request.getParameter("id"));

        if (Catalog.findById(id).isEmpty()) {
            response.sendRedirect(ctx + "/catalog");
            return;
        }

        HttpSession session = request.getSession();
        Map<Long, Integer> cart = (Map<Long, Integer>) session.getAttribute("cart");
        if (cart == null) {
            cart = new LinkedHashMap<>();
            session.setAttribute("cart", cart);
        }

        cart.merge(id, 1, Integer::sum);

        response.sendRedirect(ctx + "/cart");
    }

    private long parseIdOrDefault(String value) {
        try {
            return value == null ? -1 : Long.parseLong(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
