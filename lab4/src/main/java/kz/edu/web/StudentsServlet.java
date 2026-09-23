package kz.edu.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Controller индивидуального задания, вариант 4: список студентов (MVC).
 */
@WebServlet("/students")
public class StudentsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        List<Student> students = List.of(
                new Student(1L, "Алия Садыкова", "ИС-24", 95),
                new Student(2L, "Ерлан Ахметов", "ИС-24", 68),
                new Student(3L, "Диана Нурланова", "ИС-23", 91),
                new Student(4L, "Тимур Бекенов", "ИС-23", 74)
        );

        request.setAttribute("students", students);

        request.getRequestDispatcher("/students.jsp")
                .forward(request, response);
    }
}
