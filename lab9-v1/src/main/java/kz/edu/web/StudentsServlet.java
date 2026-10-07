package kz.edu.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/** Студенты в виде карточек с поиском по имени и фильтрами по факультету и курсу (JPQL по связям). */
@WebServlet("/students")
public class StudentsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UniversityRepository repo = (UniversityRepository) getServletContext().getAttribute(AppListener.REPOSITORY);
        String ctx = request.getContextPath();

        String name = Page.trim(request.getParameter("name"));
        Long facultyId = Page.parseLong(request.getParameter("facultyId"));
        Long courseId = Page.parseLong(request.getParameter("courseId"));
        boolean filtered = !name.isEmpty() || facultyId != null || courseId != null;

        List<Student> students = repo.students(name.isEmpty() ? null : name, facultyId, courseId);
        List<Faculty> faculties = repo.faculties();
        List<Course> courses = repo.courses();

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        Page.begin(out, request, "Студенты", "students");
        Page.title(out, "Студенты", "Найдено: " + students.size() + (filtered ? " (по фильтру)" : ""));

        out.println("<form class=\"filter\" method=\"get\" action=\"" + ctx + "/students\">"
                + "<input name=\"name\" placeholder=\"🔍 Поиск по имени\" value=\"" + Html.escape(name) + "\">"
                + "<select name=\"facultyId\"><option value=\"\">Все факультеты</option>");
        for (Faculty f : faculties) {
            out.println("<option value=\"" + f.getId() + "\"" + (f.getId().equals(facultyId) ? " selected" : "") + ">"
                    + Html.escape(f.getName()) + "</option>");
        }
        out.println("</select><select name=\"courseId\"><option value=\"\">Все курсы</option>");
        for (Course c : courses) {
            out.println("<option value=\"" + c.getId() + "\"" + (c.getId().equals(courseId) ? " selected" : "") + ">"
                    + Html.escape(c.getTitle()) + "</option>");
        }
        out.println("</select><button type=\"submit\">Найти</button>"
                + (filtered ? "<a class=\"btn ghost\" href=\"" + ctx + "/students\">Сбросить</a>" : "") + "</form>");

        out.println("<section class=\"cards\">");
        for (Student s : students) {
            out.println("<article class=\"scard\"><div class=\"scard-top\"><span class=\"ava big\">" + Page.initials(s.getFullName()) + "</span>"
                    + "<div><h3><a href=\"" + ctx + "/student?id=" + s.getId() + "\">" + Html.escape(s.getFullName()) + "</a></h3>"
                    + "<p class=\"dim\">" + Html.escape(s.getFaculty().getName()) + "</p></div></div>"
                    + "<dl><dt>Группа</dt><dd>" + Html.escape(Page.nvl(s.getGroupName())) + "</dd>"
                    + "<dt>Билет</dt><dd class=\"mono\">" + (s.getCard() == null ? "—" : Html.escape(Page.nvl(s.getCard().getNumber()))
                    + (s.getCard().getEnrollmentYear() == null ? "" : " · " + s.getCard().getEnrollmentYear())) + "</dd>"
                    + "<dt>Балл</dt><dd>" + Page.scoreBar(s.getScore()) + "</dd></dl>"
                    + "<div class=\"chips\">" + FacultiesServlet.courseChips(s) + "</div>"
                    + "<div class=\"act\"><a class=\"btn sm\" href=\"" + ctx + "/student?id=" + s.getId() + "\">Изменить</a>"
                    + FacultiesServlet.deleteForm(ctx, "student-delete", s.getId(), "Удалить студента?", "/students") + "</div></article>");
        }
        out.println("</section>");
        if (students.isEmpty()) {
            out.println("<p class=\"empty\">Ничего не найдено.</p>");
        }
        Page.end(out);
    }
}
