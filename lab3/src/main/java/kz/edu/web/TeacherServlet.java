package kz.edu.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Индивидуальное задание, вариант 4: карточка преподавателя.
 */
@WebServlet("/teacher")
public class TeacherServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        Teacher teacher = new Teacher(
                "Марат Ахметов",
                "Кафедра информационных систем",
                "Старший преподаватель",
                12);

        request.setAttribute("teacher", teacher);

        request.getRequestDispatcher("/teacher.jsp")
                .forward(request, response);
    }
}
