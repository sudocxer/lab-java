package kz.edu.web;

import org.postgresql.ds.PGSimpleDataSource;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Создание DataSource и начальной схемы.
 * По умолчанию — файловая SQLite (data/lab7.db, путь можно изменить переменной DB_FILE):
 * файл создаётся при первом запуске, а при следующих запусках данные переиспользуются.
 * Если задана переменная окружения DB_URL (например, jdbc:postgresql://localhost:5432/university),
 * используется PostgreSQL; логин и пароль берутся из DB_USER и DB_PASSWORD.
 */
final class Database {

    private Database() {
    }

    static DataSource createDataSource() {
        String url = System.getenv("DB_URL");

        if (url != null && url.startsWith("jdbc:postgresql:")) {
            PGSimpleDataSource ds = new PGSimpleDataSource();
            ds.setUrl(url);
            ds.setUser(System.getenv("DB_USER"));
            ds.setPassword(System.getenv("DB_PASSWORD"));
            return ds;
        }

        Path file = Paths.get(System.getenv().getOrDefault("DB_FILE", "data/lab7.db")).toAbsolutePath();
        try {
            Files.createDirectories(file.getParent());
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось создать каталог для БД: " + file.getParent(), e);
        }
        SQLiteDataSource ds = new SQLiteDataSource();
        ds.setUrl("jdbc:sqlite:" + file);
        ds.setJournalMode("WAL");   // читатели не блокируют запись
        ds.setBusyTimeout(5000);    // ждать освобождения блокировки, а не падать сразу
        return ds;
    }

    /** Создаёт таблицу employees (если её нет) и, только если она пуста, добавляет тестовые данные. */
    static void init(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            boolean postgres = connection.getMetaData().getURL().startsWith("jdbc:postgresql:");
            String idType = postgres ? "BIGSERIAL PRIMARY KEY" : "INTEGER PRIMARY KEY AUTOINCREMENT";

            try (Statement st = connection.createStatement()) {
                st.executeUpdate("CREATE TABLE IF NOT EXISTS employees ("
                        + "id " + idType + ", "
                        + "full_name VARCHAR(100) NOT NULL, "
                        + "position VARCHAR(60), "
                        + "department VARCHAR(60), "
                        + "salary INTEGER)");
            }

            try (PreparedStatement count = connection.prepareStatement("SELECT COUNT(*) FROM employees");
                 ResultSet rs = count.executeQuery()) {
                rs.next();
                if (rs.getLong(1) > 0) {
                    return;
                }
            }

            String insert = "INSERT INTO employees (full_name, position, department, salary) VALUES (?, ?, ?, ?)";
            Object[][] seed = {
                    {"Иван Иванов", "Инженер-программист", "Разработка", 450_000},
                    {"Алия Садыкова", "Бухгалтер", "Финансы", 380_000},
                    {"Петр Петров", "Менеджер проектов", "Разработка", 520_000},
                    {"Динара Ержанова", "HR-специалист", "Персонал", 340_000},
            };
            try (PreparedStatement ps = connection.prepareStatement(insert)) {
                for (Object[] row : seed) {
                    ps.setString(1, (String) row[0]);
                    ps.setString(2, (String) row[1]);
                    ps.setString(3, (String) row[2]);
                    ps.setInt(4, (Integer) row[3]);
                    ps.executeUpdate();
                }
            }
        }
    }
}
