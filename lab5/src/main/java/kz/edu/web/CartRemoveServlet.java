package kz.edu.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Map;

@WebServlet("/cart/remove")
public class CartRemoveServlet extends HttpServlet {

    @Override
    @SuppressWarnings("unchecked")
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException {

        String ctx = request.getContextPath();
        HttpSession session = request.getSession(false);

        if (session != null) {
            Map<Long, Integer> cart = (Map<Long, Integer>) session.getAttribute("cart");
            if (cart != null) {
                try {
                    long id = Long.parseLong(request.getParameter("id"));
                    cart.remove(id);
                } catch (NumberFormatException ignored) {
                    // некорректный id — просто игнорируем
                }
            }
        }

        response.sendRedirect(ctx + "/cart");
    }
}
