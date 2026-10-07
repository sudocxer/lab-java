package kz.edu.web;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** DAO заказов: все четыре операции CRUD через PreparedStatement. */
public class OrderDao {

    private static final String COLUMNS = "id, customer, item, quantity, amount, status";

    private final DataSource dataSource;

    public OrderDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // Read
    public List<Order> findAll() throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM orders ORDER BY id";
        List<Order> orders = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                orders.add(map(rs));
            }
        }
        return orders;
    }

    // Read (один)
    public Order findById(long id) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM orders WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    // Create
    public void insert(Order order) throws SQLException {
        String sql = "INSERT INTO orders (customer, item, quantity, amount, status) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            bind(ps, order);
            ps.executeUpdate();
        }
    }

    // Update
    public boolean update(Order order) throws SQLException {
        String sql = "UPDATE orders SET customer = ?, item = ?, quantity = ?, amount = ?, status = ? WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            bind(ps, order);
            ps.setLong(6, order.getId());
            return ps.executeUpdate() > 0;
        }
    }

    // Delete
    public boolean delete(long id) throws SQLException {
        String sql = "DELETE FROM orders WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private static void bind(PreparedStatement ps, Order order) throws SQLException {
        ps.setString(1, order.getCustomer());
        ps.setString(2, order.getItem());
        ps.setInt(3, order.getQuantity());
        ps.setInt(4, order.getAmount());
        ps.setString(5, order.getStatus());
    }

    private static Order map(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getLong("id"));
        order.setCustomer(rs.getString("customer"));
        order.setItem(rs.getString("item"));
        order.setQuantity(rs.getInt("quantity"));
        order.setAmount(rs.getInt("amount"));
        order.setStatus(rs.getString("status"));
        return order;
    }
}
