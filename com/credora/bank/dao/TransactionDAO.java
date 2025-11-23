package com.credora.bank.dao;

import com.credora.bank.model.Transaction;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    private final Connection conn;

    public TransactionDAO(Connection conn) {
        this.conn = conn;
    }

    public Transaction create(Transaction txn) throws SQLException {
        String sql = "INSERT INTO transactions " +
                "(account_id, txn_type, amount, description, balance_after_txn) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, txn.getAccountId());
            ps.setString(2, txn.getTxnType());
            ps.setBigDecimal(3, txn.getAmount());
            ps.setString(4, txn.getDescription());
            ps.setBigDecimal(5, txn.getBalanceAfterTxn());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    txn.setId(rs.getLong(1));
                }
            }
        }

        return txn;
    }

    public List<Transaction> findByAccountId(long accountId) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE account_id = ? ORDER BY created_at DESC";
        List<Transaction> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }

        return list;
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {
        Transaction t = new Transaction();
        t.setId(rs.getLong("id"));
        t.setAccountId(rs.getLong("account_id"));
        t.setTxnType(rs.getString("txn_type"));
        t.setAmount(rs.getBigDecimal("amount"));
        t.setDescription(rs.getString("description"));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            t.setCreatedAt(ts.toLocalDateTime());
        }

        t.setBalanceAfterTxn(rs.getBigDecimal("balance_after_txn"));
        return t;
    }
}
