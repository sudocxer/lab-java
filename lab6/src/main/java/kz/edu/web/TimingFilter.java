package kz.edu.web;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalTime;
import java.util.Locale;

/**
 * Индивидуальное задание, вариант 4: фильтр измерения времени выполнения Servlet.
 * Засекает время до chain.doFilter() и после него; разница — время обработки запроса
 * следующими звеньями цепочки (Servlet и фильтры, стоящие после этого).
 * Результат пишется в консоль и в {@link RequestStats} для показа на панели.
 */
@WebFilter(filterName = "TimingFilter", urlPatterns = "/*")
public class TimingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        long start = System.nanoTime();
        try {
            chain.doFilter(request, response);
        } finally {
            double millis = (System.nanoTime() - start) / 1_000_000.0;
            String uri = req.getRequestURI();
            System.out.printf(Locale.ROOT, "[Timing] %s %s -> %d, %.2f ms%n",
                    req.getMethod(), uri, resp.getStatus(), millis);

            // статические файлы в статистику не включаем — они только зашумляют панель
            if (!uri.endsWith(".css")) {
                RequestStats.add(new RequestStats.Entry(
                        LocalTime.now().withNano(0), req.getMethod(), uri, resp.getStatus(), millis));
            }
        }
    }
}
