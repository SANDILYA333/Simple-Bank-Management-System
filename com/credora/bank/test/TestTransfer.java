package com.credora.bank.test;

import com.credora.bank.service.AccountService;
import com.credora.bank.service.TransactionService;

import java.math.BigDecimal;

public class TestTransfer {
    public static void main(String[] args) {

        String fromAcc = "CRD0001";   // sender
        String toAcc   = "CRD0002";   // receiver
        BigDecimal amount = new BigDecimal("500"); // amount to transfer

        try {
            AccountService accountService = new AccountService();
            TransactionService txnService = new TransactionService();

            System.out.println("Before Transfer:");
            System.out.println(fromAcc + ": " + accountService.getAccountBalance(fromAcc));
            System.out.println(toAcc   + ": " + accountService.getAccountBalance(toAcc));

            txnService.transfer(fromAcc, toAcc, amount, "Test transfer");

            System.out.println("\nAfter Transfer:");
            System.out.println(fromAcc + ": " + accountService.getAccountBalance(fromAcc));
            System.out.println(toAcc   + ": " + accountService.getAccountBalance(toAcc));

            System.out.println("\nTransfer test finished.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
