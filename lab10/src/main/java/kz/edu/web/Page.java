package kz.edu.web;

import jakarta.servlet.http.HttpServletRequest;

import java.io.PrintWriter;
import java.util.Locale;

/** Общий каркас страниц: шапка, боковое меню, форматирование значений. */
final class Page {

    private Page() {
    }

    static void begin(PrintWriter out, HttpServletRequest request, String title, String subtitle, String active) {
        String ctx = request.getContextPath();
        out.println("<!DOCTYPE html><html lang=\"ru\"><head><meta charset=\"UTF-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
                + "<title>" + title + "</title>"
                + "<link rel=\"stylesheet\" href=\"" + ctx + "/style.css\"></head>"
                + "<body class=\"app\">");
        out.println("<aside class=\"sidebar\"><div class=\"brand\">🗂️ Staff<span>CDI</span></div><nav>"
                + link(ctx + "/employees", "Сотрудники", "list".equals(active))
                + link(ctx + "/scopes", "CDI: внедрение и scopes", "scopes".equals(active))
                + "</nav><div class=\"sidebar-foot\">Jakarta CDI 4.0<br>Weld · @Inject</div></aside>");
        out.println("<main class=\"content\"><header class=\"topbar\"><div><h1>" + title + "</h1>"
                + "<p class=\"muted\">" + subtitle + "</p></div></header>");
    }

    static void end(PrintWriter out) {
        out.println("</main></body></html>");
    }

    private static String link(String href, String text, boolean active) {
        return "<a" + (active ? " class=\"active\"" : "") + " href=\"" + href + "\">" + text + "</a>";
    }

    static String money(long value) {
        return String.format(Locale.ROOT, "%,d", value).replace(',', ' ') + " тг";
    }

    /** CSS-класс цветной метки для уровня оплаты. */
    static String gradeClass(String grade) {
        return switch (grade) {
            case "Высокий" -> "high";
            case "Выше среднего" -> "above";
            case "Средний" -> "mid";
            default -> "base";
        };
    }
}
