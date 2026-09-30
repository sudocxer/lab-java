package kz.edu.web;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import javax.sql.DataSource;
import java.sql.SQLException;

/** При старте приложения создаёт DataSource, схему БД и кладёт OrderDao в контекст. */
@WebListener
public class AppListener implements ServletContextListener {

    static final String DAO = "orderDao";

    @Override
    public void contextInitialized(ServletContextEvent event) {
        DataSource dataSource = Database.createDataSource();
        try {
            Database.init(dataSource);
        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось инициализировать базу данных", e);
        }
        event.getServletContext().setAttribute(DAO, new OrderDao(dataSource));
    }
}
