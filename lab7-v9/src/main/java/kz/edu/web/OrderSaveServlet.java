package kz.edu.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/** Create и Update: если передан id — обновление, иначе добавление. */
@WebServlet("/orders/save")
public class OrderSaveServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException, ServletException {

        request.setCharacterEncoding("UTF-8");
        String base = request.getContextPath() + "/orders";

        String customer = trim(request.getParameter("customer"));
        String item = trim(request.getParameter("item"));
        String status = trim(request.getParameter("status"));
        int quantity;
        int amount;
        try {
            quantity = Integer.parseInt(trim(request.getParameter("quantity")));
            amount = Integer.parseInt(trim(request.getParameter("amount")));
        } catch (NumberFormatException e) {
            response.sendRedirect(base + "?msg=invalid");
            return;
        }
        if (customer.isEmpty() || customer.length() > 100 || item.isEmpty() || item.length() > 100
                || quantity < 1 || amount < 0 || !OrdersServlet.STATUSES.containsKey(status)) {
            response.sendRedirect(base + "?msg=invalid");
            return;
        }

        Order order = new Order();
        order.setCustomer(customer);
        order.setItem(item);
        order.setQuantity(quantity);
        order.setAmount(amount);
        order.setStatus(status);

        OrderDao dao = (OrderDao) getServletContext().getAttribute(AppListener.DAO);
        String idParam = request.getParameter("id");

        try {
            if (idParam == null || idParam.isBlank()) {
                dao.insert(order);
                response.sendRedirect(base + "?msg=added");
            } else {
                order.setId(Long.parseLong(idParam));
                boolean found = dao.update(order);
                response.sendRedirect(base + "?msg=" + (found ? "updated" : "notfound"));
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(base + "?msg=invalid");
        } catch (SQLException e) {
            throw new ServletException("Ошибка записи в базу данных", e);
        }
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
