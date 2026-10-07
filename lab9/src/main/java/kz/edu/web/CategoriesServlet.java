package kz.edu.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/** Список категорий (Category 1 — * Product) со статистикой и форма добавления категории. */
@WebServlet("/categories")
public class CategoriesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        CatalogRepository repo = (CatalogRepository) getServletContext().getAttribute(AppListener.REPOSITORY);
        String ctx = request.getContextPath();

        List<Category> categories = repo.categories();
        long products = categories.stream().mapToLong(c -> c.getProducts().size()).sum();
        long value = categories.stream().flatMap(c -> c.getProducts().stream())
                .mapToLong(p -> p.getPrice() == null ? 0 : p.getPrice()).sum();

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        Page.begin(out, request, "Категории товаров", "Связь один-ко-многим: категория содержит много товаров.", "categories");

        out.println("<section class=\"cards\">"
                + Page.card("Категорий", String.valueOf(categories.size()), "blue")
                + Page.card("Товаров", String.valueOf(products), "green")
                + Page.card("Стоимость каталога", Page.money(value), "orange")
                + "</section>");

        out.println("<section class=\"grid\">");
        for (Category c : categories) {
            long sum = c.getProducts().stream().mapToLong(p -> p.getPrice() == null ? 0 : p.getPrice()).sum();
            out.println("<article class=\"cat-card\"><div class=\"cat-head\"><h3>"
                    + "<a href=\"" + ctx + "/category?id=" + c.getId() + "\">" + Html.escape(c.getName()) + "</a></h3>"
                    + "<span class=\"tag\">" + c.getProducts().size() + " шт.</span></div>"
                    + "<p class=\"muted\">" + Html.escape(Page.nvl(c.getDescription())) + "</p>"
                    + "<p class=\"sum\">" + Page.money(sum) + "</p>"
                    + "<div class=\"actions\"><a class=\"btn small\" href=\"" + ctx + "/category?id=" + c.getId() + "\">Открыть</a>"
                    + "<form method=\"post\" action=\"" + ctx + "/actions/category-delete\" "
                    + "onsubmit=\"return confirm('Удалить категорию вместе со всеми товарами?')\">"
                    + "<input type=\"hidden\" name=\"id\" value=\"" + c.getId() + "\">"
                    + "<button class=\"small danger\" type=\"submit\">Удалить</button></form></div></article>");
        }
        if (categories.isEmpty()) {
            out.println("<p class=\"muted\">Категорий нет — добавьте первую.</p>");
        }
        out.println("</section>");

        out.println("<section class=\"panel\" id=\"form\"><h2>Новая категория</h2>"
                + "<form class=\"emp-form\" method=\"post\" action=\"" + ctx + "/actions/category-add\">"
                + "<div class=\"field\"><label for=\"name\">Название</label><input id=\"name\" name=\"name\" required maxlength=\"80\"></div>"
                + "<div class=\"field\"><label for=\"description\">Описание</label><input id=\"description\" name=\"description\" maxlength=\"255\"></div>"
                + "<div class=\"form-actions\"><button type=\"submit\">Добавить</button></div></form></section>");
        Page.end(out);
    }
}
