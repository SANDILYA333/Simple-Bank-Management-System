package com.credora.bank.dao;

import com.credora.bank.model.Customer;

import java.sql.*;
import java.time.LocalDateTime;

public class CustomerDAO {

    private final Connection conn;

    public CustomerDAO(Connection conn) {
        this.conn = conn;
    }

    public Customer create(Customer customer) throws SQLException {
        String sql = "INSERT INTO customers (full_name, email, phone, address) " +
                     "VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, customer.getFullName());
            ps.setString(2, customer.getEmail());
            ps.setString(3, customer.getPhone());
            ps.setString(4, customer.getAddress());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    customer.setId(rs.getLong(1));
                }
            }
        }

        return customer;
    }

    public Customer findById(long id) throws SQLException {
        String sql = "SELECT * FROM customers WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Customer c = new Customer();
                    c.setId(rs.getLong("id"));
                    c.setFullName(rs.getString("full_name"));
                    c.setEmail(rs.getString("email"));
                    c.setPhone(rs.getString("phone"));
                    c.setAddress(rs.getString("address"));

                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) {
                        c.setCreatedAt(ts.toLocalDateTime());
                    }
                    return c;
                }
            }
        }
        return null;
    }
}
