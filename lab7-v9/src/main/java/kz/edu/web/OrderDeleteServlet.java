package kz.edu.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/** Delete. Только POST, чтобы удаление нельзя было вызвать простой ссылкой. */
@WebServlet("/orders/delete")
public class OrderDeleteServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException, ServletException {

        String base = request.getContextPath() + "/orders";
        OrderDao dao = (OrderDao) getServletContext().getAttribute(AppListener.DAO);

        try {
            boolean deleted = dao.delete(Long.parseLong(request.getParameter("id")));
            response.sendRedirect(base + "?msg=" + (deleted ? "deleted" : "notfound"));
        } catch (NumberFormatException e) {
            response.sendRedirect(base + "?msg=notfound");
        } catch (SQLException e) {
            throw new ServletException("Ошибка удаления из базы данных", e);
        }
    }
}
