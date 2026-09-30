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
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

/**
 * Индивидуальное задание, вариант 9: фильтр аудита действий пользователя.
 * Для каждого запроса фиксирует, кто (логин из Session), когда, откуда (IP),
 * что сделал (метод, URL, параметры) и с каким результатом (HTTP-статус).
 * Значения секретных параметров в журнал не попадают.
 */
@WebFilter(filterName = "AuditFilter", urlPatterns = "/*")
public class AuditFilter implements Filter {

    private static final Set<String> SECRET = Set.of("password");

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // логин определяем до выполнения: при выходе Session будет уничтожена
        String userBefore = currentUser(req);
        String params = describe(req.getParameterMap());

        try {
            chain.doFilter(request, response);
        } finally {
            String uri = req.getRequestURI();
            if (!uri.endsWith(".css")) {
                // при входе логин появляется только во время выполнения запроса
                String user = userBefore != null ? userBefore : currentUser(req);
                AuditLog.add(new AuditLog.Entry(LocalDateTime.now().withNano(0),
                        user == null ? "аноним" : user, req.getRemoteAddr(),
                        req.getMethod(), uri, params, resp.getStatus()));
                System.out.println("[Audit] " + (user == null ? "аноним" : user) + " "
                        + req.getMethod() + " " + uri + " -> " + resp.getStatus());
            }
        }
    }

    private static String currentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session == null ? null : (String) session.getAttribute("username");
    }

    private static String describe(Map<String, String[]> parameters) {
        StringJoiner joiner = new StringJoiner(", ");
        parameters.forEach((name, values) ->
                joiner.add(name + "=" + (SECRET.contains(name) ? "***" : String.join("|", values))));
        return joiner.toString();
    }
}
