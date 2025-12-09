package com.example.loanjena.model;

public class TransferOrderRequest {
    private String orderId;
    private String fromAccountId;
    private String toAccountId;
    private double amount;
    
    // 源账户信息
    private String fromAccountType; // "personal", "corporate", "vip"
    private double fromAccountBalance;
    private boolean fromAccountVerified;
    private int fromAccountDailyTransferCount;
    private int fromAccountRiskScore;
    
    // 目标账户信息
    private String toAccountType;
    private double toAccountBalance;
    private boolean toAccountVerified;
    private int toAccountRiskScore;

    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getFromAccountId() {
        return fromAccountId;
    }

    public void setFromAccountId(String fromAccountId) {
        this.fromAccountId = fromAccountId;
    }

    public String getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(String toAccountId) {
        this.toAccountId = toAccountId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getFromAccountType() {
        return fromAccountType;
    }

    public void setFromAccountType(String fromAccountType) {
        this.fromAccountType = fromAccountType;
    }

    public double getFromAccountBalance() {
        return fromAccountBalance;
    }

    public void setFromAccountBalance(double fromAccountBalance) {
        this.fromAccountBalance = fromAccountBalance;
    }

    public boolean isFromAccountVerified() {
        return fromAccountVerified;
    }

    public void setFromAccountVerified(boolean fromAccountVerified) {
        this.fromAccountVerified = fromAccountVerified;
    }

    public int getFromAccountDailyTransferCount() {
        return fromAccountDailyTransferCount;
    }

    public void setFromAccountDailyTransferCount(int fromAccountDailyTransferCount) {
        this.fromAccountDailyTransferCount = fromAccountDailyTransferCount;
    }

    public int getFromAccountRiskScore() {
        return fromAccountRiskScore;
    }

    public void setFromAccountRiskScore(int fromAccountRiskScore) {
        this.fromAccountRiskScore = fromAccountRiskScore;
    }

    public String getToAccountType() {
        return toAccountType;
    }

    public void setToAccountType(String toAccountType) {
        this.toAccountType = toAccountType;
    }

    public double getToAccountBalance() {
        return toAccountBalance;
    }

    public void setToAccountBalance(double toAccountBalance) {
        this.toAccountBalance = toAccountBalance;
    }

    public boolean isToAccountVerified() {
        return toAccountVerified;
    }

    public void setToAccountVerified(boolean toAccountVerified) {
        this.toAccountVerified = toAccountVerified;
    }

    public int getToAccountRiskScore() {
        return toAccountRiskScore;
    }

    public void setToAccountRiskScore(int toAccountRiskScore) {
        this.toAccountRiskScore = toAccountRiskScore;
    }
}
