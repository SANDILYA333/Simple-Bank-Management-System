package com.credora.bank.test;

import com.credora.bank.service.AccountService;
import com.credora.bank.service.TransactionService;
import com.credora.bank.db.DBConnection;

import java.sql.Connection;

public class TestWithdraw {
    public static void main(String[] args) {
        String accountNumber = "CRD0001"; // existing account in DB
        double amount = 500.00;

        try (Connection conn = DBConnection.getConnection()) {

            AccountService accountService = new AccountService(conn);
            TransactionService txnService = new TransactionService(conn);

            System.out.println("Balance BEFORE withdraw: " +
                    accountService.getAccountBalance(accountNumber));

            txnService.withdraw(accountNumber, amount, "Test withdraw from main");

            System.out.println("Balance AFTER withdraw: " +
                    accountService.getAccountBalance(accountNumber));

            System.out.println("Withdraw test finished.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
