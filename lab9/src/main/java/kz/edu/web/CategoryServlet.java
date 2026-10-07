package kz.edu.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Страница категории: её товары (OneToMany), теги товаров (ManyToMany), детали (OneToOne)
 * и форма добавления/редактирования товара. /category?id=ID[&edit=PRODUCT_ID]
 */
@WebServlet("/category")
public class CategoryServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        CatalogRepository repo = (CatalogRepository) getServletContext().getAttribute(AppListener.REPOSITORY);
        String ctx = request.getContextPath();

        Long id = Page.parseLong(request.getParameter("id"));
        Category category = id == null ? null : repo.category(id);
        if (category == null) {
            response.sendRedirect(ctx + "/categories?msg=notfound");
            return;
        }

        List<Category> allCategories = repo.categories();
        List<Tag> allTags = repo.tags();

        Product editing = null;
        Long editId = Page.parseLong(request.getParameter("edit"));
        if (editId != null) {
            editing = category.getProducts().stream().filter(p -> p.getId().equals(editId)).findFirst().orElse(null);
        }

        long value = category.getProducts().stream().mapToLong(p -> p.getPrice() == null ? 0 : p.getPrice()).sum();

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        Page.begin(out, request, Html.escape(category.getName()),
                Html.escape(Page.nvl(category.getDescription())), "categories");

        out.println("<section class=\"cards\">"
                + Page.card("Товаров в категории", String.valueOf(category.getProducts().size()), "blue")
                + Page.card("Стоимость", Page.money(value), "orange")
                + "</section>");

        out.println("<section class=\"panel\"><h2>Товары категории</h2><table><tr><th>ID</th><th>Название</th>"
                + "<th>Артикул</th><th>Теги</th><th>Цена</th><th></th></tr>");
        for (Product p : category.getProducts()) {
            out.println("<tr><td>" + p.getId() + "</td><td><b>" + Html.escape(p.getName()) + "</b>"
                    + (p.getDetails() == null ? "" : "<br><span class=\"muted\">" + Html.escape(Page.nvl(p.getDetails().getDescription())) + "</span>")
                    + "</td><td class=\"mono\">" + (p.getDetails() == null ? "—" : Html.escape(Page.nvl(p.getDetails().getSku()))) + "</td>"
                    + "<td>" + tagChips(p) + "</td>"
                    + "<td>" + Page.money(p.getPrice() == null ? 0 : p.getPrice()) + "</td>"
                    + "<td class=\"actions\"><a class=\"btn small\" href=\"" + ctx + "/category?id=" + category.getId()
                    + "&edit=" + p.getId() + "#form\">Изменить</a>"
                    + "<form method=\"post\" action=\"" + ctx + "/actions/product-delete\" onsubmit=\"return confirm('Удалить товар?')\">"
                    + "<input type=\"hidden\" name=\"id\" value=\"" + p.getId() + "\">"
                    + "<input type=\"hidden\" name=\"back\" value=\"/category?id=" + category.getId() + "\">"
                    + "<button class=\"small danger\" type=\"submit\">Удалить</button></form></td></tr>");
        }
        if (category.getProducts().isEmpty()) {
            out.println("<tr><td colspan=\"6\" class=\"muted\">В категории пока нет товаров.</td></tr>");
        }
        out.println("</table></section>");

        boolean isEdit = editing != null;
        ProductDetails d = isEdit ? editing.getDetails() : null;
        Set<Long> selected = isEdit
                ? editing.getTags().stream().map(Tag::getId).collect(Collectors.toSet()) : Set.of();

        out.println("<section class=\"panel\" id=\"form\"><h2>" + (isEdit ? "Редактирование товара #" + editing.getId() : "Новый товар") + "</h2>"
                + "<form class=\"emp-form\" method=\"post\" action=\"" + ctx + "/actions/product-save\">"
                + "<input type=\"hidden\" name=\"back\" value=\"/category?id=" + category.getId() + "\">"
                + (isEdit ? "<input type=\"hidden\" name=\"id\" value=\"" + editing.getId() + "\">" : ""));

        out.println("<div class=\"field\"><label for=\"categoryId\">Категория</label><select id=\"categoryId\" name=\"categoryId\">");
        for (Category c : allCategories) {
            out.println("<option value=\"" + c.getId() + "\"" + (c.getId().equals(category.getId()) ? " selected" : "") + ">"
                    + Html.escape(c.getName()) + "</option>");
        }
        out.println("</select></div>");
        out.println(field("name", "Название", isEdit ? editing.getName() : "", true, "text")
                + field("price", "Цена, тг", isEdit && editing.getPrice() != null ? String.valueOf(editing.getPrice()) : "", true, "number")
                + field("sku", "Артикул (OneToOne)", d == null ? "" : Page.nvl(d.getSku()), false, "text")
                + field("weight", "Вес, г", d == null || d.getWeightGrams() == null ? "" : String.valueOf(d.getWeightGrams()), false, "number")
                + field("description", "Описание", d == null ? "" : Page.nvl(d.getDescription()), false, "text"));

        out.println("<div class=\"field wide\"><label>Теги (ManyToMany)</label><div class=\"checks\">");
        for (Tag t : allTags) {
            out.println("<label class=\"check\"><input type=\"checkbox\" name=\"tagId\" value=\"" + t.getId() + "\""
                    + (selected.contains(t.getId()) ? " checked" : "") + "><span>" + Html.escape(t.getName()) + "</span></label>");
        }
        if (allTags.isEmpty()) {
            out.println("<span class=\"muted\">Тегов нет — создайте их на вкладке «Теги».</span>");
        }
        out.println("</div></div>");

        out.println("<div class=\"form-actions\"><button type=\"submit\">" + (isEdit ? "Сохранить" : "Добавить") + "</button>"
                + (isEdit ? "<a class=\"btn ghost\" href=\"" + ctx + "/category?id=" + category.getId() + "\">Отмена</a>" : "")
                + "</div></form></section>");
        Page.end(out);
    }

    static String tagChips(Product p) {
        if (p.getTags().isEmpty()) {
            return "<span class=\"muted\">—</span>";
        }
        return p.getTags().stream()
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .map(t -> "<span class=\"chip\">" + Html.escape(t.getName()) + "</span>")
                .collect(Collectors.joining(" "));
    }

    private static String field(String name, String label, String value, boolean required, String type) {
        return "<div class=\"field\"><label for=\"" + name + "\">" + label + "</label>"
                + "<input id=\"" + name + "\" name=\"" + name + "\" type=\"" + type + "\" value=\"" + Html.escape(value) + "\""
                + ("number".equals(type) ? " min=\"0\"" : "") + (required ? " required" : "") + "></div>";
    }
}
