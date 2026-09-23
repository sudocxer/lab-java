package kz.edu.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Controller базового примера "Учебные курсы" (MVC).
 */
@WebServlet("/courses")
public class CourseServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        List<Course> courses = List.of(
                new Course(1L, "Java EE", 60),
                new Course(2L, "Базы данных", 45),
                new Course(3L, "Web-разработка", 75)
        );

        request.setAttribute("courses", courses);

        request.getRequestDispatcher("/courses.jsp")
                .forward(request, response);
    }
}
