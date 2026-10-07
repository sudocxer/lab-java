package kz.edu.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/** Курсы (Student * — * Course): кто записан на каждый курс, добавление и удаление курса. */
@WebServlet("/courses")
public class CoursesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UniversityRepository repo = (UniversityRepository) getServletContext().getAttribute(AppListener.REPOSITORY);
        String ctx = request.getContextPath();

        List<Course> courses = repo.courses();
        Map<Long, List<Student>> enrolled = repo.courseStudents();

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        Page.begin(out, request, "Курсы", "courses");
        Page.title(out, "Курсы", "Связь многие-ко-многим: студент изучает несколько курсов, курс изучают многие студенты (таблица student_courses).");

        out.println("<section class=\"tiles\">");
        for (Course c : courses) {
            List<Student> list = enrolled.getOrDefault(c.getId(), List.of());
            out.println("<article class=\"tile\"><div class=\"tile-head\"><h3>" + Html.escape(c.getTitle()) + "</h3>"
                    + "<span class=\"badge\">" + (c.getCredits() == null ? 0 : c.getCredits()) + " кр.</span></div>"
                    + "<p class=\"dim\">Записано: <b>" + list.size() + "</b></p><div class=\"avas\">");
            for (Student s : list) {
                out.println("<a class=\"ava\" title=\"" + Html.escape(s.getFullName()) + "\" href=\"" + ctx + "/student?id="
                        + s.getId() + "\">" + Page.initials(s.getFullName()) + "</a>");
            }
            if (list.isEmpty()) {
                out.println("<span class=\"dim\">пока никого</span>");
            }
            out.println("</div><div class=\"act\"><a class=\"btn sm\" href=\"" + ctx + "/students?courseId=" + c.getId() + "\">Студенты</a>"
                    + FacultiesServlet.deleteForm(ctx, "course-delete", c.getId(), "Удалить курс?", "/courses") + "</div></article>");
        }
        out.println("</section>");
        if (courses.isEmpty()) {
            out.println("<p class=\"empty\">Курсов нет — добавьте первый.</p>");
        }

        out.println("<section class=\"box\"><h2>Новый курс</h2><form class=\"row-form\" method=\"post\" action=\"" + ctx + "/actions/course-add\">"
                + "<label>Название<input name=\"title\" required maxlength=\"100\"></label>"
                + "<label>Кредиты<input name=\"credits\" type=\"number\" min=\"0\" max=\"30\" value=\"4\" required></label>"
                + "<button type=\"submit\">Добавить</button></form></section>");
        Page.end(out);
    }
}
