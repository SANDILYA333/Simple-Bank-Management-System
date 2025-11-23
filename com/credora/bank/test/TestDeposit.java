package com.credora.bank.test;

import com.credora.bank.service.AccountService;
import com.credora.bank.service.TransactionService;

import java.math.BigDecimal;

public class TestDeposit {

    public static void main(String[] args) {
        String accountNumber = "CRD0001";   // must exist in DB
        BigDecimal amount = new BigDecimal("1500.00");

        try {
            AccountService accountService = new AccountService();
            TransactionService txnService = new TransactionService();

            System.out.println("Balance BEFORE deposit: " +
                    accountService.getAccountBalance(accountNumber));

            txnService.deposit(accountNumber, amount, "Test deposit from main");

            System.out.println("Balance AFTER deposit: " +
                    accountService.getAccountBalance(accountNumber));

            System.out.println("Deposit test finished.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
