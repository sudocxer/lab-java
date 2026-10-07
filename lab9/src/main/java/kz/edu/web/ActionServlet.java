package kz.edu.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Все изменяющие операции (только POST), затем redirect с сообщением (Post/Redirect/Get):
 * category-add, category-delete, product-save, product-delete, tag-add, tag-delete.
 */
@WebServlet("/actions/*")
public class ActionServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        CatalogRepository repo = (CatalogRepository) getServletContext().getAttribute(AppListener.REPOSITORY);
        String action = request.getPathInfo() == null ? "" : request.getPathInfo().substring(1);
        String ctx = request.getContextPath();

        String target;
        String msg;
        Long id = Page.parseLong(request.getParameter("id"));

        switch (action) {
            case "category-add" -> {
                String name = Page.trim(request.getParameter("name"));
                if (name.isEmpty() || name.length() > 80) {
                    msg = "invalid";
                } else {
                    msg = repo.addCategory(name, limit(Page.trim(request.getParameter("description")), 255))
                            ? "category-added" : "category-exists";
                }
                target = "/categories";
            }
            case "category-delete" -> {
                msg = id != null && repo.deleteCategory(id) ? "category-deleted" : "notfound";
                target = "/categories";
            }
            case "product-save" -> {
                Long categoryId = Page.parseLong(request.getParameter("categoryId"));
                String name = Page.trim(request.getParameter("name"));
                int price = parseInt(request.getParameter("price"), -1);
                Integer weight = parseInt(request.getParameter("weight"), -1) >= 0
                        ? parseInt(request.getParameter("weight"), -1) : null;
                if (categoryId == null || name.isEmpty() || name.length() > 100 || price < 0) {
                    msg = "invalid";
                    target = back(request, "/categories", ctx);
                } else {
                    Set<Long> tagIds = new LinkedHashSet<>();
                    String[] tagParams = request.getParameterValues("tagId");
                    if (tagParams != null) {
                        for (String t : tagParams) {
                            Long tagId = Page.parseLong(t);
                            if (tagId != null) {
                                tagIds.add(tagId);
                            }
                        }
                    }
                    CatalogRepository.ProductInput input = new CatalogRepository.ProductInput(categoryId, name, price,
                            limit(Page.trim(request.getParameter("sku")), 40),
                            limit(Page.trim(request.getParameter("description")), 500), weight, tagIds);
                    Product saved = repo.saveProduct(id, input);
                    msg = saved == null ? "notfound" : (id == null ? "product-added" : "product-updated");
                    // после сохранения показываем категорию, в которой теперь лежит товар
                    target = "/category?id=" + categoryId;
                }
            }
            case "product-delete" -> {
                msg = id != null && repo.deleteProduct(id) ? "product-deleted" : "notfound";
                target = back(request, "/categories", ctx);
            }
            case "tag-add" -> {
                String name = Page.trim(request.getParameter("name"));
                if (name.isEmpty() || name.length() > 40) {
                    msg = "invalid";
                } else {
                    msg = repo.addTag(name) ? "tag-added" : "tag-exists";
                }
                target = "/tags";
            }
            case "tag-delete" -> {
                msg = id != null && repo.deleteTag(id) ? "tag-deleted" : "notfound";
                target = "/tags";
            }
            default -> {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }
        response.sendRedirect(ctx + target + (target.contains("?") ? "&" : "?") + "msg=" + msg);
    }

    /** Относительный адрес возврата из формы; только путь внутри приложения. */
    private static String back(HttpServletRequest request, String fallback, String ctx) {
        String back = request.getParameter("back");
        if (back != null && back.startsWith("/") && !back.startsWith("//") && !back.contains("\n") && !back.contains("\r")) {
            return back;
        }
        return fallback;
    }

    private static int parseInt(String s, int fallback) {
        try {
            return Integer.parseInt(Page.trim(s));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static String limit(String s, int max) {
        return s.length() > max ? s.substring(0, max) : s;
    }
}
