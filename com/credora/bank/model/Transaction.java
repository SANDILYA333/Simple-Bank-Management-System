package com.credora.bank.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {

    private long id;
    private long accountId;
    private String txnType;          // "DEPOSIT", "WITHDRAW", "TRANSFER_IN", "TRANSFER_OUT"
    private BigDecimal amount;
    private String description;
    private LocalDateTime createdAt;
    private BigDecimal balanceAfterTxn;

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public long getAccountId() {
        return accountId;
    }
    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public String getTxnType() {
        return txnType;
    }
    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }

    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public BigDecimal getBalanceAfterTxn() {
        return balanceAfterTxn;
    }
    public void setBalanceAfterTxn(BigDecimal balanceAfterTxn) {
        this.balanceAfterTxn = balanceAfterTxn;
    }
}
