package kz.edu.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Список заказов (Read) и форма добавления/редактирования.
 * /orders — список и пустая форма, /orders?edit=ID — форма с данными заказа.
 */
@WebServlet("/orders")
public class OrdersServlet extends HttpServlet {

    static final Map<String, String> STATUSES = Map.of(
            "NEW", "Новый",
            "PAID", "Оплачен",
            "SHIPPED", "Отправлен",
            "CANCELLED", "Отменён");
    private static final List<String> STATUS_ORDER = List.of("NEW", "PAID", "SHIPPED", "CANCELLED");

    private static final Map<String, String[]> MESSAGES = Map.of(
            "added", new String[]{"ok", "Заказ добавлен."},
            "updated", new String[]{"ok", "Заказ обновлён."},
            "deleted", new String[]{"ok", "Заказ удалён."},
            "invalid", new String[]{"err", "Проверьте поля: клиент и товар обязательны, количество больше 0, сумма не отрицательная."},
            "notfound", new String[]{"err", "Заказ не найден."});

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException, ServletException {

        OrderDao dao = (OrderDao) getServletContext().getAttribute(AppListener.DAO);
        String ctx = request.getContextPath();

        List<Order> orders;
        Order editing = null;
        try {
            orders = dao.findAll();
            String edit = request.getParameter("edit");
            if (edit != null) {
                try {
                    editing = dao.findById(Long.parseLong(edit));
                } catch (NumberFormatException ignored) {
                    // некорректный id — покажем пустую форму
                }
            }
        } catch (SQLException e) {
            throw new ServletException("Ошибка чтения из базы данных", e);
        }

        long revenue = orders.stream().filter(o -> !o.getStatus().equals("CANCELLED"))
                .mapToLong(Order::getAmount).sum();
        long active = orders.stream().filter(o -> o.getStatus().equals("NEW") || o.getStatus().equals("PAID")).count();

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html><html lang=\"ru\"><head><meta charset=\"UTF-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
                + "<title>Заказы</title>"
                + "<link rel=\"stylesheet\" href=\"" + ctx + "/style.css\"></head>"
                + "<body class=\"app\">");

        out.println("<aside class=\"sidebar\"><div class=\"brand\">▣ Orders<span>DB</span></div>"
                + "<nav><a class=\"active\" href=\"" + ctx + "/orders\">Заказы</a>"
                + "<a href=\"" + ctx + "/orders#form\">Новый заказ</a></nav></aside>");

        out.println("<main class=\"content\">");
        out.println("<header class=\"topbar\"><div><h1>Заказы</h1>"
                + "<p class=\"muted\">CRUD через JDBC: DAO, PreparedStatement, DataSource.</p></div></header>");

        String msg = request.getParameter("msg");
        String[] message = msg == null ? null : MESSAGES.get(msg);
        if (message != null) {
            out.println("<div class=\"flash " + message[0] + "\">" + message[1] + "</div>");
        }

        out.println("<section class=\"cards\">"
                + card("Всего заказов", String.valueOf(orders.size()), "blue")
                + card("В работе", String.valueOf(active), "orange")
                + card("Выручка (без отменённых)", money(revenue), "green")
                + "</section>");

        out.println("<section class=\"panel\"><h2>Список</h2>");
        out.println("<table><tr><th>ID</th><th>Клиент</th><th>Товар</th><th>Кол-во</th><th>Сумма</th><th>Статус</th><th></th></tr>");
        for (Order o : orders) {
            out.println("<tr><td>" + o.getId() + "</td><td>" + Html.escape(o.getCustomer()) + "</td>"
                    + "<td>" + Html.escape(o.getItem()) + "</td><td>" + o.getQuantity() + "</td>"
                    + "<td>" + money(o.getAmount()) + "</td>"
                    + "<td><span class=\"tag st-" + o.getStatus().toLowerCase(Locale.ROOT) + "\">"
                    + STATUSES.getOrDefault(o.getStatus(), o.getStatus()) + "</span></td>"
                    + "<td class=\"actions\"><a class=\"btn small\" href=\"" + ctx + "/orders?edit=" + o.getId()
                    + "#form\">Изменить</a>"
                    + "<form method=\"post\" action=\"" + ctx + "/orders/delete\" "
                    + "onsubmit=\"return confirm('Удалить заказ?')\">"
                    + "<input type=\"hidden\" name=\"id\" value=\"" + o.getId() + "\">"
                    + "<button class=\"small danger\" type=\"submit\">Удалить</button></form></td></tr>");
        }
        if (orders.isEmpty()) {
            out.println("<tr><td colspan=\"7\" class=\"muted\">Заказов нет — создайте первый.</td></tr>");
        }
        out.println("</table></section>");

        boolean isEdit = editing != null;
        out.println("<section class=\"panel\" id=\"form\"><h2>"
                + (isEdit ? "Редактирование заказа #" + editing.getId() : "Новый заказ") + "</h2>");
        out.println("<form class=\"order-form\" method=\"post\" action=\"" + ctx + "/orders/save\">");
        if (isEdit) {
            out.println("<input type=\"hidden\" name=\"id\" value=\"" + editing.getId() + "\">");
        }
        out.println(field("customer", "Клиент", isEdit ? editing.getCustomer() : "", "text")
                + field("item", "Товар", isEdit ? editing.getItem() : "", "text")
                + field("quantity", "Количество", isEdit ? String.valueOf(editing.getQuantity()) : "1", "number\" min=\"1")
                + field("amount", "Сумма, тг", isEdit ? String.valueOf(editing.getAmount()) : "", "number\" min=\"0"));

        String current = isEdit ? editing.getStatus() : "NEW";
        StringBuilder select = new StringBuilder("<div class=\"field\"><label for=\"status\">Статус</label><select id=\"status\" name=\"status\">");
        for (String code : STATUS_ORDER) {
            select.append("<option value=\"").append(code).append("\"")
                    .append(code.equals(current) ? " selected" : "").append(">")
                    .append(STATUSES.get(code)).append("</option>");
        }
        out.println(select.append("</select></div>"));

        out.println("<div class=\"form-actions\"><button type=\"submit\">" + (isEdit ? "Сохранить" : "Создать") + "</button>"
                + (isEdit ? "<a class=\"btn ghost\" href=\"" + ctx + "/orders\">Отмена</a>" : "") + "</div>");
        out.println("</form></section>");
        out.println("</main></body></html>");
    }

    private static String field(String name, String label, String value, String type) {
        return "<div class=\"field\"><label for=\"" + name + "\">" + label + "</label>"
                + "<input id=\"" + name + "\" name=\"" + name + "\" type=\"" + type + "\" value=\""
                + Html.escape(value) + "\" required></div>";
    }

    private static String card(String title, String value, String color) {
        return "<div class=\"card " + color + "\"><span>" + title + "</span><strong>" + value + "</strong></div>";
    }

    private static String money(long value) {
        return String.format(Locale.ROOT, "%,d", value).replace(',', ' ') + " тг";
    }
}
