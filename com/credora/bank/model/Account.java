package com.credora.bank.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Account {

    private long id;
    private String accountNumber;
    private long customerId;
    private String accountType;   // "SAVINGS" / "CURRENT"
    private BigDecimal balance;
    private String status;        // "ACTIVE" / "BLOCKED" / "CLOSED"
    private LocalDateTime createdAt;

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public long getCustomerId() {
        return customerId;
    }
    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public String getAccountType() {
        return accountType;
    }
    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public BigDecimal getBalance() {
        return balance;
    }
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
