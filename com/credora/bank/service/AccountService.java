package com.credora.bank.service;

import com.credora.bank.dao.AccountDAO;
import com.credora.bank.dao.CustomerDAO;
import com.credora.bank.db.DBConnection;
import com.credora.bank.model.Account;
import com.credora.bank.model.Customer;
import com.credora.bank.util.AccountNumberGenerator;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

public class AccountService {

    private final Connection conn;
    private final CustomerDAO customerDAO;
    private final AccountDAO accountDAO;

    // No-arg constructor -> uses DBConnection
    public AccountService() throws SQLException {
        this(DBConnection.getConnection());
    }

    public AccountService(Connection conn) {
        this.conn = conn;
        this.customerDAO = new CustomerDAO(conn);
        this.accountDAO = new AccountDAO(conn);
    }

    public Account openAccount(Customer customer, String accountType, BigDecimal initialDeposit)
            throws SQLException {

        conn.setAutoCommit(false);
        try {
            Customer savedCustomer = customerDAO.create(customer);
            String accNo = AccountNumberGenerator.generate(savedCustomer.getId());

            Account account = new Account();
            account.setAccountNumber(accNo);
            account.setCustomerId(savedCustomer.getId());
            account.setAccountType(accountType);
            account.setBalance(initialDeposit);
            account.setStatus("ACTIVE");

            Account savedAccount = accountDAO.create(account);
            conn.commit();
            return savedAccount;
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public BigDecimal getAccountBalance(String accountNumber) throws SQLException {
        Account acc = accountDAO.findByAccountNumber(accountNumber);
        if (acc == null) {
            throw new SQLException("Account not found: " + accountNumber);
        }
        return acc.getBalance();
    }
}
