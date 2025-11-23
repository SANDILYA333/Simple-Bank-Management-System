package com.credora.bank.service;

import com.credora.bank.dao.AccountDAO;
import com.credora.bank.dao.TransactionDAO;
import com.credora.bank.db.DBConnection;
import com.credora.bank.model.Account;
import com.credora.bank.model.Transaction;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionService {

    private final Connection conn;
    private final AccountDAO accountDAO;
    private final TransactionDAO transactionDAO;

    // No-arg constructor -> uses DBConnection
    public TransactionService() throws SQLException {
        this(DBConnection.getConnection());
    }

    public TransactionService(Connection conn) {
        this.conn = conn;
        this.accountDAO = new AccountDAO(conn);
        this.transactionDAO = new TransactionDAO(conn);
    }

    /* ===================== DEPOSIT ===================== */

    public void deposit(String accountNumber, BigDecimal amount, String description)
            throws SQLException {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive.");
        }

        conn.setAutoCommit(false);
        try {
            Account acc = accountDAO.findByAccountNumber(accountNumber);
            if (acc == null) {
                throw new SQLException("Account not found: " + accountNumber);
            }

            BigDecimal newBalance = acc.getBalance().add(amount);

            // 1) Update account balance
            accountDAO.updateBalance(acc.getId(), newBalance);

            // 2) Insert transaction
            Transaction txn = new Transaction();
            txn.setAccountId(acc.getId());
            txn.setTxnType("DEPOSIT");
            txn.setAmount(amount);
            txn.setDescription(description);
            txn.setBalanceAfterTxn(newBalance);

            transactionDAO.create(txn);

            conn.commit();
        } catch (SQLException | RuntimeException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    // Overload in case some callers still use double
    public void deposit(String accountNumber, double amount, String description) throws SQLException {
        deposit(accountNumber, BigDecimal.valueOf(amount), description);
    }

    /* ===================== WITHDRAW ===================== */

    public void withdraw(String accountNumber, BigDecimal amount, String description) throws SQLException {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive.");
        }

        conn.setAutoCommit(false);
        try {
            // Load account
            Account acc = accountDAO.findByAccountNumber(accountNumber);
            if (acc == null) {
                throw new SQLException("Account not found: " + accountNumber);
            }

            BigDecimal currentBalance = acc.getBalance();

            // Check sufficient funds
            if (currentBalance.compareTo(amount) < 0) {
                throw new IllegalArgumentException("INSUFFICIENT_FUNDS");
            }

            BigDecimal newBalance = currentBalance.subtract(amount);

            // 1) Update account balance
            accountDAO.updateBalance(acc.getId(), newBalance);

            // 2) Insert transaction
            Transaction txn = new Transaction();
            txn.setAccountId(acc.getId());
            txn.setTxnType("WITHDRAW");
            txn.setAmount(amount);
            txn.setDescription(description);
            txn.setBalanceAfterTxn(newBalance);

            transactionDAO.create(txn);

            conn.commit();
        } catch (SQLException | RuntimeException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    // Overload to support existing code that calls with double amount
    public void withdraw(String accountNumber, double amount, String description) throws SQLException {
        withdraw(accountNumber, BigDecimal.valueOf(amount), description);
    }

    /* ===================== TRANSFER ===================== */

    public void transfer(String fromAccNo, String toAccNo, BigDecimal amount, String description)
            throws SQLException {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive.");
        }

        conn.setAutoCommit(false);
        try {
            // 1. Load both accounts
            Account fromAccount = accountDAO.findByAccountNumber(fromAccNo);
            Account toAccount = accountDAO.findByAccountNumber(toAccNo);

            if (fromAccount == null) {
                throw new SQLException("Sender Account not found: " + fromAccNo);
            }
            if (toAccount == null) {
                throw new SQLException("Receiver Account not found: " + toAccNo);
            }

            // 2. Verify balance
            if (fromAccount.getBalance().compareTo(amount) < 0) {
                throw new IllegalArgumentException("Insufficient balance for transfer");
            }

            // 3. New balances
            BigDecimal newSenderBal = fromAccount.getBalance().subtract(amount);
            BigDecimal newReceiverBal = toAccount.getBalance().add(amount);

            // 4. Update both accounts
            accountDAO.updateBalance(fromAccount.getId(), newSenderBal);
            accountDAO.updateBalance(toAccount.getId(), newReceiverBal);

            // 5. Create transaction logs
            // OUT
            Transaction txnOut = new Transaction();
            txnOut.setAccountId(fromAccount.getId());
            txnOut.setTxnType("TRANSFER_OUT");
            txnOut.setAmount(amount);
            txnOut.setDescription(description + " -> " + toAccNo);
            txnOut.setBalanceAfterTxn(newSenderBal);
            transactionDAO.create(txnOut);

            // IN
            Transaction txnIn = new Transaction();
            txnIn.setAccountId(toAccount.getId());
            txnIn.setTxnType("TRANSFER_IN");
            txnIn.setAmount(amount);
            txnIn.setDescription(description + " <- " + fromAccNo);
            txnIn.setBalanceAfterTxn(newReceiverBal);
            transactionDAO.create(txnIn);

            conn.commit();
        } catch (SQLException | RuntimeException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    // Optional overload if you ever pass double
    public void transfer(String fromAccNo, String toAccNo, double amount, String description)
            throws SQLException {
        transfer(fromAccNo, toAccNo, BigDecimal.valueOf(amount), description);
    }
}
