package kz.edu.web;

import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Демонстрация CDI: что именно внедряет контейнер и как живут бины разных областей.
 * Обновите страницу: ApplicationScoped и SessionScoped сохраняют экземпляр, RequestScoped создаётся заново.
 */
@WebServlet("/scopes")
public class ScopesServlet extends HttpServlet {

    @Inject
    private EmployeeService service;

    @Inject
    private ApplicationInfo application;

    @Inject
    private SessionInfo session;

    @Inject
    private RequestInfo requestInfo;

    /** Две точки внедрения @Dependent-бина — два разных экземпляра. */
    @Inject
    private DependentInfo firstDependent;

    @Inject
    private DependentInfo secondDependent;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int applicationViews = application.hit();
        int sessionViews = session.hit();

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        Page.begin(out, request, "CDI: внедрение и scopes",
                "Что внедрил контейнер и как долго живёт каждый бин.", "scopes");

        out.println("<section class=\"panel\"><h2>Цепочка зависимостей</h2>"
                + "<table><tr><th>Слой</th><th>Класс</th><th>Что получено через @Inject</th></tr>"
                + "<tr><td>Controller</td><td>ScopesServlet</td><td class=\"muted\">создаёт контейнер сервлетов</td></tr>"
                + "<tr><td>Service</td><td>EmployeeService</td><td><code>" + service.getClass().getName() + "</code></td></tr>"
                + "<tr><td>Repository</td><td>EmployeeRepository</td><td><code>" + service.repositoryClass() + "</code></td></tr>"
                + "</table>"
                + "<p class=\"query\">Для бинов с областью (@ApplicationScoped и др.) контейнер внедряет не сам объект, "
                + "а <b>client proxy</b> (класс с суффиксом <code>WeldClientProxy</code>) и при каждом вызове "
                + "находит настоящий экземпляр нужной области.</p></section>");

        out.println("<section class=\"panel\"><h2>CDI Scopes</h2>"
                + "<table><tr><th>Область</th><th>Класс</th><th>Экземпляр</th><th>Создан</th><th>Просмотров</th></tr>"
                + row("@ApplicationScoped", "ApplicationInfo", application, String.valueOf(applicationViews))
                + row("@SessionScoped", "SessionInfo", session, String.valueOf(sessionViews))
                + row("@RequestScoped", "RequestInfo", requestInfo, "—")
                + row("@Dependent (точка 1)", "DependentInfo", firstDependent, "—")
                + row("@Dependent (точка 2)", "DependentInfo", secondDependent, "—")
                + "</table>"
                + "<p class=\"query\">Обновите страницу: <b>Application</b> и <b>Session</b> сохраняют экземпляр "
                + "(растут счётчики), <b>Request</b> каждый раз новый. Откройте страницу в другом браузере или в режиме "
                + "инкогнито: счётчик сессии начнётся заново, счётчик приложения — общий. "
                + "<b>Dependent</b> живёт столько же, сколько сервлет, в который он внедрён, "
                + "и у каждой точки внедрения свой экземпляр.</p></section>");
        Page.end(out);
    }

    private static String row(String scope, String type, TrackedBean bean, String views) {
        return "<tr><td><span class=\"tag\">" + scope + "</span></td><td>" + type + "</td>"
                + "<td><code>" + bean.instanceId() + "</code></td>"
                + "<td>" + bean.created() + "</td><td>" + views + "</td></tr>";
    }
}
