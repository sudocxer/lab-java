package kz.edu.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Главная: университет, статистика и факультеты в виде раскрывающихся блоков
 * со студентами внутри (Faculty 1 — * Student).
 */
@WebServlet("/faculties")
public class FacultiesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UniversityRepository repo = (UniversityRepository) getServletContext().getAttribute(AppListener.REPOSITORY);
        String ctx = request.getContextPath();

        University university = repo.university();
        List<Faculty> faculties = repo.faculties();
        long students = faculties.stream().mapToLong(f -> f.getStudents().size()).sum();
        double avg = faculties.stream().flatMap(f -> f.getStudents().stream())
                .filter(s -> s.getScore() != null).mapToInt(Student::getScore).average().orElse(0);

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        Page.begin(out, request, "Факультеты", "faculties");

        out.println("<section class=\"hero\"><div><span class=\"eyebrow\">"
                + Html.escape(university == null ? "" : Page.nvl(university.getCity())) + "</span>"
                + "<h1>" + Html.escape(university == null ? "Университет" : university.getName()) + "</h1>"
                + "<p>Иерархия University → Faculty → Student: факультет объединяет студентов, "
                + "студенты записываются на курсы.</p></div><div class=\"stats\">"
                + Page.stat("факультетов", String.valueOf(faculties.size()))
                + Page.stat("студентов", String.valueOf(students))
                + Page.stat("средний балл", String.format(java.util.Locale.ROOT, "%.1f", avg))
                + "</div></section>");

        for (Faculty f : faculties) {
            out.println("<details class=\"fac\" open><summary><span class=\"fac-name\">" + Html.escape(f.getName()) + "</span>"
                    + "<span class=\"fac-dean\">декан: " + Html.escape(Page.nvl(f.getDean())) + "</span>"
                    + "<span class=\"badge\">" + f.getStudents().size() + " студ.</span></summary><div class=\"fac-body\">");
            if (f.getStudents().isEmpty()) {
                out.println("<p class=\"dim\">На факультете пока нет студентов.</p>");
            } else {
                out.println("<table class=\"list\"><tr><th>Студент</th><th>Группа</th><th>Билет</th><th>Балл</th><th>Курсы</th><th></th></tr>");
                for (Student s : f.getStudents()) {
                    out.println("<tr><td><span class=\"ava\">" + Page.initials(s.getFullName()) + "</span>"
                            + "<a href=\"" + ctx + "/student?id=" + s.getId() + "\">" + Html.escape(s.getFullName()) + "</a></td>"
                            + "<td>" + Html.escape(Page.nvl(s.getGroupName())) + "</td>"
                            + "<td class=\"mono\">" + (s.getCard() == null ? "—" : Html.escape(Page.nvl(s.getCard().getNumber()))) + "</td>"
                            + "<td>" + Page.scoreBar(s.getScore()) + "</td>"
                            + "<td>" + courseChips(s) + "</td>"
                            + "<td class=\"act\"><a class=\"btn sm\" href=\"" + ctx + "/student?id=" + s.getId() + "\">Изменить</a>"
                            + deleteForm(ctx, "student-delete", s.getId(), "Удалить студента?", "/faculties") + "</td></tr>");
                }
                out.println("</table>");
            }
            out.println("<div class=\"fac-foot\"><a class=\"btn\" href=\"" + ctx + "/student?faculty=" + f.getId() + "\">+ Добавить студента</a>"
                    + deleteForm(ctx, "faculty-delete", f.getId(), "Удалить факультет вместе со всеми студентами?", "/faculties")
                    .replace("btn sm danger", "btn danger") + "</div></div></details>");
        }
        if (faculties.isEmpty()) {
            out.println("<p class=\"dim\">Факультетов нет — добавьте первый.</p>");
        }

        out.println("<section class=\"box\"><h2>Новый факультет</h2><form class=\"row-form\" method=\"post\" action=\""
                + ctx + "/actions/faculty-add\"><label>Название<input name=\"name\" required maxlength=\"100\"></label>"
                + "<label>Декан<input name=\"dean\" maxlength=\"100\"></label>"
                + "<button type=\"submit\">Добавить</button></form></section>");
        Page.end(out);
    }

    static String courseChips(Student s) {
        if (s.getCourses().isEmpty()) {
            return "<span class=\"dim\">—</span>";
        }
        return s.getCourses().stream()
                .sorted((a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle()))
                .map(c -> "<span class=\"pill\">" + Html.escape(c.getTitle()) + "</span>")
                .collect(Collectors.joining(" "));
    }

    static String deleteForm(String ctx, String action, Long id, String question, String back) {
        return "<form method=\"post\" action=\"" + ctx + "/actions/" + action + "\" onsubmit=\"return confirm('" + question + "')\">"
                + "<input type=\"hidden\" name=\"id\" value=\"" + id + "\">"
                + "<input type=\"hidden\" name=\"back\" value=\"" + back + "\">"
                + "<button class=\"btn sm danger\" type=\"submit\">Удалить</button></form>";
    }
}
