package kz.edu.web;

import jakarta.servlet.http.HttpServletRequest;

import java.io.PrintWriter;
import java.util.Locale;
import java.util.Map;

/** Общий каркас страниц: шапка, боковое меню, сообщения, форматирование значений. */
final class Page {

    private static final Map<String, String[]> MESSAGES = Map.ofEntries(
            Map.entry("category-added", new String[]{"ok", "Категория добавлена."}),
            Map.entry("category-deleted", new String[]{"ok", "Категория удалена вместе с её товарами (cascade)."}),
            Map.entry("category-exists", new String[]{"err", "Категория с таким названием уже существует."}),
            Map.entry("product-added", new String[]{"ok", "Товар добавлен."}),
            Map.entry("product-updated", new String[]{"ok", "Товар обновлён."}),
            Map.entry("product-deleted", new String[]{"ok", "Товар удалён (orphanRemoval)."}),
            Map.entry("tag-added", new String[]{"ok", "Тег добавлен."}),
            Map.entry("tag-deleted", new String[]{"ok", "Тег удалён и снят со всех товаров."}),
            Map.entry("tag-exists", new String[]{"err", "Такой тег уже существует."}),
            Map.entry("invalid", new String[]{"err", "Проверьте поля: название обязательно, цена — неотрицательное целое число."}),
            Map.entry("notfound", new String[]{"err", "Запись не найдена."}));

    private Page() {
    }

    static void begin(PrintWriter out, HttpServletRequest request, String title, String subtitle, String active) {
        String ctx = request.getContextPath();
        out.println("<!DOCTYPE html><html lang=\"ru\"><head><meta charset=\"UTF-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
                + "<title>" + title + "</title>"
                + "<link rel=\"stylesheet\" href=\"" + ctx + "/style.css\"></head>"
                + "<body class=\"app\">");
        out.println("<aside class=\"sidebar\"><div class=\"brand\">🛍️ Catalog<span>JPA</span></div><nav>"
                + link(ctx + "/categories", "Категории", "categories".equals(active))
                + link(ctx + "/products", "Товары", "products".equals(active))
                + link(ctx + "/tags", "Теги", "tags".equals(active))
                + "</nav><div class=\"sidebar-foot\">Связи JPA:<br>OneToMany · ManyToOne<br>OneToOne · ManyToMany</div></aside>");
        out.println("<main class=\"content\"><header class=\"topbar\"><div><h1>" + title + "</h1>"
                + "<p class=\"muted\">" + subtitle + "</p></div></header>");

        String msg = request.getParameter("msg");
        String[] message = msg == null ? null : MESSAGES.get(msg);
        if (message != null) {
            out.println("<div class=\"flash " + message[0] + "\">" + message[1] + "</div>");
        }
    }

    static void end(PrintWriter out) {
        out.println("</main></body></html>");
    }

    private static String link(String href, String text, boolean active) {
        return "<a" + (active ? " class=\"active\"" : "") + " href=\"" + href + "\">" + text + "</a>";
    }

    static String card(String title, String value, String color) {
        return "<div class=\"card " + color + "\"><span>" + title + "</span><strong>" + value + "</strong></div>";
    }

    static String money(long value) {
        return String.format(Locale.ROOT, "%,d", value).replace(',', ' ') + " тг";
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
