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
 * faculty-add, faculty-delete, student-save, student-delete, course-add, course-delete.
 */
@WebServlet("/actions/*")
public class ActionServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        UniversityRepository repo = (UniversityRepository) getServletContext().getAttribute(AppListener.REPOSITORY);
        String action = request.getPathInfo() == null ? "" : request.getPathInfo().substring(1);
        String ctx = request.getContextPath();
        Long id = Page.parseLong(request.getParameter("id"));

        String target;
        String msg;
        switch (action) {
            case "faculty-add" -> {
                String name = Page.trim(request.getParameter("name"));
                if (name.isEmpty() || name.length() > 100) {
                    msg = "invalid";
                } else {
                    msg = repo.addFaculty(name, limit(Page.trim(request.getParameter("dean")), 100))
                            ? "faculty-added" : "faculty-exists";
                }
                target = "/faculties";
            }
            case "faculty-delete" -> {
                msg = id != null && repo.deleteFaculty(id) ? "faculty-deleted" : "notfound";
                target = "/faculties";
            }
            case "student-save" -> {
                Long facultyId = Page.parseLong(request.getParameter("facultyId"));
                String fullName = Page.trim(request.getParameter("fullName"));
                String scoreParam = Page.trim(request.getParameter("score"));
                Integer score = parseInt(scoreParam);
                boolean scoreOk = scoreParam.isEmpty() || (score != null && score >= 0 && score <= 100);
                if (facultyId == null || fullName.isEmpty() || fullName.length() > 100 || !scoreOk) {
                    msg = "invalid";
                    target = id == null ? "/students" : "/student?id=" + id;
                } else {
                    Set<Long> courseIds = new LinkedHashSet<>();
                    String[] params = request.getParameterValues("courseId");
                    if (params != null) {
                        for (String p : params) {
                            Long courseId = Page.parseLong(p);
                            if (courseId != null) {
                                courseIds.add(courseId);
                            }
                        }
                    }
                    UniversityRepository.StudentInput input = new UniversityRepository.StudentInput(facultyId, fullName,
                            limit(Page.trim(request.getParameter("groupName")), 30), score,
                            limit(Page.trim(request.getParameter("cardNumber")), 30),
                            parseInt(request.getParameter("cardYear")), courseIds);
                    Student saved = repo.saveStudent(id, input);
                    msg = saved == null ? "notfound" : (id == null ? "student-added" : "student-updated");
                    target = "/students";
                }
            }
            case "student-delete" -> {
                msg = id != null && repo.deleteStudent(id) ? "student-deleted" : "notfound";
                target = back(request, "/students");
            }
            case "course-add" -> {
                String title = Page.trim(request.getParameter("title"));
                Integer credits = parseInt(request.getParameter("credits"));
                if (title.isEmpty() || title.length() > 100 || credits == null || credits < 0) {
                    msg = "invalid";
                } else {
                    msg = repo.addCourse(title, credits) ? "course-added" : "course-exists";
                }
                target = "/courses";
            }
            case "course-delete" -> {
                msg = id != null && repo.deleteCourse(id) ? "course-deleted" : "notfound";
                target = "/courses";
            }
            default -> {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }
        response.sendRedirect(ctx + target + (target.contains("?") ? "&" : "?") + "msg=" + msg);
    }

    /** Адрес возврата из формы: только путь внутри приложения. */
    private static String back(HttpServletRequest request, String fallback) {
        String back = request.getParameter("back");
        if (back != null && back.startsWith("/") && !back.startsWith("//") && !back.contains("\n") && !back.contains("\r")) {
            return back;
        }
        return fallback;
    }

    private static Integer parseInt(String s) {
        try {
            String t = Page.trim(s);
            return t.isEmpty() ? null : Integer.valueOf(t);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String limit(String s, int max) {
        return s.length() > max ? s.substring(0, max) : s;
    }
}
