package kz.edu.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;

/** Защищённая страница /admin/audit: журнал действий пользователей. */
@WebServlet("/admin/audit")
public class AuditServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        // AuthFilter гарантирует, что сессия и username существуют
        String username = (String) request.getSession(false).getAttribute("username");
        String ctx = request.getContextPath();
        List<AuditLog.Entry> entries = AuditLog.all();

        long errors = entries.stream().filter(e -> e.status() >= 400).count();
        long users = entries.stream().map(AuditLog.Entry::user).distinct().count();

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html><html lang=\"ru\"><head><meta charset=\"UTF-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
                + "<title>Журнал аудита</title>"
                + "<link rel=\"stylesheet\" href=\"" + ctx + "/style.css\"></head>"
                + "<body class=\"app\">");

        out.println("<aside class=\"sidebar\"><div class=\"brand\">⏱ Audit<span>Log</span></div>"
                + "<nav><a class=\"active\" href=\"" + ctx + "/admin/audit\">Журнал</a>"
                + "<a href=\"" + ctx + "/admin/action?type=create\">Действие: создать</a>"
                + "<a href=\"" + ctx + "/admin/action?type=edit\">Действие: изменить</a>"
                + "<a href=\"" + ctx + "/admin/action?type=delete\">Действие: удалить</a>"
                + "<a href=\"" + ctx + "/admin/action?type=hack\">Неверное действие</a>"
                + "<a href=\"" + ctx + "/logout\">Выйти</a></nav></aside>");

        out.println("<main class=\"content\">");
        out.println("<header class=\"topbar\"><div><h1>Журнал аудита</h1>"
                + "<p class=\"muted\">Вы вошли как " + Html.escape(username)
                + ". Каждое действие фиксирует AuditFilter.</p></div>"
                + "<div class=\"avatar\">" + Html.escape(username.substring(0, 1).toUpperCase(Locale.ROOT))
                + "</div></header>");

        out.println("<section class=\"cards\">"
                + card("Записей в журнале", String.valueOf(entries.size()), "blue")
                + card("Пользователей", String.valueOf(users), "green")
                + card("Ошибок (4xx/5xx)", String.valueOf(errors), "orange")
                + "</section>");

        out.println("<section class=\"panel\"><h2>Действия пользователей</h2>");
        out.println("<table><tr><th>Время</th><th>Пользователь</th><th>IP</th><th>Запрос</th>"
                + "<th>Параметры</th><th>Статус</th></tr>");
        for (AuditLog.Entry e : entries) {
            String cls = e.status() >= 400 ? "bar slow" : e.status() >= 300 ? "bar mid" : "bar";
            out.println("<tr><td>" + e.time().toLocalTime() + "</td><td>" + Html.escape(e.user()) + "</td>"
                    + "<td class=\"mono\">" + Html.escape(e.ip()) + "</td>"
                    + "<td><span class=\"tag\">" + e.method() + "</span> <span class=\"mono\">"
                    + Html.escape(e.uri()) + "</span></td>"
                    + "<td class=\"mono\">" + Html.escape(e.params()) + "</td>"
                    + "<td><div class=\"barwrap\"><div class=\"" + cls + "\" style=\"width:8px;height:8px;border-radius:50%\"></div>"
                    + "<span>" + e.status() + "</span></div></td></tr>");
        }
        out.println("</table></section>");
        out.println("</main></body></html>");
    }

    private static String card(String title, String value, String color) {
        return "<div class=\"card " + color + "\"><span>" + title + "</span><strong>" + value + "</strong></div>";
    }
}
