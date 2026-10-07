package kz.edu.web;

import jakarta.servlet.http.HttpServletRequest;

import java.io.PrintWriter;
import java.util.Map;

/** Общий каркас страниц: верхнее меню, сообщения, вспомогательные функции форматирования. */
final class Page {

    private static final Map<String, String[]> MESSAGES = Map.ofEntries(
            Map.entry("faculty-added", new String[]{"ok", "Факультет добавлен."}),
            Map.entry("faculty-deleted", new String[]{"ok", "Факультет удалён вместе со студентами (cascade)."}),
            Map.entry("faculty-exists", new String[]{"err", "Факультет с таким названием уже существует."}),
            Map.entry("student-added", new String[]{"ok", "Студент добавлен."}),
            Map.entry("student-updated", new String[]{"ok", "Данные студента обновлены."}),
            Map.entry("student-deleted", new String[]{"ok", "Студент удалён (orphanRemoval)."}),
            Map.entry("course-added", new String[]{"ok", "Курс добавлен."}),
            Map.entry("course-deleted", new String[]{"ok", "Курс удалён и снят со всех студентов."}),
            Map.entry("course-exists", new String[]{"err", "Курс с таким названием уже существует."}),
            Map.entry("invalid", new String[]{"err", "Проверьте поля: имя/название обязательно, балл — число от 0 до 100."}),
            Map.entry("notfound", new String[]{"err", "Запись не найдена."}));

    private Page() {
    }

    static void begin(PrintWriter out, HttpServletRequest request, String title, String active) {
        String ctx = request.getContextPath();
        out.println("<!DOCTYPE html><html lang=\"ru\"><head><meta charset=\"UTF-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
                + "<title>" + title + "</title>"
                + "<link rel=\"stylesheet\" href=\"" + ctx + "/style.css\"></head><body>");
        out.println("<header class=\"nav\"><div class=\"nav-in\"><a class=\"logo\" href=\"" + ctx + "/faculties\">"
                + "<span class=\"logo-mark\">◈</span> Uni<b>Hub</b></a><nav>"
                + link(ctx + "/faculties", "Факультеты", "faculties".equals(active))
                + link(ctx + "/students", "Студенты", "students".equals(active))
                + link(ctx + "/courses", "Курсы", "courses".equals(active))
                + "</nav><a class=\"cta\" href=\"" + ctx + "/student\">+ Студент</a></div></header>");
        out.println("<main class=\"wrap\">");

        String msg = request.getParameter("msg");
        String[] message = msg == null ? null : MESSAGES.get(msg);
        if (message != null) {
            out.println("<div class=\"toast " + message[0] + "\">" + message[1] + "</div>");
        }
    }

    static void end(PrintWriter out) {
        out.println("</main><footer class=\"foot\">JPA · Hibernate ORM · OneToMany · ManyToOne · OneToOne · ManyToMany</footer></body></html>");
    }

    /** Заголовок страницы. */
    static void title(PrintWriter out, String title, String subtitle) {
        out.println("<div class=\"page-title\"><h1>" + title + "</h1><p>" + subtitle + "</p></div>");
    }

    private static String link(String href, String text, boolean active) {
        return "<a" + (active ? " class=\"on\"" : "") + " href=\"" + href + "\">" + text + "</a>";
    }

    static String stat(String label, String value) {
        return "<div class=\"stat\"><strong>" + value + "</strong><span>" + label + "</span></div>";
    }

    static String initials(String name) {
        String[] parts = name.trim().split("\\s+");
        String s = parts[0].isEmpty() ? "?" : parts[0].substring(0, 1);
        if (parts.length > 1 && !parts[1].isEmpty()) {
            s += parts[1].substring(0, 1);
        }
        return Html.escape(s.toUpperCase());
    }

    /** Полоска балла: цвет зависит от значения. */
    static String scoreBar(Integer score) {
        if (score == null) {
            return "<span class=\"dim\">—</span>";
        }
        String level = score >= 85 ? "hi" : score >= 70 ? "mid" : "lo";
        return "<div class=\"score " + level + "\"><i style=\"width:" + score + "%\"></i><em>" + score + "</em></div>";
    }

    static String nvl(String s) {
        return s == null ? "" : s;
    }

    static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    static Long parseLong(String s) {
        try {
            return s == null || s.isBlank() ? null : Long.valueOf(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
