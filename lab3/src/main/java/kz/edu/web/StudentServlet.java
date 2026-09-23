package kz.edu.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/student")
public class StudentServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        Student student = new Student("Алия Садыкова", "ИС-24", 95);

        request.setAttribute("student", student);

        request.getRequestDispatcher("/student.jsp")
                .forward(request, response);
    }
}
