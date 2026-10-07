package kz.edu.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.List;
import java.util.Locale;

/** Защищённая панель (/admin/dashboard): данные TimingFilter в виде карточек и таблицы. */
@WebServlet("/admin/dashboard")
public class DashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        // AuthFilter гарантирует, что сессия и username существуют
        HttpSession session = request.getSession(false);
        String username = (String) session.getAttribute("username");
        long loginTime = (Long) session.getAttribute("loginTime");
        String ctx = request.getContextPath();

        List<RequestStats.Entry> entries = RequestStats.recent();
        double avg = entries.stream().mapToDouble(RequestStats.Entry::millis).average().orElse(0);
        double max = entries.stream().mapToDouble(RequestStats.Entry::millis).max().orElse(0);
        long minutes = Duration.ofMillis(System.currentTimeMillis() - loginTime).toMinutes();
        String initial = username.substring(0, 1).toUpperCase(Locale.ROOT);

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html><html lang=\"ru\"><head><meta charset=\"UTF-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
                + "<title>Панель — TimingFilter</title>"
                + "<link rel=\"stylesheet\" href=\"" + ctx + "/style.css\"></head>"
                + "<body class=\"app\">");

        out.println("<aside class=\"sidebar\"><div class=\"brand\">⏱ Timing<span>Filter</span></div>"
                + "<nav><a class=\"active\" href=\"" + ctx + "/admin/dashboard\">Панель</a>"
                + "<a href=\"" + ctx + "/admin/slow?ms=300\">Запрос 300 мс</a>"
                + "<a href=\"" + ctx + "/admin/slow?ms=1200\">Запрос 1200 мс</a>"
                + "<a href=\"" + ctx + "/logout\">Выйти</a></nav></aside>");

        out.println("<main class=\"content\">");
        out.println("<header class=\"topbar\"><div><h1>Привет, " + Html.escape(username) + "!</h1>"
                + "<p class=\"muted\">В системе " + minutes
                + " мин. Время выполнения запросов измеряет TimingFilter.</p></div>"
                + "<div class=\"avatar\">" + Html.escape(initial) + "</div></header>");

        out.println("<section class=\"cards\">"
                + card("Всего запросов", String.valueOf(RequestStats.total()), "blue")
                + card("Среднее время", fmt(avg) + " мс", "green")
                + card("Максимум", fmt(max) + " мс", "orange")
                + "</section>");

        out.println("<section class=\"panel\"><h2>Последние запросы</h2>");
        out.println("<table><tr><th>Время</th><th>Метод</th><th>URL</th><th>Статус</th><th>Длительность</th></tr>");
        double scale = Math.max(max, 1);
        for (RequestStats.Entry e : entries) {
            int width = (int) Math.max(2, Math.round(e.millis() / scale * 100));
            String cls = e.millis() > 500 ? "bar slow" : e.millis() > 100 ? "bar mid" : "bar";
            out.println("<tr><td>" + e.time() + "</td><td><span class=\"tag\">" + e.method() + "</span></td>"
                    + "<td class=\"mono\">" + Html.escape(e.uri()) + "</td><td>" + e.status() + "</td>"
                    + "<td><div class=\"barwrap\"><div class=\"" + cls + "\" style=\"width:" + width + "px\"></div>"
                    + "<span>" + fmt(e.millis()) + " мс</span></div></td></tr>");
        }
        out.println("</table></section>");
        out.println("</main></body></html>");
    }

    private static String card(String title, String value, String color) {
        return "<div class=\"card " + color + "\"><span>" + title + "</span><strong>" + value + "</strong></div>";
    }

    private static String fmt(double v) {
        return String.format(Locale.ROOT, "%.1f", v);
    }
}
