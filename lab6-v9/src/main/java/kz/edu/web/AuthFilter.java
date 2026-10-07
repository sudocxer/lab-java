package kz.edu.web;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/** Пропускает в /admin/* только пользователей с атрибутом username в Session. */
@WebFilter(filterName = "AuthFilter", urlPatterns = "/admin/*")
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);

        if (session == null || session.getAttribute("username") == null) {
            // запоминаем, куда шёл пользователь, чтобы вернуть его туда после входа
            String target = req.getRequestURI().substring(req.getContextPath().length());
            if (req.getQueryString() != null) {
                target += "?" + req.getQueryString();
            }
            req.getSession().setAttribute("returnTo", target);
            resp.sendRedirect(req.getContextPath() + "/login.html?denied=1");
            return;
        }

        chain.doFilter(request, response);
    }
}
