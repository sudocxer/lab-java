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
 * Форма студента: /student?id=ID — редактирование, /student?faculty=ID (или без параметров) — новый студент.
 * Здесь задаются все связи: факультет (ManyToOne), студенческий билет (OneToOne), курсы (ManyToMany).
 */
@WebServlet("/student")
public class StudentServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UniversityRepository repo = (UniversityRepository) getServletContext().getAttribute(AppListener.REPOSITORY);
        String ctx = request.getContextPath();

        Long id = Page.parseLong(request.getParameter("id"));
        Student student = id == null ? null : repo.student(id);
        if (id != null && student == null) {
            response.sendRedirect(ctx + "/students?msg=notfound");
            return;
        }
        boolean isEdit = student != null;
        Long preselected = isEdit ? student.getFaculty().getId() : Page.parseLong(request.getParameter("faculty"));

        List<Faculty> faculties = repo.faculties();
        List<Course> courses = repo.courses();
        StudentCard card = isEdit ? student.getCard() : null;
        Set<Long> selected = isEdit
                ? student.getCourses().stream().map(Course::getId).collect(Collectors.toSet()) : Set.of();

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        Page.begin(out, request, isEdit ? "Студент" : "Новый студент", "students");
        Page.title(out, isEdit ? Html.escape(student.getFullName()) : "Новый студент",
                isEdit ? "Редактирование записи #" + student.getId() : "Заполните данные и выберите курсы");

        out.println("<form class=\"split\" method=\"post\" action=\"" + ctx + "/actions/student-save\">"
                + (isEdit ? "<input type=\"hidden\" name=\"id\" value=\"" + student.getId() + "\">" : ""));

        out.println("<section class=\"box\"><h2>Основное</h2>"
                + "<label>Факультет <small>(ManyToOne)</small><select name=\"facultyId\">");
        for (Faculty f : faculties) {
            out.println("<option value=\"" + f.getId() + "\"" + (f.getId().equals(preselected) ? " selected" : "") + ">"
                    + Html.escape(f.getName()) + "</option>");
        }
        out.println("</select></label>"
                + input("fullName", "ФИО", isEdit ? student.getFullName() : "", true, "text")
                + input("groupName", "Группа", isEdit ? Page.nvl(student.getGroupName()) : "", false, "text")
                + input("score", "Балл (0–100)", isEdit && student.getScore() != null ? String.valueOf(student.getScore()) : "", false, "number")
                + "</section>");

        out.println("<section class=\"box\"><h2>Студенческий билет <small>(OneToOne)</small></h2>"
                + input("cardNumber", "Номер билета", card == null ? "" : Page.nvl(card.getNumber()), false, "text")
                + input("cardYear", "Год поступления", card == null || card.getEnrollmentYear() == null ? "" : String.valueOf(card.getEnrollmentYear()), false, "number")
                + "<h2 class=\"gap\">Курсы <small>(ManyToMany)</small></h2><div class=\"pick\">");
        for (Course c : courses) {
            out.println("<label class=\"opt\"><input type=\"checkbox\" name=\"courseId\" value=\"" + c.getId() + "\""
                    + (selected.contains(c.getId()) ? " checked" : "") + "><span>" + Html.escape(c.getTitle())
                    + " <em>" + (c.getCredits() == null ? 0 : c.getCredits()) + " кр.</em></span></label>");
        }
        if (courses.isEmpty()) {
            out.println("<span class=\"dim\">Курсов нет — создайте их на вкладке «Курсы».</span>");
        }
        out.println("</div></section>");

        out.println("<div class=\"bar\"><button type=\"submit\">" + (isEdit ? "Сохранить" : "Добавить студента") + "</button>"
                + "<a class=\"btn ghost\" href=\"" + ctx + "/faculties\">Отмена</a></div></form>");
        Page.end(out);
    }

    private static String input(String name, String label, String value, boolean required, String type) {
        return "<label>" + label + "<input name=\"" + name + "\" type=\"" + type + "\" value=\"" + Html.escape(value) + "\""
                + ("number".equals(type) ? " min=\"0\"" : "") + (required ? " required" : "") + "></label>";
    }
}
