package kz.edu.web;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** DAO сотрудников: все четыре операции CRUD через PreparedStatement. */
public class EmployeeDao {

    private static final String COLUMNS = "id, full_name, position, department, salary";

    private final DataSource dataSource;

    public EmployeeDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // Read
    public List<Employee> findAll() throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM employees ORDER BY id";
        List<Employee> employees = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                employees.add(map(rs));
            }
        }
        return employees;
    }

    // Read (один)
    public Employee findById(long id) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM employees WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    // Create
    public void insert(Employee employee) throws SQLException {
        String sql = "INSERT INTO employees (full_name, position, department, salary) VALUES (?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, employee.getFullName());
            ps.setString(2, employee.getPosition());
            ps.setString(3, employee.getDepartment());
            ps.setInt(4, employee.getSalary());
            ps.executeUpdate();
        }
    }

    // Update
    public boolean update(Employee employee) throws SQLException {
        String sql = "UPDATE employees SET full_name = ?, position = ?, department = ?, salary = ? WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, employee.getFullName());
            ps.setString(2, employee.getPosition());
            ps.setString(3, employee.getDepartment());
            ps.setInt(4, employee.getSalary());
            ps.setLong(5, employee.getId());
            return ps.executeUpdate() > 0;
        }
    }

    // Delete
    public boolean delete(long id) throws SQLException {
        String sql = "DELETE FROM employees WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private static Employee map(ResultSet rs) throws SQLException {
        Employee employee = new Employee();
        employee.setId(rs.getLong("id"));
        employee.setFullName(rs.getString("full_name"));
        employee.setPosition(rs.getString("position"));
        employee.setDepartment(rs.getString("department"));
        employee.setSalary(rs.getInt("salary"));
        return employee;
    }
}
