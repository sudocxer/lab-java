package kz.edu.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/** Теги (вторая сторона ManyToMany): список с числом товаров, добавление и удаление. */
@WebServlet("/tags")
public class TagsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        CatalogRepository repo = (CatalogRepository) getServletContext().getAttribute(AppListener.REPOSITORY);
        String ctx = request.getContextPath();

        List<Tag> tags = repo.tags();
        Map<Long, Long> usage = repo.tagUsage();

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        Page.begin(out, request, "Теги", "Связь многие-ко-многим: товар имеет много тегов, тег относится ко многим товарам (таблица product_tags).", "tags");

        out.println("<section class=\"grid\">");
        for (Tag t : tags) {
            long used = usage.getOrDefault(t.getId(), 0L);
            out.println("<article class=\"cat-card\"><div class=\"cat-head\"><h3><a href=\"" + ctx + "/products?tagId=" + t.getId() + "\">"
                    + Html.escape(t.getName()) + "</a></h3><span class=\"tag\">" + used + " тов.</span></div>"
                    + "<div class=\"actions\"><a class=\"btn small\" href=\"" + ctx + "/products?tagId=" + t.getId() + "\">Товары</a>"
                    + "<form method=\"post\" action=\"" + ctx + "/actions/tag-delete\" onsubmit=\"return confirm('Удалить тег?')\">"
                    + "<input type=\"hidden\" name=\"id\" value=\"" + t.getId() + "\">"
                    + "<button class=\"small danger\" type=\"submit\">Удалить</button></form></div></article>");
        }
        if (tags.isEmpty()) {
            out.println("<p class=\"muted\">Тегов нет — добавьте первый.</p>");
        }
        out.println("</section>");

        out.println("<section class=\"panel\"><h2>Новый тег</h2><form class=\"emp-form\" method=\"post\" action=\"" + ctx + "/actions/tag-add\">"
                + "<div class=\"field\"><label for=\"name\">Название</label><input id=\"name\" name=\"name\" required maxlength=\"40\"></div>"
                + "<div class=\"form-actions\"><button type=\"submit\">Добавить</button></div></form></section>");
        Page.end(out);
    }
}
