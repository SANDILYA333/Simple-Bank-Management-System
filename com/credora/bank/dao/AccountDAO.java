package com.credora.bank.dao;

import com.credora.bank.model.Account;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;

public class AccountDAO {

    private final Connection conn;

    public AccountDAO(Connection conn) {
        this.conn = conn;
    }

    public Account create(Account account) throws SQLException {
        String sql = "INSERT INTO accounts " +
                "(account_number, customer_id, account_type, balance, status) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, account.getAccountNumber());
            ps.setLong(2, account.getCustomerId());
            ps.setString(3, account.getAccountType());
            ps.setBigDecimal(4, account.getBalance());
            ps.setString(5, account.getStatus());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    account.setId(rs.getLong(1));
                }
            }
        }

        return account;
    }

    public Account findByAccountNumber(String accountNumber) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE account_number = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public BigDecimal getBalanceByAccountNumber(String accountNumber) throws SQLException {
        Account acc = findByAccountNumber(accountNumber);
        return (acc != null) ? acc.getBalance() : null;
    }

    public void updateBalance(long accountId, BigDecimal newBalance) throws SQLException {
        String sql = "UPDATE accounts SET balance = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, newBalance);
            ps.setLong(2, accountId);
            ps.executeUpdate();
        }
    }

    private Account mapRow(ResultSet rs) throws SQLException {
        Account a = new Account();
        a.setId(rs.getLong("id"));
        a.setAccountNumber(rs.getString("account_number"));
        a.setCustomerId(rs.getLong("customer_id"));
        a.setAccountType(rs.getString("account_type"));
        a.setBalance(rs.getBigDecimal("balance"));
        a.setStatus(rs.getString("status"));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            a.setCreatedAt(ts.toLocalDateTime());
        }
        return a;
    }
}
