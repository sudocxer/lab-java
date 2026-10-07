package kz.edu.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/** Все товары (ManyToOne к категории) с фильтрами по названию, категории и тегу. */
@WebServlet("/products")
public class ProductsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        CatalogRepository repo = (CatalogRepository) getServletContext().getAttribute(AppListener.REPOSITORY);
        String ctx = request.getContextPath();

        String name = Page.trim(request.getParameter("name"));
        Long categoryId = Page.parseLong(request.getParameter("categoryId"));
        Long tagId = Page.parseLong(request.getParameter("tagId"));
        boolean filtered = !name.isEmpty() || categoryId != null || tagId != null;

        List<Product> products = repo.products(name.isEmpty() ? null : name, categoryId, tagId);
        List<Category> categories = repo.categories();
        List<Tag> tags = repo.tags();

        long total = products.stream().mapToLong(p -> p.getPrice() == null ? 0 : p.getPrice()).sum();

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        Page.begin(out, request, "Товары", "Связи ManyToOne (категория), OneToOne (детали) и ManyToMany (теги).", "products");

        out.println("<section class=\"cards\">"
                + Page.card(filtered ? "Найдено" : "Товаров", String.valueOf(products.size()), "blue")
                + Page.card("Средняя цена", Page.money(products.isEmpty() ? 0 : total / products.size()), "green")
                + Page.card("Общая стоимость", Page.money(total), "orange")
                + "</section>");

        out.println("<section class=\"panel\"><h2>Фильтр</h2><form class=\"search-form\" method=\"get\" action=\"" + ctx + "/products\">"
                + "<div class=\"field\"><label for=\"name\">Название</label><input id=\"name\" name=\"name\" placeholder=\"Например: чай\" value=\""
                + Html.escape(name) + "\"></div>"
                + "<div class=\"field\"><label for=\"categoryId\">Категория</label><select id=\"categoryId\" name=\"categoryId\"><option value=\"\">Все</option>");
        for (Category c : categories) {
            out.println("<option value=\"" + c.getId() + "\"" + (c.getId().equals(categoryId) ? " selected" : "") + ">"
                    + Html.escape(c.getName()) + "</option>");
        }
        out.println("</select></div><div class=\"field\"><label for=\"tagId\">Тег</label><select id=\"tagId\" name=\"tagId\"><option value=\"\">Все</option>");
        for (Tag t : tags) {
            out.println("<option value=\"" + t.getId() + "\"" + (t.getId().equals(tagId) ? " selected" : "") + ">"
                    + Html.escape(t.getName()) + "</option>");
        }
        out.println("</select></div><div class=\"form-actions\"><button type=\"submit\">Найти</button>"
                + (filtered ? "<a class=\"btn ghost\" href=\"" + ctx + "/products\">Сбросить</a>" : "")
                + "</div></form></section>");

        out.println("<section class=\"panel\"><h2>Список</h2><table><tr><th>ID</th><th>Название</th><th>Категория</th>"
                + "<th>Артикул</th><th>Теги</th><th>Цена</th></tr>");
        for (Product p : products) {
            out.println("<tr><td>" + p.getId() + "</td><td><b>" + Html.escape(p.getName()) + "</b></td>"
                    + "<td><a href=\"" + ctx + "/category?id=" + p.getCategory().getId() + "\"><span class=\"tag\">"
                    + Html.escape(p.getCategory().getName()) + "</span></a></td>"
                    + "<td class=\"mono\">" + (p.getDetails() == null ? "—" : Html.escape(Page.nvl(p.getDetails().getSku()))) + "</td>"
                    + "<td>" + CategoryServlet.tagChips(p) + "</td>"
                    + "<td>" + Page.money(p.getPrice() == null ? 0 : p.getPrice()) + "</td></tr>");
        }
        if (products.isEmpty()) {
            out.println("<tr><td colspan=\"6\" class=\"muted\">Ничего не найдено.</td></tr>");
        }
        out.println("</table></section>");
        Page.end(out);
    }
}
